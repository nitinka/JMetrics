package nitinka.jmetrics.daemon;

import nitinka.jmetrics.archive.MetricProcessingQueue;
import nitinka.jmetrics.archive.MetricArchivingEngine;
import nitinka.jmetrics.cache.MetricThresholdCache;
import nitinka.jmetrics.domain.Threshold;
import nitinka.jmetrics.monitor.Metric;
import nitinka.jmetrics.monitor.ResourceMetric;
import nitinka.jmetrics.util.Clock;
import nitinka.jmetrics.util.ObjectMapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * User: NitinK.Agarwal@yahoo.com
 * Do Threshold Checks
 * Do Metric Archiving
 */
public class MetricProcessingThread extends Thread{

    private boolean keepRunning = true;
    private static Logger logger = LoggerFactory.getLogger(MetricProcessingThread.class);
    private static MetricProcessingThread instance;

    private final MetricArchivingEngine archivingEngine;

    private MetricProcessingThread(MetricArchivingEngine archivingEngine) {
        this.archivingEngine = archivingEngine;
    }

    public static MetricProcessingThread startMetricArchiving(MetricArchivingEngine metricArchivingEngine) {
        if(instance == null) {
            instance = new MetricProcessingThread(metricArchivingEngine);
            instance.start();
        }
        return instance;
    }

    public void run() {
        logger.info("Started");
        while(keepRunning) {
            try{
                ResourceMetric resourceMetric = MetricProcessingQueue.poll();
                logger.info("Processing :"+ObjectMapperUtil.instance().writeValueAsString(resourceMetric));
                if(resourceMetric == null) {
                    continue;
                }
                doThresholdChecks(resourceMetric);
                archivingEngine.archive(resourceMetric);
            }
            catch (Exception e) {
                logger.error("Error while processing metrics", e);
            }
        }
        logger.info("Finished");
    }

    private void doThresholdChecks(ResourceMetric resourceMetric) throws ExecutionException {
        List<Metric> breachMetrics = new ArrayList<Metric>();
        for(Metric metric : resourceMetric.getMetrics()) {
            List<Threshold> thresholdList = MetricThresholdCache.get(metric.getName());
            for(Threshold threshold : thresholdList) {
                switch(threshold.doCheck(metric.getValue())) {
                    case OK:
                        breachMetrics.add(new Metric(metric.getName()+".breachLevel", 0.0));
                        break;
                    case WARNING:
                        breachMetrics.add(new Metric(metric.getName()+".breachLevel", 1.0));
                        break;
                    case CRITICAL:
                        breachMetrics.add(new Metric(metric.getName()+".breachLevel", 2.0));
                        break;
                }
            }
        }
        resourceMetric.getMetrics().addAll(breachMetrics);
    }

    public static void stopRunning() {
        instance.keepRunning = false;
        instance = null;
    }
}



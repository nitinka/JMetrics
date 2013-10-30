package nitinka.jmetrics.daemon;

import nitinka.jmetrics.archive.MetricArchiverQueue;
import nitinka.jmetrics.archive.MetricArchivingEngine;
import nitinka.jmetrics.domain.ResourceMetric;
import nitinka.jmetrics.util.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: NitinK.Agarwal@yahoo.com
 */
public class MetricArchivingThread extends Thread{

    private boolean keepRunning = true;
    private static Logger logger = LoggerFactory.getLogger(MetricArchivingThread.class);
    private static MetricArchivingThread instance;

    private final MetricArchivingEngine archivingEngine;
    private final int interval = 1000;

    private MetricArchivingThread(MetricArchivingEngine archivingEngine) {
        this.archivingEngine = archivingEngine;
    }

    public static MetricArchivingThread startMetricArchiving(MetricArchivingEngine metricArchivingEngine) {
        if(instance == null) {
            instance = new MetricArchivingThread(metricArchivingEngine);
            instance.start();
        }
        return instance;
    }

    public void run() {
        while(keepRunning) {
            try{
                ResourceMetric resourceMetric = MetricArchiverQueue.poll();
                if(resourceMetric == null) {
                    Clock.sleep(interval);
                    continue;
                }
                archivingEngine.archive(resourceMetric);
            }
            catch (Exception e) {
                logger.error("Error while archiving metrics", e);
            }
        }
        logger.info("Finished");
    }

    public static void stopRunning() {
        instance.keepRunning = false;
    }
}



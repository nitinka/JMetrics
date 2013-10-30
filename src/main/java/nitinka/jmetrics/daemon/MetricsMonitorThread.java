package nitinka.jmetrics.daemon;

import nitinka.jmetrics.archive.MetricArchiverQueue;
import nitinka.jmetrics.domain.MetricMonitor;
import nitinka.jmetrics.util.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: NitinK.Agarwal@yahoo.com
 * Monitors various metrics as specified by users at given interval and pass it to archiving engine.
 */
public class MetricsMonitorThread extends Thread{

    private boolean keepRunning = true;
    private int interval = 1000;
    private List<MetricMonitor> metricMonitors;
    private Map<String, Long> monitorLastExecutionTime;
    private static Logger logger = LoggerFactory.getLogger(MetricsMonitorThread.class);
    private static MetricsMonitorThread instance;

    private  MetricsMonitorThread() {
        this.metricMonitors = new ArrayList<MetricMonitor>();
        monitorLastExecutionTime = new HashMap<String, Long>();
    }

    public static MetricsMonitorThread startMonitoring() {
        if(instance == null) {
            instance = new MetricsMonitorThread();
            instance.start();
        }
        return instance;
    }

    public void run() {
        while(keepRunning) {
            synchronized (metricMonitors) {
                for(MetricMonitor metricMonitor : metricMonitors) {
                    if(canMetricMonitorRun(metricMonitor)) {
                        try {
                            logger.info("Collecting Metric for "+metricMonitor.getName()+ " monitor");
                            MetricArchiverQueue.offer(metricMonitor.get());
                        }
                        catch (Exception e) {
                            logger.error("Error while running "+metricMonitor.getName() + " Monitor", e);
                        }
                        finally {
                            this.monitorLastExecutionTime.put(metricMonitor.getName(), Clock.milliTick());
                        }
                    }
                }
            }
            try {
                Clock.sleep(this.interval);
            } catch (InterruptedException e) {
                logger.error("Error while sleeping", e);
            }
        }
    }

    private boolean canMetricMonitorRun(MetricMonitor metricMonitor) {
        Long lastExecutionTime =  this.monitorLastExecutionTime.get(metricMonitor.getName());
        if(lastExecutionTime == null) {
            return true;
        }
        else {
            return (Clock.milliTick() - lastExecutionTime) > metricMonitor.getInterval();
        }
    }

    public static void stopRunning() {
        instance.keepRunning = false;
    }

    synchronized public static void addMetricMonitor(MetricMonitor metricMonitor) {
        instance.metricMonitors.add(metricMonitor);
    }
}

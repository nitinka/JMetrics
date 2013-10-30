package nitinka.jmetrics;

import com.strategicgains.restexpress.RestExpress;
import nitinka.jmetrics.archive.ConsolePrintingEngine;
import nitinka.jmetrics.archive.MetricArchiverQueue;
import nitinka.jmetrics.archive.MetricArchivingEngine;
import nitinka.jmetrics.daemon.MetricArchivingThread;
import nitinka.jmetrics.daemon.MetricsMonitorThread;
import nitinka.jmetrics.domain.JmxMetricMonitor;
import nitinka.jmetrics.domain.Metric;
import nitinka.jmetrics.domain.MetricMonitor;
import nitinka.jmetrics.domain.ResourceMetric;
import nitinka.jmetrics.util.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;

/**
 * User: NitinK.Agarwal@yahoo.com
 */
public class JMetric {

    private final JMetricConfig config;
    private static JMetric instance;
    private static Logger logger = LoggerFactory.getLogger(JMetric.class);
    private static MetricArchivingEngine metricArchivingEngine;
    public JMetric(JMetricConfig config)
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        this.config = config;
        MetricsMonitorThread.startMonitoring();
        MetricsMonitorThread.addMetricMonitor(new JmxMetricMonitor("self.jmx").setInterval(30000));
        metricArchivingEngine = MetricArchivingEngine.build(config);
        MetricArchivingThread.startMetricArchiving(metricArchivingEngine);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                MetricArchiverQueue.clear();
                MetricArchivingThread.stopRunning();
                MetricsMonitorThread.stopRunning();
            }
        });
        logger.info("JMetric Initialized");
    }

    public static void initialize(JMetricConfig config)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if(instance == null) {
            instance = new JMetric(config);
        }
    }

    public static void addMetricMonitor(MetricMonitor metricMonitor) {
        if(instance == null)
            throw new RuntimeException("JMetric is not initialized yet");
        MetricsMonitorThread.addMetricMonitor(metricMonitor);
    }

    public static void offerMetric(String metricName, double metricValue) {
        MetricArchiverQueue.offer(new ResourceMetric().addMetrics(metricName, metricValue, Metric.MetricType.GAUGE));
    }

    public static MetricArchivingEngine metricArchivingEngine() {
        return metricArchivingEngine;
    }

    public static void main(String[] args)
            throws InterruptedException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        JMetricConfig config = new JMetricConfig();
        config.setArchivalEngineClass(ConsolePrintingEngine.class.getCanonicalName());
        JMetric.initialize(config);

        while(true) {
            JMetric.offerMetric("M1", new Random().nextInt(10000));
            Clock.sleep(1000);
        }
    }
}

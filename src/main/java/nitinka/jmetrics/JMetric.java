package nitinka.jmetrics;

import com.strategicgains.restexpress.Format;
import com.strategicgains.restexpress.RestExpress;
import nitinka.jmetrics.archive.MetricProcessingQueue;
import nitinka.jmetrics.archive.MetricArchivingEngine;
import nitinka.jmetrics.archive.RRD4JArchivingEngine;
import nitinka.jmetrics.cache.MetricThresholdCache;
import nitinka.jmetrics.controller.restexpress.JMetricController;
import nitinka.jmetrics.daemon.MetricProcessingThread;
import nitinka.jmetrics.daemon.MetricsMonitorThread;
import nitinka.jmetrics.monitor.JmxMetricMonitor;
import nitinka.jmetrics.monitor.Metric;
import nitinka.jmetrics.monitor.MetricMonitor;
import nitinka.jmetrics.monitor.ResourceMetric;
import nitinka.jmetrics.util.Clock;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

/**
 * User: NitinK.Agarwal@yahoo.com
 */
public class JMetric {

    public static JMetricConfig config;
    private static JMetric instance;
    private static Logger logger = LoggerFactory.getLogger(JMetric.class);
    private static MetricArchivingEngine metricArchivingEngine;
    private static boolean running = true;

    private JMetric(JMetricConfig config)
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        JMetric.config = config;
        File thresholdPath = new File(config.getThresholdPath());
        if(!thresholdPath.exists()) {
            thresholdPath.mkdirs();
        }

        MetricsMonitorThread.startMonitoring();
        MetricsMonitorThread.addMetricMonitor(new JmxMetricMonitor("self.jmx").setInterval(20000));
        metricArchivingEngine = MetricArchivingEngine.build(config);
        MetricProcessingThread.startMetricArchiving(metricArchivingEngine);

        final RestExpress server;
        if(config.getServerPort() != 0) {
            logger.info("Starting Embedded Server to server JMetric Resources");
            server = new RestExpress()
                    .setName("JMetric")
                    .setPort(config.getServerPort())
                    .setDefaultFormat(Format.JSON)
                    .setExecutorThreadCount(10)
                    .setIoThreadCount(10);

            JMetricController controller = new JMetricController();
            server.uri("/metrics", controller)
                    .action("metricNames", HttpMethod.GET);
            logger.info("Do Http Get on /metrics to get all metric names");

            server.uri("/metrics/img", controller)
                    .action("allMetricsImg", HttpMethod.GET).
                    noSerialization();
            logger.info("Do Http Get on /metrics/img to get all metric images");

            server.uri("/metrics/{metricName}/raw", controller)
                    .action("metricRaw", HttpMethod.GET);
            logger.info("Do Http Get on /metrics/{metricName}/raw to get metric raw details");

            server.uri("/metrics/{metricName}/img", controller)
                    .action("metricImg", HttpMethod.GET).
                    noSerialization();
            logger.info("Do Http Get on /metrics/{metricName}/img to get metric stats image");

            server.uri("/metrics/{metricName}/threshold", controller).
                    action("getMetricThreshold", HttpMethod.GET);
            logger.info("Do Http Get on /metrics/{metricName}/threshold to get metric threshold limits");

            server.uri("/metrics/{metricName}/threshold", controller).
                    action("updateMetricThreshold", HttpMethod.PUT);
            logger.info("Do Http Put on /metrics/{metricName}/threshold to update metric threshold limits");

            server.uri("/stop", controller).
                    action("stop", HttpMethod.PUT);
            logger.info("Do Http Put on /stop to stop metric collection");

            server.uri("/start", controller).
                    action("start", HttpMethod.PUT);
            logger.info("Do Http Put on /start to start metric collection");
            server.bind();
        }
        else
            server = null;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                MetricProcessingQueue.clear();
                MetricProcessingThread.stopRunning();
                MetricsMonitorThread.stopRunning();
                if(server != null) {
                    logger.info("Shutting down JMetric Embedded Server");
                    server.shutdown();
                }

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
        if(running) {
            MetricProcessingQueue.offer(new ResourceMetric().addMetrics(metricName, metricValue, Metric.MetricType.GAUGE));
        }
    }

    public static MetricArchivingEngine metricArchivingEngine() {
        return metricArchivingEngine;
    }

    synchronized public static void stop() {
        if(!running)
            return;

        MetricProcessingThread.stopRunning();
        MetricsMonitorThread.stopRunning();
        MetricProcessingQueue.clear();
        running = false;
    }

    synchronized public static void start() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if(running)
            return;

        MetricsMonitorThread.startMonitoring();
        MetricsMonitorThread.addMetricMonitor(new JmxMetricMonitor("self.jmx").setInterval(30000));
        MetricProcessingThread.startMetricArchiving(metricArchivingEngine);
        running = true;
    }

    public static void main(String[] args)
            throws InterruptedException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        JMetricConfig config = new JMetricConfig();
        config.setArchivalEngineClass(RRD4JArchivingEngine.class.getCanonicalName());
        config.setServerPort(4444);

        JMetric.initialize(config);

        while(true) {
            JMetric.offerMetric("M1", new Random().nextInt(10000));
            Clock.sleep(10000);
        }
    }
}

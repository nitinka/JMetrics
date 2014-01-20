package nitinka.jmetrics.controller.restexpress;

import com.strategicgains.restexpress.Request;
import com.strategicgains.restexpress.Response;
import com.strategicgains.restexpress.RestExpress;
import nitinka.jmetrics.JMetric;
import nitinka.jmetrics.JMetricConfig;
import nitinka.jmetrics.archive.MetricArchivingEngine;
import nitinka.jmetrics.archive.RRD4JArchivingEngine;
import nitinka.jmetrics.util.Clock;
import nitinka.jmetrics.util.MathConstant;
import org.apache.commons.io.IOUtils;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import static nitinka.jmetrics.util.Clock.*;

/**
 * User: NitinK.Agarwal@yahoo.com
 */
public class JMetricController {
    private static Logger logger = LoggerFactory.getLogger(JMetricController.class);
    private final MetricArchivingEngine metricArchivingEngine;

    public JMetricController() {
        metricArchivingEngine = JMetric.metricArchivingEngine();
    }

    public static void setup(RestExpress server) {
        JMetricController controller = new JMetricController();
        server.uri("/metrics", new JMetricController())
                .action("metricNames", HttpMethod.GET);
        logger.info("Do Http Get on /metrics to get all metric names");

        server.uri("/metrics/img", new JMetricController())
                .action("allMetricsImg", HttpMethod.GET).
                noSerialization();
        logger.info("Do Http Get on /metrics/img to get all metric images");

        server.uri("/metrics/{metricName}/raw", new JMetricController())
                .action("metricRaw", HttpMethod.GET);
        logger.info("Do Http Get on /metrics/{metricName}/raw to get metric raw details");

        server.uri("/metrics/{metricName}/img", new JMetricController())
                .action("metricImg", HttpMethod.GET).
                noSerialization();
        logger.info("Do Http Get on /metrics/{metricName}/img to get metric stats image");
    }

    /**
     * Will return all metric Names that have been archived so far.
     * @param request
     * @param response
     * @throws IOException
     */
    public void metricNames(Request request, Response response) throws IOException {
        response.setBody(metricArchivingEngine.metrics());
        response.setResponseStatus(HttpResponseStatus.OK);
    }

    /**
     * Will return raw stats for the given metric
     * @param request
     * @param response
     * @throws IOException
     */
    public void metricRaw(Request request, Response response) throws IOException {
        response.setBody(metricArchivingEngine.fetchMetrics(request.getHeader("metricName"),
                "TOTAL",
                getStartTime(request.getHeader("startTime")),
                getEndTime(request.getHeader("endTime"))));
        response.setResponseStatus(HttpResponseStatus.OK);
    }

    /**
     * Will Return img representing given metric
     * @param request
     * @param response
     * @throws IOException
     */
    public void metricImg(Request request, Response response) throws IOException {
        InputStream is = metricArchivingEngine.fetchMetricsImage(request.getHeader("metricName"),
                "TOTAL",
                getStartTime(request.getHeader("startTime")),
                getEndTime(request.getHeader("endTime")));
        response.setBody(ChannelBuffers.wrappedBuffer(IOUtils.toByteArray(is)));
        response.setResponseStatus(HttpResponseStatus.OK);
    }

    /**
     * Will Return Images for all metrics being monitored
     * @param request
     * @param response
     * @throws IOException
     */
    public void allMetricsImg(Request request, Response response) throws IOException {
        List<String> metrics = metricArchivingEngine.metrics();
        Collections.sort(metrics);

        StringBuilder html = new StringBuilder("");
        html.append("<!DOCTYPE html>\n<html>\n");
        for(String metric : metrics) {
            html.append("<img src=\"http://"+request.getHost()+"/metrics/"+metric+"/img?startTime="+getStartTime(request.getHeader("startTime"))+"&endTime="+getEndTime(request.getHeader("endTime"))+"\"/>\n");
        }
        html.append("</html>");

        response.setBody(html);
        response.setResponseStatus(HttpResponseStatus.OK);
        response.setContentType("text/html");
    }


    public static void main(String[] args)
            throws InterruptedException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        JMetricConfig config = new JMetricConfig();
        config.setArchivalEngineClass(RRD4JArchivingEngine.class.getCanonicalName());
        JMetric.initialize(config);

        new Thread() {
            public void run(){
                while(true) {
                    JMetric.offerMetric("M1", new Random().nextInt(10000));
                    try {
                        Clock.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }.start();

        logger.info("Initializing Rest Express");
        RestExpress server = new RestExpress();
        server.uri("/metrics", new JMetricController()).action("metricNames", org.jboss.netty.handler.codec.http.HttpMethod.GET);
        server.uri("/metrics/{metricName}/raw", new JMetricController()).action("metricRaw", org.jboss.netty.handler.codec.http.HttpMethod.GET);
        server.uri("/metrics/{metricName}/img", new JMetricController()).action("metricImg", org.jboss.netty.handler.codec.http.HttpMethod.GET);
        server.uri("/metrics/img", new JMetricController()).action("allMetricsImg", org.jboss.netty.handler.codec.http.HttpMethod.GET);

        logger.info("Initialized");
        server.bind(4567);
    }

}

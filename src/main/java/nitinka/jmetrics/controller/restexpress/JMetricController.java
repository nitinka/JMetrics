package nitinka.jmetrics.controller.restexpress;

import ch.qos.logback.core.util.FileUtil;
import com.strategicgains.restexpress.Request;
import com.strategicgains.restexpress.Response;
import com.strategicgains.restexpress.RestExpress;
import nitinka.jmetrics.JMetric;
import nitinka.jmetrics.JMetricConfig;
import nitinka.jmetrics.archive.ConsolePrintingEngine;
import nitinka.jmetrics.archive.MetricArchivingEngine;
import nitinka.jmetrics.archive.RRD4JArchivingEngine;
import nitinka.jmetrics.util.Clock;
import nitinka.jmetrics.util.MathConstant;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * User: NitinK.Agarwal@yahoo.com
 */
public class JMetricController {
    private static Logger logger = LoggerFactory.getLogger(JMetricController.class);
    private final MetricArchivingEngine metricArchivingEngine;

    public JMetricController() {
        metricArchivingEngine = JMetric.metricArchivingEngine();
    }

    /**
     * Will return all metric Names that have been archived so far.
     * @param request
     * @param response
     * @throws IOException
     */
    public void metricNames(Request request, Response response) throws IOException {
        try {
            response.setBody(metricArchivingEngine.metrics());
            response.setResponseStatus(HttpResponseStatus.OK);
        }
        catch (Throwable t) {
            logger.error("Error while fetching metric names", t);
            response.setResponseStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            response.setBody(t.getMessage());
        }
    }

    /**
     * Will return raw stats for the given metric
     * @param request
     * @param response
     * @throws IOException
     */
    public void metricRaw(Request request, Response response) throws IOException {
        String metricName = request.getHeader("metricName");
        String startTime = request.getHeader("startTime");
        String endTime = request.getHeader("endTime");
        long startTimeSec = startTime == null ? (Clock.milliTick() - 2 * 24 * 60 * 60 * 1000) / MathConstant.THOUSAND: Long.parseLong(startTime);
        long endTimeSec = endTime == null ? Clock.milliTick() / MathConstant.THOUSAND: Long.parseLong(endTime);
        try {
            response.setBody(metricArchivingEngine.fetchMetrics(metricName, "TOTAL", startTimeSec, endTimeSec));
            response.setResponseStatus(HttpResponseStatus.OK);
        }
        catch (Throwable t) {
            logger.error("Error while fetching metric raw for '"+metricName+"'", t);
            response.setResponseStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            response.setBody(t.getMessage());
        }
    }

    /**
     * Will Return img representing given metric
     * @param request
     * @param response
     * @throws IOException
     */
    public void metricImg(Request request, Response response) throws IOException {
        String metricName = request.getHeader("metricName");
        String startTime = request.getHeader("startTime");
        String endTime = request.getHeader("endTime");
        long startTimeSec = startTime == null ? (Clock.milliTick() - 2 * 24 * 60 * 60 * 1000) / MathConstant.THOUSAND: Long.parseLong(startTime);
        long endTimeSec = endTime == null ? Clock.milliTick() / MathConstant.THOUSAND: Long.parseLong(endTime);
        try {
            InputStream is = metricArchivingEngine.fetchMetricsImage(metricName, "TOTAL", startTimeSec, endTimeSec);
            response.setBody(ChannelBuffers.wrappedBuffer(IOUtils.toByteArray(is)));
            response.setResponseStatus(HttpResponseStatus.OK);
        }
        catch (Throwable t) {
            logger.error("Error while fetching metric img for '"+metricName+"'", t);
            response.setResponseStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            response.setBody(t.getMessage());
        }
    }

    public void allMetricsImg(Request request, Response response) throws IOException {
        String startTime = request.getHeader("startTime");
        String endTime = request.getHeader("endTime");
        long startTimeSec = startTime == null ? (Clock.milliTick() - 2 * 24 * 60 * 60 * 1000) / MathConstant.THOUSAND: Long.parseLong(startTime);
        long endTimeSec = endTime == null ? Clock.milliTick() / MathConstant.THOUSAND: Long.parseLong(endTime);

        try {
            List<String> metrics = metricArchivingEngine.metrics();
            Collections.sort(metrics);

            StringBuilder html = new StringBuilder("");
            html.append("<!DOCTYPE html>\n<html>\n");
            for(String metric : metrics) {
                html.append("<img src=\"http://"+request.getHost()+"/metrics/"+metric+"/img?startTime="+startTimeSec+"&endTime="+endTimeSec+"\"/>\n");
            }
            html.append("</html>");

            response.setBody(html);
            response.setResponseStatus(HttpResponseStatus.OK);
            response.setContentType("text/html");
        }
        catch (Throwable t) {
            logger.error("Error while fetching images for all metrics", t);
            response.setResponseStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            response.setBody(t.getMessage());
        }
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

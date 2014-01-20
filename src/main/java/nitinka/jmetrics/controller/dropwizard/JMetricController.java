package nitinka.jmetrics.controller.dropwizard;

/**
 * User: NitinK.Agarwal@yahoo.com
 */

import com.strategicgains.restexpress.RestExpress;
import com.yammer.metrics.annotation.Timed;
import nitinka.jmetrics.JMetric;
import nitinka.jmetrics.archive.MetricArchivingEngine;
import nitinka.jmetrics.util.Clock;
import nitinka.jmetrics.util.MathConstant;
import nitinka.jmetrics.util.ObjectMapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import static nitinka.jmetrics.util.Clock.*;

/**
 * Resource to manage operations on loader-agents
 */
@Path("/metrics")
public class JMetricController {
    private static Logger logger = LoggerFactory.getLogger(JMetricController.class);
    private final MetricArchivingEngine metricArchivingEngine;

    public JMetricController() {
        this.metricArchivingEngine = JMetric.metricArchivingEngine();
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Timed
    synchronized public List<String> metricNames()
            throws IOException, ExecutionException, InterruptedException {
        return metricArchivingEngine.metrics();
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Timed
    @Path("/{metricName}/raw")
    synchronized public String metricRaw(@PathParam("metricName") String metricName,
                                         @QueryParam("startTime") @DefaultValue("-1") String startTime,
                                         @QueryParam("endTime")  @DefaultValue("-1") String endTime)
            throws IOException, ExecutionException, InterruptedException {
        return metricArchivingEngine.fetchMetrics(metricName, "TOTAL", getStartTime(startTime), getEndTime(endTime));
    }

    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @GET
    @Timed
    @Path("/{metricName}/img")
    synchronized public InputStream metricImage(@PathParam("metricName") String metricName,
                                                @QueryParam("startTime") @DefaultValue("-1") String startTime,
                                                @QueryParam("endTime")  @DefaultValue("-1") String endTime)
            throws IOException, ExecutionException, InterruptedException {
        return metricArchivingEngine.fetchMetricsImage(metricName, "TOTAL", getStartTime(startTime), getEndTime(endTime));
    }

    @Produces(MediaType.TEXT_HTML)
    @GET
    @Timed
    @Path("/img")
    synchronized public String allMetricsImg(@Context HttpServletRequest request,
                                             @QueryParam("startTime") @DefaultValue("-1") String startTime,
                                             @QueryParam("endTime")  @DefaultValue("-1") String endTime)
            throws IOException, ExecutionException, InterruptedException {

        List<String> metrics = metricArchivingEngine.metrics();
        Collections.sort(metrics);

        StringBuilder html = new StringBuilder("");
        for(String metric : metrics) {
            html.append("<img src=\"http://"+request.getServerName()+":"+request.getLocalPort()
                    + request.getServletPath()
                    + "/metrics/" + metric
                    + "/img?startTime="+getStartTime(startTime)+"&entTime="+getEndTime(endTime)+"\"/>\n");
        }
        return html.toString().trim();
    }
}
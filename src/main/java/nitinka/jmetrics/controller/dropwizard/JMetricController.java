package nitinka.jmetrics.controller.dropwizard;

/**
 * User: NitinK.Agarwal@yahoo.com
 */

import com.yammer.metrics.annotation.Timed;
import nitinka.jmetrics.JMetric;
import nitinka.jmetrics.archive.MetricArchivingEngine;
import nitinka.jmetrics.domain.Threshold;
import nitinka.jmetrics.util.ObjectMapperUtil;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import static nitinka.jmetrics.util.Clock.*;

/**
 * Resource to manage operations on loader-agents
 */
public class JMetricController {
    private final MetricArchivingEngine metricArchivingEngine;
    private static ObjectMapper mapper = ObjectMapperUtil.instance();

    public JMetricController() {
        this.metricArchivingEngine = JMetric.metricArchivingEngine();
    }

    @Path("/metrics")
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
    @Path("/metrics/{metricName}/raw")
    synchronized public String metricRaw(@PathParam("metricName") String metricName,
                                         @QueryParam("startTime") @DefaultValue("-1") String startTime,
                                         @QueryParam("endTime")  @DefaultValue("-1") String endTime)
            throws IOException, ExecutionException, InterruptedException {
        return metricArchivingEngine.fetchMetrics(metricName, "TOTAL", getStartTime(startTime), getEndTime(endTime));
    }

    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @GET
    @Timed
    @Path("/metrics/{metricName}/img")
    synchronized public InputStream metricImage(@PathParam("metricName") String metricName,
                                                @QueryParam("startTime") @DefaultValue("-1") String startTime,
                                                @QueryParam("endTime")  @DefaultValue("-1") String endTime)
            throws IOException, ExecutionException, InterruptedException {
        return metricArchivingEngine.fetchMetricsImage(metricName, "TOTAL", getStartTime(startTime), getEndTime(endTime));
    }

    @Produces(MediaType.TEXT_HTML)
    @GET
    @Timed
    @Path("/metrics/img")
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

    /**
     * Get Metric Threshold
     * @param metricName
     * @return
     * @throws IOException
     */
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Timed
    @Path("/metrics/{metricName}/threshold")
    public List getMetricThreshold(@PathParam("metricName") String metricName) throws IOException {
        if(metricArchivingEngine.metrics().contains(metricName)) {
            File metricThresholdFile = new File(JMetric.config.getThresholdPath()
                    + File.separator
                    + metricName + ".threshold");

            if(metricThresholdFile.exists()) {
               return mapper.readValue(metricThresholdFile, List.class);
            }
            else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).
                        entity("Threshold doesn't exist").
                        build());
            }
        }
        else {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).
                    entity("Metric "+metricName+" doesn't exist").
                    build());
        }
    }


    @PUT
    @Timed
    @Path("/metrics/{metricName}/threshold")
    /**
     * Update Metric Threshold
     * @param metricName
     * @param thresholds
     * @throws IOException
     */
    public void updateMetricThreshold(@PathParam("metricName") String metricName, List<Threshold> thresholds) throws IOException {
        if(metricArchivingEngine.metrics().contains(metricName)) {
            File metricThresholdFile = new File(JMetric.config.getThresholdPath()
                    + File.separator
                    + metricName + ".threshold");

            mapper.defaultPrettyPrintingWriter().
                    writeValue(metricThresholdFile,
                            thresholds);
        }
        else {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).
                    entity("Metric "+metricName+" doesn't exist").
                    build());
        }
    }

    @Path("/stop")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Timed
    synchronized public void stop()
            throws IOException, ExecutionException, InterruptedException {
        JMetric.stop();
    }

    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Timed
    synchronized public void start()
            throws IOException, ExecutionException, InterruptedException,
            ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        JMetric.start();
    }
}
package nitinka.jmetrics.archive;

import nitinka.jmetrics.monitor.ResourceMetric;
import nitinka.jmetrics.util.ObjectMapperUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * User: NitinK.Agarwal@yahoo.com
 */

public class ConsolePrintingEngine extends MetricArchivingEngine{

    public ConsolePrintingEngine(Map<String, Object> config) {
        super(config);
    }

    @Override
    public void archive(List<ResourceMetric> resourceMetrics) throws IOException {
        System.out.println(ObjectMapperUtil.instance().defaultPrettyPrintingWriter().writeValueAsString(resourceMetrics));
    }

    @Override
    public List<String> metrics() throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String fetchMetrics(String metricName, String consolFuc, long startTimeSec, long endTimeSec) throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public InputStream fetchMetricsImage(String metricName, String consolFuc, long startTimeSec, long endTimeSec) throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

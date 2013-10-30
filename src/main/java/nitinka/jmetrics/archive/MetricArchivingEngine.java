package nitinka.jmetrics.archive;

import nitinka.jmetrics.JMetricConfig;
import nitinka.jmetrics.domain.ResourceMetric;
import nitinka.jmetrics.util.ObjectMapperUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * User: NitinK.Agarwal@yahoo.com
 */

abstract public class MetricArchivingEngine {
    private Map<String, Object> config;
    protected static Logger logger = LoggerFactory.getLogger(MetricArchivingEngine.class);
    protected static ObjectMapper objectMapper = ObjectMapperUtil.instance();

    public MetricArchivingEngine(Map<String, Object> config) {
        this.config = config;
    }

    final public void archive(ResourceMetric resourceMetric) throws IOException {
        archive(Arrays.asList(new ResourceMetric[]{resourceMetric}));
    }
    abstract public void archive(List<ResourceMetric> resourceMetrics) throws IOException;
    abstract public List<String> metrics() throws IOException;
    abstract public String fetchMetrics(String metricName, String consolFuc, long startTimeSec, long endTimeSec) throws IOException;
    abstract public InputStream fetchMetricsImage(String metricName, String consolFuc, long startTimeSec, long endTimeSec) throws IOException;

    public static final MetricArchivingEngine build(JMetricConfig config) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return getClassInstance(config.getArchivalEngineClass(),
                new Class[]{Map.class},
                new Object[]{config.getConfigParams()},
                MetricArchivingEngine.class);
    }

    private static <T> T getClassInstance(String className,Class[] paramTypes, Object[] params, Class<T> c) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Object  obj =   null;
        Class actionClassObj;
        actionClassObj      =   Class.forName(className);
        Constructor cons    =   actionClassObj.getConstructor(paramTypes);
        obj                 =   cons.newInstance(params);
        return (T) obj;
    }

}

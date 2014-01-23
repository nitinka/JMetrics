package nitinka.jmetrics.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import nitinka.jmetrics.JMetric;
import nitinka.jmetrics.domain.Threshold;
import nitinka.jmetrics.util.CollectionHelper;
import nitinka.jmetrics.util.ObjectMapperUtil;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 21/1/14
 * Time: 12:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetricThresholdCache {
    private static ObjectMapper objectMapper;
    private static LoadingCache<String, List<Threshold>> metricThresholds;
    private static Logger logger = LoggerFactory.getLogger(MetricThresholdCache.class);

    static {
        objectMapper = ObjectMapperUtil.instance();
        initiateCache(JMetric.config.getThresholdPath());
    }

    private static void initiateCache(final String thresholdPath) {
        metricThresholds = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .build(
                        new CacheLoader<String, List<Threshold>>() {
                            public List<Threshold> load(String metricName) throws IOException {
                                File metricThresholdFile = new File(thresholdPath + File.separator + metricName + ".threshold");
                                if(metricThresholdFile.exists())
                                    return CollectionHelper.transformList(objectMapper.readValue(metricThresholdFile, List.class),Threshold.class);
                                return null;
                            }
                        });
    }

    public static List<Threshold> get(String metricName) throws ExecutionException {
        try {
            return metricThresholds.get(metricName);
        } catch (CacheLoader.InvalidCacheLoadException e) {
            if(e.getLocalizedMessage().contains("null"))
                return new ArrayList<Threshold>();
        }
        return null;
    }

    public static void remove(String metricName) {
        metricThresholds.invalidate(metricName);
    }

    public static void put(String metricName, List<Threshold> thresholds) {
        metricThresholds.put(metricName, thresholds);
    }
}

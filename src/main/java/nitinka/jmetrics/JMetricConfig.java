package nitinka.jmetrics;

import nitinka.jmetrics.archive.RRD4JArchivingEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * User: NitinK.Agarwal@yahoo.com
 */
public class JMetricConfig {
    private String archivalEngineClass;
    private Map<String, Object> configParams;

    public JMetricConfig() {
        this.archivalEngineClass = RRD4JArchivingEngine.class.getCanonicalName();
        configParams = new HashMap<String, Object>();
        configParams.put(RRD4JArchivingEngine.RRD_BASE_PATH, "./jMetrics");
    }

    public String getArchivalEngineClass() {
        return archivalEngineClass;
    }

    public void setArchivalEngineClass(String archivalEngineClass) {
        this.archivalEngineClass = archivalEngineClass;
    }

    public Map<String, Object> getConfigParams() {
        return configParams;
    }

    public void setConfigParams(Map<String, Object> configParams) {
        this.configParams = configParams;
    }
}

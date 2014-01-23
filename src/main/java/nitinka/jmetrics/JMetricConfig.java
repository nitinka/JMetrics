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
    private String thresholdPath;
    private int serverPort = 0; // Needed only if you want JMetric to Host it self as a server

    public JMetricConfig() {
        this.archivalEngineClass = RRD4JArchivingEngine.class.getCanonicalName();
        configParams = new HashMap<String, Object>();
        configParams.put(RRD4JArchivingEngine.RRD_BASE_PATH, "./jMetrics/data");
        thresholdPath = "./jMetrics/threshold";
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

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getThresholdPath() {
        return thresholdPath;
    }

    public void setThresholdPath(String thresholdPath) {
        this.thresholdPath = thresholdPath;
    }
}

package nitinka.jmetrics.monitor;

import nitinka.jmetrics.util.Clock;

import java.util.ArrayList;
import java.util.List;

/**
 * User: NitinK.Agarwal@yahoo.com
 */
public class ResourceMetric {
    private long at;
    private List<Metric> metrics;

    public ResourceMetric() {
        this.metrics = new ArrayList<Metric>();
        this.at = Clock.milliTick();
    }

    public long getAt() {
        return at;
    }

    public ResourceMetric setAt(long at) {
        this.at = at;
        return this;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public ResourceMetric setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
        return this;
    }

    public ResourceMetric addMetrics(Metric metric) {
        this.metrics.add(metric);
        return this;
    }

    public ResourceMetric addMetrics(String name, double value) {
        this.metrics.add(new Metric(name, value));
        return this;
    }

    public ResourceMetric addMetrics(String name, double value, Metric.MetricType metricType) {
        this.metrics.add(new Metric(name, value).setMetricType(metricType));
        return this;
    }
}

* Its a very simple java lib which allows users to capture various metrics from within running jvm and store them locally using rrd4j library.<br>
* JMX metrics are captured out of box. Library also expose single liner code to add any user defined metrics either explicitly or by writing threaded metric monitors.<br>
* Such stored metrics can be viewed, graphed to make meaningful decisions in terms of scaling, improving performance and functional aspects.<br>
* This library can be embedded within any existing java application to get meaningful metrics driven information with in few minutes. <br>
<br><b>Sample metric report :
![Alt Image](https://github.com/nitinka/JMetrics/raw/master/images/JMetricSample.png)
<br><b>Steps to Integrate with DropWizard based BAckend Application :</b>
1) Add maven dep(For time being you will have to build it locally. Will have it in central repo soon) :<br>
```xml
<dependency>
    <groupId>nitinka.jmetrics</groupId>
    <artifactId>JMetrics</artifactId>
    <version>0.1.2</version>
</dependency> 
```

2) Add following in your application.yml file :<br>
<pre>
jMetricConfig:
  archivalEngineClass: "nitinka.jmetrics.archive.RRD4JArchivingEngine"
  configParams:
    basePath: "/var/log/your-app/stats"
</pre>

3) Add following in your service initialization code :<br>
```java
    JMetric.initialize(configuration.getjMetricConfig());
    environment.addResource(new JMetricController())
```
4) Add following code in your Dropwizard Applicaion Config Class :<br>
```java
    private JMetricConfig jMetricConfig;

    public JMetricConfig getjMetricConfig() {
        return jMetricConfig;
    }

    public void setjMetricConfig(JMetricConfig jMetricConfig) {
        this.jMetricConfig = jMetricConfig;
    }
```
5) Done

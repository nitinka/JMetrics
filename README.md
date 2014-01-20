* Its a very simple java lib which allows users to capture various metrics from within running jvm and store them locally using rrd4j library and serve them back using simple http end points from with in the app<br>
* JMX metrics are captured out of box. Library also expose single liner code to add any user defined metrics either explicitly or by writing threaded metric monitors.<br>
* Such stored metrics can be viewed, graphed to make meaningful decisions in terms of scaling, improving performance and functional aspects.<br>
* This library can be embedded within any existing java application to get meaningful metrics driven information with in few minutes. DThere is direct support for Dropwizard and RestExpress based web application. For stand alone application, JMetric can be told to start a light weight embedded server.<br>
<br><b>Sample metric report :
![Alt Image](https://github.com/nitinka/JMetrics/raw/master/images/JMetricSample.png)
<br><br>
<br><b>Steps to Integrate with DropWizard(0.5.*,0.6.1) based Backend Application :</b>
<br>

1) Add maven dep :<br>
```xml

<repositories>
    <repository>
        <id>nitinka.mvn.repo</id>
        <url>https://github.com/nitinka/mvn-repo/raw/master</url>
        <!-- use snapshot version -->
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>

<dependency>
    <groupId>nitinka.jmetrics</groupId>
    <artifactId>JMetrics</artifactId>
    <version>0.1.3</version>
</dependency> 
```
<br>
2) Add following in your application.yml file :
<pre>
jMetricConfig:
  archivalEngineClass: "nitinka.jmetrics.archive.RRD4JArchivingEngine"
  configParams:
    basePath: "/var/log/your-app/stats"
</pre><br>

3) Add following in your service initialization code :<br>

<pre>
    JMetric.initialize(configuration.getjMetricConfig());
    environment.addResource(new JMetricController())
</pre>

<br>
4) Add following code in your Dropwizard Applicaion Config Class :<br>

<pre>
    private JMetricConfig jMetricConfig;

    public JMetricConfig getjMetricConfig() {
        return jMetricConfig;
    }

    public void setjMetricConfig(JMetricConfig jMetricConfig) {
        this.jMetricConfig = jMetricConfig;
    }
</pre>

<br>
5) Restart your app and hit following urls
<pre>
  http://host:port/servletPath/metric
     Returns all metrics that are being captured
  http://host:port/servletPath/metric/img
     Returns last 2 day graphical statistics of all metrics
  http://host:port/servletPath/metric/metricName/img
     Returns last 2 day graphical statistics of metric specifird
  http://host:port/servletPath/metric/metricName/raw
     Returns last 2 day raw statistics of metric specifird
</pre>
<br><br>
<br><b>Steps to Integrate with RestExpress(0.5.*,0.6.1) based Backend Application :</b>
<br>

1) Add maven dep :<br>
```xml

<repositories>
    <repository>
        <id>nitinka.mvn.repo</id>
        <url>https://github.com/nitinka/mvn-repo/raw/master</url>
        <!-- use snapshot version -->
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>

<dependency>
    <groupId>nitinka.jmetrics</groupId>
    <artifactId>JMetrics</artifactId>
    <version>0.1.3</version>
</dependency> 
```
<br>
2) Add following in your application.yml file :
<pre>
jMetricConfig:
  archivalEngineClass: "nitinka.jmetrics.archive.RRD4JArchivingEngine"
  configParams:
    basePath: "/var/log/your-app/stats"
</pre><br>

3) Add following in your service initialization code :<br>

<pre>
    JMetric.initialize(configuration.getjMetricConfig());
    environment.addResource(new JMetricController())
</pre>

<br>
4) Add following code in your Dropwizard Applicaion Config Class :<br>

<pre>
    private JMetricConfig jMetricConfig;

    public JMetricConfig getjMetricConfig() {
        return jMetricConfig;
    }

    public void setjMetricConfig(JMetricConfig jMetricConfig) {
        this.jMetricConfig = jMetricConfig;
    }
</pre>

<br>
5) Restart your app and hit following urls
<pre>
  http://host:port/servletPath/metric
     Returns all metrics that are being captured
  http://host:port/servletPath/metric/img
     Returns last 2 day graphical statistics of all metrics
  http://host:port/servletPath/metric/metricName/img
     Returns last 2 day graphical statistics of metric specifird
  http://host:port/servletPath/metric/metricName/raw
     Returns last 2 day raw statistics of metric specifird
</pre>

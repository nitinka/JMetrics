* Its a very simple java lib which allows users to capture various metrics from within running jvm and store them locally using rrd4j library and serve them back using simple http end points from with in the app<br><br>
* JMX metrics are captured out of box. Library also expose single liner code to add any user defined metrics either explicitly or by writing threaded metric monitors.<br><br>
* Such stored metrics can be viewed, graphed to make meaningful decisions in terms of scaling, improving performance and functional aspects.<br><br>
* Users can add threshold limits to any monitored metric( say deadlock, memory etc), when ever any metric breaches the threshold it would also be captured and showcased in report.<br><br>
* This library can be embedded within any existing java application to get meaningful metrics driven information with in few minutes. DThere is direct support for Dropwizard and RestExpress based web application. For stand alone application, JMetric can be told to start a light weight embedded server.<br><br>
* You can start/stop service at runtime just by hitting http end point<br>
* Http end points :
<pre>
  http://host:port/servletPath/metrics
     Returns all metrics that are being captured
  http://host:port/servletPath/metrics/img
     Returns last 2 day graphical statistics of all metrics
  http://host:port/servletPath/metrics/metricName/img
     Returns last 2 day graphical statistics of metric specifird
  http://host:port/servletPath/metrics/metricName/raw
     Returns last 2 day raw statistics of metric specifird

  You can query statistics for specific duration with startTime and endTime query Parameters:
  Possible Values for <b>startTime/endTime</b> : -60s(60 seconds ago from now), -60m(60 mins ago from now)
     Use h and d   for hours and days. 
     If startTime is missing then it is assumed to be -2d (2 days ago from now)
     If endTime is missing then its assumed to be current Time
</pre>
* To Add Threshold to metrics:
<pre>
  Http PUT on http://host:port/servletPath/metrics/metricName/threshold
    [
      {
        level: "CRITICAL",
        check: "GT",
        thresholdValues: [
          -1
        ]
      }
    ]
    check can take : LT, GT, BT with single, single and 2 thresholdValues respectively
    level can take CRITICAL and WARNING

  Http GET on http://host:port/servletPath/metrics/metricName/threshold 
     Returns set threshold for given metric
</pre>
* To Start/Stop Metric Collection:
<pre>
  http://host:port/servletPath/stop
     Stop the monitoring
  http://host:port/servletPath/start
     Start the monitoring
</pre>

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
    <version>0.1.6</version>
</dependency> 
```
<br>
2) Add following in your application.yml file :
<pre>
jMetricConfig:
  archivalEngineClass: "nitinka.jmetrics.archive.RRD4JArchivingEngine"
  configParams:
    basePath: "/var/log/your-app/jmetric/data"
  thresholdPath : "/var/log/your-app/jmetric/threshold"  
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
5) Restart your app and hit urls
<br>
<br><b>Steps to Integrate with RestExpress :</b>
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
    <version>0.1.6</version>
</dependency> 
```
<br>
2) Initialize JMetricConfig class with following details :
<pre>
jMetricConfig:
  archivalEngineClass: "nitinka.jmetrics.archive.RRD4JArchivingEngine"
  configParams:
    basePath: "/var/log/your-app/jmetric/data"
  thresholdPath : "/var/log/your-app/jmetric/threshold"  
</pre><br>

3) Add following in your service initialization code :<br>

<pre>
    JMetric.initialize(jMetricConfigInstance);
    nitinka.jmetrics.controller.restexpress.JMetricController.setup();
</pre>

<br>

4) Restart your app and hit urls

<br>
<br><b>Steps to Embedded JMetric Controlled in other Applications :</b>
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
    <version>0.1.6</version>
</dependency> 
```
<br>
2) Initialize JMetricConfig class with following details :
<pre>
jMetricConfig:
  archivalEngineClass: "nitinka.jmetrics.archive.RRD4JArchivingEngine"
  configParams:
    basePath: "/var/log/your-app/jmetric/data"
  serverPort: 4567
  thresholdPath : "/var/log/your-app/jmetric/threshold"
</pre><br>

3) Add following in your service initialization code :<br>

<pre>
    JMetric.initialize(jMetricConfigInstance);
</pre>

<br>

4) Restart your app and hit urls

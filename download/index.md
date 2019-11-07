---
layout: default
title: Download citeproc-java
---

Everything you need to use citeproc-java has already been prepared
for you in the following binary package:

<div class="download-section">
<a href="https://github.com/michel-kraemer/citeproc-java/releases/download/2.0.0/citeproc-java-tool-2.0.0.zip" class="btn btn-primary btn-lg download-link-main"><i class="icon-download-alt"></i> Download citeproc-java 2.0.0 (binaries)</a>
<a href="https://github.com/michel-kraemer/citeproc-java/releases/download/2.0.0/citeproc-java-2.0.0-javadoc.jar" class="btn btn-default btn-lg download-link-main"><i class="icon-download-alt"></i> JavaDoc</a>
<a href="https://github.com/michel-kraemer/citeproc-java/archive/2.0.0.tar.gz" class="btn btn-default btn-lg download-link-main"><i class="icon-download-alt"></i> Source code</a>
</div>

citeproc-java requires Java 8 or higher.

### Installing with Homebrew

On macOS, you can install citeproc-java with the [Homebrew package manager](http://brew.sh).

    brew tap michel-kraemer/citeproc-java
    brew install citeproc-java

### Installing the library for development

Download the citeproc-java library from Maven central to include it into
your own application.

[http://central.maven.org/maven2/de/undercouch/citeproc-java/2.0.0/](http://central.maven.org/maven2/de/undercouch/citeproc-java/2.0.0/)

The library has dependencies to [JBibTeX](https://github.com/jbibtex/jbibtex),
[ANTLR](http://www.antlr.org/), [Apache Commons Lang](http://commons.apache.org/proper/commons-lang/),
[Apache Commons Text](https://commons.apache.org/proper/commons-text/),
[SnakeYAML](http://www.snakeyaml.org), and
[GraalVM JavaScript](https://github.com/graalvm/graaljs).

I highly recommend using a build tool such as [Maven](http://maven.apache.org/)
or [Gradle](http://www.gradle.org/) to manage your application dependencies.
Add the following snippet to your build file:

<ul class="nav nav-tabs" id="installing-tab">
  <li class="active"><a href="#installing-maven" data-toggle="tab" class="no-scroll">Maven</a></li>
  <li><a href="#installing-gradle" data-toggle="tab" class="no-scroll">Gradle</a></li>
</ul>

<div class="tab-content">

<div class="tab-pane active" id="installing-maven">

{% highlight xml %}
<dependencies>
  <dependency>
    <groupId>de.undercouch</groupId>
    <artifactId>citeproc-java</artifactId>
    <version>2.0.0</version>
  </dependency>
</dependencies>
{% endhighlight %}

</div> <!-- tab-pane installing-maven -->

<div class="tab-pane" id="installing-gradle">

{% highlight groovy %}
repositories {
    mavenCentral()
}

dependencies {
    compile 'de.undercouch:citeproc-java:2.0.0'
}
{% endhighlight %}

</div> <!-- tab-pane installing-gradle -->

</div> <!-- tab-content -->

### Installing CSL Styles and locales

In order to use citeproc-java, you need three things: the library itself,
the CSL styles, and the locales. The [binary bundle](#top)
includes everything you need. However, if you don't use the binary
package (e.g. because you're installing citeproc-java via Maven), you
need to download the CSL styles and locales.

<div class="alert alert-success" markdown="1">
Please note that without these files the citeproc-java library alone
will be rather useless.
</div>

You can download the styles and locales from the following GitHub repositories:

[https://github.com/citation-style-language/styles](https://github.com/citation-style-language/styles)

[https://github.com/citation-style-language/locales](https://github.com/citation-style-language/locales)

For convenience, we provide them as Maven artifacts. Add the following snippet
to your build file:

<ul class="nav nav-tabs" id="installing-csl-tab">
  <li class="active"><a href="#installing-csl-maven" data-toggle="tab" class="no-scroll">Maven</a></li>
  <li><a href="#installing-csl-gradle" data-toggle="tab" class="no-scroll">Gradle</a></li>
</ul>

<div class="tab-content">

<div class="tab-pane active" id="installing-csl-maven">

{% highlight xml %}
<dependencies>
  <dependency>
    <groupId>org.citationstyles</groupId>
    <artifactId>styles</artifactId>
    <version>2.0.0</version>
  </dependency>
  <dependency>
    <groupId>org.citationstyles</groupId>
    <artifactId>locales</artifactId>
    <version>2.0.0</version>
  </dependency>
</dependencies>
{% endhighlight %}

</div> <!-- tab-pane installing-csl-maven -->

<div class="tab-pane" id="installing-csl-gradle">

{% highlight groovy %}
repositories {
    mavenCentral()
}

dependencies {
    compile 'org.citationstyles:styles:2.0.0'
    compile 'org.citationstyles:locales:2.0.0'
}
{% endhighlight %}

</div> <!-- tab-pane installing-csl-gradle -->

</div> <!-- tab-content -->

The artifacts above represent releases that are updated at irregular intervals.
If you need the most up-to-date styles and locales, you may use the
following snapshots from the Sonatype OSS repository, which we update daily:

<ul class="nav nav-tabs" id="installing-csl-snapshots-tab">
  <li class="active"><a href="#installing-csl-snapshots-maven" data-toggle="tab" class="no-scroll">Maven</a></li>
  <li><a href="#installing-csl-snapshots-gradle" data-toggle="tab" class="no-scroll">Gradle</a></li>
</ul>

<div class="tab-content">

<div class="tab-pane active" id="installing-csl-snapshots-maven">

{% highlight xml %}
<repositories>
  <repository>
    <id>oss-snapshots-repo</id>
    <name>Sonatype OSS Maven Repository</name>
    <url>https://oss.sonatype.org/content/groups/public</url>
    <snapshots>
      <enabled>true</enabled>
      <updatePolicy>always</updatePolicy>
    </snapshots>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>org.citationstyles</groupId>
    <artifactId>styles</artifactId>
    <version>2.0.1-SNAPSHOT</version>
  </dependency>
  <dependency>
    <groupId>org.citationstyles</groupId>
    <artifactId>locales</artifactId>
    <version>2.0.1-SNAPSHOT</version>
  </dependency>
</dependencies>
{% endhighlight %}

</div> <!-- tab-pane installing-csl-snapshots-maven -->

<div class="tab-pane" id="installing-csl-snapshots-gradle">

{% highlight groovy %}
repositories {
    maven {
        url 'https://oss.sonatype.org/content/groups/public'
    }
}

dependencies {
    compile 'org.citationstyles:styles:2.0.1-SNAPSHOT'
    compile 'org.citationstyles:locales:2.0.1-SNAPSHOT'
}
{% endhighlight %}

</div> <!-- tab-pane installing-csl-snapshots-gradle -->

</div> <!-- tab-content -->

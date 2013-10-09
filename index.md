---
layout: default
title: citeproc-java
lead: A Citation Style Language (CSL) processor for Java.
---

<div class="sample">
{% capture sample_content %}
{% include sample.md %}
{% endcapture %}
{{ sample_content | markdownify }}
</div>

Introduction
------------

citeproc-java is a [Citation Style Language (CSL)](http://citationstyles.org/)
processor for Java. It interprets CSL styles and generates citations and
bibliographies. In addition to that, citeproc-java contains a
[Bib<span class="tex">T<sub>e</sub>X</span>](http://www.bibtex.org/) converter that is able to map Bib<span class="tex">T<sub>e</sub>X</span>
database entries to CSL citations.

Installing
----------

<div class="alert alert-success" markdown="1">
In order to use citeproc-java you need three things: the library itself,
the Citation Style Language Styles, and the locales (see [below](#installing-csl-styles-and-locales)).
</div>

You can download citeproc-java from Maven central:

[http://central.maven.org/maven2/de/undercouch/citeproc-java/0.1](http://central.maven.org/maven2/de/undercouch/citeproc-java/0.1)

The library has dependencies to [JBibTeX 1.0.8](https://code.google.com/p/java-bibtex/),
[ANTLR 4.1](http://www.antlr.org/), and [Apache Commons Lang 2.6](http://commons.apache.org/proper/commons-lang/).

I highly recommend using a build tool such as [Maven](http://maven.apache.org/)
or [Gradle](http://www.gradle.org/) to manage your application dependencies.
The following snippet can be added to your build file:

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
    <version>0.1</version>
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
    compile 'de.undercouch:citeproc-java:0.1'
}
{% endhighlight %}

</div> <!-- tab-pane installing-gradle -->

</div> <!-- tab-content -->

### Installing CSL Styles and locales

In addition to citeproc-java you will need the CSL styles and locales which
the library interprets to generate citations and bibliographies.

<div class="alert alert-success" markdown="1">
Please note that without these files citeproc-java will be rather useless.
</div>

You can download the styles and locales from the following GitHub repositories:

[https://github.com/citation-style-language/styles](https://github.com/citation-style-language/styles)

[https://github.com/citation-style-language/locales](https://github.com/citation-style-language/locales)

For convenience we provide the styles and locales as Maven artifacts through
the Sonatype OSS repository. The snapshots are updated daily, so you'll always
get the latest styles and locales.

Add the following snippet to your build file:

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
    <version>1.0.1-SNAPSHOT</version>
  </dependency>
  <dependency>
    <groupId>org.citationstyles</groupId>
    <artifactId>locales</artifactId>
    <version>1.0.1-SNAPSHOT</version>
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
    compile 'org.citationstyles:styles:1.0.1-SNAPSHOT'
    compile 'org.citationstyles:locales:1.0.1-SNAPSHOT'
}
{% endhighlight %}

</div> <!-- tab-pane installing-csl-snapshots-gradle -->

</div> <!-- tab-content -->

I highly recommend using the snapshots of the styles and locales as
these are compiled against CSL 1.0.1 and updated daily. However, if
you need a stable version you may refer to the old CSL 1.0 styles and
locales. They are available at Maven central.

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
    <version>1.0</version>
  </dependency>
  <dependency>
    <groupId>org.citationstyles</groupId>
    <artifactId>locales</artifactId>
    <version>1.0</version>
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
    compile 'org.citationstyles:styles:1.0'
    compile 'org.citationstyles:locales:1.0'
}
{% endhighlight %}

</div> <!-- tab-pane installing-csl-gradle -->

</div> <!-- tab-content -->

Building
--------

If you want to hack on citeproc-java (which would be much appreciated
by the way) download its source code from the
[GitHub repository](https://github.com/michel-kraemer/citeproc-java).
Execute the following command to compile the library and to run the
unit tests:

{% highlight bash %}
$ ./gradlew test
{% endhighlight %}

The script automatically downloads the correct Gradle version, so you
won't have to do anything else. If everything runs successfully, you
can create a `.jar` library:

{% highlight bash %}
$ ./gradlew jar
{% endhighlight %}

The library will be located under the ``build/libs`` directory.

To install the library in your local Maven repository execute the
following command:

{% highlight bash %}
$ ./gradlew install
{% endhighlight %}

citeproc-java is CSL 1.0.1 compliant and *all 757 tests* from the
[CSL test suite](https://bitbucket.org/bdarcus/citeproc-test) run
successfully. To run the test suite on your computer just execute the
following command:

{% highlight bash %}
$ ./gradlew runTestSuite
{% endhighlight %}

You can create a project which you can import into
[Eclipse](http://www.eclipse.org) with the following command:

{% highlight bash %}
$ ./gradlew eclipse
{% endhighlight %}

License
-------

citeproc-java is licensed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

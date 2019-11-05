---
layout: default
title: Building citeproc-java
prev: using/getting-started/
prevtitle: Getting started
next: using/importers/
nexttitle: Importers
---

If you want to hack on citeproc-java (which would be much appreciated
by the way), download its source code from the
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

The library will be located under the ``citeproc-java/build/libs`` directory.

To install the library in your local Maven repository execute the
following command:

{% highlight bash %}
$ ./gradlew install
{% endhighlight %}

If you want to build the [command line tool]({{ site.baseurl }}using/command-line-tool)
run the following command:

{% highlight bash %}
$ ./gradlew installDist
{% endhighlight %}

The command line tool will be installed to ``citeproc-java-tool/build/install/citeproc-java-tool``.

citeproc-java is CSL 1.0.1 compliant and *all standard tests* from the
[CSL test suite](https://github.com/citation-style-language/test-suite) run
successfully. To run the test suite on your computer, execute the
following command:

{% highlight bash %}
$ ./gradlew runTestSuite
{% endhighlight %}

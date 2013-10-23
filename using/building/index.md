---
layout: default
title: Building citeproc-java
prev: using/getting-started/
prevtitle: Getting started
next: using/bibtex-converter/
nexttitle: Bib<span class="tex">T<sub>e</sub>X</span> converter
---

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

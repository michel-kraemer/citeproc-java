---
layout: default
title: JavaScript engines
prev: using/output-formats/
prevtitle: Output formats
next: using/command-line-tool/
nexttitle: Command line tool
---

citeproc-java is a wrapper around [citeproc-js](https://github.com/Juris-M/citeproc-js),
which is a popular CSL processor written in JavaScript. Running JavaScript on
the JVM is a topic that seems to be in constant flux. While older versions of
citeproc-java used [Mozilla Rhino](https://www.mozilla.org/rhino/), as of
version 2.0.0, there are three alternatives you can chose from differing in
performance and compatibility to a specific Java version.

Nashorn
-------

[Oracle Nashorn](https://www.oracle.com/technical-resources/articles/java/jf14-nashorn.html)
is the default JavaScript engine if you run citeproc-java under Java 8. You do
not have to configure anything. However, Nashorn is the <em>slowest</em> of all
three alternatives.

Note that [Nashorn has been deprecated](http://openjdk.java.net/jeps/335) and
will be removed from the JVM in the future. I will eventually also remove
support for it from citeproc-java.

GraalVM JavaScript
------------------

If you run citeproc-java under Java 11 or higher,
[GraalVM JavaScript](https://github.com/graalvm/graaljs)
will automatically be selected as the default JavaScript engine. You do not
have to configure anything. In my experience, citeproc-java is about
<em>1.5 times faster</em> when running with GraalVM JavaScript than with Nashorn.

GraalVM JavaScript will be the default engine once support for Java 8 and
Nashorn has been dropped from citeproc-java.

V8
---

While Nashorn and GraalVM JavaScript are engines completely written in Java,
[V8](https://chromium.googlesource.com/v8/v8) is a native implementation
by Google. V8 is known to be fast with a very short ramp-up time. It is the
<em>fastest of the three alternatives</em> and runs with Java 8, 11, and higher.
However, it requires an additional library to be added to your project's
dependencies:

<ul class="nav nav-tabs" id="installing-j2v8-tab">
  <li class="active"><a href="#installing-j2v8-maven" data-toggle="tab" class="no-scroll">Maven</a></li>
  <li><a href="#installing-j2v8-gradle" data-toggle="tab" class="no-scroll">Gradle</a></li>
</ul>

<div class="tab-content">

<div class="tab-pane active" id="installing-j2v8-maven">

{% highlight xml %}
<dependencies>
  <dependency>
    <groupId>com.eclipsesource.j2v8</groupId>
    <artifactId>j2v8_linux_x86_64</artifactId>
    <version>4.6.0</version>
  </dependency>
</dependencies>
{% endhighlight %}

</div> <!-- tab-pane installing-j2v8-maven -->

<div class="tab-pane" id="installing-j2v8-gradle">

{% highlight groovy %}
dependencies {
    compile 'com.eclipsesource.j2v8:j2v8_linux_x86_64:4.6.0'
}
{% endhighlight %}

</div> <!-- tab-pane installing-j2v8-gradle -->

</div> <!-- tab-content -->

Since V8 is a native library, you have to replace `linux_x86_64` by your
operating system and architecture. Other valid values are `win32_x86_64` or
`macosx_x86_64`, for example. For a complete list, visit
[Maven Central](https://search.maven.org/search?q=g:com.eclipsesource.j2v8%20v:4.6.0).

citeproc-java will automatically select V8 as JavaScript engine if it finds it
in the classpath.

Forcing an engine
-----------------

citeproc-java is able to automatically select the fastest JavaScript engine
available. However, if you want to manually select an engine, you can call
`ScriptRunnerFactory#setRunnerType(RunnerType)` before you instantiate your
first `CSL` instance in your application. Valid values for `RunnerType`
are `AUTO`, `V8`, `GRAALJS`, and `JRE` (Nashorn).

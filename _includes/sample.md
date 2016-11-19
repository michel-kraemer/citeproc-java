<ul class="nav nav-tabs" id="samples-tab">
  <li class="active"><a href="#sample-ieee" data-toggle="tab" class="no-scroll">IEEE</a></li>
  <li><a href="#sample-acm-siggraph" data-toggle="tab" class="no-scroll">ACM</a></li>
  <li><a href="#sample-apa" data-toggle="tab" class="no-scroll">APA</a></li>
  <li><a href="#sample-chicago" data-toggle="tab" class="no-scroll">Chicago</a></li>
  <li><a href="#sample-cse" data-toggle="tab" class="no-scroll">CSE</a></li>
  <li><a href="#sample-mla" data-toggle="tab" class="no-scroll">MLA</a></li>
  <li><a href="#sample-others" data-toggle="tab" class="no-scroll">Others &hellip;</a></li>
</ul>

<div class="tab-content">

<div class="tab-pane active" id="sample-ieee">

{% assign breakmake = false %}
{% assign format = "ieee" %}
{% include java-sample.txt %}

<h4>Output</h4>

{% highlight html %}
<div class="csl-bib-body">
  <div class="csl-entry">
    <div class="csl-left-margin">[1]</div>
    <div class="csl-right-inline">M. Krämer, “citeproc-java: A Citation Style
      Language (CSL) processor for Java,” {{ site.time | date: "%d" }}-{{ site.time | date: "%b" }}-{{ site.time | date: "%Y" }}. [Online]. Available:
      http://michel-kraemer.github.io/citeproc-java/. [Accessed: {{ site.time | date: "%d" }}-{{ site.time | date: "%b" }}-{{ site.time | date: "%Y" }}].
    </div>
  </div>
</div>
{% endhighlight %}

<h4>Rendered</h4>

<div class="csl-bib-body">
  <div class="csl-entry">
    <div class="csl-left-margin">[1]</div><div class="csl-right-inline">M. Krämer,
      “citeproc-java: A Citation Style Language (CSL) processor for Java,” {{ site.time | date: "%d" }}-{{ site.time | date: "%b" }}-{{ site.time | date: "%Y" }}. [Online].
      Available: http://michel-kraemer.github.io/citeproc-java/. [Accessed: <span class="today-daylong">{{ site.time | date: "%d" }}</span>-<span class="today-month">{{ site.time | date: "%b" }}</span>-<span class="today-year">{{ site.time | date: "%Y" }}</span>].
    </div>
  </div>
</div>

</div> <!-- tab-pane sample-ieee -->

<div class="tab-pane" id="sample-acm-siggraph">

{% assign breakmake = false %}
{% assign format = "acm-siggraph" %}
{% include java-sample.txt %}

<h4>Output</h4>

{% highlight html %}
<div class="csl-bib-body">
  <div class="csl-entry">
    <span style="font-variant:small-caps;">Krämer, M.</span> {{ site.time | date: "%Y" }}.
    citeproc-java: A Citation Style Language (CSL) processor for Java.
    http://michel-kraemer.github.io/citeproc-java/.
  </div>
</div>
{% endhighlight %}

<h4>Rendered</h4>

<div class="csl-bib-body acm-siggraph">
  <div class="csl-entry">
    <span style="font-variant:small-caps;">Krämer, M.</span> {{ site.time | date: "%Y" }}. citeproc-java: A Citation Style Language (CSL) processor for Java. http://michel-kraemer.github.io/citeproc-java/.
  </div>
</div>

</div> <!-- tab-pane sample-acm-siggraph -->

<div class="tab-pane" id="sample-apa">

{% assign breakmake = false %}
{% assign format = "apa" %}
{% include java-sample.txt %}

<h4>Output</h4>

{% highlight html %}
<div class="csl-bib-body">
  <div class="csl-entry">Krämer, M. ({{ site.time | date: "%Y" }}, {{ site.time | date: "%B" }} {{ site.time | date: "%-d" }}). citeproc-java:
    A Citation Style Language (CSL) processor for Java. Retrieved
    {{ site.time | date: "%B" }} {{ site.time | date: "%-d" }}, {{ site.time | date: "%Y" }}, from http://michel-kraemer.github.io/citeproc-java/
  </div>
</div>
{% endhighlight %}

<h4>Rendered</h4>

<div class="csl-bib-body apa">
  <div class="csl-entry">Krämer, M. ({{ site.time | date: "%Y" }}, {{ site.time | date: "%B" }} {{ site.time | date: "%-d" }}). citeproc-java:
    A Citation Style Language (CSL) processor for Java. Retrieved <span class="today-monthlong">{{ site.time | date: "%B" }}</span> <span class="today-day">{{ site.time | date: "%-d" }}</span>, <span class="today-year">{{ site.time | date: "%Y" }}</span>, from http://michel-kraemer.github.io/citeproc-java/
  </div>
</div>

</div> <!-- tab-pane sample-apa -->

<div class="tab-pane" id="sample-chicago">

{% assign breakmake = false %}
{% assign format = "chicago-author-date" %}
{% include java-sample.txt %}

<h4>Output</h4>

{% highlight html %}
<div class="csl-bib-body">
  <div class="csl-entry">Krämer, Michel. {{ site.time | date: "%Y" }}. “Citeproc-Java: A Citation
    Style Language (CSL) Processor for Java.” {{ site.time | date: "%B" }} {{ site.time | date: "%-d" }}.
    http://michel-kraemer.github.io/citeproc-java/.
  </div>
</div>
{% endhighlight %}

<h4>Rendered</h4>

<div class="csl-bib-body chicago">
  <div class="csl-entry">Krämer, Michel. {{ site.time | date: "%Y" }}. “Citeproc-Java: A Citation
    Style Language (CSL) Processor for Java.” {{ site.time | date: "%B" }} {{ site.time | date: "%-d" }}.
    http://michel-kraemer.github.io/citeproc-java/.
  </div>
</div>

</div> <!-- tab-pane sample-chicago -->

<div class="tab-pane" id="sample-cse">

{% assign breakmake = true %}
{% assign format = "council-of-science-editors" %}
{% include java-sample.txt %}

<h4>Output</h4>

{% highlight html %}
<div class="csl-bib-body">
  <div class="csl-entry">1. Krämer M. citeproc-java: A Citation Style Language
    (CSL) processor for Java. {{ site.time | date: "%Y" }} {{ site.time | date: "%B" }} {{ site.time | date: "%-d" }} [cited {{ site.time | date: "%Y" }} {{ site.time | date: "%B" }} {{ site.time | date: "%-d" }}].
    Available from: http://michel-kraemer.github.io/citeproc-java/
  </div>
</div>
{% endhighlight %}

<h4>Rendered</h4>

<div class="csl-bib-body">
  <div class="csl-entry">1. Krämer M. citeproc-java: A Citation Style Language
    (CSL) processor for Java. {{ site.time | date: "%Y" }} {{ site.time | date: "%B" }} {{ site.time | date: "%-d" }} [cited <span class="today-year">{{ site.time | date: "%Y" }}</span> <span class="today-monthlong">{{ site.time | date: "%B" }}</span> <span class="today-day">{{ site.time | date: "%-d" }}</span>].
    Available from: http://michel-kraemer.github.io/citeproc-java/
  </div>
</div>

</div> <!-- tab-pane sample-cse -->

<div class="tab-pane" id="sample-mla">

{% assign breakmake = true %}
{% assign format = "modern-language-association" %}
{% include java-sample.txt %}

<h4>Output</h4>

{% highlight html %}
<div class="csl-bib-body">
  <div class="csl-entry">Krämer, Michel. “Citeproc-Java: A Citation Style
    Language (CSL) Processor for Java.” {{ site.time | date: "%-d" }} {{ site.time | date: "%B" | replace: 'January', 'Jan.' | replace: 'February', 'Feb.' | replace: 'March', 'Mar.' | replace: 'April', 'Apr.' | replace: 'August', 'Aug.' | replace: 'September', 'Sept.' | replace: 'October', 'Oct.' | replace: 'November', 'Nov.' | replace: 'December', 'Dec.' }} {{ site.time | date: "%Y" }}. Web. {{ site.time | date: "%-d" }} {{ site.time | date: "%B" | replace: 'January', 'Jan.' | replace: 'February', 'Feb.' | replace: 'March', 'Mar.' | replace: 'April', 'Apr.' | replace: 'August', 'Aug.' | replace: 'September', 'Sept.' | replace: 'October', 'Oct.' | replace: 'November', 'Nov.' | replace: 'December', 'Dec.' }} {{ site.time | date: "%Y" }}.
  </div>
</div>
{% endhighlight %}

<h4>Rendered</h4>

<div class="csl-bib-body mla">
  <div class="csl-entry">Krämer, Michel. “Citeproc-Java: A Citation Style
    Language (CSL) Processor for Java.” {{ site.time | date: "%-d" }} {{ site.time | date: "%B" | replace: 'January', 'Jan.' | replace: 'February', 'Feb.' | replace: 'March', 'Mar.' | replace: 'April', 'Apr.' | replace: 'August', 'Aug.' | replace: 'September', 'Sept.' | replace: 'October', 'Oct.' | replace: 'November', 'Nov.' | replace: 'December', 'Dec.' }} {{ site.time | date: "%Y" }}. Web. <span class="today-day">{{ site.time | date: "%-d" }}</span> <span class="today-monthmedium">{{ site.time | date: "%B" | replace: 'January', 'Jan.' | replace: 'February', 'Feb.' | replace: 'March', 'Mar.' | replace: 'April', 'Apr.' | replace: 'August', 'Aug.' | replace: 'September', 'Sept.' | replace: 'October', 'Oct.' | replace: 'November', 'Nov.' | replace: 'December', 'Dec.' }}</span> <span class="today-year">{{ site.time | date: "%Y" }}</span>.
  </div>
</div>

</div> <!-- tab-pane sample-mla -->

<div class="tab-pane" id="sample-others" markdown="1">

<h2>More than 8000 styles</h2>

citeproc-java is based on [citeproc-js](https://github.com/Juris-M/citeproc-js)
and uses the [Citation Style Language (CSL)](http://citationstyles.org/) citation styles.

Select from more than [8000 styles](http://citationstyles.org/styles). All
CSL styles are freely available and distributed under a
[Creative Commons Attribution-ShareAlike license](http://creativecommons.org/licenses/by-sa/3.0/).

</div> <!-- tab-pane sample-others -->

</div> <!-- tab-content -->
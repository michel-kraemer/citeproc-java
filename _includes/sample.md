<span class="label label-primary">Input</span>

{% highlight java %}
CSLItemData item = new CSLItemDataBuilder("citeproc-java", CSLType.WEBPAGE)
    .title("citeproc-java: A Citation Style Language (CSL) processor for Java")
    .author("Michel", "Krämer")
    .issued({{ site.time | date: "%Y" }}, {{ site.time | date: "%-m" }}, {{ site.time | date: "%-d" }})
    .URL("http://michel-kraemer.github.io/citeproc-java/")
    .accessed({{ site.time | date: "%Y" }}, {{ site.time | date: "%-m" }}, {{ site.time | date: "%-d" }})
    .build();

String bibl = CSL.makeAdhocBibliography("ieee", item);
{% endhighlight %}

<span class="label label-primary">Output</span>

{% highlight html %}
<div class="csl-entry">
  <div class="csl-left-margin">[1]</div>
  <div class="csl-right-inline">M. Krämer, “citeproc-java: A Citation Style
    Language (CSL) processor for Java,” {{ site.time | date: "%d" }}-{{ site.time | date: "%b" }}-{{ site.time | date: "%Y" }}. [Online]. Available:
    http://michel-kraemer.github.io/citeproc-java/. [Accessed: {{ site.time | date: "%d" }}-{{ site.time | date: "%b" }}-{{ site.time | date: "%Y" }}].
  </div>
</div>
{% endhighlight %}

<span class="label label-info">Rendered</span>

<div class="csl-entry">
  <div class="csl-left-margin">[1]</div><div class="csl-right-inline">M. Krämer,
    “citeproc-java: A Citation Style Language (CSL) processor for Java,” {{ site.time | date: "%d" }}-{{ site.time | date: "%b" }}-{{ site.time | date: "%Y" }}. [Online].
    Available: http://michel-kraemer.github.io/citeproc-java/. [Accessed: <span class="today-day">{{ site.time | date: "%d" }}</span>-<span class="today-month">{{ site.time | date: "%b" }}</span>-<span class="today-year">{{ site.time | date: "%Y" }}</span>].
  </div>
</div>

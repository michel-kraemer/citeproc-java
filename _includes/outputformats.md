<ul class="nav nav-tabs" id="samples-tab">
  <li class="active"><a href="#outputformat-html" data-toggle="tab" class="no-scroll">HTML</a></li>
  <li><a href="#outputformat-text" data-toggle="tab" class="no-scroll">Text</a></li>
  <li><a href="#outputformat-asciidoc" data-toggle="tab" class="no-scroll">AsciiDoc</a></li>
  <li><a href="#outputformat-fo" data-toggle="tab" class="no-scroll">FO</a></li>
  <li><a href="#outputformat-rtf" data-toggle="tab" class="no-scroll">RTF</a></li>
</ul>

<div class="tab-content">

<div class="tab-pane active" id="outputformat-html">

{% highlight java %}
citeproc.setOutputFormat("html");
{% endhighlight %}

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

</div> <!-- tab-pane outputformat-html -->

<div class="tab-pane" id="outputformat-text">

{% highlight java %}
citeproc.setOutputFormat("text");
{% endhighlight %}

{% highlight text %}
[1]M. Krämer, “citeproc-java: A Citation Style Language (CSL) processor for Java,” {{ site.time | date: "%d" }}-{{ site.time | date: "%b" }}-{{ site.time | date: "%Y" }}. [Online]. Available: http://michel-kraemer.github.io/citeproc-java/. [Accessed: {{ site.time | date: "%d" }}-{{ site.time | date: "%b" }}-{{ site.time | date: "%Y" }}].
{% endhighlight %}

</div> <!-- tab-pane outputformat-text -->

<div class="tab-pane" id="outputformat-asciidoc">

{% highlight java %}
citeproc.setOutputFormat("asciidoc");
{% endhighlight %}

{% highlight text %}
[1] M. Krämer, ``citeproc-java: A Citation Style Language (CSL) processor for Java,'' {{ site.time | date: "%d" }}-{{ site.time | date: "%b" }}-{{ site.time | date: "%Y" }}. [Online]. Available: http://michel-kraemer.github.io/citeproc-java/. [Accessed: {{ site.time | date: "%d" }}-{{ site.time | date: "%b" }}-{{ site.time | date: "%Y" }}].
{% endhighlight %}

</div> <!-- tab-pane outputformat-asciidoc -->

<div class="tab-pane" id="outputformat-fo">

{% highlight java %}
citeproc.setOutputFormat("fo");
{% endhighlight %}

{% highlight xml %}
<fo:block id="citeproc-java">
  <fo:table table-layout="fixed" width="100%">
    <fo:table-column column-number="1" column-width="2.5em"/>
    <fo:table-column column-number="2"
        column-width="proportional-column-width(1)"/>
    <fo:table-body>
      <fo:table-row>
        <fo:table-cell>
          <fo:block>[1]</fo:block>
        </fo:table-cell>
        <fo:table-cell>
          <fo:block>M. Krämer, “citeproc-java: A Citation Style Language
            (CSL) processor for Java,” {{ site.time | date: "%d" }}-{{ site.time | date: "%b" }}-{{ site.time | date: "%Y" }}. [Online]. Available:
            http://michel-kraemer.github.io/citeproc-java/. [Accessed:
            {{ site.time | date: "%d" }}-{{ site.time | date: "%b" }}-{{ site.time | date: "%Y" }}].</fo:block>
        </fo:table-cell>
      </fo:table-row>
    </fo:table-body>
  </fo:table>
</fo:block>
{% endhighlight %}

</div> <!-- tab-pane outputformat-fo -->

<div class="tab-pane" id="outputformat-rtf">

{% highlight java %}
citeproc.setOutputFormat("rtf");
{% endhighlight %}

{% highlight text %}
{% raw %}{\rtf [1]\tab M. Kr\uc0\u228{}mer, \uc0\u8220{}citeproc-java: A Citation Style Language (CSL) processor for Java,\uc0\u8221{} {% endraw %}{{ site.time | date: "%d" }}-{{ site.time | date: "%b" }}-{{ site.time | date: "%Y" }}{% raw %}. [Online]. Available: http://michel-kraemer.github.io/citeproc-java/. [Accessed: {% endraw %}{{ site.time | date: "%d" }}-{{ site.time | date: "%b" }}-{{ site.time | date: "%Y" }}{% raw %}].
}{% endraw %}
{% endhighlight %}

</div> <!-- tab-pane outputformat-rtf -->

</div> <!-- tab-content -->
---
layout: default
title: Getting started with citeproc-java
next: using/building/
nexttitle: Building
---

Generally, citeproc-java runs on JRE 6 or higher. However, JRE 7 or higher is
preferred for the following reasons.

The library wraps around [citeproc-js](https://bitbucket.org/fbennett/citeproc-js/wiki/Home).
It executes this JavaScript library through the Java Scripting API.
Out of the box, citeproc-java runs on all systems with a JRE 7 or higher installed.

Although the JRE 6 is bundled with the [Rhino JavaScript engine](https://developer.mozilla.org/de/docs/Rhino) it lacks
support for E4X (ECMAScript for XML) which is needed by citeproc-java. However,
there is a fallback mechanism integrated into citeproc-java. Just add a recent version
of Rhino to your classpath and the library should automatically use it instead of the
one bundled with the JRE.

If you experience problems please [contact me](http://www.michel-kraemer.com/about).

Getting started
---------------

First you have to create a `ItemDataProvider` that provides
citation item data to the CSL processor. For example, the following
dummy provider returns always the same data:

{% highlight java %}
import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLType;

public class DummyProvider implements ItemDataProvider {
    @Override
    public CSLItemData retrieveItem(String id) {
        return new CSLItemDataBuilder()
            .id(id)
            .type(CSLType.ARTICLE_JOURNAL)
            .title("A dummy journal article")
            .author("John", "Smith")
            .issued(2013, 9, 6)
            .containerTitle("Dummy journal")
            .build();
    }
    public String[] getIds() {
        String ids[] = {"ID-0", "ID-1", "ID-2"};
        return ids;
    }
}
{% endhighlight %}

Note how the item data is created through a neat builder DSL.
In citeproc-java you can use a builder for all model objects.

Of course, in real implementations you would normally load the
citation item data from a file or a database (see the predefined
<a href="#using-the-bibtex-converter">BibTeXItemDataProvider</a> below).

Now you can instantiate the CSL processor.

{% highlight java %}
import de.undercouch.citeproc.CSL;

CSL citeproc = new CSL(new DummyProvider(), "ieee");
citeproc.setOutputFormat("html");
{% endhighlight %}

You have to provide the item data provider and a CSL style (select
one from the 6500+ styles provided by
[CitationStyles.org](http://citationstyles.org/styles/)). The
processor tries to load the style from the classpath, but you may
also pass your own style as a serialized CSL string to the
constructor.

In order to create a bibliography that contains a set of citation
items, call the `registerCitationItems(String...)` method to
introduce the item IDs to the processor.

{% highlight java %}
citeproc.registerCitationItems("ID-1", "ID-2", "ID-3");
{% endhighlight %}

The processor will request the corresponding citation item data
from your `ItemDataProvider`.

Alternatively, you can call `makeCitation(String)` to generate
citation strings that you can insert into your document.

{% highlight java %}
import de.undercouch.citeproc.output.Citation;
import java.util.List;

List<Citation> s1 = citeproc.makeCitation("ID-1");
System.out.println(s1.get(0).getText());
//=> [1] (for the "ieee" style)

List<Citation> s2 = citeproc.makeCitation("ID-2");
System.out.println(s2.get(0).getText());
//=> [2]
{% endhighlight %}

The processor saves each ID so you can generate a bibliography
that contains all citations you used in your document.

{% highlight java %}
import de.undercouch.citeproc.output.Bibliography;

Bibliography bibl = citeproc.makeBibliography();
for (String entry : bibl.getEntries()) {
    System.out.println(entry);
}

//=> [1]....
//   [2]....
//   ....
{% endhighlight %}

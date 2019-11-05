---
layout: default
title: Getting started with citeproc-java
next: using/building/
nexttitle: Building
---

**Note**: The following guide assumes that you either already
[downloaded]({{ site.baseurl }}download) and installed citeproc-java
or that you [built]({{ site.baseurl }}using/building) it from source.

In order to use citeproc-java in your application, you first have to
create an `ItemDataProvider` that provides
citation item data to the CSL processor. For example, the following
dummy provider returns always the same data:

{% highlight java %}
import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLType;

public class MyItemProvider implements ItemDataProvider {
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
In citeproc-java you can use builders for all model objects.

Of course, in a real implementation you would normally load the
citation item data from a file or a database---e.g. see the predefined
<a href="{{ site.baseurl }}using/importers">BibTeXItemDataProvider</a>.

Now you can instantiate the CSL processor.

{% highlight java %}
import de.undercouch.citeproc.CSL;

CSL citeproc = new CSL(new MyItemProvider(), "ieee");
citeproc.setOutputFormat("html");
{% endhighlight %}

You have to provide the item data provider and a CSL style (select
one from the 9000+ styles provided by
[CitationStyles.org](http://citationstyles.org/)). The
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

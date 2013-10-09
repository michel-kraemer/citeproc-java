---
layout: default
title: Using the Bib<span class="tex">T<sub>e</sub>X</span> converter
prev: using/getting-started/
prevtitle: Getting started
next: using/output-formats/
nexttitle: Output formats
---

With citeproc-java you can also generate citations and bibliographies
from your Bib<span class="tex">T<sub>e</sub>X</span> databases.

First you have to load a database:

{% highlight java %}
import java.io.FileInputStream;
import de.undercouch.citeproc.bibtex.BibTeXConverter;
import org.jbibtex.BibTeXDatabase;

BibTeXDatabase db = BibTeXConverter.loadDatabase(new FileInputStream("mydb.bib"));
{% endhighlight %}

After that, you can create a `ItemDataProvider` and pass it to the
CSL processor.

{% highlight java %}
import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;

BibTeXItemDataProvider provider = new BibTeXItemDataProvider();
provider.addDatabase(db);

CSL citeproc = new CSL(provider, "ieee");
{% endhighlight %}

Now you can use the CSL processor as described on the [getting started page]({{ site.baseurl }}using/getting-started/). You can
even call the `registerCitationItems(CSL)` method to generate a
bibliography that contains all items from your Bib<span class="tex">T<sub>e</sub>X</span>
database.

{% highlight java %}
provider.registerCitationItems(citeproc);
{% endhighlight %}

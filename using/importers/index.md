---
layout: default
title: Importers
prev: using/building/
prevtitle: Building
next: using/remote/
nexttitle: Remote connectors
---

citeproc-java supports several input file formats. This allows you
to create citations and bibliographies from your existing citation
databases.

Using the Bib<span class="tex">T<sub>e</sub>X</span> converter
--------------------------------------------------------------

The library supports [Bib<span class="tex">T<sub>e</sub>X</span>](http://www.bibtex.org/)
databases. First you have to load a Bib<span class="tex">T<sub>e</sub>X</span> file as follows:

{% highlight java %}
import java.io.FileInputStream;
import de.undercouch.citeproc.bibtex.BibTeXConverter;
import org.jbibtex.BibTeXDatabase;

BibTeXDatabase db = new BibTeXConverter().loadDatabase(
    new FileInputStream("mydb.bib"));
{% endhighlight %}

After that, you can create an `ItemDataProvider` and pass it to the
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

Using the EndNote and RIS converters
------------------------------------

Importing EndNote and RIS files works exactly like loading
Bib<span class="tex">T<sub>e</sub>X</span> databases. You just have to
use the classes from the ``de.undercouch.citeproc.endnote`` or the
``de.undercouch.citeproc.ris`` package.

For EndNote you may use the following code.

{% highlight java %}
import java.io.FileInputStream;
import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.endnote.EndNoteConverter;
import de.undercouch.citeproc.endnote.EndNoteItemDataProvider;
import de.undercouch.citeproc.endnote.EndNoteLibrary;

EndNoteLibrary lib = new EndNoteConverter().loadLibrary(
    new FileInputStream("mydb.enl"));

EndNoteItemDataProvider provider = new EndNoteItemDataProvider();
provider.addLibrary(lib);

CSL citeproc = new CSL(provider, "ieee");
{% endhighlight %}

For RIS use the following snippet.

{% highlight java %}
import java.io.FileInputStream;
import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.ris.RISConverter;
import de.undercouch.citeproc.ris.RISItemDataProvider;
import de.undercouch.citeproc.ris.RISLibrary;

RISLibrary lib = new RISConverter().loadLibrary(
    new FileInputStream("mydb.ris"));

RISItemDataProvider provider = new RISItemDataProvider();
provider.addLibrary(lib);

CSL citeproc = new CSL(provider, "ieee");
{% endhighlight %}

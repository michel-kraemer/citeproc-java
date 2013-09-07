---
layout: default
title: citeproc-java
lead: A Citation Style Language (CSL) processor for Java.
---

Introduction
------------

citeproc-java is a [Citation Style Language (CSL)](http://citationstyles.org/)
processor for Java. It interprets CSL styles and generates citations and
bibliographies. In addition to that, citeproc-java contains a
[BibTeX](http://www.bibtex.org/) converter that is able to map BibTeX
database entries to CSL citations.

Prerequisites
-------------

The library wraps around [citeproc-js](https://bitbucket.org/fbennett/citeproc-js/wiki/Home).
It executes this JavaScript library through the Java Scripting API
(available since Java 6). The JRE bundles the
[Rhino JavaScript engine](https://developer.mozilla.org/de/docs/Rhino),
so citeproc-java should work on all systems with a JRE 6 or higher
installed. If you experience problems please contact me.

Using
-----

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
        return new CSLItemDataBuilder(id, CSLType.ARTICLE_JOURNAL)
            .title("A dummy journal article")
            .author("John", "Smith")
            .issued(2013, 9, 6)
            .containerTitle("Dummy journal")
            .build();
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

The next step is to introduce citation item IDs to the processor.

{% highlight java %}
citeproc.registerCitationItems("ID-1", "ID-2", "ID-3", ...);
{% endhighlight %}

The processor will request the corresponding citation item data
from your `ItemDataProvider`.

You can now generate citation strings that you can insert into
your document:

{% highlight java %}
String s1 = citeproc.makeCitation("ID-1");
System.out.println(s1)
//=> [1] for the "ieee" style

String s2 = citeproc.makeCitation("ID-1");
System.out.println(s2)
//=> [2]
{% endhighlight %}

Finally, you can generate a bibliography from all citations
you used in your document.

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

Using the BibTeX converter
--------------------------

With citeproc-java you can also generate citations and bibliographies
from your BibTeX databases.

First you have to load a database:

{% highlight java %}
import java.io.FileInputStream;
import de.undercouch.citeproc.bibtex.BibTeXConverter;
import org.jbibtex.BibTeXDatabase;

BibTeXDatabase db = BibTeXConverter.loadDatabase(new FileInputStream("mydb.bib"));
{% endhighlight %}

After that, you can create a `ItemDataProvider` and pass it to the
CSL processor. Make sure you call `registerCitationItems(CSL)` to
introduce all citations in your database to the processor.

{% highlight java %}
import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;

BibTeXItemDataProvider provider = new BibTeXItemDataProvider();
provider.addDatabase(db);

CSL citeproc = new CSL(provider, "ieee");
provider.registerCitationItems(CSL);
{% endhighlight %}

Now you can use the CSL processor as described above.

Building
--------

Execute the following command to compile the library and to run the
unit tests:

{% highlight bash %}
$ ./gradlew test
{% endhighlight %}

The script automatically downloads the correct Gradle version, so you
won't have to do anything else. If everything runs successfully, you
may create a .jar library:

{% highlight bash %}
$ ./gradlew jar
{% endhighlight %}

The library will be located under the ``build/libs`` directory.

To install the library in your local Maven repository execute the
following command:

{% highlight bash %}
$ ./gradlew install
{% endhighlight %}

You will need the CSL styles and locales in your local repo as well.
Please use the `pom.xml` files in my forks:

* https://github.com/michel-kraemer/styles/tree/pom
* https://github.com/michel-kraemer/locales/tree/pom

You have to execute `mvn install` from the forked Git repos to
install the artifacts in your local Maven repository.

Installing
----------

The library is not yet available on Maven central. So, you first
have to follow the building instructions above.

After that, use the following snippets in your build files:

<br><span class="label label-primary">Maven</span>

{% highlight xml %}
<dependencies>
  <dependency>
    <groupId>de.undercouch</groupId>
    <artifactId>citeproc-java</artifactId>
    <version>0.1</version>
  </dependency>
</dependencies>
{% endhighlight %}

<br><span class="label label-primary">Gradle</span>

{% highlight groovy %}
dependencies {
  compile 'de.undercouch:citeproc-java:0.1'
}
{% endhighlight %}

License
-------

citeproc-java is licensed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

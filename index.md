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
installed. If you experience problems please [contact me](http://www.michel-kraemer.com/about).

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

In order to create a bibliography that contains a set of citation
items, call the `registerCitationItems(String...)` method to
introduce the item IDs to the processor.

{% highlight java %}
citeproc.registerCitationItems("ID-1", "ID-2", "ID-3", ...);
{% endhighlight %}

The processor will request the corresponding citation item data
from your `ItemDataProvider`.

Alternatively, you can call `makeCitation(String)` to generate
citation strings that you can insert into your document.

{% highlight java %}
String s1 = citeproc.makeCitation("ID-1");
System.out.println(s1)
//=> [1] for the "ieee" style

String s2 = citeproc.makeCitation("ID-1");
System.out.println(s2)
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
CSL processor.

{% highlight java %}
import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;

BibTeXItemDataProvider provider = new BibTeXItemDataProvider();
provider.addDatabase(db);

CSL citeproc = new CSL(provider, "ieee");
{% endhighlight %}

Now you can use the CSL processor as described above. You can
even call the `registerCitationItems(CSL)` method to generate a
bibliography that contains all items from your BibTeX database.

{% highlight java %}
provider.registerCitationItems(citeproc);
{% endhighlight %}

Output formats
--------------

citeproc-java supports several output formats. The most common
ones are `"html"` and `"text"` but you can also use `"asciidoc"`,
`"fo"`, and `"rtf"`.

Call the CSL processor's `setOutputFormat(String)` method to set
the desired format.

{% capture outputformats_content %}
{% include outputformats.md %}
{% endcapture %}
{{ outputformats_content | markdownify }}

Installing
----------

<div class="alert alert-success" markdown="1">
In order to use citeproc-java you need three things: the library itself,
the Citation Style Language Styles, and the locales (see [below](#installing-csl-styles-and-locales)).
</div>

You can download citeproc-java from Maven central:

[http://central.maven.org/maven2/de/undercouch/citeproc-java/0.1](http://central.maven.org/maven2/de/undercouch/citeproc-java/0.1)

The library has dependencies to [JBibTeX 1.0.8](https://code.google.com/p/java-bibtex/),
[ANTLR 4.1](http://www.antlr.org/), and [Apache Commons Lang 2.6](http://commons.apache.org/proper/commons-lang/).

I highly recommend using a build tool such as [Maven](http://maven.apache.org/)
or [Gradle](http://www.gradle.org/) to manage your application dependencies.
The following snippet can be added to your build file:

<ul class="nav nav-tabs" id="installing-tab">
  <li class="active"><a href="#installing-maven" data-toggle="tab" class="no-scroll">Maven</a></li>
  <li><a href="#installing-gradle" data-toggle="tab" class="no-scroll">Gradle</a></li>
</ul>

<div class="tab-content">

<div class="tab-pane active" id="installing-maven">

{% highlight xml %}
<dependencies>
  <dependency>
    <groupId>de.undercouch</groupId>
    <artifactId>citeproc-java</artifactId>
    <version>0.1</version>
  </dependency>
</dependencies>
{% endhighlight %}

</div> <!-- tab-pane installing-maven -->

<div class="tab-pane" id="installing-gradle">

{% highlight groovy %}
repositories {
    mavenCentral()
}

dependencies {
    compile 'de.undercouch:citeproc-java:0.1'
}
{% endhighlight %}

</div> <!-- tab-pane installing-gradle -->

</div> <!-- tab-content -->

### Installing CSL Styles and locales

In addition to citeproc-java you will need the CSL styles and locales which
the library interprets to generate citations and bibliographies.

<div class="alert alert-success" markdown="1">
Please note that without these files citeproc-java will be rather useless.
</div>

You can download the styles and locales from the following GitHub repositories:

[https://github.com/citation-style-language/styles](https://github.com/citation-style-language/styles)

[https://github.com/citation-style-language/locales](https://github.com/citation-style-language/locales)

For convenience we provide the styles and locales as Maven artifacts through
the Sonatype OSS repository. The snapshots are updated daily, so you'll always
get the latest styles and locales.

Add the following snippet to your build file:

<ul class="nav nav-tabs" id="installing-csl-snapshots-tab">
  <li class="active"><a href="#installing-csl-snapshots-maven" data-toggle="tab" class="no-scroll">Maven</a></li>
  <li><a href="#installing-csl-snapshots-gradle" data-toggle="tab" class="no-scroll">Gradle</a></li>
</ul>

<div class="tab-content">

<div class="tab-pane active" id="installing-csl-snapshots-maven">

{% highlight xml %}
<repositories>
  <repository>
    <id>oss-snapshots-repo</id>
    <name>Sonatype OSS Maven Repository</name>
    <url>https://oss.sonatype.org/content/groups/public</url>
    <snapshots>
      <enabled>true</enabled>
      <updatePolicy>always</updatePolicy>
    </snapshots>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>org.citationstyles</groupId>
    <artifactId>styles</artifactId>
    <version>1.0.1-SNAPSHOT</version>
  </dependency>
  <dependency>
    <groupId>org.citationstyles</groupId>
    <artifactId>locales</artifactId>
    <version>1.0.1-SNAPSHOT</version>
  </dependency>
</dependencies>
{% endhighlight %}

</div> <!-- tab-pane installing-csl-snapshots-maven -->

<div class="tab-pane" id="installing-csl-snapshots-gradle">

{% highlight groovy %}
repositories {
    maven {
        url 'https://oss.sonatype.org/content/groups/public'
    }
}

dependencies {
    compile 'org.citationstyles:styles:1.0.1-SNAPSHOT'
    compile 'org.citationstyles:locales:1.0.1-SNAPSHOT'
}
{% endhighlight %}

</div> <!-- tab-pane installing-csl-snapshots-gradle -->

</div> <!-- tab-content -->

I highly recommend using the snapshots of the styles and locales as
these are compiled against CSL 1.0.1 and updated daily. However, if
you need a stable version you may refer to the old CSL 1.0 styles and
locales. They are available at Maven central.

<ul class="nav nav-tabs" id="installing-csl-tab">
  <li class="active"><a href="#installing-csl-maven" data-toggle="tab" class="no-scroll">Maven</a></li>
  <li><a href="#installing-csl-gradle" data-toggle="tab" class="no-scroll">Gradle</a></li>
</ul>

<div class="tab-content">

<div class="tab-pane active" id="installing-csl-maven">

{% highlight xml %}
<dependencies>
  <dependency>
    <groupId>org.citationstyles</groupId>
    <artifactId>styles</artifactId>
    <version>1.0</version>
  </dependency>
  <dependency>
    <groupId>org.citationstyles</groupId>
    <artifactId>locales</artifactId>
    <version>1.0</version>
  </dependency>
</dependencies>
{% endhighlight %}

</div> <!-- tab-pane installing-csl-maven -->

<div class="tab-pane" id="installing-csl-gradle">

{% highlight groovy %}
repositories {
    mavenCentral()
}

dependencies {
    compile 'org.citationstyles:styles:1.0'
    compile 'org.citationstyles:locales:1.0'
}
{% endhighlight %}

</div> <!-- tab-pane installing-csl-gradle -->

</div> <!-- tab-content -->

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

License
-------

citeproc-java is licensed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

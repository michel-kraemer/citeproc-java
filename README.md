<div align="center">
<h1>
	<br>
	<br>
	<img width="100%" src="https://michel-kraemer.github.io/citeproc-java/images/logo.svg" alt="citeproc-java">
	<br>
	<br>
	<br>
</h1>

<p>
<b>

citeproc-java is a [Citation Style Language (CSL)](http://citationstyles.org/) processor for Java.<br>
It interprets CSL styles and generates citations and bibliographies.


</b>
</p>
</div>

<div align="center">

[![Apache License, Version 2.0](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0) [![Actions Status](https://github.com/michel-kraemer/citeproc-java/workflows/Java%20CI/badge.svg)](https://github.com/michel-kraemer/citeproc-java/actions)

<br>
</div>

Quick start
-----------

```java
CSLItemData item = new CSLItemDataBuilder()
    .type(CSLType.ARTICLE_JOURNAL)
    .title("Protein measurement with the Folin phenol reagent")
    .author(
        new CSLNameBuilder().given("Oliver H.").family("Lowry").build(),
        new CSLNameBuilder().given("Nira J.").family("Rosebrough").build(),
        new CSLNameBuilder().given("A. Lewis").family("Farr").build(),
        new CSLNameBuilder().given("Rose J.").family("Randall").build()
    )
    .issued(1951)
    .containerTitle("The Journal of biological chemistry")
    .volume(193)
    .issue(1)
    .page(265, 275)
    .build();

String bibl = CSL.makeAdhocBibliography("apa", item).makeString();
```

**Output:**

```html
<div class="csl-bib-body">
  <div class="csl-entry">Lowry, O. H., Rosebrough, N. J., Farr, A. L., &amp; Randall, R. J. (1951). Protein measurement with the Folin phenol reagent. <span style="font-style: italic">The Journal of Biological Chemistry</span>, <span style="font-style: italic">193</span>(1), 265&ndash;275.</div>
</div>
```

**Rendered:**

<table><tr><td><br>

Lowry, O. H., Rosebrough, N. J., Farr, A. L., &amp; Randall, R. J. (1951). Protein measurement with the Folin phenol reagent. *The Journal of Biological Chemistry*, *193*(1), 265&ndash;275.<br><br>

</td></tr></table>

Features
--------

* Generates citations and bibliographies
* Supports 10000+ citation styles and various locales
* Different output formats such as `html`, `text`, `asciidoc`, `markdown`, `markdown-pure`, and `fo`
* Importers for BibTeX, EndNote, and RIS
* Command-line tool to execute the library without setting up a development environment

Dependencies
------------

To use citeproc-java, you need three things: the library itself, the CSL styles, and the CSL locales. The styles and locales are distributed separately, because they are updated more frequently than citeproc-java.

Here's the configuration for [Gradle](https://gradle.org/):

```groovy
dependencies {
    implementation 'de.undercouch:citeproc-java:3.2.1'
    implementation 'org.citationstyles:styles:25.4'
    implementation 'org.citationstyles:locales:25.4'
}
```

And here's the configuration for [Maven](http://maven.apache.org/):

```xml
<dependencies>
  <dependency>
    <groupId>de.undercouch</groupId>
    <artifactId>citeproc-java</artifactId>
    <version>3.2.1</version>
  </dependency>
  <dependency>
    <groupId>org.citationstyles</groupId>
    <artifactId>styles</artifactId>
    <version>25.4</version>
  </dependency>
  <dependency>
    <groupId>org.citationstyles</groupId>
    <artifactId>locales</artifactId>
    <version>25.4</version>
  </dependency>
</dependencies>
```

Usage
-----

In the [quick start example above](#quick-start), you have seen how to create bibliographies with `CSL.makeAdhocBibliography()`. This method provides a simple interface that covers many use cases. The following usage instructions give you with much more control.

First, create an `ItemDataProvider` that provides citation item data to the CSL processor. You can either implement this interface yourself or use one of the default implementations such as the `ListItemDataProvider`:

```java
ListItemDataProvider lidp = new ListItemDataProvider(
    new CSLItemDataBuilder()
      	.id("Smith2013")
      	.type(CSLType.ARTICLE_JOURNAL)
      	.title("Some journal article")
      	.author("John", "Smith")
      	.issued(2013)
      	.containerTitle("Some journal")
      	.build(),
    new CSLItemDataBuilder()
        .id("Johnson2024")
        .type(CSLType.BOOK)
        .title("My turbulent life")
        .author("Peter", "Johnson")
        .issued(2024)
        .build()
);
```

Note how the item data is created through a builder DSL. In citeproc-java you can use builders for all model objects.

If you like, you can also load the citation item data from a file. For example, the `BibTeXItemDataProvider` can import BibTeX databases. See the section on [importers](#importers) below.

After having created an `ItemDataProvider`, you can instantiate the CSL processor:

```java
CSL citeproc = new CSL(lidp, "apa");
citeproc.setOutputFormat("text");
```

You have to provide the item data provider and a CSL style (select one of the 10,000+ styles bundled in the `org.citationstyles:styles` dependency). The processor tries to load the style from the classpath, but you may also pass your own style as a serialized CSL string to the constructor.

To create a bibliography that contains a set of citation items, call the `registerCitationItems(String...)` method and introduce the item IDs to the processor:

```java
citeproc.registerCitationItems("Smith2013", "Johnson2024");
```

The processor will request the corresponding citation item data from the provided `ItemDataProvider`.

Alternatively, you can call `makeCitation(String)` to generate citation strings that you can insert into your document.

```java
List<Citation> s1 = citeproc.makeCitation("Smith2013");
System.out.println(s1.get(0).getText());
//=> (Smith, 2013) (for the "apa" style configured above)

List<Citation> s2 = citeproc.makeCitation("Johnson2024");
System.out.println(s2.get(0).getText());
//=> (Johnson, 2024)
```

The processor saves each ID, so you can generate a bibliography that contains all citations you used in your document.

```java
Bibliography bibl = citeproc.makeBibliography();
for (String entry : bibl.getEntries()) {
    System.out.println(entry);
}

//=> Johnson, P. (2024). My turbulent life.
//   Smith, J. (2013). Some journal article. Some Journal.
```

Importers
---------

citeproc-java supports several input file formats. This allows you to create citations and bibliographies from your existing citation databases.

For example, you can load a BibTeX file as follows:

```java
BibTeXDatabase db = new BibTeXConverter().loadDatabase(
    Files.newInputStream(Paths.get("mydb.bib")));
```

After that, you can create an `ItemDataProvider` and pass it to the CSL processor.

```java
BibTeXItemDataProvider provider = new BibTeXItemDataProvider();
    provider.addDatabase(db);

CSL citeproc = new CSL(provider, "apa");
```

Now, you can use the CSL processor as described [above](#usage).

Besides `BibTeXItemDataProvider`, there is `EndNoteItemDataProvider` and `RISItemDataProvider`, which allow you to read EndNote and RIS files respectively.

For convenience, you can use `BibliographyFileReader`, which automatically detects the file format and returns a corresponding `ItemDataProvider`:

```java
BibliographyFileReader bfr = new BibliographyFileReader();
ItemDataProvider idp = bfr.readBibliographyFile(new File("mydb.bib"));
CSL citeproc = new CSL(idp, "apa");
```

Output formats
--------------

citeproc-java supports several output formats. The most common ones are `html` and `text`, but you can also use `asciidoc`, `markdown`, `markdown-pure`, and `fo`.

Call the CSL processor’s `setOutputFormat(String)` method to set the desired format.

```java
citeproc.setOutputFormat("html");
```

Command-line tool
-----------------

citeproc-java binaries are available from the [GitHub releases page](https://github.com/michel-kraemer/citeproc-java/releases). On macOS, you can install the command line tool with [Homebrew](http://brew.sh/).

```sh
brew tap michel-kraemer/citeproc-java
brew install citeproc-java
```


With the command-line tool, can use citeproc-java without setting up a full development environment. The tool allows you to easily create citations and bibliographies. In particular, it comes in handy if ...

* you are an author using CSL and you want a quick way to preview your citations or bibliographies, or if
* you are a CSL style author and want to test your style files.

The tool can render bibliographies and citations, convert a database (e.g. BibTeX, EndNote, or RIS file) to a CSL json document, or list citation IDs from a database. There even is an interactive shell.

To get the tool's help, call the following command:

```sh
citeproc-java --help
```

Building
--------

If you want to hack on citeproc-java (which would be much appreciated, by the way), clone the repository and execute the following command to compile the library and to run the unit tests:

```sh
./gradlew test
```

The script automatically downloads the correct Gradle version, so you won’t have to do anything else. If everything runs successfully, you can create a `.jar` library:

```sh
./gradlew jar
```

The library will be located under the `citeproc-java/build/libs` directory.

To install the library in your local Maven repository execute the following command:

```sh
./gradlew publishToMavenLocal
```

If you want to build the command line tool run the following command:

```sh
./gradlew installDist
```

The command line tool will be installed to `citeproc-java-tool/build/install/citeproc-java-tool`.

Notes on used components
------------------------

### citeproc-js

The library uses [citeproc-js](https://bitbucket.org/fbennett/citeproc-js/wiki/Home) for testing (i.e. in unit tests to compare its own output to what citeproc-js renders). citeproc-js has been created
by Frank G. Bennett and is licensed under the
[Common Public Attribution License Version 1.0](http://bitbucket.org/fbennett/citeproc-js/src/tip/LICENSE).

### Name Parser

The BibTeX name parser's grammar is based on the one found
in the [bibtex-ruby](https://github.com/inukshuk/bibtex-ruby).
The original grammar is licensed under GPL v3. It has been
converted to ANTLR and is released here under the Apache License
2.0 by permission of the original author Sylvester Keil.

### BibTeX Converter

The BibTeX to CSL converter is based on the mapping used in
[Docear](http://www.docear.org) as [presented by Joeran Beel](http://www.docear.org/2012/08/08/docear4word-mapping-bibtex-fields-and-types-with-the-citation-style-language).

Docear is released under the GPLv2 but its code
[may also be reused](http://www.docear.org/software/licence/)
in projects licensed under Apache License 2.0. The mapping is released here
under the Apache License 2.0 by permission of Joeran Beel, Docear.

### Smart Quotes

The algorithm that produces typographically correct quotation marks and
apostrophes is based on [smartquotes.js](https://smartquotes.js.org/) written
by Kelly Martin and released under the MIT license. The code has been
translated to Java and improved to support more edge cases as well as
multiple languages.

### Alphanum Algorithm

citeproc-java is able to sort citations in a natural, language-sensitive way.
The implementation of this behaviour is loosely based on the Alphanum algorithm
by [Dave Koelle](http://www.davekoelle.com/alphanum.html) and the
[Java implementation](http://www.davekoelle.com/files/AlphanumComparator.java)
released under the MIT license. However, it has been extended to use a
`java.text.Collator` for locale-sensitive comparison, and it is also able to
compare arbitrarily large numbers.

### Title case algorithm

The implementation of the algorithm that converts strings to title case is
based on the JavaScript library [to-title-case](https://github.com/gouch/to-title-case)
by David Gouch released under the MIT license. The code has been translated to
Java and was slightly modified to produce strings that adhere to the CSL
specification.

License
-------

citeproc-java is licensed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

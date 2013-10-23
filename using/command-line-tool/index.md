---
layout: default
title: citeproc-java command line tool
prev: using/output-formats/
prevtitle: Output formats
---

The [binary bundle]({{ site.baseurl}}#download) of citeproc-java
contains a command line tool which you can use to execute the library
without setting up a full development environment. The tool allows you
to create citations and bibliographies in an easy and fast manner. In
particular, it comes in very handy if you are in the following situations:

* You are an author using CSL and you want a quick way to preview your
  citations or bibliographies using various styles.
* You are a CSL style author and you want to test your style files
  very quickly.

Description
-----------

The citeproc-java tool's general usage is as follows:

    citeproc-java [OPTION]... [CITATION ID]...

The tool accepts the following command line options:

{:.man-page}
`-b, --bibliography <FILE>`
: Specifies the input bibliography file. Valid input files are
  Bib<span class="tex">T<sub>e</sub>X</span> files (`*.bib`) and CSL
  citations in JSON format (`*.json`).

`-s, --style <STYLE>`
: The citation style name. Can either be a simple name specifying one
  of the 6500+ CSL styles distributed with the command line tool (such
  as `ieee`, `apa`, or `chicago-author-date`), or a string containing
  an XML-serialized representation of a CSL style. The default value
  of this argument is `ieee`.

`-l, --locale <LOCALE>`
: Specifies the locale to use for citations and bibliographies. The
  default value for this argument is `en-US`.

`-f, --format <FORMAT>`
: The output format to use. Valid values are `text` (default), `html`,
  `asciidoc`, `fo`, `rtf`.

`-c, --citation`
: Specifies that citations should be generated instead of a bibliography.
  If you use this argument, you have to provide one or more citation IDs
  to the command line tool.

`-h, --help`
: Displays the help text

`-v, --version`
: Displays the command line tool's version

The citeproc-java tool also accepts one or more citation IDs. How these
IDs are interpreted depends on the generation mode:

* By default the tool generates a bibliography. If you specify
  citation IDs, the bibliography will only include items with
  the given IDs. If you do not specify citation IDs the bibliograpphy
  will contain all items from the given input file.
* If you use the `-c` or `--citation` command line switch,
  the tool will create citations that can be inserted into the text.
  In this case you have to specify at least one citation ID. The
  tool will only create citations for items with the specified IDs.

Examples
--------

Generate a bibliography from all items in the given
Bib<span class="tex">T<sub>e</sub>X</span> file using the `ieee` style
and the `en-US` locale:

    citeproc-java -b references.bib

Generate a bibliography that only contains items with the citation
IDs `Fowler_2010` and `Kisker_2012`:

    citeproc-java -b references.bib Fowler_2010 Kisker_2012

Generate a German bibliography using the `din-1505-2` style:

    citeproc-java -b references.bib -s din-1505-2 -l de-DE

Generate `ieee` citations that can be inserted into the text:

    citeproc-java -b references.bib -c Fowler_2010 Kisker_2012

The tool will help you if you specify an incorrect citation ID. The
following command

    citeproc-java -b references.bib -c Fwler_2010 Kisker_2012

will output

    citeproc-java: unknown citation id: Fwler_2010
    Did you mean `Fowler_2010'?

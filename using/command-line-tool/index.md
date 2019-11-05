---
layout: default
title: citeproc-java command line tool
prev: using/output-formats/
prevtitle: Output formats
---

The [binary bundle]({{ site.baseurl }}download) of citeproc-java
contains a command line tool that you can use to execute the library
without setting up a full development environment. The tool allows you
to create citations and bibliographies in an easy and fast manner. In
particular, it comes in very handy if ...

* you are an author using CSL and you want a quick way to preview your
  citations or bibliographies using various styles, or
* you are a CSL style author and you want to test your style files
  very quickly.

Description
-----------

The citeproc-java tool's general usage is as follows:

    citeproc-java [OPTION]... [COMMAND]

The tool accepts the following command line options:

{:.man-page}
`-o, --output <FILE>`
: Specify a file to write the tool's output to. By default, the tool writes to
  standard out.

`-h, --help`
: Displays the help text

`-V, --version`
: Displays the command line tool's version

The next sections describe the commands you can use and their options.

- [bibliography](#bibliography) - Generate a bibliography from an input file.
- [citation](#citation) - Generate citations from an input file.
- [list](#list) - Display a sorted list of available citation IDs.
- [json](#json) - Convert an input bibliography to JSON.
- [shell](#shell) - Run citeproc-java in interactive mode.
- [help](#help) - Display help about citeproc-java or about a specific command.

bibliography
------------

### Usage

    citeproc-java bibliography [OPTION]... [CITATION ID]...

Generate a bibliography from an input file.

### Options

{:.man-page}
{% include command-line-tool/option-input.md %}
{% include command-line-tool/option-style.md %}
{% include command-line-tool/option-format.md %}
{% include command-line-tool/option-locale.md %}
{% include command-line-tool/option-help.md %}

### Arguments

{:.man-page}
`CITATION ID`
: One or more IDs of citations to include in the bibliography (optional).

### Examples

Generate a bibliography from all items in the given
Bib<span class="tex">T<sub>e</sub>X</span> file using the `ieee` style
and the `en-US` locale:

    citeproc-java bibliography -i references.bib

Generate a bibliography that only contains items with the citation
IDs `Fowler_2010` and `Kisker_2012`:

    citeproc-java bibliography -i references.bib Fowler_2010 Kisker_2012

Generate a German bibliography using the `din-1505-2` style:

    citeproc-java bibliography -i references.bib -s din-1505-2 -l de-DE

citation
--------

### Usage

    citeproc-java citation [OPTION]... [CITATION ID]...

Generate citations from an input file.

### Options

{:.man-page}
{% include command-line-tool/option-input.md %}
{% include command-line-tool/option-style.md %}
{% include command-line-tool/option-format.md %}
{% include command-line-tool/option-locale.md %}
{% include command-line-tool/option-help.md %}

### Arguments

{:.man-page}
`CITATION ID`
: One or more IDs of the citations to generate.

### Examples

Generate `ieee` citations that can be inserted into the text:

    citeproc-java citation -i references.bib Fowler_2010 Kisker_2012

The tool will help you if you specify an incorrect citation ID. The
following command

    citeproc-java citation -i references.bib Fwler_2010 Kisker_2012

will output

    citeproc-java: unknown citation id: Fwler_2010

    Did you mean this?
            Fowler_2010

list
----

### Usage

    citeproc-java list [OPTION]...

Display sorted list of available citation IDs.

### Options

{:.man-page}
{% include command-line-tool/option-input.md %}
{% include command-line-tool/option-help.md %}

json
----

### Usage

    citeproc-java json [OPTION]... [CITATION ID]...

Convert input bibliography to JSON. The resulting JSON object can be used as
input to other CSL processors such as [citeproc-js](https://github.com/Juris-M/citeproc-js).

### Options

{:.man-page}
{% include command-line-tool/option-input.md %}
{% include command-line-tool/option-help.md %}

### Arguments

{:.man-page}
`CITATION ID`
: One or more IDs of citations to convert (optional).

shell
-----

### Usage

    citeproc-java shell

Run citeproc-java in interactive mode.

In the interactive shell you can open input bibliographies and apply commands
quite similar to the ones described above. The shell also keeps a history of
last used commands. Use the `arrow up` and `down` keys to scroll through it.
It also supports auto-completion via the `tab` key.

The following commands are specified:

{:.man-page}
`load <FILE>`
: Load an input bibliography file. Valid input files are
  Bib<span class="tex">T<sub>e</sub>X</span> files (`*.bib`), EndNote files
  (`*.enl`), RIS files (`*.ris`), and CSL citations in JSON (`*.json`) or
  YAML format (`*.yml`, `*.yaml`).

`bibliography [CITATION ID]...`
: Generate a bibliography from the currently loaded input file. See the
  [bibliography](#bibliography) top-level command.

`citation [CITATION ID]...`
: Generate a citations from the currently loaded input file. See the
  [citation](#citation) top-level command.

`list`
: Display sorted list of available citation IDs in the currently loaded
  input file. See the [list](#list) top-level command.

`json [CITATION ID]...`
: Convert currently loaded input bibliography to JSON. See the
  [json](#json) top-level command.

`help [COMMAND]`
: Display the list of possible commands or display help about a given command.

`exit` or `quit`
: Leave the interactive shell

help
----

### Usage

    citeproc-java help [COMMAND]...

Display help about citeproc-java or about a specific command.

---
layout: default
title: Output formats
prev: using/mendeley/
prevtitle: Mendeley connector
next: using/command-line-tool/
nexttitle: Command line tool
---

citeproc-java supports several output formats. The most common
ones are `"html"` and `"text"` but you can also use `"asciidoc"`,
`"fo"`, and `"rtf"`.

Call the CSL processor's `setOutputFormat(String)` method to set
the desired format.

{% capture outputformats_content %}
{% include outputformats.md %}
{% endcapture %}
{{ outputformats_content | markdownify }}

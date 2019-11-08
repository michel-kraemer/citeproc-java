---
layout: default
title: Output formats
prev: using/importers/
prevtitle: Importers
next: using/javascript-engines/
nexttitle: JavaScript engines
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

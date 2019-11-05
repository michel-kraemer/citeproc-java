---
layout: home
title: citeproc-java
lead: A Citation Style Language (CSL) processor for Java.
---

What is citeproc-java?
----------------------

citeproc-java is a [Citation Style Language (CSL)](http://citationstyles.org/)
processor for Java. It interprets CSL styles and generates citations and
bibliographies. Some of the highlights in citeproc-java are:

* [Importers]({{ site.baseurl }}using/importers) for Bib<span class="tex">T<sub>e</sub>X</span>,
  EndNote, and RIS allow you to create citations and bibliographies from
  your existing citation databases.
* The library supports a range of [output formats]({{ site.baseurl }}using/output-formats)
  such as `html`, `text`, `asciidoc`, `rtf`, and `fo`.
* Use the [command line tool]({{ site.baseurl }}using/command-line-tool)
  to execute the library without setting up a development environment.
  This is great for testing, in particular if you are a CSL style author
  and want to test your style files in an easy and quick manner. The
  command line tool even contains an interactive shell with command
  completion and automatic suggestions.
* citeproc-java is CSL 1.0.1 compliant and all standard tests from the
  [CSL test suite](https://github.com/citation-style-language/test-suite) run
  successfully.

Example
-------

<div class="sample">
{% capture sample_content %}
{% include sample.md %}
{% endcapture %}
{{ sample_content | markdownify }}
</div>

License
-------

citeproc-java is licensed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

---
layout: default
title: citeproc-java
lead: A Citation Style Language (CSL) processor for Java.
---

<div class="sample">
{% capture sample_content %}
{% include sample.md %}
{% endcapture %}
{{ sample_content | markdownify }}
</div>

Introduction
------------

citeproc-java is a [Citation Style Language (CSL)](http://citationstyles.org/)
processor for Java. It interprets CSL styles and generates citations and
bibliographies. Some of the highlights in citeproc-java are:

* With the [Bib<span class="tex">T<sub>e</sub>X</span> importer]({{ site.baseurl }}using/bibtex-converter)
  it is possible to create citations and bibliographies from BibTeX files.
* The [command line tool]({{ site.baseurl }}using/command-line-tool) can
  be used to execute the library without setting up a development environment.
  This is great for testing, in particular if you are a CSL style author
  and want to test your style files in an easy and quick manner.
* The library supports a wide range of [output formats]({{ site.baseurl }}using/output-formats)
  such as `html`, `text`, `asciidoc`, `rtf`, and `fo`.
* citeproc-java is CSL 1.0.1 compliant and all tests from the
  [CSL test suite](https://bitbucket.org/bdarcus/citeproc-test) run
  successfully.

License
-------

citeproc-java is licensed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

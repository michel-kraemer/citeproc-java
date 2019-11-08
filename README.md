<h1 align="center">
	<br>
	<br>
	<img width="200" src="https://michel-kraemer.github.io/citeproc-java/images/reader.svg" alt="GeoRocket">
	<br>
	citeproc-java
	<br>
	<br>
	<br>
</h1>

> citeproc-java is a [Citation Style Language (CSL)](http://citationstyles.org/)
> processor for Java. It interprets CSL styles and generates citations and
> bibliographies.

[![Apache License, Version 2.0](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0) [![Actions Status](https://github.com/michel-kraemer/citeproc-java/workflows/Java%20CI/badge.svg)](https://github.com/michel-kraemer/citeproc-java/actions)

Documentation
-------------

The full documentation is available at the project's website:  
https://michel-kraemer.github.io/citeproc-java/

Notes on used components
------------------------

### citeproc-js

The library includes [citeproc-js](https://bitbucket.org/fbennett/citeproc-js/wiki/Home),
a CSL processor written in JavaScript. citeproc-js has been created
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

License
-------

citeproc-java is licensed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

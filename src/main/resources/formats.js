// Copyright 2013 Michel Kraemer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

CSL.Output.Formats.asciidoc = {
	"text_escape": function (text) {
		if (!text) {
			text = "";
		}
		return text.replace("*", "pass:[*]", "g")
			.replace("_", "pass:[_]", "g")
			.replace("#", "pass:[#]", "g")
			.replace("^", "pass:[^]", "g")
			.replace("~", "pass:[~]", "g")
			.replace("[[", "pass:[[[]", "g")
			.replace("  ", "&#160; ", "g")
			.replace(CSL.SUPERSCRIPTS_REGEXP, function(aChar) {
				return "^" + CSL.SUPERSCRIPTS[aChar] + "^";
			});
	},
	
	"bibstart": "",
	"bibend": "",
	"@passthrough/true": CSL.Output.Formatters.passthrough,
	
	"@font-style/italic": "__%%STRING%%__",
	"@font-style/oblique": "__%%STRING%%__",
	"@font-style/normal": false,
	
	"@font-variant/small-caps": "[small-caps]#%%STRING%%#",
	"@font-variant/normal": false,
	
	"@font-weight/bold": "**%%STRING%%**",
	"@font-weight/normal": false,
	"@font-weight/light": false,
	
	"@text-decoration/none": false,
	"@text-decoration/underline": "[underline]##%%STRING%%##",
	
	"@vertical-align/sup": "^^%%STRING%%^^",
	"@vertical-align/sub": "~~%%STRING%%~~",
	"@vertical-align/baseline": false,
	
	"@strip-periods/true": CSL.Output.Formatters.passthrough,
	"@strip-periods/false": CSL.Output.Formatters.passthrough,
	
	"@quotes/true": function (state, str) {
		if ("undefined" === typeof str) {
			return "``";
		}
		return "``" + str + "''";
	},
	
	"@quotes/inner": function (state, str) {
		if ("undefined" === typeof str) {
			return "`";
		}
		return "`" + str + "'";
	},
	
	"@quotes/false": false,

	"@cite/entry": function (state, str) {
		// if wrapCitationEntry does not exist, cite/entry is not applied
		return state.sys.wrapCitationEntry(str, this.item_id, this.locator_txt, this.suffix_txt);
	},
	
	"@bibliography/entry": function (state, str) {
		return str + "\n";
	},
	"@display/block": function (state, str) {
		return str;
	},
	"@display/left-margin": function (state, str) {
		return str;
	},
	"@display/right-inline": function (state, str) {
		return " " + str;
	},
	"@display/indent": function (state, str) {
		return " " + str;
	},
	"@URL/true": function (state, str) {
		//AsciiDoc renders URLs automatically as links
		return str;
	},
	"@DOI/true": function (state, str) {
		return "http://dx.doi.org/" + str + "[" + str + "]";
	}
};

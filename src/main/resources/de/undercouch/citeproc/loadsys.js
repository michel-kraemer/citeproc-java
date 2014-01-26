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

var Sys = function() {
	this.abbrevsname = "default";
};

function __handleReturnValue__(val) {
	if (val == null) {
		return null;
	}
	var ji = val.toJson(__scriptRunner__.createJsonBuilder());
	if (ji.hasOwnProperty('length')) {
		return JSON.parse(ji);
	}
	return ji;
}

Sys.prototype.retrieveLocale = function(lang) {
	var l = __localeProvider__.retrieveLocale(lang);
	if (l == null) {
		return null;
	}
	return new String(l);
};

Sys.prototype.retrieveItem = function(id) {
	return __handleReturnValue__(__itemDataProvider__.retrieveItem(id));
};

Sys.prototype.getAbbreviation = function(styleID, abbrevs, name, category, orig, itemType) {
	var r = __handleReturnValue__(__abbreviationProvider__.getAbbreviations(this.abbrevsname));
	if (r == null) {
		return;
	}
	if (r[category] && r[category][orig]) {
		abbrevs[name][category][orig] = r[category][orig];
	} else {
		abbrevs[name][category][orig] = "";
	}
};

Sys.prototype.setAbbreviations = function(name) {
	this.abbrevsname = name;
};

Sys = new Sys();

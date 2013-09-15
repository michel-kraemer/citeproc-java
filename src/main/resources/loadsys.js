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

var Sys = function() {};

Sys.prototype.retrieveLocale = function(lang) {
	return new String(__localeProvider__.retrieveLocale(lang));
};

Sys.prototype.retrieveItem = function(id) {
	var item = __itemDataProvider__.retrieveItem(id);
	if (item == null) {
		return null;
	}
	var ji = item.toJson(__scriptRunner__.createJsonBuilder());
	if (ji.hasOwnProperty('length')) {
		return JSON.parse(ji);
	}
	return ji;
};

Sys = new Sys();
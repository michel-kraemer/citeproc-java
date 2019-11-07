var Sys = function() {
    this.abbrevsname = "default";
};

Sys.prototype.handleReturnValue = function(val) {
    if (val == null) {
        return null;
    }
    if (typeof val.toJson === "function") {
        val = val.toJson(this.scriptRunner.createJsonBuilder());
    }
    if (val.hasOwnProperty('length')) {
        return JSON.parse(val);
    }
    return val;
}

Sys.prototype.retrieveLocale = function(lang) {
    return this.localeProvider.retrieveLocale(lang);
};

Sys.prototype.retrieveItem = function(id) {
    return this.handleReturnValue(this.itemDataProvider.retrieveItem(id));
};

Sys.prototype.getAbbreviation = function(styleID, abbrevs, name, category, orig, itemType) {
    var r = this.handleReturnValue(this.abbreviationProvider.getAbbreviations(this.abbrevsname));
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

function makeCsl(style, lang, forceLang, scriptRunner, itemDataProvider,
        localeProvider, abbreviationProvider, variableWrapper) {
    var sys = new Sys();
    sys.scriptRunner = scriptRunner;
    sys.itemDataProvider = itemDataProvider;
    sys.localeProvider = localeProvider;
    sys.abbreviationProvider = abbreviationProvider;
    if (variableWrapper) {
        sys.variableWrapper = function(params, prePunct, str, postPunct) {
            return variableWrapper.wrap(params, prePunct, str, postPunct);
        };
    }
    return new CSL.Engine(sys, style, lang, forceLang);
}

function setConvertLinks(engine, convert) {
    engine.opt.development_extensions.wrap_url_and_doi = convert;
}

function getSupportedFormats() {
    var result = [];
    for (var f in CSL.Output.Formats) {
        // disable LaTeX output format until the following issue has been solved:
        // https://github.com/Juris-M/citeproc-js/issues/122
        if (f !== "latex") {
            result.push(f);
        }
    }
    return result;
}

function getBaseLocales() {
    var result = [];
    for (var f in CSL.LANG_BASES) {
        result.push(f);
    }
    return result;
}

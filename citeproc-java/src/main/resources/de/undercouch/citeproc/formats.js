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
    "@showid/true": function (state, str, cslid) {
        if (!state.tmp.just_looking && !state.tmp.suppress_decorations && this.params && "string" === typeof str) {
            var prePunct = "";
            if (str) {
                var m = str.match(CSL.VARIABLE_WRAPPER_PREPUNCT_REX);
                prePunct = m[1];
                str = m[2];
            }
            var postPunct = "";
            if (str && CSL.SWAPPING_PUNCTUATION.indexOf(str.slice(-1)) > -1) {
                postPunct = str.slice(-1);
                str = str.slice(0,-1);
            }
            return state.sys.variableWrapper(this.params, prePunct, str, postPunct);
        } else {
            return str;
        }
    },
    "@URL/true": function (state, str) {
        // AsciiDoc renders URLs automatically as links
        return str;
    },
    "@DOI/true": function (state, str) {
        return "http://dx.doi.org/" + str + "[" + str + "]";
    }
};


CSL.Output.Formats.fo = {
    "text_escape": function (text) {
        if (!text) {
            text = "";
        }
        return text.replace(/&/g, "&#38;")
            .replace(/</g, "&#60;")
            .replace(/>/g, "&#62;")
            .replace("  ", "&#160; ", "g")
            .replace(CSL.SUPERSCRIPTS_REGEXP, function(aChar) {
                return "<fo:inline vertical-align=\"super\">" + CSL.SUPERSCRIPTS[aChar] + "</fo:inline>";
            });
    },
    "bibstart": "",
    "bibend": "",
    "@passthrough/true": CSL.Output.Formatters.passthrough,
    
    "@font-style/italic": "<fo:inline font-style=\"italic\">%%STRING%%</fo:inline>",
    "@font-style/oblique": "<fo:inline font-style=\"oblique\">%%STRING%%</fo:inline>",
    "@font-style/normal": "<fo:inline font-style=\"normal\">%%STRING%%</fo:inline>",
    
    "@font-variant/small-caps": "<fo:inline font-variant=\"small-caps\">%%STRING%%</fo:inline>",
    "@font-variant/normal": "<fo:inline font-variant=\"normal\">%%STRING%%</fo:inline>",
    
    "@font-weight/bold": "<fo:inline font-weight=\"bold\">%%STRING%%</fo:inline>",
    "@font-weight/normal": "<fo:inline font-weight=\"normal\">%%STRING%%</fo:inline>",
    "@font-weight/light": "<fo:inline font-weight=\"lighter\">%%STRING%%</fo:inline>",
    
    "@text-decoration/none": "<fo:inline text-decoration=\"none\">%%STRING%%</fo:inline>",
    "@text-decoration/underline": "<fo:inline text-decoration=\"underline\">%%STRING%%</fo:inline>",
    
    "@vertical-align/sup": "<fo:inline vertical-align=\"super\">%%STRING%%</fo:inline>",
    "@vertical-align/sub": "<fo:inline vertical-align=\"sub\">%%STRING%%</fo:inline>",
    "@vertical-align/baseline": "<fo:inline vertical-align=\"baseline\">%%STRING%%</fo:inline>",
    
    "@strip-periods/true": CSL.Output.Formatters.passthrough,
    "@strip-periods/false": CSL.Output.Formatters.passthrough,
    
    "@quotes/true": function (state, str) {
        if ("undefined" === typeof str) {
            return state.getTerm("open-quote");
        }
        return state.getTerm("open-quote") + str + state.getTerm("close-quote");
    },
    "@quotes/inner": function (state, str) {
        if ("undefined" === typeof str) {
            return "\u2019";
        }
        return state.getTerm("open-inner-quote") + str + state.getTerm("close-inner-quote");
    },
    "@quotes/false": false,
    
    "@cite/entry": function (state, str) {
        return state.sys.wrapCitationEntry(str, this.item_id, this.locator_txt, this.suffix_txt);
    },
    
    "@bibliography/entry": function (state, str) {
        var indent = "";
        if (state.bibliography && state.bibliography.opt && state.bibliography.opt.hangingindent) {
            var hi = state.bibliography.opt.hangingindent;
            indent = " start-indent=\"" + hi +"em\" text-indent=\"-" + hi + "em\"";
        }
        var insert = "";
        if (state.sys.embedBibliographyEntry) {
            insert = state.sys.embedBibliographyEntry(this.item_id) + "\n";
        }
        return "<fo:block id=\"" + this.system_id + "\"" + indent + ">" + str + "</fo:block>\n" + insert;
    },
    
    "@display/block": function (state, str) {
        return "\n  <fo:block>" + str + "</fo:block>\n";
    },
    "@display/left-margin": function (state, str) {
        return "\n  <fo:table table-layout=\"fixed\" width=\"100%\">\n    " +
                "<fo:table-column column-number=\"1\" column-width=\"$$$__COLUMN_WIDTH_1__$$$\"/>\n    " +
                "<fo:table-column column-number=\"2\" column-width=\"proportional-column-width(1)\"/>\n    " +
                "<fo:table-body>\n      " +
                    "<fo:table-row>\n        " +
                        "<fo:table-cell>\n          " +
                            "<fo:block>" + str + "</fo:block>\n        " +
                        "</fo:table-cell>\n        ";
    },
    "@display/right-inline": function (state, str) {
        return "<fo:table-cell>\n          " +
                "<fo:block>" + str + "</fo:block>\n        " +
            "</fo:table-cell>\n      " +
            "</fo:table-row>\n    " +
            "</fo:table-body>\n  " +
            "</fo:table>\n";
    },
    "@display/indent": function (state, str) {
        return "<fo:block margin-left=\"2em\">" + str + "</fo:block>\n";
    },
    "@showid/true": function (state, str, cslid) {
        if (!state.tmp.just_looking && !state.tmp.suppress_decorations && this.params && "string" === typeof str) {
            var prePunct = "";
            if (str) {
                var m = str.match(CSL.VARIABLE_WRAPPER_PREPUNCT_REX);
                prePunct = m[1];
                str = m[2];
            }
            var postPunct = "";
            if (str && CSL.SWAPPING_PUNCTUATION.indexOf(str.slice(-1)) > -1) {
                postPunct = str.slice(-1);
                str = str.slice(0,-1);
            }
            return state.sys.variableWrapper(this.params, prePunct, str, postPunct);
        } else {
            return str;
        }
    },
    "@URL/true": function (state, str) {
        return "<fo:basic-link external-destination=\"url('" + str + "')\">" + str + "</fo:basic-link>";
    },
    "@DOI/true": function (state, str) {
        return "<fo:basic-link external-destination=\"url('http://dx.doi.org/" + str + "')\">" + str + "</fo:basic-link>";
    }
};

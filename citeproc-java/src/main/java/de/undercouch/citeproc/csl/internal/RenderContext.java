package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.CSLCitation;
import de.undercouch.citeproc.csl.CSLCitationItem;
import de.undercouch.citeproc.csl.CSLCitationItemBuilder;
import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.internal.locale.LLocale;
import de.undercouch.citeproc.csl.internal.locale.LTerm;
import de.undercouch.citeproc.csl.internal.rendering.SLabel;
import de.undercouch.citeproc.csl.internal.rendering.SNameInheritableAttributes;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Contains information necessary to render citations and bibliographies. This
 * includes citation items, variables, and terms. The render context also
 * provides methods to emit rendered text to a {@link TokenBuffer}. This buffer
 * is available through the {@link #getResult()} method.
 * @author Michel Kraemer
 */
public class RenderContext {
    /**
     * The style used to render citation items and bibliographies
     */
    private final SStyle style;

    /**
     * Localization data
     */
    private final LLocale locale;

    /**
     * The citation item data to render
     */
    private final CSLItemData itemData;

    /**
     * The citation to render. Will be {@code null} if a bibliography should be
     * rendered.
     */
    private final CSLCitation citation;

    /**
     * All citations generated so far. Will be {@code null} if we are currently
     * rendering a bibliography.
     */
    private final List<GeneratedCitation> generatedCitations;

    /**
     * The citation item to render
     */
    private final CSLCitationItem citationItem;

    /**
     * A token buffer collecting the rendered text
     */
    private final TokenBuffer result = new TokenBuffer();

    /**
     * A set of listeners to call whenever a variable value is fetched from
     * the context
     */
    private final Set<VariableListener> variableListeners;

    /**
     * A set of variables that should not be rendered any more for the rest of
     * the output (i.e. where the context should pretend the variable's value
     * is {@code null}).
     */
    private final Set<String> suppressedVariables;

    /**
     * The last label rendered. This field is an {@link AtomicReference} so
     * child contexts can alter its value.
     */
    private final AtomicReference<SLabel> lastLabelRendered;

    /**
     * Attributes for name elements inherited from the style, bibliography, or citation
     */
    private final SNameInheritableAttributes inheritedNameAttributes;

    /**
     * Creates a new render context
     * @param style the style used to render citation items and bibliographies
     * @param locale localization data
     * @param itemData the citation item to render
     */
    public RenderContext(SStyle style, LLocale locale, CSLItemData itemData) {
        this(style, locale, itemData, null, null);
    }

    /**
     * Creates a new render context
     * @param style the style used to render citation items and bibliographies
     * @param locale localization data
     * @param citation the citation to render
     * @param generatedCitations all citations generated so far
     */
    public RenderContext(SStyle style, LLocale locale, CSLItemData itemData,
            CSLCitation citation, List<GeneratedCitation> generatedCitations) {
        this.style = style;
        this.locale = locale;
        this.itemData = itemData;
        this.citation = citation;
        this.generatedCitations = generatedCitations;
        if (itemData != null) {
            this.citationItem = new CSLCitationItemBuilder(itemData.getId())
                    .itemData(itemData).build();
        } else {
            this.citationItem = null;
        }
        this.variableListeners = new LinkedHashSet<>();
        this.suppressedVariables = new HashSet<>();
        this.lastLabelRendered = new AtomicReference<>();
        this.inheritedNameAttributes = style.getInheritableNameAttributes();
    }

    /**
     * Creates a new render context that has the same attributes as the given
     * parent context but with an empty token buffer. Changes to any of the
     * properties (except for the token buffer) will reflect in the parent
     * context.
     * @param parent the parent context
     */
    public RenderContext(RenderContext parent) {
        this(parent, parent.itemData, parent.citation,
                parent.generatedCitations, parent.citationItem,
                parent.inheritedNameAttributes);
    }

    /**
     * Creates a new render context that has the same attributes as the given
     * parent context but with an empty token buffer and different inherited
     * name attributes. Changes to any of the properties (except for the token
     * buffer) will reflect in the parent context.
     * @param parent the parent context
     * @param inheritedNameAttributes the new inherited name attributes
     */
    public RenderContext(RenderContext parent,
            SNameInheritableAttributes inheritedNameAttributes) {
        this(parent, parent.itemData, parent.citation,
                parent.generatedCitations, parent.citationItem,
                inheritedNameAttributes);
    }

    /**
     * Creates a new render context that has the same attributes as the given
     * parent context but with an empty token buffer and a different citation
     * item. Changes to any of the properties (except for the token buffer)
     * will reflect in the parent context.
     * @param parent the parent context
     * @param citationItem the citation item to render
     */
    public RenderContext(RenderContext parent, CSLCitationItem citationItem) {
        this(parent, citationItem.getItemData(), parent.getCitation(),
                parent.getGeneratedCitations(), citationItem,
                parent.inheritedNameAttributes);
    }

    /**
     * Creates a new render context that has the same attributes as the given
     * parent context but with an empty token buffer and a different citation
     * item. Changes to any of the properties (except for the token buffer)
     * will reflect in the parent context.
     * @param parent the parent context
     * @param itemData the citation item data to render
     * @param citation the citation to render
     * @param generatedCitations all citations generated so far
     * @param citationItem the citation item to render
     * @param inheritedNameAttributes inherited name attributes
     */
    private RenderContext(RenderContext parent, CSLItemData itemData,
            CSLCitation citation, List<GeneratedCitation> generatedCitations,
            CSLCitationItem citationItem,
            SNameInheritableAttributes inheritedNameAttributes) {
        this.style = parent.style;
        this.locale = parent.locale;
        this.itemData = itemData;
        this.citation = citation;
        this.generatedCitations = generatedCitations;
        this.citationItem = citationItem;
        this.variableListeners = parent.variableListeners;
        this.suppressedVariables = parent.suppressedVariables;
        this.lastLabelRendered = parent.lastLabelRendered;
        this.inheritedNameAttributes = inheritedNameAttributes;
    }

    /**
     * Get the style used to render citation items and bibliographies
     * @return the style
     */
    public SStyle getStyle() {
        return style;
    }

    /**
     * Get the localization data
     * @return the localization data
     */
    public LLocale getLocale() {
        return locale;
    }

    /**
     * Get the {@link Locale} that is valid for the current item to render
     * @return the locale
     */
    public Locale getItemLocale() {
        if (itemData != null && itemData.getLanguage() != null) {
            return Locale.forLanguageTag(itemData.getLanguage());
        }
        return getLocale().getLang();
    }

    /**
     * Get the macro with the given name
     * @param name the macro's name
     * @return the macro (never {@code null})
     */
    public SMacro getMacro(String name) {
        SMacro result = style.getMacros().get(name);
        if (result == null) {
            throw new IllegalArgumentException("Unknown macro: " + name);
        }
        return result;
    }

    /**
     * Get the value of a string, date, or name variable
     * @param name the variable's name
     * @return the variable's value or {@code null} if the value is not set
     * @throws IllegalArgumentException if the variable is unknown
     */
    public Object getVariable(String name) {
        return getVariable(name, false);
    }

    /**
     * Get the value of a string, date, or name variable
     * @param name the variable's name
     * @param ignoreListeners {@code true} if {@link VariableListener}s should
     * not be notified about this call.
     * @return the variable's value or {@code null} if the value is not set
     * @throws IllegalArgumentException if the variable is unknown
     */
    public Object getVariable(String name, boolean ignoreListeners) {
        Object result = getStringVariable(name, ignoreListeners);
        if (result != null) {
            return result;
        }

        result = getDateVariable(name, ignoreListeners);
        if (result != null) {
            return result;
        }

        return getNameVariable(name, ignoreListeners);
    }

    /**
     * Get the value of the string variable with the given name
     * @param name the variable's name
     * @return the variable's value or {@code null} if the value is not set
     * @throws IllegalArgumentException if the variable is unknown
     */
    public String getStringVariable(String name) {
        return getStringVariable(name, false);
    }

    /**
     * Get the value of the string variable with the given name
     * @param name the variable's name
     * @param ignoreListeners {@code true} if {@link VariableListener}s should
     * not be notified about this call.
     * @return the variable's value or {@code null} if the value is not set
     * @throws IllegalArgumentException if the variable is unknown
     */
    public String getStringVariable(String name, boolean ignoreListeners) {
        return getStringVariable(name, VariableForm.LONG, ignoreListeners);
    }

    /**
     * Get the value of the string variable with the given name
     * @param name the variable's name
     * @param form the variable form to get
     * @return the variable's value or {@code null} if the value is not set
     * @throws IllegalArgumentException if the variable is unknown
     */
    public String getStringVariable(String name, VariableForm form) {
        return getStringVariable(name, form, false);
    }

    /**
     * Get the value of the string variable with the given name
     * @param name the variable's name
     * @param form the variable form to get
     * @param ignoreListeners {@code true} if {@link VariableListener}s should
     * not be notified about this call.
     * @return the variable's value or {@code null} if the value is not set
     * @throws IllegalArgumentException if the variable is unknown
     */
    public String getStringVariable(String name, VariableForm form, boolean ignoreListeners) {
        String result = null;
        if (!suppressedVariables.contains(name)) {
            switch (name) {
                case "abstract":
                    result = itemData.getAbstrct();
                    break;
                case "annote":
                    result = itemData.getAnnote();
                    break;
                case "archive":
                    result = itemData.getArchive();
                    break;
                case "archive_location":
                    result = itemData.getArchiveLocation();
                    break;
                case "archive-place":
                    result = itemData.getArchivePlace();
                    break;
                case "authority":
                    result = itemData.getAuthority();
                    break;
                case "call-number":
                    result = itemData.getCallNumber();
                    break;
                case "chapter-number":
                    result = itemData.getChapterNumber();
                    break;
                case "citation-label":
                    result = itemData.getCitationLabel();
                    break;
                case "citation-number":
                    result = itemData.getCitationNumber();
                    break;
                case "collection-number":
                    result = itemData.getCollectionNumber();
                    break;
                case "collection-title":
                    if (form == VariableForm.SHORT) {
                        result = itemData.getCollectionTitleShort();
                    }
                    if (result == null) {
                        result = itemData.getCollectionTitle();
                    }
                    break;
                case "collection-title-short":
                    result = itemData.getCollectionTitleShort();
                    break;
                case "container-title":
                    if (form == VariableForm.SHORT) {
                        result = itemData.getContainerTitleShort();
                    }
                    if (result == null) {
                        result = itemData.getContainerTitle();
                    }
                    break;
                case "container-title-short":
                    result = itemData.getContainerTitleShort();
                    break;
                case "dimensions":
                    result = itemData.getDimensions();
                    break;
                case "DOI":
                    result = itemData.getDOI();
                    break;
                case "edition":
                    result = itemData.getEdition();
                    break;
                case "event":
                    result = itemData.getEvent();
                    break;
                case "event-place":
                    result = itemData.getEventPlace();
                    break;
                case "first-reference-note-number":
                    result = itemData.getFirstReferenceNoteNumber();
                    break;
                case "genre":
                    result = itemData.getGenre();
                    break;
                case "ISBN":
                    result = itemData.getISBN();
                    break;
                case "ISSN":
                    result = itemData.getISSN();
                    break;
                case "issue":
                    result = itemData.getIssue();
                    break;
                case "jurisdiction":
                    result = itemData.getJurisdiction();
                    break;
                case "keyword":
                    result = itemData.getKeyword();
                    break;
                case "locator":
                    result = itemData.getLocator();
                    break;
                case "medium":
                    result = itemData.getMedium();
                    break;
                case "note":
                    result = itemData.getNote();
                    break;
                case "number":
                    result = itemData.getNumber();
                    break;
                case "number-of-pages":
                    result = itemData.getNumberOfPages();
                    break;
                case "number-of-volumes":
                    result = itemData.getNumberOfVolumes();
                    break;
                case "original-publisher":
                    result = itemData.getOriginalPublisher();
                    break;
                case "original-publisher-place":
                    result = itemData.getOriginalPublisherPlace();
                    break;
                case "original-title":
                    result = itemData.getOriginalTitle();
                    break;
                case "page":
                    result = itemData.getPage();
                    break;
                case "page-first":
                    result = itemData.getPageFirst();
                    break;
                case "PMCID":
                    result = itemData.getPMCID();
                    break;
                case "PMID":
                    result = itemData.getPMID();
                    break;
                case "publisher":
                    result = itemData.getPublisher();
                    break;
                case "publisher-place":
                    result = itemData.getPublisherPlace();
                    break;
                case "references":
                    result = itemData.getReferences();
                    break;
                case "reviewed-title":
                    result = itemData.getReviewedTitle();
                    break;
                case "scale":
                    result = itemData.getScale();
                    break;
                case "section":
                    result = itemData.getSection();
                    break;
                case "source":
                    result = itemData.getSource();
                    break;
                case "status":
                    result = itemData.getStatus();
                    break;
                case "title":
                    if (form == VariableForm.SHORT) {
                        result = itemData.getTitleShort();
                    }
                    if (result == null) {
                        result = itemData.getTitle();
                    }
                    break;
                case "title-short":
                    result = itemData.getTitleShort();
                    break;
                case "URL":
                    result = itemData.getURL();
                    break;
                case "version":
                    result = itemData.getVersion();
                    break;
                case "volume":
                    result = itemData.getVolume();
                    break;
                case "year-suffix":
                    result = itemData.getYearSuffix();
                    break;
                default:
                    break;
            }
        }

        if (!ignoreListeners) {
            for (VariableListener l : variableListeners) {
                l.onFetchStringVariable(name, result);
            }
        }

        return result;
    }

    /**
     * Get the value of the date variable with the given name
     * @param name the variable's name
     * @return the variable's value or {@code null} if the value is not set
     * @throws IllegalArgumentException if the date variable is unknown
     */
    public CSLDate getDateVariable(String name) {
        return getDateVariable(name, false);
    }

    /**
     * Get the value of the date variable with the given name
     * @param name the variable's name
     * @param ignoreListeners {@code true} if {@link VariableListener}s should
     * not be notified about this call.
     * @return the variable's value or {@code null} if the value is not set
     * @throws IllegalArgumentException if the date variable is unknown
     */
    public CSLDate getDateVariable(String name, boolean ignoreListeners) {
        CSLDate result;
        if (!suppressedVariables.contains(name)) {
            switch (name) {
                case "accessed":
                    result = itemData.getAccessed();
                    break;
                case "container":
                    result = itemData.getContainer();
                    break;
                case "event-date":
                    result = itemData.getEventDate();
                    break;
                case "issued":
                    result = itemData.getIssued();
                    break;
                case "original-date":
                    result = itemData.getOriginalDate();
                    break;
                case "submitted":
                    result = itemData.getSubmitted();
                    break;
                default:
                    result = null;
                    break;
            }
        } else {
            result = null;
        }

        if (!ignoreListeners) {
            for (VariableListener l : variableListeners) {
                l.onFetchDateVariable(name, result);
            }
        }

        return result;
    }

    public CSLName[] getNameVariable(String name) {
        return getNameVariable(name, false);
    }

    public CSLName[] getNameVariable(String name, boolean ignoreListeners) {
        CSLName[] result;
        if (!suppressedVariables.contains(name)) {
            switch (name) {
                case "author":
                    result = itemData.getAuthor();
                    break;
                case "collection-editor":
                    result = itemData.getCollectionEditor();
                    break;
                case "composer":
                    result = itemData.getComposer();
                    break;
                case "container-author":
                    result = itemData.getContainerAuthor();
                    break;
                case "director":
                    result = itemData.getDirector();
                    break;
                case "editor":
                    result = itemData.getEditor();
                    break;
                case "editorial-director":
                    result = itemData.getEditorialDirector();
                    break;
                case "illustrator":
                    result = itemData.getIllustrator();
                    break;
                case "interviewer":
                    result = itemData.getInterviewer();
                    break;
                case "original-author":
                    result = itemData.getOriginalAuthor();
                    break;
                case "recipient":
                    result = itemData.getRecipient();
                    break;
                case "reviewed-author":
                    result = itemData.getReviewedAuthor();
                    break;
                case "translator":
                    result = itemData.getTranslator();
                    break;
                default:
                    result = null;
                    break;
            }
        } else {
            result = null;
        }

        if (!ignoreListeners) {
            for (VariableListener l : variableListeners) {
                l.onFetchNameVariable(name, result);
            }
        }

        return result;
    }

    /**
     * Add a variable to the set of suppressed variables. Suppressed variables
     * should not be rendered any more for the rest of the output. The context
     * will pretend the variable's value is {@code null}).
     * @param name the variable's name
     */
    public void suppressVariable(String name) {
        suppressedVariables.add(name);
    }

    /**
     * Get the singular long form of a term
     * @param name the term's name
     * @return the term's value (never {@code null})
     */
    public String getTerm(String name) {
        return getTerm(name, LTerm.Form.LONG);
    }

    /**
     * Get the long form of a term
     * @param name the term's name
     * @param plural {@code true} if the plural form should be retrieved,
     * {@code false} for the singular form
     * @return the term's value (never {@code null})
     */
    public String getTerm(String name, boolean plural) {
        return getTerm(name, LTerm.Form.LONG, plural);
    }

    /**
     * Get the singular form of a term
     * @param name the term's name
     * @param form the form to retrieve
     * @return the term's value (never {@code null})
     */
    public String getTerm(String name, LTerm.Form form) {
        return getTerm(name, form, false);
    }

    /**
     * Get a term
     * @param name the term's name
     * @param form the form to retrieve
     * @param plural {@code true} if the plural form should be retrieved,
     * {@code false} for the singular form
     * @return the term's value (or {@code null} if the term is unknown)
     */
    public String getTerm(String name, LTerm.Form form, boolean plural) {
        Map<String, LTerm> tm = locale.getTerms().get(form);
        if (tm == null) {
            throw new IllegalStateException("Unknown term form: " + form);
        }
        LTerm t = tm.get(name);
        if (t == null) {
            return null;
        }
        if (plural) {
            return t.getMultiple();
        }
        return t.getSingle();
    }

    /**
     * Get the citation item data currently being rendered
     * @return the citation item data
     */
    public CSLItemData getItemData() {
        return itemData;
    }

    /**
     * Get the citation currently being rendered. Will return {@code null} if
     * we are currently rendering a bibliography and not a citation.
     * @return the citation
     */
    public CSLCitation getCitation() {
        return citation;
    }

    /**
     * Get all citations generated so far. Will return {@code null} if
     * we are currently rendering a bibliography and not a citation.
     * @return all citations generated so far
     */
    public List<GeneratedCitation> getGeneratedCitations() {
        return generatedCitations;
    }

    /**
     * Get the citation item currently being rendered
     * @return the citation item
     */
    public CSLCitationItem getCitationItem() {
        return citationItem;
    }

    /**
     * Adds a variable listener to this context
     * @param listener the variable listener to register
     */
    public void addVariableListener(VariableListener listener) {
        variableListeners.add(listener);
    }

    /**
     * Removes a variable listener from this context
     * @param listener the variable listener to remove
     */
    public void removeVariableListener(VariableListener listener) {
        variableListeners.remove(listener);
    }

    /**
     * Get the set of variable listeners
     * @return the set
     */
    public Set<VariableListener> getVariableListeners() {
        return variableListeners;
    }

    /**
     * Save the last label rendered
     * @param label the label (may be {@code null})
     */
    public void setLastLabelRendered(SLabel label) {
        lastLabelRendered.set(label);
    }

    /**
     * Get the last label rendered
     * @return the label (may be {@code null})
     */
    public SLabel getLastLabelRendered() {
        return lastLabelRendered.get();
    }

    /**
     * Get attributes that can be inherited to name elements
     * @return the attributes
     */
    public SNameInheritableAttributes getInheritedNameAttributes() {
        return inheritedNameAttributes;
    }

    /**
     * Emit a text token
     * @param text the text token
     * @return this render context
     */
    public RenderContext emit(String text) {
        return emit(text, Token.Type.TEXT);
    }

    /**
     * Emit a text token and attach the given formatting attributes to it
     * (unless they are {code 0})
     * @param text the text token
     * @param formattingAttributes the token's formatting attributes
     * @return this render context
     */
    public RenderContext emit(String text, int formattingAttributes) {
        return emit(text, Token.Type.TEXT, formattingAttributes);
    }

    /**
     * Emit a token of a given type
     * @param text the token's text
     * @param type the token's type
     * @return this render context
     */
    public RenderContext emit(String text, Token.Type type) {
        if (text != null) {
            result.append(text, type);
        }
        return this;
    }

    /**
     * Emit a token of a given type and attach the specified formatting
     * attributes to it (unless they are {code 0})
     * @param text the token's text
     * @param type the token's type
     * @param formattingAttributes the token's formatting attributes
     * @return this render context
     */
    public RenderContext emit(String text, Token.Type type, int formattingAttributes) {
        if (text != null) {
            result.append(text, type, formattingAttributes);
        }
        return this;
    }

    /**
     * Emit a token
     * @param token the token
     * @return this render context
     */
    public RenderContext emit(Token token) {
        result.append(token);
        return this;
    }

    /**
     * Emit all tokens from the given token buffer
     * @param buffer the token buffer
     * @return this render context
     */
    public RenderContext emit(TokenBuffer buffer) {
        result.append(buffer);
        return this;
    }

    /**
     * Emit all tokens from the given token buffer and append the given
     * formatting attributes to all of them (unless they are {code 0})
     * @param buffer the token buffer
     * @param formattingAttributes the formatting attributes to append
     * @return this render context
     */
    public RenderContext emit(TokenBuffer buffer, int formattingAttributes) {
        if (formattingAttributes == 0) {
            result.append(buffer);
        } else {
            buffer.getTokens().stream()
                    .map(t -> new Token.Builder(t)
                            .mergeFormattingAttributes(formattingAttributes)
                            .build())
                    .forEach(result::append);
        }
        return this;
    }

    /**
     * Get a token buffer containing all emitted tokens
     * @return the token buffer
     */
    public TokenBuffer getResult() {
        return result;
    }

    /**
     * Reset the render context so it can be used to render another citation
     */
    public void reset() {
        suppressedVariables.clear();
        variableListeners.clear();
        lastLabelRendered.set(null);
    }
}

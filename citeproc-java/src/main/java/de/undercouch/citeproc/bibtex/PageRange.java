package de.undercouch.citeproc.bibtex;

/**
 * A range of pages
 * @author Michel Kraemer
 */
public class PageRange {
    private final String literal;
    private final String pageFirst;
    private final String pageLast;
    private final Integer numberOfPages;
    private final boolean multiplePages;

    /**
     * Constructs a range of pages
     * @param literal the string from which this range has been created
     * @param pageFirst the first page in the range (can be {@code null})
     * @param numberOfPages the number of pages in this range (can be {@code null})
     * @param multiplePages {@code true} if this object represents multiple
     * pages (may be {@code true} even if the actual {@code numberOfPages}
     * could not be determined)
     */
    public PageRange(String literal, String pageFirst, String pageLast,
            Integer numberOfPages, boolean multiplePages) {
        this.literal = literal;
        this.pageFirst = pageFirst;
        this.pageLast = pageLast;
        this.numberOfPages = numberOfPages;
        this.multiplePages = multiplePages;
    }
    /**
     * @return the string from which this range has been created
     */
    public String getLiteral() {
        return literal;
    }

    /**
     * @return the first page in the range (can be {@code null})
     */
    public String getPageFirst() {
        return pageFirst;
    }

    /**
     * @return the last page in the range (can be {@code null})
     */
    public String getPageLast() {
        return pageLast;
    }

    /**
     * @return the number of pages in this range (can be {@code null})
     */
    public Integer getNumberOfPages() {
        return numberOfPages;
    }

    /**
     * @return {@code true} if this object represents multiple pages (may be
     * {@code true} even if the actual number of pages could not be determined,
     * i.e. if {@link #getNumberOfPages()} returns {@code null})
     */
    public boolean isMultiplePages() {
        return multiplePages;
    }
}

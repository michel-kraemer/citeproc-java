package de.undercouch.citeproc.bibtex;

/**
 * A range of pages
 * @author Michel Kraemer
 */
public class PageRange {
    private final String literal;
    private final String pageFirst;
    private final Integer numberOfPages;

    /**
     * Constructs a range of pages
     * @param literal the string from which this range has been created
     * @param pageFirst the first page in the range (can be {@code null})
     * @param numberOfPages the number of pages in this range (can be {@code null})
     */
    public PageRange(String literal, String pageFirst, Integer numberOfPages) {
        this.literal = literal;
        this.pageFirst = pageFirst;
        this.numberOfPages = numberOfPages;
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
     * @return the number of pages in this range (can be {@code null})
     */
    public Integer getNumberOfPages() {
        return numberOfPages;
    }
}

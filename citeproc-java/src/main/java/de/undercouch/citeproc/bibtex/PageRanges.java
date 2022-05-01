package de.undercouch.citeproc.bibtex;

import org.apache.commons.lang3.StringUtils;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A list of {@link PageRange}s
 * @author Michel Kraemer
 */
public class PageRanges extends AbstractList<PageRange> {
    private final List<PageRange> list = new ArrayList<>();

    /**
     * Construct an empty list of page ranges
     */
    public PageRanges() {
        // nothing to do here
    }

    /**
     * Construct a list containing a single page range
     * @param singleRange the page range
     */
    public PageRanges(PageRange singleRange) {
        list.add(singleRange);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public PageRange get(int index) {
        return list.get(index);
    }

    @Override
    public PageRange set(int index, PageRange element) {
        return list.set(index, element);
    }

    @Override
    public boolean add(PageRange pageRange) {
        return list.add(pageRange);
    }

    @Override
    public PageRange remove(int index) {
        return list.remove(index);
    }

    /**
     * @return the literal representation of this list of page ranges
     */
    public String getLiteral() {
        return list.stream()
                .map(PageRange::getLiteral)
                .collect(Collectors.joining(", "));
    }

    /**
     * @return the first page of all page ranges in this list
     */
    public String getPageFirst() {
        String first = null;
        for (PageRange pr : list) {
            if (first == null) {
                first = pr.getPageFirst();
            } else if (pr.getPageFirst() != null && StringUtils.isNumeric(first) &&
                    StringUtils.isNumeric(first)) {
                int pp1 = Integer.parseInt(first);
                int pp2 = Integer.parseInt(pr.getPageFirst());
                if (pp2 < pp1) {
                    first = pr.getPageFirst();
                }
            }
        }
        return first;
    }

    /**
     * @return the sum of the number of pages of all page ranges in this list
     * or {@code null} if the page ranges were unparsable and the sum could
     * not be determined
     */
    public Integer getNumberOfPages() {
        List<PageRange> filteredList = list.stream()
                .filter(pr -> pr.getNumberOfPages() != null)
                .collect(Collectors.toList());
        if (filteredList.isEmpty()) {
            return null;
        }
        return filteredList.stream()
                .mapToInt(PageRange::getNumberOfPages)
                .sum();
    }

    /**
     * @return {@code true} if the page ranges in this list represent multiple pages
     */
    public boolean isMultiplePages() {
        return list.size() > 1 || list.stream().anyMatch(PageRange::isMultiplePages);
    }
}

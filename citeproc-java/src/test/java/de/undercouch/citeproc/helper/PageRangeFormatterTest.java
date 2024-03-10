package de.undercouch.citeproc.helper;

import de.undercouch.citeproc.bibtex.PageParser;
import de.undercouch.citeproc.bibtex.PageRange;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static de.undercouch.citeproc.helper.PageRangeFormatter.Format.CHICAGO15;
import static de.undercouch.citeproc.helper.PageRangeFormatter.Format.CHICAGO16;
import static de.undercouch.citeproc.helper.PageRangeFormatter.Format.EXPANDED;
import static de.undercouch.citeproc.helper.PageRangeFormatter.Format.MINIMAL;
import static de.undercouch.citeproc.helper.PageRangeFormatter.Format.MINIMAL2;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link PageRangeFormatter}. Test cases are based on the CSL test suite
 * (<a href="https://github.com/citation-style-language/test-suite">https://github.com/citation-style-language/test-suite</a>)
 * but slightly modified.
 * @author Michel Kraemer
 */
public class PageRangeFormatterTest {
    @Test
    public void expanded() {
        Map<String, String> cases = new LinkedHashMap<>();
        cases.put("101", "101");
        cases.put("any", "any");

        cases.put("101-108", "101–108");
        cases.put("3-10", "3–10");
        cases.put("71-72", "71–72");
        cases.put("96-117", "96–117");
        cases.put("100-4", "100–104");
        cases.put("600-13", "600–613");
        cases.put("1100-23", "1100–1123");
        cases.put("107-108", "107–108");
        cases.put("505-17", "505–517");
        cases.put("1002-1006", "1002–1006");
        cases.put("321-325", "321–325");
        cases.put("415-532", "415–532");
        cases.put("1536-538", "1536–1538");
        cases.put("11564-11568", "11564–11568");
        cases.put("11564-11578", "11564–11578");
        cases.put("13792-13803", "13792–13803");
        cases.put("1496-504", "1496–1504");
        cases.put("2787-816", "2787–2816");
        cases.put("n11564 - n1568", "n11564–n11568");
        cases.put("n11564 - 1568", "n11564–1568");

        cases.put("110-5", "110–115");
        cases.put("N110 - 5", "N110–5");
        cases.put("N110 - N5", "N110–N115");
        cases.put("110 - N6", "110–N6");
        cases.put("N110 - P5", "N110–P5");
        cases.put("123N110 - N5", "123N110–N5");

        for (Map.Entry<String, String> e : cases.entrySet()) {
            PageRange range = PageParser.parse(e.getKey()).get(0);
            assertEquals(e.getValue(), PageRangeFormatter.format(range, EXPANDED));
        }
    }

    @Test
    public void chicago15() {
        Map<String, String> cases = new LinkedHashMap<>();
        cases.put("3-10", "3–10");
        cases.put("71-72", "71–72");
        cases.put("96-113", "96–113");

        cases.put("100-104", "100–104");
        cases.put("600-613", "600–613");
        cases.put("1100-1123", "1100–1123");

        cases.put("107-108", "107–8");
        cases.put("505-517", "505–17");
        cases.put("1002-1006", "1002–6");
        cases.put("1536-38", "1536–38");

        cases.put("321-325", "321–25");
        cases.put("415-532", "415–532");
        cases.put("11564-11568", "11564–68");
        cases.put("13792-13803", "13792–803");

        cases.put("1496-1504", "1496–1504");
        cases.put("2787-2816", "2787–2816");

        for (Map.Entry<String, String> e : cases.entrySet()) {
            PageRange range = PageParser.parse(e.getKey()).get(0);
            assertEquals(e.getValue(), PageRangeFormatter.format(range, CHICAGO15));
        }
    }

    @Test
    public void chicago16() {
        Map<String, String> cases = new LinkedHashMap<>();
        cases.put("3-10", "3–10");
        cases.put("71-72", "71–72");
        cases.put("96-113", "96–113");

        cases.put("100-104", "100–104");
        cases.put("600-613", "600–613");
        cases.put("1100-1123", "1100–1123");

        cases.put("107-108", "107–8");
        cases.put("505-517", "505–17");
        cases.put("1002-1006", "1002–6");
        cases.put("1536-38", "1536–38");

        cases.put("321-325", "321–25");
        cases.put("415-532", "415–532");
        cases.put("1087–1089", "1087–89");
        cases.put("1496-1500", "1496–500");
        cases.put("2787-2816", "2787–816");
        cases.put("11564-11568", "11564–68");
        cases.put("13792-13803", "13792–803");
        cases.put("12991-13001", "12991–3001");

        for (Map.Entry<String, String> e : cases.entrySet()) {
            PageRange range = PageParser.parse(e.getKey()).get(0);
            assertEquals(e.getValue(), PageRangeFormatter.format(range, CHICAGO16));
        }
    }

    @Test
    public void minimal() {
        Map<String, String> cases = new LinkedHashMap<>();
        cases.put("101-108", "101–8");
        cases.put("3-10", "3–10");
        cases.put("71-72", "71–2");
        cases.put("96-117", "96–117");
        cases.put("100-4", "100–4");
        cases.put("600-13", "600–13");
        cases.put("600-613", "600–13");
        cases.put("1100-23", "1100–23");
        cases.put("1100-1123", "1100–23");
        cases.put("107-108", "107–8");
        cases.put("505-517", "505–17");
        cases.put("505-17", "505–17");
        cases.put("1002-1006", "1002–6");
        cases.put("321-325", "321–5");
        cases.put("415-532", "415–532");
        cases.put("1536-538", "1536–8");
        cases.put("11564-11568", "11564–8");
        cases.put("11564-11578", "11564–78");
        cases.put("13792-13803", "13792–803");
        cases.put("1496-504", "1496–504");
        cases.put("2787-816", "2787–816");
        cases.put("n11564 - n1568", "n11564–8");
        cases.put("n11564 - 1568", "n11564–1568");

        for (Map.Entry<String, String> e : cases.entrySet()) {
            PageRange range = PageParser.parse(e.getKey()).get(0);
            assertEquals(e.getValue(), PageRangeFormatter.format(range, MINIMAL));
        }
    }

    @Test
    public void minimal2() {
        Map<String, String> cases = new LinkedHashMap<>();
        cases.put("101-108", "101–8");
        cases.put("3-10", "3–10");
        cases.put("71-72", "71–72");
        cases.put("96-117", "96–117");
        cases.put("100-4", "100–4");
        cases.put("600-13", "600–13");
        cases.put("600-613", "600–13");
        cases.put("1100-23", "1100–23");
        cases.put("1100-1123", "1100–23");
        cases.put("107-108", "107–8");
        cases.put("505-517", "505–17");
        cases.put("505-17", "505–17");
        cases.put("1002-1006", "1002–6");
        cases.put("321-325", "321–25");
        cases.put("415-532", "415–532");
        cases.put("1536-538", "1536–38");
        cases.put("11564-11568", "11564–68");
        cases.put("11564-11578", "11564–78");
        cases.put("13792-13803", "13792–803");
        cases.put("1496-504", "1496–504");
        cases.put("2787-816", "2787–816");
        cases.put("n11564 - n1568", "n11564–68");
        cases.put("n11564 - 1568", "n11564–1568");

        for (Map.Entry<String, String> e : cases.entrySet()) {
            PageRange range = PageParser.parse(e.getKey()).get(0);
            assertEquals(e.getValue(), PageRangeFormatter.format(range, MINIMAL2));
        }
    }
}

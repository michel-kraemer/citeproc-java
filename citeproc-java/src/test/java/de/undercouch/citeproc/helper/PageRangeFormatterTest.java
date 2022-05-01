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

        cases.put("101-108", "101\u2013108");
        cases.put("3-10", "3\u201310");
        cases.put("71-72", "71\u201372");
        cases.put("96-117", "96\u2013117");
        cases.put("100-4", "100\u2013104");
        cases.put("600-13", "600\u2013613");
        cases.put("1100-23", "1100\u20131123");
        cases.put("107-108", "107\u2013108");
        cases.put("505-17", "505\u2013517");
        cases.put("1002-1006", "1002\u20131006");
        cases.put("321-325", "321\u2013325");
        cases.put("415-532", "415\u2013532");
        cases.put("1536-538", "1536\u20131538");
        cases.put("11564-11568", "11564\u201311568");
        cases.put("11564-11578", "11564\u201311578");
        cases.put("13792-13803", "13792\u201313803");
        cases.put("1496-504", "1496\u20131504");
        cases.put("2787-816", "2787\u20132816");
        cases.put("n11564 - n1568", "n11564\u2013n11568");
        cases.put("n11564 - 1568", "n11564\u20131568");

        cases.put("110-5", "110\u2013115");
        cases.put("N110 - 5", "N110\u20135");
        cases.put("N110 - N5", "N110\u2013N115");
        cases.put("110 - N6", "110\u2013N6");
        cases.put("N110 - P5", "N110\u2013P5");
        cases.put("123N110 - N5", "123N110\u2013N5");

        for (Map.Entry<String, String> e : cases.entrySet()) {
            PageRange range = PageParser.parse(e.getKey()).get(0);
            assertEquals(e.getValue(), PageRangeFormatter.format(range, EXPANDED));
        }
    }

    @Test
    public void chicago15() {
        Map<String, String> cases = new LinkedHashMap<>();
        cases.put("3-10", "3\u201310");
        cases.put("71-72", "71\u201372");
        cases.put("96-113", "96\u2013113");

        cases.put("100-104", "100\u2013104");
        cases.put("600-613", "600\u2013613");
        cases.put("1100-1123", "1100\u20131123");

        cases.put("107-108", "107\u20138");
        cases.put("505-517", "505\u201317");
        cases.put("1002-1006", "1002\u20136");
        cases.put("1536-38", "1536\u201338");

        cases.put("321-325", "321\u201325");
        cases.put("415-532", "415\u2013532");
        cases.put("11564-11568", "11564\u201368");
        cases.put("13792-13803", "13792\u2013803");

        cases.put("1496-1504", "1496\u20131504");
        cases.put("2787-2816", "2787\u20132816");

        for (Map.Entry<String, String> e : cases.entrySet()) {
            PageRange range = PageParser.parse(e.getKey()).get(0);
            assertEquals(e.getValue(), PageRangeFormatter.format(range, CHICAGO15));
        }
    }

    @Test
    public void chicago16() {
        Map<String, String> cases = new LinkedHashMap<>();
        cases.put("3-10", "3\u201310");
        cases.put("71-72", "71\u201372");
        cases.put("96-113", "96\u2013113");

        cases.put("100-104", "100\u2013104");
        cases.put("600-613", "600\u2013613");
        cases.put("1100-1123", "1100\u20131123");

        cases.put("107-108", "107\u20138");
        cases.put("505-517", "505\u201317");
        cases.put("1002-1006", "1002\u20136");
        cases.put("1536-38", "1536\u201338");

        cases.put("321-325", "321\u201325");
        cases.put("415-532", "415\u2013532");
        cases.put("1087–1089", "1087\u201389");
        cases.put("1496-1500", "1496\u2013500");
        cases.put("2787-2816", "2787\u2013816");
        cases.put("11564-11568", "11564\u201368");
        cases.put("13792-13803", "13792\u2013803");
        cases.put("12991-13001", "12991\u20133001");

        for (Map.Entry<String, String> e : cases.entrySet()) {
            PageRange range = PageParser.parse(e.getKey()).get(0);
            assertEquals(e.getValue(), PageRangeFormatter.format(range, CHICAGO16));
        }
    }

    @Test
    public void minimal() {
        Map<String, String> cases = new LinkedHashMap<>();
        cases.put("101-108", "101\u20138");
        cases.put("3-10", "3\u201310");
        cases.put("71-72", "71\u20132");
        cases.put("96-117", "96\u2013117");
        cases.put("100-4", "100\u20134");
        cases.put("600-13", "600\u201313");
        cases.put("600-613", "600\u201313");
        cases.put("1100-23", "1100\u201323");
        cases.put("1100-1123", "1100\u201323");
        cases.put("107-108", "107\u20138");
        cases.put("505-517", "505\u201317");
        cases.put("505-17", "505\u201317");
        cases.put("1002-1006", "1002\u20136");
        cases.put("321-325", "321\u20135");
        cases.put("415-532", "415\u2013532");
        cases.put("1536-538", "1536\u20138");
        cases.put("11564-11568", "11564\u20138");
        cases.put("11564-11578", "11564\u201378");
        cases.put("13792-13803", "13792\u2013803");
        cases.put("1496-504", "1496\u2013504");
        cases.put("2787-816", "2787\u2013816");
        cases.put("n11564 - n1568", "n11564\u20138");
        cases.put("n11564 - 1568", "n11564\u20131568");

        for (Map.Entry<String, String> e : cases.entrySet()) {
            PageRange range = PageParser.parse(e.getKey()).get(0);
            assertEquals(e.getValue(), PageRangeFormatter.format(range, MINIMAL));
        }
    }

    @Test
    public void minimal2() {
        Map<String, String> cases = new LinkedHashMap<>();
        cases.put("101-108", "101\u20138");
        cases.put("3-10", "3\u201310");
        cases.put("71-72", "71\u201372");
        cases.put("96-117", "96\u2013117");
        cases.put("100-4", "100\u20134");
        cases.put("600-13", "600\u201313");
        cases.put("600-613", "600\u201313");
        cases.put("1100-23", "1100\u201323");
        cases.put("1100-1123", "1100\u201323");
        cases.put("107-108", "107\u20138");
        cases.put("505-517", "505\u201317");
        cases.put("505-17", "505\u201317");
        cases.put("1002-1006", "1002\u20136");
        cases.put("321-325", "321\u201325");
        cases.put("415-532", "415\u2013532");
        cases.put("1536-538", "1536\u201338");
        cases.put("11564-11568", "11564\u201368");
        cases.put("11564-11578", "11564\u201378");
        cases.put("13792-13803", "13792\u2013803");
        cases.put("1496-504", "1496\u2013504");
        cases.put("2787-816", "2787\u2013816");
        cases.put("n11564 - n1568", "n11564\u201368");
        cases.put("n11564 - 1568", "n11564\u20131568");

        for (Map.Entry<String, String> e : cases.entrySet()) {
            PageRange range = PageParser.parse(e.getKey()).get(0);
            assertEquals(e.getValue(), PageRangeFormatter.format(range, MINIMAL2));
        }
    }
}

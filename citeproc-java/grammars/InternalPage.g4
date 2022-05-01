grammar InternalPage;

@header {
package de.undercouch.citeproc.bibtex.internal;

import de.undercouch.citeproc.bibtex.PageRange;
import de.undercouch.citeproc.bibtex.PageRanges;
import org.apache.commons.lang3.StringUtils;
}

pages
  returns [
    PageRanges ranges = new PageRanges()
  ]
  : r1=range {
      String r1literal = $r1.literal;
      if (r1literal == null) {
        r1literal = $r1.text;
      }
      $ranges.add(new PageRange(r1literal, $r1.pageFrom, $r1.pageTo, $r1.numberOfPages, $r1.multiplePages));
    }
    (
      z1=SPACE* z2=COMMA z3=SPACE* r2=range {
        String r2literal = $r2.literal;
        if (r2literal == null) {
          r2literal = $r2.text;
        }
        $ranges.add(new PageRange(r2literal, $r2.pageFrom, $r2.pageTo, $r2.numberOfPages, $r2.multiplePages));
      }
    )*
  ;

range
  returns [
    String literal,
    String pageFrom,
    String pageTo,
    Integer numberOfPages,
    boolean multiplePages = false
  ]
  @after {
    if ($pageFrom != null && $pageTo != null && StringUtils.isNumeric($pageFrom) && StringUtils.isNumeric($pageTo)) {
      int pf = Integer.parseInt($pageFrom);
      int pt = Integer.parseInt($pageTo);
      $numberOfPages = pt - pf + 1;
      
      if (pf == pt) {
        $literal = String.valueOf(pf);
      } else {
        $literal = pf + "-" + pt;
      }
    }
  }
  : PAGE { $pageFrom = $pageTo = $PAGE.text; }
  | p1=PAGE SPACE* DASH+ SPACE* p2=PAGE { $pageFrom = $p1.text; $pageTo = $p2.text; $multiplePages = true; }
  ;

SPACE : ' ' ;
COMMA : ',' ;
DASH : [-\u2013] ;
PAGE : [0-9a-zA-Z?:\u00C0-\u2012\u2014-\uFFFF]+ ;

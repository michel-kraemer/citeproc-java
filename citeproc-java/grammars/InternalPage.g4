grammar InternalPage;

@header {
package de.undercouch.citeproc.bibtex.internal;

import de.undercouch.citeproc.bibtex.PageRange;
import org.apache.commons.lang3.StringUtils;
}

@members {
  private Integer addPages(Integer p1, Integer p2) {
    if (p1 == null)
      p1 = p2;
    else if (p2 != null)
      p1 += p2;
    return p1;
  }
  
  private String makePageFrom(String p1, String p2) {
    if (p1 == null)
      p1 = p2;
    else if (p2 != null && StringUtils.isNumeric(p1) && StringUtils.isNumeric(p2)) {
      int pp1 = Integer.parseInt(p1);
      int pp2 = Integer.parseInt(p2);
      if (pp2 < pp1)
        p1 = p2;
    }
    return p1;
  }
}

pages
  locals [
    String literal,
    Integer numberOfPages,
    String pageFrom
  ]
  : r1=range {
      $numberOfPages = addPages($numberOfPages, $r1.numberOfPages);
      $pageFrom = makePageFrom($pageFrom, $r1.pageFrom);
      if ($r1.literal != null)
        $literal = $r1.literal;
      else
        $literal = $r1.text;
    }
    (
      SPACE? COMMA SPACE? r2=range {
        $numberOfPages = addPages($numberOfPages, $r2.numberOfPages);
        $pageFrom = makePageFrom($pageFrom, $r2.pageFrom);
        $literal += ",";
        if ($r2.literal != null)
          $literal += $r2.literal;
        else
          $literal += $r2.text;
      }
    )*
  ;

range
  returns [
    String literal,
    String pageFrom,
    Integer numberOfPages
  ]
  locals [
    String pageTo = null
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
  | p1=PAGE SPACE? DASH SPACE? p2=PAGE { $pageFrom = $p1.text; $pageTo = $p2.text; }
  ;

SPACE : ' '+ ;
COMMA : ',' ;
DASH : [-\u2013]+ ;
PAGE : [0-9a-zA-Z\?\:\u00C0-\u2012\u2014-\uFFFF]+ ;

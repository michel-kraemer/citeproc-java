grammar InternalNumber;

@header {
package de.undercouch.citeproc.csl.internal.helper;

import de.undercouch.citeproc.csl.CSLLabel;
}

numbers
  returns [
    List<NumberElement> elements = new ArrayList<>()
  ]
  : SPACE* e1=element {
    $elements.add($e1.el);
  }
  (
    SPACE* (
      ',' { $elements.add(new NumberElement(", ")); }
    | ';' { $elements.add(new NumberElement("; ")); }
    )+ SPACE* e2=element {
      $elements.add($e2.el);
    }
  )*
  ( SPACE | ',' | ';' )* EOF
  ;

element
  returns [
    NumberElement el
  ]
  : l=label SPACE* r1=range {
    $el = new NumberElement($r1.parsedText, $l.lbl, $r1.plural);
  } | r2=range ( SPACE+ ( label | range) )+ {
    $el = new NumberElement($text);
  } | r3=range {
    $el = new NumberElement($r3.parsedText, null, $r3.plural);
  }
  ;

label
  returns [
    CSLLabel lbl
  ]
  : sl=short_label { $lbl = $sl.lbl; }
  | ll=long_label { $lbl = $ll.lbl; }
  ;

short_label
  returns [
    CSLLabel lbl
  ]
  : ('bk.'   | 'bks.'   ) { $lbl = CSLLabel.BOOK; }
  | ('ch.'              ) { $lbl = CSLLabel.CHAPTER; }
  | ('chap.' | 'chaps.' ) { $lbl = CSLLabel.CHAPTER; }
  | ('col.'  | 'cols.'  ) { $lbl = CSLLabel.COLUMN; }
  | ('fig.'  | 'figs.'  ) { $lbl = CSLLabel.FIGURE; }
  | ('fol.'  | 'fols.'  ) { $lbl = CSLLabel.FOLIO; }
  | ('no.'   | 'nos.'   ) { $lbl = CSLLabel.ISSUE; }
  | ('l.'    | 'll.'    ) { $lbl = CSLLabel.LINE; }
  | ('n.'    | 'nn.'    ) { $lbl = CSLLabel.NOTE; }
  | ('op.'   | 'opp.'   ) { $lbl = CSLLabel.OPUS; }
  | ('p.'    | 'pp.'    ) { $lbl = CSLLabel.PAGE; }
  | ('para.' | 'paras.' ) { $lbl = CSLLabel.PARAGRAPH; }
  | ('pt.'   | 'pts.'   ) { $lbl = CSLLabel.PART; }
  | ('sec.'  | 'secs.'  ) { $lbl = CSLLabel.SECTION; }
  | ('sv.'   | 'svv.'   ) { $lbl = CSLLabel.SUB_VERBO; }
  | ('s.v.'  | 's.vv.'  ) { $lbl = CSLLabel.SUB_VERBO; }
  | ('vrs.'             ) { $lbl = CSLLabel.VERSE; }
  | ('v.'    | 'vv.'    ) { $lbl = CSLLabel.VERSE; }
  | ('vol.'  | 'vols.'  ) { $lbl = CSLLabel.VOLUME; }
  ;

long_label
  returns [
    CSLLabel lbl
  ]
  : ( 'book'      | 'books'      ) { $lbl = CSLLabel.BOOK; }
  | ( 'chapter'   | 'chapters'   ) { $lbl = CSLLabel.CHAPTER; }
  | ( 'column'    | 'columns'    ) { $lbl = CSLLabel.COLUMN; }
  | ( 'figure'    | 'figures'    ) { $lbl = CSLLabel.FIGURE; }
  | ( 'folio'     | 'folios'     ) { $lbl = CSLLabel.FOLIO; }
  | ( 'issue'     | 'issues'     ) { $lbl = CSLLabel.ISSUE; }
  | ( 'number'    | 'numbers'    ) { $lbl = CSLLabel.ISSUE; }
  | ( 'line'      | 'lines'      ) { $lbl = CSLLabel.LINE; }
  | ( 'note'      | 'notes'      ) { $lbl = CSLLabel.NOTE; }
  | ( 'opus'      | 'opuses'     ) { $lbl = CSLLabel.OPUS; }
  | ( 'opera'                    ) { $lbl = CSLLabel.OPUS; }
  | ( 'page'      | 'pages'      ) { $lbl = CSLLabel.PAGE; }
  | ( 'paragraph' | 'paragraphs' ) { $lbl = CSLLabel.PARAGRAPH; }
  | ( 'part'      | 'parts'      ) { $lbl = CSLLabel.PART; }
  | ( 'section'   | 'sections'   ) { $lbl = CSLLabel.SECTION; }
  | ( 'sub verbo' | 'sub verbis' ) { $lbl = CSLLabel.SUB_VERBO; }
  | ( 'sub-verbo' | 'sub-verbis' ) { $lbl = CSLLabel.SUB_VERBO; }
  | ( 'verse'     | 'verses'     ) { $lbl = CSLLabel.VERSE; }
  | ( 'volume'    | 'volumes'    ) { $lbl = CSLLabel.VOLUME; }
  ;

range
  returns [
    boolean plural = false,
    String parsedText
  ]
  : n1=NUMBER {
    $parsedText = $n1.text;
  } (
    ( sep=( ';' | ',' | ':' | '-' | '\u2013' | '&' | 'and' | SPACE ) {
      $plural |= !Character.isWhitespace($sep.text.charAt(0));
      boolean lastWhite = Character.isWhitespace($parsedText.charAt($parsedText.length() - 1));
      switch ($sep.text) {
        case ";":
          $parsedText += "; ";
          break;
        case ",":
          $parsedText += ", ";
          break;
        case ":":
          $parsedText += ":";
          break;
        case "-":
        case "\u2013":
          $parsedText += "\u2013";
          break;
        case "&":
          if (!lastWhite) {
            $parsedText += " ";
          }
          $parsedText += "& ";
          break;
        case "and":
          if (!lastWhite) {
            $parsedText += " ";
          }
          $parsedText += "and ";
          break;
        case " ":
          if (!lastWhite) {
            $parsedText += " ";
          }
          break;
        default:
          break;
      }
    } )+ n2=NUMBER {
      $parsedText += $n2.text;
    }
  )*
  ;

// Numbers such as 1, 2.3, 10a-b, 1.a, I.a, 1.I, I, and IV
NUMBER : [0-9.]+[a-zA-Z]+[-:][a-zA-Z]+ | [0-9a-zA-Z.]+ ;
SPACE  : [\p{White_Space}] ;

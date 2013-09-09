// This grammar is based on names.y from the bibtex-ruby project:
// https://github.com/inukshuk/bibtex-ruby/blob/2.3.4/lib/bibtex/names.y
//
// The original grammar is licensed under GPL v3. It has been
// converted to ANTLR and is released here under the Apache License 2.0
// by permission of the original author Sylvester Keil.

grammar InternalName;

@header {
package de.undercouch.citeproc.bibtex.internal;

import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.CSLNameBuilder;
}

names
  locals [
    List<CSLName> result = new ArrayList<CSLName>();
  ]
  : name { if ($name.result != null) $result.add($name.result); } ( SPACE AND SPACE name { if ($name.result != null) $result.add($name.result); } )*
  ;

name
  returns [
    CSLName result
  ]
  locals [
     CSLNameBuilder builder = new CSLNameBuilder();
  ]
  @after {
    $result = $builder.build();
  }
  : uwords SPACE word SPACE? COMMA SPACE? UWORD { $builder.given($uwords.text).suffix($UWORD.text).family($word.text); }
  | uwords SPACE von SPACE last SPACE? COMMA SPACE? first { $builder.given($first.result[1]).suffix($first.result[0]).nonDroppingParticle($uwords.text + " " + $von.text).family($last.text); }
  | von SPACE last SPACE? COMMA SPACE? first { $builder.given($first.result[1]).suffix($first.result[0]).nonDroppingParticle($von.text).family($last.text); }
  | last SPACE? COMMA SPACE? first { $builder.given($first.result[1]).suffix($first.result[0]).family($last.text); }
  | von SPACE last { $builder.nonDroppingParticle($von.text).family($last.text); }
  | uwords SPACE von SPACE last { $builder.given($uwords.text).nonDroppingParticle($von.text).family($last.text); }
  | uwords SPACE word { $builder.given($uwords.text).family($word.text); }
  | word { $builder.family($word.text); }
  ;

uwords
  : UWORD ( SPACE UWORD )*
  ;

word
  : UWORD | LWORD
  ;

words
  : word ( SPACE word )*
  ;

von
  : LWORD
  | von SPACE LWORD
  | von SPACE uwords SPACE LWORD
  ;

first
  returns [
    String[] result
  ]
  @init {
    $result = new String[2];
  }
  : words? { $result[1] = $words.text; }
  | w1=words? SPACE? COMMA SPACE? w2=words? { $result[0] = $w1.text; $result[1] = $w2.text; }
  ;

last
  : LWORD | uwords
  ;

AND  : 'and' ;
SPACE : ' '+ ;
COMMA : ',' ;
UWORD : ULETTER ( ULETTER | LLETTER )* ;
LWORD : LLETTER ( ULETTER | LLETTER )* ;
fragment ULETTER : [A-Z\u00C0-\uFFFF\(\?] ;
fragment LLETTER : [a-z\-\)\&\/\.] ;

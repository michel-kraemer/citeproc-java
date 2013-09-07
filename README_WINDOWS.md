If try to build this site with Jekyll under Windows and you get
the following error

    Regenerating: 1 files at 2013-09-07 19:17:45   Liquid Exception:
    incompatible character encodings: UTF-8 and CP850 in index.md

then execute the following command

    chcp 65001

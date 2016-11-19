FROM openjdk:8-jre-alpine
MAINTAINER Michel Kraemer <michel@undercouch.de>

ENV CITEPROC_JAVA_ARCHIVENAME citeproc-java-tool-1.0.0

RUN apk --no-cache add wget bash \
    && wget https://github.com/michel-kraemer/citeproc-java/releases/download/1.0.0/$CITEPROC_JAVA_ARCHIVENAME.zip \
    && unzip $CITEPROC_JAVA_ARCHIVENAME.zip \
    && rm $CITEPROC_JAVA_ARCHIVENAME.zip \
    && apk del wget \
    && echo "#!/bin/bash" > entrypoint.sh \
    && echo "$CITEPROC_JAVA_ARCHIVENAME/bin/citeproc-java \$@" >> entrypoint.sh \
    && chmod +x entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]

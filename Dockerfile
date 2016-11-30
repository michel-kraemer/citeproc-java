FROM openjdk:8-jre-alpine
MAINTAINER Michel Kraemer <michel@undercouch.de>

RUN CITEPROC_JAVA_VERSION=1.0.1 \
    && CITEPROC_JAVA_ARCHIVENAME=citeproc-java-tool-$CITEPROC_JAVA_VERSION \
    && apk --no-cache add wget bash \
    && wget https://github.com/michel-kraemer/citeproc-java/releases/download/$CITEPROC_JAVA_VERSION/$CITEPROC_JAVA_ARCHIVENAME.zip \
    && unzip $CITEPROC_JAVA_ARCHIVENAME.zip \
    && rm $CITEPROC_JAVA_ARCHIVENAME.zip \
    && apk del wget \
    && echo "#!/bin/bash" > entrypoint.sh \
    && echo "$CITEPROC_JAVA_ARCHIVENAME/bin/citeproc-java \$@" >> entrypoint.sh \
    && chmod +x entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]

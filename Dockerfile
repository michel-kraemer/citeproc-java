FROM azul/zulu-openjdk:11 AS build
LABEL org.opencontainers.image.authors="Michel Kraemer <michel@undercouch.de>"

COPY . /citeproc-java
WORKDIR /citeproc-java
RUN ./gradlew installDist

FROM azul/zulu-openjdk:11-jre
LABEL org.opencontainers.image.authors="Michel Kraemer <michel@undercouch.de>"

COPY --from=build /citeproc-java/citeproc-java-tool/build/install/citeproc-java-tool /citeproc-java-tool
ENTRYPOINT ["/citeproc-java-tool/bin/citeproc-java"]

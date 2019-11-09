FROM openjdk:8-jdk-slim AS build
MAINTAINER Michel Kraemer <michel@undercouch.de>

COPY . /citeproc-java
WORKDIR /citeproc-java
RUN ./gradlew installDist -Pincludej2v8=linux_x86_64:4.8.0

FROM openjdk:11-jre-slim
MAINTAINER Michel Kraemer <michel@undercouch.de>

COPY --from=build /citeproc-java/citeproc-java-tool/build/install/citeproc-java-tool /citeproc-java-tool
ENTRYPOINT ["/citeproc-java-tool/bin/citeproc-java"]

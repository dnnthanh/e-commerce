FROM eclipse-temurin:25-jre

ARG JMX_EXPORTER_VERSION=1.6.0

WORKDIR /opt/jmx-exporter

# The standalone 1.6.x distribution is published as an official GitHub release
# asset. It is not available from Maven Central at the old standalone path.
RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates curl \
    && curl --fail --location \
        --retry 5 --retry-delay 2 --retry-all-errors \
        --connect-timeout 15 \
        "https://github.com/prometheus/jmx_exporter/releases/download/${JMX_EXPORTER_VERSION}/jmx_prometheus_standalone-${JMX_EXPORTER_VERSION}.jar" \
        --output /opt/jmx-exporter/jmx_prometheus_standalone.jar \
    && test -s /opt/jmx-exporter/jmx_prometheus_standalone.jar \
    && apt-get purge -y --auto-remove curl \
    && rm -rf /var/lib/apt/lists/*

COPY exporter.yml /opt/jmx-exporter/exporter.yml

EXPOSE 9404

ENTRYPOINT ["java", "-jar", "/opt/jmx-exporter/jmx_prometheus_standalone.jar", "9404", "/opt/jmx-exporter/exporter.yml"]

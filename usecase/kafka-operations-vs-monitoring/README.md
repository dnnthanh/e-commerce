# Kafka operations UI vs metrics monitoring

## Problem
A topic browser is useful for developers but it is not a substitute for continuous monitoring and alerting.

## Chosen design
- Redpanda Console connects to the Apache Kafka cluster for topic/message/partition/consumer-group inspection and operational troubleshooting.
- `kafka-exporter` exposes consumer-group lag/offset metrics.
- JMX Exporter exposes broker/controller/JVM/request/replication metrics.
- Bundled Prometheus in `grafana/otel-lgtm` scrapes both exporters.
- Grafana dashboards and alerts answer operational questions such as lag growth, under-replicated partitions, broker health and throughput.

## Why both
Redpanda Console is optimized for interactive inspection. Prometheus/Grafana is optimized for time-series history, alerting and correlation with application/DB metrics. Keeping both avoids forcing one tool into the wrong job.

FROM golang:1.26-alpine AS build
RUN apk add --no-cache git ca-certificates
WORKDIR /src
RUN git clone --depth 1 --branch 2.4.0 https://github.com/oracle/oracle-db-appdev-monitoring.git .
RUN CGO_ENABLED=0 go build -trimpath -ldflags="-s -w" -o /out/oracledb_exporter .

FROM gcr.io/distroless/static-debian12:nonroot
COPY --from=build /out/oracledb_exporter /oracledb_exporter
COPY --from=build /src/default-metrics.yaml /default-metrics.yaml
ENTRYPOINT ["/oracledb_exporter"]
CMD ["--config.file=/config/config.yaml"]

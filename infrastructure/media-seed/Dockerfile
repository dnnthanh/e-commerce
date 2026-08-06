FROM minio/mc:RELEASE.2025-08-13T08-35-41Z

COPY asset-manifest.txt /tmp/asset-manifest.txt
COPY --chmod=755 generate-assets.sh /usr/local/bin/generate-assets.sh
RUN /usr/local/bin/generate-assets.sh /seed-assets/seed /tmp/asset-manifest.txt \
    && rm -f /tmp/asset-manifest.txt

COPY --chmod=755 upload-to-minio.sh /usr/local/bin/upload-to-minio.sh

ENTRYPOINT ["/usr/local/bin/upload-to-minio.sh"]

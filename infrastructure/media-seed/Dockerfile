FROM minio/mc:RELEASE.2025-08-13T08-35-41Z

COPY asset-manifest.txt /tmp/source-asset-manifest.txt
COPY --chmod=755 normalize-manifest.sh /usr/local/bin/normalize-manifest.sh
COPY --chmod=755 generate-assets.sh /usr/local/bin/generate-assets.sh
RUN /usr/local/bin/normalize-manifest.sh /tmp/source-asset-manifest.txt /tmp/asset-manifest.txt \
    && /usr/local/bin/generate-assets.sh /seed-assets/seed /tmp/asset-manifest.txt \
    && rm -f /tmp/source-asset-manifest.txt /tmp/asset-manifest.txt

COPY --chmod=755 upload-to-minio.sh /usr/local/bin/upload-to-minio.sh

ENTRYPOINT ["/usr/local/bin/upload-to-minio.sh"]

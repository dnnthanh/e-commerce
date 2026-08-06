-- Performance dataset: 500,000 media assets + 2,000,000 image-variant metadata rows.
-- Binary files are intentionally NOT generated for the performance dataset.
INSERT INTO media_asset(product_id,seller_id,original_object_key,media_type,status,content_type,created_at,updated_at)
SELECT
    1000000 + g,
    10001 + (g % 500),
    'perf/products/' || g || '/original.webp',
    'IMAGE','READY','image/webp',
    now() - ((g % 365) || ' day')::interval, now()
FROM generate_series(1, 500000) AS g;

INSERT INTO media_variant(asset_id,placement,width_px,height_px,format,object_key,byte_size,created_at)
SELECT a.id, v.placement, v.width_px, v.width_px, v.format,
       'perf/products/' || a.product_id || '/' || lower(v.placement) || '-' || v.width_px || '.' || lower(v.format),
       10000 + ((a.id * v.width_px) % 250000), now()
FROM media_asset a
CROSS JOIN (VALUES
    ('THUMBNAIL',240,'AVIF'),
    ('HOME_CARD',480,'AVIF'),
    ('SEARCH_CARD',640,'AVIF'),
    ('PRODUCT_DETAIL',1600,'WEBP')
) AS v(placement,width_px,format)
WHERE a.original_object_key LIKE 'perf/%';

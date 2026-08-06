-- Performance dataset: 1,000,000 authorization change records for audit/query labs.
INSERT INTO authorization_change_log(user_id,change_type,changed_by,changed_at,payload_json)
SELECT
    'perf-user-' || (g % 100000),
    CASE WHEN g % 2 = 0 THEN 'ROLE_ASSIGNED' ELSE 'SELLER_SCOPE_ASSIGNED' END,
    'perf-admin-' || (g % 100),
    now() - ((g % 365) || ' day')::interval,
    CASE WHEN g % 2 = 0 THEN '{"role":"SELLER_MANAGER"}' ELSE '{"sellerId":10001}' END
FROM generate_series(1, 1000000) AS g;

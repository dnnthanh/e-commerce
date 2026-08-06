-- Production-style PostgreSQL performance lab dataset.
-- Run in an isolated database. The default scale intentionally creates millions of rows.
-- psql example: psql ... -v product_rows=300000 -v event_rows=2000000 -f 00-bootstrap.sql
\if :{?product_rows}
\else
\set product_rows 300000
\endif
\if :{?event_rows}
\else
\set event_rows 2000000
\endif

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DROP SCHEMA IF EXISTS perf_lab CASCADE;
CREATE SCHEMA perf_lab;
SET search_path = perf_lab, public;

CREATE TABLE product (
  id bigint PRIMARY KEY,
  seller_id bigint NOT NULL,
  category_id bigint NOT NULL,
  status text NOT NULL,
  name text NOT NULL,
  attributes jsonb NOT NULL,
  description text,
  created_at timestamptz NOT NULL
);

CREATE TABLE invoice (
  id bigint PRIMARY KEY,
  invoice_no text NOT NULL UNIQUE,
  seller_id bigint NOT NULL,
  status text NOT NULL,
  invoice_date date NOT NULL,
  payload jsonb,
  attachment_text text
);

CREATE TABLE invoice_line (
  id bigint PRIMARY KEY,
  invoice_id bigint NOT NULL REFERENCES invoice(id),
  product_id bigint NOT NULL,
  brand_code text NOT NULL,
  quantity int NOT NULL,
  amount numeric(18,2) NOT NULL
);

CREATE TABLE payment (
  id bigint PRIMARY KEY,
  payment_key text NOT NULL UNIQUE,
  user_id bigint NOT NULL,
  provider text NOT NULL,
  status text NOT NULL,
  amount numeric(18,2) NOT NULL,
  created_at timestamptz NOT NULL
);

CREATE TABLE outbox_event (
  id bigint PRIMARY KEY,
  event_id uuid NOT NULL,
  aggregate_id text NOT NULL,
  event_type text NOT NULL,
  status text NOT NULL,
  payload jsonb NOT NULL,
  created_at timestamptz NOT NULL,
  published_at timestamptz
);

CREATE TABLE inventory_ledger (
  id bigint PRIMARY KEY,
  sku_id bigint NOT NULL,
  warehouse_id bigint NOT NULL,
  delta int NOT NULL,
  reason text NOT NULL,
  created_at timestamptz NOT NULL
);

CREATE TABLE promotion_window (
  id bigint PRIMARY KEY,
  seller_id bigint NOT NULL,
  sku_id bigint NOT NULL,
  valid_from timestamptz NOT NULL,
  valid_to timestamptz NOT NULL,
  discount_percent numeric(5,2) NOT NULL
);

-- Correlated/skewed product data: seller 1 dominates electronics, PUBLISHED dominates overall.
INSERT INTO product
SELECT g,
       CASE WHEN g % 10 < 6 THEN 1 ELSE 2 + (g % 999) END,
       CASE WHEN g % 10 < 6 THEN 10 ELSE 10 + (g % 90) END,
       CASE WHEN g % 100 < 94 THEN 'PUBLISHED' WHEN g % 100 < 98 THEN 'DRAFT' ELSE 'ARCHIVED' END,
       'Product ' || g,
       jsonb_build_object(
          'tier', CASE WHEN g % 20 = 0 THEN 'premium' ELSE 'standard' END,
          'color', (ARRAY['black','white','blue','red'])[1 + (g % 4)],
          'ram', (ARRAY['8GB','16GB','32GB'])[1 + (g % 3)]),
       repeat(md5(g::text), CASE WHEN g % 500 = 0 THEN 1200 ELSE 2 END),
       now() - ((g % 730) || ' days')::interval
FROM generate_series(1, :product_rows) g;

-- Invoice header/lines deliberately produce many-to-one join fan-out and wide-row TOAST reads.
INSERT INTO invoice
SELECT g,
       'INV-' || lpad(g::text, 10, '0'),
       1 + (g % 500),
       CASE WHEN g % 100 < 88 THEN 'SIGNED' ELSE 'CANCELLED' END,
       current_date - (g % 730),
       jsonb_build_object('source','lab','sequence',g),
       CASE WHEN g % 200 = 0 THEN repeat(md5(g::text), 8000) ELSE repeat(md5(g::text), 2) END
FROM generate_series(1, greatest(50000, :product_rows / 3)) g;

INSERT INTO invoice_line
SELECT g,
       1 + (g % greatest(50000, :product_rows / 3)),
       1 + (g % :product_rows),
       'BR' || lpad((g % 300)::text,3,'0'),
       1 + (g % 5),
       (100000 + (g % 30000000))::numeric(18,2)
FROM generate_series(1, greatest(500000, :product_rows * 4)) g;

-- Payment status skew: PAID is common, UNKNOWN is rare and operationally important.
INSERT INTO payment
SELECT g,
       'PAY-' || g,
       1 + (g % 100000),
       (ARRAY['MOMO','VNPAY','VIETQR'])[1 + (g % 3)],
       CASE WHEN g % 10000 < 9500 THEN 'PAID'
            WHEN g % 10000 < 9850 THEN 'FAILED'
            WHEN g % 10000 < 9980 THEN 'PENDING'
            ELSE 'UNKNOWN' END,
       (50000 + g % 50000000)::numeric(18,2),
       now() - ((g % 7776000) || ' seconds')::interval
FROM generate_series(1, :event_rows) g;

-- Outbox history is huge, active queue is tiny: ideal partial-index case.
INSERT INTO outbox_event
SELECT g,
       gen_random_uuid(),
       'ORDER-' || (1 + g % 300000),
       (ARRAY['ORDER_CREATED','PAYMENT_CAPTURED','STOCK_RESERVED','SHIPMENT_UPDATED'])[1 + (g % 4)],
       CASE WHEN g > :event_rows - 5000 THEN 'PENDING' ELSE 'PROCESSED' END,
       jsonb_build_object('sequence',g,'sellerId',1 + g % 1000),
       now() - ((:event_rows - g) || ' seconds')::interval,
       CASE WHEN g > :event_rows - 5000 THEN NULL ELSE now() - ((:event_rows - g - 1) || ' seconds')::interval END
FROM generate_series(1, :event_rows) g;

-- Append-ordered ledger gives high physical correlation for created_at/BRIN experiments.
INSERT INTO inventory_ledger
SELECT g,
       1 + (g % 250000),
       1 + (g % 50),
       CASE WHEN g % 4 = 0 THEN -1 ELSE 1 END,
       (ARRAY['SALE','RECEIPT','RETURN','ADJUSTMENT'])[1 + (g % 4)],
       now() - ((:event_rows - g) || ' seconds')::interval
FROM generate_series(1, :event_rows) g;

INSERT INTO promotion_window
SELECT g,
       1 + (g % 1000),
       1 + (g % 250000),
       now() - ((g % 90) || ' days')::interval,
       now() - ((g % 90) || ' days')::interval + ((1 + g % 14) || ' days')::interval,
       5 + (g % 40)
FROM generate_series(1, 500000) g;

ANALYZE;

CREATE OR REPLACE TEMP VIEW raw_orders AS
SELECT * FROM VALUES
    (101, 'east', 10000, 'PAID'),
    (102, 'east', 5000, 'PAID'),
    (101, 'east', 10000, 'PAID'),
    (103, 'south', 8000, 'PAID'),
    (104, 'south', 7000, 'CANCELLED')
AS t(order_id, region, amount_cents, status);

CREATE OR REPLACE TEMP VIEW clean_orders AS
SELECT DISTINCT order_id, region, amount_cents
FROM raw_orders
WHERE status = 'PAID';

SELECT region, COUNT(*) AS order_count, SUM(amount_cents) AS amount_cents
FROM clean_orders
GROUP BY region
ORDER BY region;

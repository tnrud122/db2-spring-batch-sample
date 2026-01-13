-- =========================================================
-- Business sample tables (DB2)
-- - SRC_TXN: input table
-- - TGT_TXN: output table
-- - AGG_DAILY: aggregated daily table (tasklet job writes here)
-- =========================================================

CREATE TABLE SRC_TXN (
    ID           BIGINT       NOT NULL PRIMARY KEY,
    CUSTOMER_NM  VARCHAR(100) NOT NULL,
    AMOUNT       DECIMAL(18,2) NOT NULL,
    CREATED_AT   TIMESTAMP    NOT NULL
);

CREATE TABLE TGT_TXN (
    ID           BIGINT       NOT NULL PRIMARY KEY,
    CUSTOMER_NM  VARCHAR(100) NOT NULL,
    AMOUNT       DECIMAL(18,2) NOT NULL,
    CREATED_AT   TIMESTAMP    NOT NULL,
    LOADED_AT    TIMESTAMP    NOT NULL
);

CREATE TABLE AGG_DAILY (
    BIZ_DATE     DATE         NOT NULL PRIMARY KEY,
    TXN_CNT      BIGINT       NOT NULL,
    AMT_SUM      DECIMAL(18,2) NOT NULL,
    AGG_AT       TIMESTAMP    NOT NULL
);

-- Sample seed: generate 10,000 rows (DB2 syntax using recursive CTE)
WITH RECURSIVE seq(n) AS (
  SELECT 1 FROM SYSIBM.SYSDUMMY1
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 10000
)
INSERT INTO SRC_TXN (ID, CUSTOMER_NM, AMOUNT, CREATED_AT)
SELECT
  BIGINT(n) AS ID,
  'CUST-' || VARCHAR_FORMAT(n, '000000') AS CUSTOMER_NM,
  DECIMAL(MOD(n, 1000) + 0.25, 18, 2) AS AMOUNT,
  CURRENT TIMESTAMP - (n) SECONDS AS CREATED_AT
FROM seq;

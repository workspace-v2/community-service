CREATE TABLE stock_snapshot (
    symbol             VARCHAR(6) PRIMARY KEY,
    name               VARCHAR(100) NOT NULL,
    market             VARCHAR(20) NOT NULL,
    status             VARCHAR(20) NOT NULL,
    listed_at          DATE,
    delisted_at        DATE,
    source_updated_at  DATETIME(6) NOT NULL,
    synced_at          DATETIME(6) NOT NULL
);

CREATE INDEX idx_stock_snapshot_name ON stock_snapshot (name);
CREATE INDEX idx_stock_snapshot_status ON stock_snapshot (status);
CREATE INDEX idx_stock_snapshot_source_updated_at ON stock_snapshot (source_updated_at);

ALTER TABLE post
ADD COLUMN symbol VARCHAR(6);

CREATE INDEX idx_post_symbol ON post (symbol);
CREATE INDEX idx_post_symbol_post_id ON post (symbol, post_id DESC);

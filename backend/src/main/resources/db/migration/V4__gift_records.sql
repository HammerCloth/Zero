CREATE TABLE IF NOT EXISTS gift_recipients (
  id TEXT PRIMARY KEY,
  user_id TEXT NOT NULL,
  name TEXT NOT NULL,
  relationship TEXT,
  note TEXT,
  is_active INTEGER NOT NULL DEFAULT 1,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE (user_id, name)
);

CREATE TABLE IF NOT EXISTS gift_records (
  id TEXT PRIMARY KEY,
  user_id TEXT NOT NULL,
  gift_recipient_id TEXT NOT NULL,
  occasion TEXT NOT NULL,
  gift_date TEXT NOT NULL,
  amount REAL NOT NULL CHECK (amount > 0),
  payment_method TEXT,
  note TEXT,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (gift_recipient_id) REFERENCES gift_recipients(id)
);

CREATE INDEX IF NOT EXISTS idx_gift_recipients_user_active
  ON gift_recipients(user_id, is_active, name);
CREATE INDEX IF NOT EXISTS idx_gift_records_user_date
  ON gift_records(user_id, gift_date DESC);
CREATE INDEX IF NOT EXISTS idx_gift_records_recipient
  ON gift_records(gift_recipient_id, gift_date DESC);

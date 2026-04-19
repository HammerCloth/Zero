CREATE TABLE IF NOT EXISTS user_option_items (
  id TEXT PRIMARY KEY,
  user_id TEXT NOT NULL,
  dimension TEXT NOT NULL,
  opt_key TEXT NOT NULL,
  label TEXT NOT NULL,
  sort_order INTEGER NOT NULL DEFAULT 0,
  enabled INTEGER NOT NULL DEFAULT 1,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE (user_id, dimension, opt_key)
);

CREATE INDEX IF NOT EXISTS idx_user_option_user_dim ON user_option_items(user_id, dimension);

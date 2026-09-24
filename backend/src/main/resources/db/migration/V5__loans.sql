CREATE TABLE IF NOT EXISTS loans (
  id TEXT PRIMARY KEY,
  user_id TEXT NOT NULL,
  lender_name TEXT NOT NULL,
  relationship TEXT,
  amount REAL NOT NULL CHECK (amount > 0),
  loan_date TEXT NOT NULL,
  due_date TEXT,
  note TEXT,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS loan_repayments (
  id TEXT PRIMARY KEY,
  user_id TEXT NOT NULL,
  loan_id TEXT NOT NULL,
  amount REAL NOT NULL CHECK (amount > 0),
  repay_date TEXT NOT NULL,
  note TEXT,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_loans_user_date ON loans(user_id, loan_date DESC);
CREATE INDEX IF NOT EXISTS idx_loan_repayments_loan_date ON loan_repayments(loan_id, repay_date DESC);
CREATE INDEX IF NOT EXISTS idx_loan_repayments_user ON loan_repayments(user_id, repay_date DESC);

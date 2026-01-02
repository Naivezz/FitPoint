CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_email_lower ON users(LOWER(email));

CREATE INDEX IF NOT EXISTS idx_reservations_user_id ON reservations(user_id);
CREATE INDEX IF NOT EXISTS idx_reservations_class_id_status ON reservations(training_class_id, status);
CREATE INDEX IF NOT EXISTS idx_reservations_user_class_status ON reservations(user_id, training_class_id, status);

CREATE INDEX IF NOT EXISTS idx_training_classes_start_time ON training_classes(start_time);
CREATE INDEX IF NOT EXISTS idx_training_classes_trainer_id ON training_classes(trainer_id);

CREATE INDEX IF NOT EXISTS idx_memberships_user_id_active ON memberships(user_id, active, end_date);
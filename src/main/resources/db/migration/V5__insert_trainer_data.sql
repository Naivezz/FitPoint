-- Trainer notes (test data)
INSERT INTO trainer_notes (trainer_id, client_id, note, created_at)
VALUES (4, 1, 'Test1 note about client progress.', '2026-01-05 09:00:00'),
       (4, 2, 'Test2 note about workout routine.', '2026-01-06 10:00:00'),
       (5, 3, 'Test3 note about food.', '2026-01-07 11:00:00');

-- Personal training sessions (test data)
INSERT INTO personal_training_sessions (trainer_id, client_id, start_time, end_time, session_goal, session_notes,
                                        status, created_at)
VALUES (4, 1, '2026-01-10 15:00:00', '2026-01-10 16:00:00', 'Test goal 1', 'Test note 1', 'SCHEDULED',
        '2026-01-08 12:00:00'),
       (4, 2, '2026-01-11 16:00:00', '2026-01-11 17:00:00', 'Test goal 2', 'Test note 2', 'SCHEDULED',
        '2026-01-09 12:15:00'),
       (5, 3, '2026-01-09 14:00:00', '2026-01-09 15:00:00', 'Test goal 3', 'Test note 3', 'COMPLETED',
        '2026-01-08 10:00:00');

-- Schedule change requests (test data)
INSERT INTO schedule_change_requests (trainer_id, request_type, reason, class_name, class_description,
                                      requested_start_time, requested_end_time, requested_capacity, requested_room_id,
                                      status, created_at)
VALUES (4, 'ADD', 'Test reason 1', 'TestClass1', 'Some description 1', '2026-01-15 19:00:00', '2026-01-15 20:00:00', 10,
        1, 'PENDING', '2026-01-10 08:00:00'),
       (5, 'MODIFY', 'Test reason 2', 'TestClass2', 'Some description 2', '2026-01-16 09:00:00', '2026-01-16 10:30:00',
        15, 2, 'PENDING', '2026-01-10 09:30:00');

-- Notifications (test data)
INSERT INTO notifications (recipient_id, message, sent_at, read)
VALUES (4, 'Test notification 1 for trainer.', '2026-01-10 10:00:00', false),
       (4, 'Test notification 2 for trainer.', '2026-01-11 11:00:00', false),
       (5, 'Test notification 3 for trainer.', '2026-01-12 15:00:00', true),
       (1, 'Test notification 1 for client.', '2026-01-10 12:00:00', false),
       (2, 'Test notification 2 for client.', '2026-01-11 12:15:00', false),
       (3, 'Test notification 3 for client.', '2026-01-12 15:00:00', true);

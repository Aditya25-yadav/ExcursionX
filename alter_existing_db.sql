-- =============================================================
--  ExcursionX — ALTER SCRIPT for EXISTING databases
--  Run this IF you already had a database from a previous session
--  and did NOT run the full excursionx_schema.sql yet.
-- =============================================================

USE excursionx;

-- Add missing columns to Trips (safe — only adds if not present)
ALTER TABLE Trips
    ADD COLUMN IF NOT EXISTS start_date  DATE        DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS end_date    DATE        DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS teacher_id  VARCHAR(20) DEFAULT NULL;

-- Add FK for teacher_id (ignore error if Teachers table doesn't exist yet)
-- If this fails, it's safe to skip — the app works without the FK.
ALTER TABLE Trips
    ADD CONSTRAINT fk_trip_teacher
    FOREIGN KEY (teacher_id) REFERENCES Teachers(teacher_id)
    ON DELETE SET NULL;

-- Create EmergencyLogs table if missing
CREATE TABLE IF NOT EXISTS EmergencyLogs (
    emergency_id   INT          AUTO_INCREMENT PRIMARY KEY,
    trip_id        INT          NOT NULL,
    reported_by    VARCHAR(20),
    description    TEXT         NOT NULL,
    contact_number VARCHAR(15),
    logged_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_emergency_trip FOREIGN KEY (trip_id) REFERENCES Trips(trip_id) ON DELETE CASCADE
);

-- Make sure Bookings has correct enum columns
-- (These ALTER statements are safe even if already correct)
ALTER TABLE Bookings
    MODIFY COLUMN payment_status    ENUM('Pending','Paid','Refunded')                   DEFAULT 'Pending',
    MODIFY COLUMN attendance_status ENUM('Present','Absent','Excused','Not Marked')     DEFAULT 'Not Marked';

SELECT 'Migration complete!' AS Status;

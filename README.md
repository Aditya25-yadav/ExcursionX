# ExcursionX — Smart Student Excursion & Trip Management System

> **Full-stack Java Desktop Application** | Java Swing · JDBC · MySQL

---

## 📁 Project Structure

```
minproject01/
├── excursionx_schema.sql          ← Run this in MySQL first!
├── run.bat                        ← Double-click to launch the app
├── sources.txt                    ← Compile file list
├── lib/
│   └── mysql-connector-j-9.6.0.jar
└── src/
    ├── Main.java                  ← Entry point
    ├── databases/
    │   ├── DBconnection.java      ← Singleton DB connection
    │   ├── UserDao.java           ← Login & Registration
    │   ├── TripDao.java           ← Trip CRUD + manifest + expenses
    │   ├── BookingDao.java        ← Thread-safe booking + payments
    │   └── TeacherDao.java        ← Attendance + complaints + emergency
    ├── models/
    │   ├── User.java              ← Abstract base class (OOP)
    │   ├── Admin.java
    │   ├── Teacher.java
    │   ├── Student.java
    │   ├── Trip.java              ← With dates + teacher assignment
    │   ├── Booking.java
    │   ├── Payment.java
    │   └── SecurityUtils.java     ← SHA-256 password hashing
    └── ui/
        ├── UITheme.java           ← Dark design system (colours, fonts, widgets)
        ├── LoginFrame.java        ← Login + Register (tabbed)
        ├── AdminDashboard.java    ← 6 tabs
        ├── TeacherDashboard.java  ← 5 tabs
        └── StudentDashboard.java  ← 3 tabs
```

---

## 🚀 Setup & Run

### Step 1 — Import Database

Open **MySQL Workbench** (or any MySQL client) and run:

```sql
SOURCE /path/to/excursionx_schema.sql;
```

Or paste the contents of `excursionx_schema.sql` and execute.

### Step 2 — Update Password (if needed)

Open `src/databases/DBconnection.java` and verify:

```java
private static final String PASS = "Messi2025#";  // ← your MySQL root password
```

### Step 3 — Compile

```powershell
# From project root (minproject01/)
javac -cp "src;lib/mysql-connector-j-9.6.0.jar" -d src `
    src/Main.java src/databases/*.java src/models/*.java src/ui/*.java
```

### Step 4 — Run

```powershell
java -cp "src;lib/mysql-connector-j-9.6.0.jar" Main
```

**Or** simply double-click `run.bat`.

---

## 🔑 Default Test Credentials

| Role    | Email                   | Password     |
|---------|-------------------------|--------------|
| Admin   | admin@excursionx.com    | Admin@123    |
| Teacher | priya@excursionx.com    | Teacher@123  |
| Teacher | rahul@excursionx.com    | Teacher@123  |
| Student | aarav@excursionx.com    | Student@123  |
| Student | meera@excursionx.com    | Student@123  |
| Student | rohan@excursionx.com    | Student@123  |

---

## 🖥️ Feature Walkthrough

### 👑 Admin Dashboard (6 Tabs)

| Tab | Features |
|-----|----------|
| 🗺 **Manage Trips** | Create / Update / Delete trips with destination, dates, capacity, budget. Click a row to pre-fill form. |
| 📋 **Manifest** | Enter Trip ID → see all booked students with payment & attendance status |
| 💳 **Payments** | View all payment transactions; manually override payment status |
| 📊 **Analytics** | Live stat cards: Total Trips, Students, Teachers, Bookings, Revenue, Pending Payments |
| 👥 **Users** | View all registered teachers and students |
| 🚨 **Emergencies** | View all emergency logs reported by teachers across all trips |

### 👨‍🏫 Teacher Dashboard (5 Tabs)

| Tab | Features |
|-----|----------|
| ✈ **My Trips** | View trips assigned to this teacher (filtered by teacher_id) |
| 📅 **Schedule** | Write and save trip schedules to `.txt` files |
| ✅ **Attendance** | Mark student attendance (Present / Absent / Excused) by Trip ID + Student ID |
| 📝 **Complaints** | File conduct complaints against students |
| 🚨 **Emergency Log** | Report emergency incidents with description + contact; view history per trip |

### 🎓 Student Dashboard (3 Tabs)

| Tab | Features |
|-----|----------|
| ✈ **Browse & Book** | See all trips with dates and capacity; confirm booking → optional immediate payment |
| 📋 **My Bookings** | View booking history with payment/attendance status; "Pay Now" for pending |
| 📁 **My Record** | View conduct complaints filed by teachers |

---

## ⚙️ Key Technical Concepts

### Multithreading (Booking)
```java
Thread bookingThread = new Thread(() -> {
    boolean success = bookingDao.bookTrip(student.getId(), tripId);
    SwingUtilities.invokeLater(() -> { /* update UI */ });
});
bookingThread.start();
```
- `bookTrip()` is declared `synchronized` — prevents overbooking under concurrent access
- DB transaction wraps capacity decrement + booking insert — atomic operation

### Password Security
- Passwords hashed with **SHA-256** via `SecurityUtils.hashPassword()`
- Never stored in plaintext; DB uses `SHA2()` for sample data too

### OOP Hierarchy
```
User (abstract)
├── Admin
├── Teacher
└── Student
```

### Exception Handling
- All DAO methods wrap JDBC calls in try-catch
- SQL transactions use `conn.rollback()` on failure
- UI shows friendly `JOptionPane` error dialogs

---

## 🗄️ Database Tables

| Table | Purpose |
|-------|---------|
| `Admins` | Admin user accounts |
| `Teachers` | Teacher accounts with contact |
| `Students` | Student accounts with class + emergency contact |
| `Trips` | Trip records with dates, capacity, budget, teacher |
| `Bookings` | Student–Trip booking with payment & attendance status |
| `Payments` | Payment transactions (UPI/Card/Cash/Net Banking) |
| `Expenses` | Budget expense tracking per trip |
| `Activities` | Itinerary activities per trip |
| `Complaints` | Conduct complaints filed by teachers |
| `EmergencyLogs` | Emergency incidents reported during trips |

---

## 🔧 Requirements

- **Java** 8 or higher
- **MySQL** 8.x running locally on port 3306
- `lib/mysql-connector-j-9.6.0.jar` (already in project)

# CyberGuard – Security Operations Center (SOC) Monitoring System

![CyberGuard Architecture](https://img.shields.io/badge/Security-SOC%20SIEM-00d2ff?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21%20%7C%2026-blue?style=for-the-badge&logo=java)
![JavaFX](https://img.shields.io/badge/GUI-JavaFX%2021-0284c7?style=for-the-badge)
![Database](https://img.shields.io/badge/Database-MySQL%208.0-orange?style=for-the-badge&logo=mysql)

A professional, enterprise-grade desktop **Security Operations Center (SOC) & SIEM Monitoring System** built with **JavaFX**, **JavaFX CSS**, **JavaFX Charts**, **MySQL**, and **JDBC**.

Designed for B.Tech Computer Science & Cybersecurity mini projects with clean, modular, and beginner-friendly architecture.

---

## 🛡️ Default Administrator Credentials

When launching the application for the first time, use the following seeded administrator credentials:

| Field | Value |
| :--- | :--- |
| **Username** | `admin` |
| **Password** | `password123` |
| **Security Role** | Chief Security Officer / SOC Lead |

*(Credentials are secured using cryptographic salt + SHA-256 password hashing. Plain text passwords are never stored).*

---

## 🚀 Quick Start: How to Run in VS Code

### Option 1: Double-Click Launcher (Easiest)
Simply double-click **`run-cyberguard.bat`** located in the workspace folder or inside `cyberguard-soc/run.bat`. The application will start immediately.

### Option 2: Run via VS Code Build Task
1. Open this workspace in **Visual Studio Code**.
2. Press `Ctrl + Shift + B` (or go to **Terminal** -> **Run Build Task...**).
3. Select **`Run CyberGuard SOC`**.

### Option 3: Command Line (PowerShell / Command Prompt)
Navigate into the `cyberguard-soc` directory and execute:
```bash
.\run.bat
```
Or via Maven Wrapper:
```bash
.\mvnw.cmd compile
.\run.bat
```

---

## 📁 Project Architecture & Package Structure

The project follows a strict separation of concerns into distinct packages:

```
cyberguard-soc/
│
├── .mvn/                              # Embedded Apache Maven Wrapper (no global Maven needed)
├── .vscode/                           # Pre-configured VS Code debug & build tasks
│   ├── launch.json
│   ├── settings.json
│   └── tasks.json
│
├── pom.xml                            # Maven project object model & dependencies
├── run.bat                            # Instant native desktop launcher script
│
├── src/
│   ├── main/
│   │   ├── java/com/cyberguard/
│   │   │   ├── App.java               # JavaFX Application Main entry point
│   │   │   │
│   │   │   ├── model/                 # Data Models & Entities
│   │   │   │   ├── Admin.java         # Authenticated SOC Administrator entity
│   │   │   │   ├── SecurityEvent.java # Monitored authentication event
│   │   │   │   ├── Alert.java         # Defensive security alert entity
│   │   │   │   └── DashboardStats.java# Real-time aggregated metric counters
│   │   │   │
│   │   │   ├── view/                  # Modern UI Views & Layouts
│   │   │   │   ├── LoginView.java     # CyberShield dark login screen with show/hide password
│   │   │   │   ├── DashboardView.java # SIEM monitoring console (Sidebar, Topbar, Cards, Tables)
│   │   │   │   └── ViewNavigator.java # Clean scene router & transition coordinator
│   │   │   │
│   │   │   ├── controller/            # UI Controllers & Action Listeners
│   │   │   │   ├── LoginController.java     # Credential validation & auth transitions
│   │   │   │   └── DashboardController.java # Live refreshes, filters, alert resolutions
│   │   │   │
│   │   │   ├── service/               # Business & Cybersecurity Logic
│   │   │   │   ├── AuthService.java            # Auth validation, salt verification & event logging
│   │   │   │   ├── ThreatDetectionService.java # Defensive rule correlation engine
│   │   │   │   └── EventSimulationService.java # Safe local attack & benign stream generator
│   │   │   │
│   │   │   ├── database/              # Persistence & JDBC Connectivity
│   │   │   │   ├── DBConnection.java    # MySQL connection manager & auto-standby fallback
│   │   │   │   └── DatabaseManager.java # PreparedStatement queries & in-memory cache
│   │   │   │
│   │   │   └── util/                  # Helper Utilities
│   │   │       ├── PasswordHasher.java  # Cryptographic salt + SHA-256 digest engine
│   │   │       └── IconUtil.java        # Resolution-independent SVG vector icons
│   │   │
│   │   └── resources/
│   │       ├── css/
│   │       │   └── cyberguard.css     # Modern dark navy SIEM theme styling
│   │       └── database/
│   │           ├── db.properties      # MySQL port, user, password configuration
│   │           └── schema.sql         # SQL schema script (database, tables, indexes)
│   │
│   └── test/java/com/cyberguard/
│       ├── CoreLogicTest.java         # Automated verification for hashing, auth, and threat rules
│       └── HeadlessAppTest.java       # Automated JavaFX UI component verification
```

---

## ⚙️ MySQL Database Setup (Optional & Automatic)

CyberGuard includes **smart resilience**:
- If MySQL Server is active on `localhost:3306`, it connects automatically and creates the database `cyberguard_soc` and tables.
- If MySQL is not running yet, it seamlessly switches to **Standby Mode**, keeping all features, simulation, search, filtering, and alert resolution 100% active in memory without crashing!

### Configuring Custom MySQL Credentials:
Edit `src/main/resources/database/db.properties`:
```properties
db.host=localhost
db.port=3306
db.name=cyberguard_soc
db.user=root
db.password=YOUR_PASSWORD
```

### Manual Database Creation (Workbench / MySQL CLI):
You can also run the SQL script located in `src/main/resources/database/schema.sql`:
```sql
CREATE DATABASE IF NOT EXISTS cyberguard_soc;
USE cyberguard_soc;
```

---

## 🧠 Defensive Threat Detection Engine Rules

CyberGuard implements rule-based defensive detection against common cyber threats:

1. **Rule 1: Multiple Failed Logins (High Severity - Red)**
   - **Trigger**: More than 4 failed login attempts from the same IP address within a 10-minute sliding window.
   - **Action**: Raises a HIGH severity alert, flags the source IP, and logs incident details.

2. **Rule 2: Targeted Account Brute Force (Medium Severity - Yellow)**
   - **Trigger**: 3 or more consecutive failed attempts targeting a specific username.
   - **Action**: Raises a MEDIUM severity alert for administrator account lockout review.

3. **Rule 3: Simulation Controls (Safe Local Testing)**
   - Use the **"⚡ Execute Brute Force Attack Sequence"** button on the dashboard or Threat Detection tab to safely simulate an automated dictionary attack and observe real-time automated alerting!

---

## 📊 Dashboard Visualizations & Telemetry

- **Total Login Attempts**: Aggregated count of all monitored authentications.
- **Successful Logins**: Routine approved connections (Green pill).
- **Failed Logins**: Authentication rejections (Red pill).
- **Suspicious Activities**: Anomalous incidents flagged by SIEM rules (Amber/Yellow pill).
- **JavaFX Charts**:
  - *PieChart*: Successful vs Failed Logins distribution.
  - *BarChart*: Security Alerts grouped by severity (HIGH, MEDIUM, LOW).
  - *LineChart*: Authentication velocity over time.

# CodeStreak ⚡

**CodeStreak** is a full-stack coding progress management platform that tracks your Data Structures and Algorithms (DSA) preparation across platforms like LeetCode, CodeChef, Codeforces, and HackerRank.

Built entirely with **Core Java, Jakarta Servlets, JDBC, PostgreSQL, Apache Tomcat 10.1, HTML5, CSS3, and Vanilla JavaScript** following a strict three-tier architecture without heavyweight frameworks.

---

## 🚀 Key Features

1. **User Authentication & Session Security**:
   - Secure BCrypt password hashing.
   - Session-based authentication with `AuthFilter` access control.
   - User profile customization & daily solving targets.

2. **Dashboard**:
   - Total solved and attempted metric cards.
   - Easy / Medium / Hard difficulty distribution.
   - Platform-wise solved breakdowns.
   - Active coding streak calculation and weekly submission activity.
   - Topic-wise completion progress bars.

3. **Problem Management (CRUD)**:
   - Add, edit, filter, search, and delete DSA problems.
   - Track statuses: `Not Started`, `Attempted`, `Solved`, `Revision Needed`.
   - Map problems to one or multiple DSA topics and platforms.
   - Problem notes, URLs, and difficulty overrides.

4. **Platform Management**:
   - Master support for LeetCode, CodeChef, Codeforces, and HackerRank.
   - Save user handles, profiles, problem counts, contest ratings, and global badges.
   - Reliable manual progress tracking as default.

5. **Curated DSA Mastery Roadmap**:
   - 12 sequential milestones: Arrays, Strings, Searching & Sorting, Hashing, Recursion & Backtracking, Linked Lists, Stacks & Queues, Trees & BST, Heaps, Graphs, Greedy, and Dynamic Programming.
   - Target practice goals per topic with status toggles (`Not Started`, `In Progress`, `Completed`).

6. **Intelligent In-Memory Recommendation Engine**:
   - Computes dynamic DSA topic weakness using formula: `(attempted * 3.0 + revision * 2.5 + unsolved * 0.8) / total`.
   - Utilizes Java `PriorityQueue`, `HashMap`, and `HashSet` to rank unsolved problems.
   - Rationale generation explaining why each problem was recommended.

7. **Spaced Repetition & Revision Planner**:
   - Schedule future revisions with priorities (Normal, High, Urgent).
   - Tracks `Pending`, `Upcoming`, `Overdue`, and `Completed` reviews.
   - Automated spaced repetition suggestions based on stale problems and flagged items.

8. **Analytics & Charts**:
   - Conic-gradient CSS difficulty pie chart.
   - Bar charts for 7-day submission volume.
   - In-depth topic mastery breakdown.

---

## 🛠️ Mandatory Tech Stack

- **Backend**: Core Java (Java 17+), Jakarta Servlet API 6.0, JDBC, HikariCP, Jackson (JSON), jBCrypt.
- **Database**: PostgreSQL 14+ (`codestreak` database).
- **Frontend**: Semantic HTML5, CSS3 (Dark Navy theme `#0B1120`, deep blue `#131C31`, teal accents `#14B8A6`), Vanilla JavaScript (ES6+ fetch API).
- **Server**: Apache Tomcat 10.1+.
- **Build**: Apache Maven (`war` packaging).

---

## 📁 Project Architecture

```
CodeStreak/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/codestreak/
│   │   │   ├── controller/      # Jakarta Servlets (Auth, Problem, Platform, Roadmap, etc.)
│   │   │   ├── service/         # Business logic & recommendation engine (PriorityQueue)
│   │   │   ├── dao/             # Data Access Objects using PreparedStatement & JDBC
│   │   │   ├── model/           # POJO domain entities
│   │   │   ├── util/            # HikariCP config & Jackson JSON helpers
│   │   │   └── filter/          # AuthFilter protecting non-public routes
│   │   ├── resources/
│   │   │   ├── db.properties    # PostgreSQL connection settings
│   │   │   └── schema.sql       # Complete schema and seed data
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml      # Servlet 6.0 configuration
│   │       ├── css/
│   │       │   └── style.css    # Clean developer dashboard design system
│   │       ├── js/
│   │       │   └── app.js       # Global auth, sidebar rendering & toast alerts
│   │       ├── index.html       # Landing page (redirects to dashboard)
│   │       ├── login.html       # Sign-in page
│   │       ├── register.html    # Registration page
│   │       ├── dashboard.html   # Core overview dashboard
│   │       ├── problems.html    # Problem management & search
│   │       ├── platforms.html   # Platform profile connections
│   │       ├── roadmap.html     # Structured DSA learning roadmap
│   │       ├── recommendations.html # PriorityQueue recommendation UI
│   │       ├── revisions.html   # Spaced repetition scheduler
│   │       ├── analytics.html   # Performance charts and activity stats
│   │       └── profile.html     # User profile and password settings
│   └── test/
│       └── java/com/codestreak/
│           └── CodeStreakCoreTest.java # JUnit 5 test suite
```

---

## 💻 Mac Terminal Commands to Setup & Run

### 1. Configure PostgreSQL & Load Schema

```bash
# Ensure PostgreSQL service is running
brew services start postgresql@18

# Create the codestreak database (if not already created)
psql -d postgres -c "CREATE DATABASE codestreak;"

# Initialize the schema and demo dataset
psql -d codestreak -f src/main/resources/schema.sql
```

### 2. Configure Database Connection

Check `src/main/resources/db.properties` or set environment variables:
```properties
db.host=localhost
db.port=5432
db.name=codestreak
db.user=snehachauhan
db.password=
```

### 3. Build & Run Tests with Maven

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"

mvn clean test
mvn clean package
```

This compiles all classes, runs the 6 JUnit tests, and generates `target/codestreak.war`.

### 4. Deploy and Run on Tomcat 10.1

```bash
# Copy WAR to Tomcat webapps
cp target/codestreak.war /opt/homebrew/Cellar/tomcat@10/10.1.61/libexec/webapps/

# Start Tomcat 10
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
export JRE_HOME="/opt/homebrew/opt/openjdk@17"
/opt/homebrew/opt/tomcat@10/bin/catalina start
```

### 5. Access the Application

Open your browser and navigate to:
👉 **[http://localhost:8080/codestreak/](http://localhost:8080/codestreak/)**

#### Default Demo Credentials:
- **Username**: `demouser`
- **Password**: `password123`

---

## 🔗 Major REST API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/login` | Authenticate user & start session |
| `POST` | `/api/auth/register` | Register new user with BCrypt |
| `POST` | `/api/auth/logout` | Invalidate current session |
| `GET` | `/api/auth/me` | Fetch currently logged-in user |
| `GET` | `/api/problems` | List problems with search & filters |
| `POST` | `/api/problems` | Create new custom problem |
| `PUT` | `/api/problems/{id}` | Update existing problem |
| `DELETE` | `/api/problems/{id}` | Delete custom problem |
| `POST` | `/api/problems/{id}/status` | Update problem solving status |
| `GET` | `/api/platforms` | Get master platforms & user handles |
| `POST` | `/api/platforms` | Connect/update user platform profile |
| `DELETE` | `/api/platforms/{id}` | Disconnect user platform profile |
| `GET` | `/api/roadmap` | Get DSA roadmap with user progress |
| `POST` | `/api/roadmap/{id}/status` | Update topic roadmap status |
| `GET` | `/api/recommendations` | Get ranked DSA problem recommendations |
| `GET` | `/api/revisions` | Get revision tasks (filters: ALL, PENDING, OVERDUE, etc.) |
| `POST` | `/api/revisions` | Schedule a spaced repetition review |
| `POST` | `/api/revisions/{id}/status` | Mark revision completed or pending |
| `GET` | `/api/analytics/overview` | Solved metrics, streak, and recent history |
| `GET` | `/api/analytics/topics` | In-depth topic completion percentages |
| `GET` | `/api/user` | User settings |
| `PUT` | `/api/user` | Update full name and daily target |
| `PUT` | `/api/user/password` | Securely change user password |
# codestreak

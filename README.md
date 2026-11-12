# CodeStreak ⚡

**CodeStreak** is a full-stack coding progress and revision management platform designed for DSA preparation across platforms (LeetCode, CodeChef, Codeforces, HackerRank).

Built using **Core Java (OOP), Jakarta Servlets, JDBC, PostgreSQL, HTML5, CSS3, and Vanilla JavaScript**.

---

## 🎯 5 Core Sidebar Sections

1. **Dashboard** (`dashboard.html`): High-level overview of total problems solved, difficulty distribution (Easy / Medium / Hard), pending revisions, platform progress, recent coding activity, and topic mastery.
2. **Problems** (`problems.html`): Search and filter by difficulty, topic, and platform. Add, edit, or delete custom problems; track status (`Not Started`, `Attempted`, `Solved`, `Revision Needed`); and store notes.
3. **Platforms** (`platforms.html`): Separate dedicated page to manage and connect coding platform accounts (LeetCode, CodeChef, Codeforces, HackerRank), usernames, ratings, and ranks.
4. **Recommendations** (`recommendations.html`): In-memory algorithm using weakness scoring to recommend the best next problems to solve.
5. **Profile & Settings** (`profile.html`): Manage personal profile, username, email, full name, daily solving goal, and password changes.

---

## 💻 Running the Application

### 1. Build and Test with Maven
```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"

mvn clean test
mvn clean package
```

### 2. Deploy to Tomcat 10
```bash
cp target/codestreak.war /opt/homebrew/Cellar/tomcat@10/10.1.61/libexec/webapps/
/opt/homebrew/opt/tomcat@10/bin/catalina start
```

### 3. Open in Browser
👉 **[http://localhost:8080/codestreak/](http://localhost:8080/codestreak/)**

- **Demo Account**: `demouser` / `password123`

# GUIDANCE-TEACHER-SYSTEM
   The Guidance Teacher System is a web application developed to support coordination
between Mae Fah Luang University's Admissions Office and its network of school
guidance counselors. It allows counselors to access university news, admission
schedules, applicant information, and reports for their own schools, while
administrators can manage announcements and view system-wide statistics. The
ation uses HTML, CSS, and JavaScript for the frontend, Java 21 with
the backend, and an embedded H2 database, with JWT authentication and ro
access control to protect school-specific information.

# LIVE DEMO
https://guidance-system-801537313339.us-central1.run.app

## GROUP MEMBERS
    6831503086  Naung Yoe Tun (Backend,Frontend,Database)
    6831503109  Ananya Suwandee  (Database,Backend)
    6831503111  Harry Foster  (Database,Backend)
    6831503114  Chompunut Namkort  (Frontend,Database,Deployment)
    6831503115  Yanisa Chaiwut  (Database,Backend)

## HOW TO RUN THE PROJECTS
### Requirements

- Java Development Kit (JDK) 21
- Git
- A web browser
- Gmail SMTP credentials for email verification features

The project uses the Maven Wrapper and an embedded H2 database. No separate database server is required.

### 1. Clone the Repository

```bash
git clone https://github.com/6831503086/GUIDANCE-SYSTEM.git
cd GUIDANCE-SYSTEM
```

### 2. Check Java

```bash
java -version
```

Ensure that the active Java version is 21.

### 3. Configure Environment Variables

Run these commands in the same terminal that will start the application:

```bash
export JWT_SECRET="$(openssl rand -hex 32)"
export MAIL_USERNAME="YOUR_SENDER_EMAIL"
read -r -s -p "Enter your mail app password: " MAIL_APP_PASSWORD
echo
export MAIL_APP_PASSWORD
export APP_VERIFICATION_BASE_URL="http://localhost:8080"
```

Use the sender email and app password provided by the team. Do not commit actual credentials.

The generated JWT secret is for local testing. Generating a new secret invalidates previously issued login tokens.

For GitHub Codespaces, replace `APP_VERIFICATION_BASE_URL` with the forwarded HTTPS URL for port 8080.

### 4. Start the Application

On macOS or Linux:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

On Windows PowerShell, configure the corresponding environment variables and run:

```powershell
.\mvnw.cmd spring-boot:run
```

Wait until the terminal displays:

```text
Started GuidanceSystemApplication
```

Keep this terminal running. Start only one application instance to avoid port conflicts and H2 database file locks.

### 5. Open the Website

Default port: **8080**

Open:

http://localhost:8080/

Spring Boot serves both the frontend and backend. A separate frontend server is not required.

In GitHub Codespaces, open the forwarded address for port 8080 from the Ports tab.

### 6. Test Accounts


### 7. Run Automated Tests

From the project root, with the required environment variables configured:

```bash
./mvnw test
```

### 8. Stop the Application

Press `Ctrl + C` in the terminal running Spring Boot.

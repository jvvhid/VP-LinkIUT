# LinkIUT

LinkIUT is an alumni-student networking platform built for the Islamic University of Technology (IUT). The platform enables students and alumni to connect, share opportunities, showcase profiles, and communicate through a structured mentorship and networking system.

---

# Project Overview

The goal of LinkIUT is to bridge the gap between current students and alumni by providing:

* Professional networking
* Alumni mentorship
* Internship opportunities
* Job postings
* Career guidance
* Direct messaging between users

The application is built using:

* Java 21
* Spring Boot 3
* Spring Security
* Spring Data JPA
* Thymeleaf
* HTMX
* Tailwind CSS
* H2 Database (Development)
* PostgreSQL (Production Ready)

---

# Technology Stack

## Backend

* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate
* Maven

## Frontend

* Thymeleaf
* HTMX
* Tailwind CSS
* HTML5

## Database

Development:

* H2 In-Memory Database

Production:

* PostgreSQL

---

# Project Structure

```text
src
├── main
│   ├── java
│   │   └── edu.iutdhaka.linkiut
│   │       ├── config
│   │       ├── controller
│   │       ├── model
│   │       ├── repository
│   │       ├── service
│   │       └── LinkIutApplication.java
│   │
│   └── resources
│       ├── templates
│       ├── static
│       ├── application.yml
│       └── schema.sql
```

---

# Core Architecture

The application follows a layered Spring Boot architecture:

```text
Browser
   ↓
Controller
   ↓
Service
   ↓
Repository
   ↓
Database
```

Each layer has a specific responsibility.

---

# Configuration Layer

## config/

Contains application-wide configurations.

### SecurityConfig.java

Responsible for:

* Spring Security configuration
* Login page configuration
* Logout configuration
* Authentication rules
* Password encryption using BCrypt

Features:

* Custom login page
* BCrypt password hashing
* User authentication through database
* Route protection

---

# Controller Layer

Controllers handle incoming HTTP requests.

## RegistrationController.java

Handles:

### GET /register

Displays registration page.

### POST /register

Creates a new user.

Validations:

* Password confirmation check
* Duplicate email prevention
* Minimum password length

Creates:

* AppUser record
* UserProfile record

---

## FeedController.java

Responsible for:

* Homepage feed
* Opportunity listing
* Opportunity creation

---

## ProfileController.java

Responsible for:

* Viewing user profiles
* Displaying profile details
* Displaying projects and experiences

---

## MessageController.java

Responsible for:

* Opening chat sessions
* Sending messages
* Viewing chat history

---

# Model Layer

Models represent database entities.

## AppUser.java

Represents:

```text
app_user
```

Fields:

* id
* email
* passwordHash
* role
* displayName
* avatarUrl
* createdAt

User Roles:

* STUDENT
* ALUMNI

---

## UserProfile.java

Represents:

```text
user_profile
```

Contains:

* headline
* bio
* department
* batch
* currentCompany
* location
* linkedinUrl

Each user has exactly one profile.

---

## Experience.java

Represents work experience.

Contains:

* title
* company
* dates
* description

---

## Project.java

Represents portfolio projects.

Contains:

* project name
* description
* tech stack
* repository URL

---

## Opportunity.java

Represents:

* Jobs
* Internships
* Mentorships

Opportunity Types:

* JOB
* INTERNSHIP
* MENTORSHIP

---

## ChatSession.java

Represents a conversation between:

* Student
* Alumni

Contains:

* session status
* creation time
* SLA deadline

Status:

* ACTIVE
* EXPIRED
* CLOSED

---

## Message.java

Represents individual chat messages.

Contains:

* sender
* content
* timestamp

---

# Repository Layer

Repositories communicate with the database.

## UserRepository

Responsible for:

```java
findByEmail(...)
existsByEmail(...)
save(...)
```

Used during:

* Login
* Registration

---

## ProfileRepository

Handles:

* Profile retrieval
* Profile updates

---

## OpportunityRepository

Handles:

* Opportunity listing
* Opportunity creation

---

## ChatSessionRepository

Handles:

* Chat sessions
* Session expiration checks

---

## MessageRepository

Handles:

* Message storage
* Message retrieval

---

# Service Layer

Services contain business logic.

## OpportunityService

Responsibilities:

* Create opportunities
* Fetch opportunities
* Validate opportunity data

---

## MessageService

Responsibilities:

* Create chat sessions
* Send messages
* Retrieve chat history

---

# Database

## schema.sql

Creates all database tables.

Tables:

### app_user

Stores:

* User accounts
* Roles
* Credentials

### user_profile

Stores:

* Extended profile information

### experience

Stores:

* Professional experiences

### project

Stores:

* User projects

### opportunity

Stores:

* Jobs
* Internships
* Mentorships

### chat_session

Stores:

* Conversations

### message

Stores:

* Chat messages

---

# Authentication Flow

Registration:

```text
User
 ↓
Registration Form
 ↓
Validation
 ↓
Password Hashing
 ↓
Save User
 ↓
Save Profile
```

Login:

```text
User
 ↓
Login Form
 ↓
Spring Security
 ↓
UserRepository
 ↓
Database Lookup
 ↓
Password Verification
 ↓
Authentication
```

---

# Frontend Pages

## login.html

Features:

* User login
* Demo credentials
* Error handling
* Registration link

---

## register.html

Features:

* User registration
* Role selection
* Validation feedback

---

## index.html

Features:

* Opportunity feed
* Posting opportunities
* Live updates

---

## chat.html

Features:

* Messaging interface
* Real-time updates
* Session status

---

## view.html

Features:

* Profile display
* Experience timeline
* Project showcase

---

# Running the Project

## Requirements

* Java 21
* Maven Wrapper (included)

## Start Application

```bash
mvnw.cmd spring-boot:run
```

Application URL:

```text
http://localhost:8080
```

---

# Development Database

Current configuration:

```yaml
jdbc:h2:mem:linkiut_db
```

Benefits:

* Zero setup
* Fast startup
* Ideal for development

---

# Future Improvements

* PostgreSQL migration
* Real-time WebSocket chat
* Alumni verification
* Resume uploads
* OAuth authentication
* Notifications
* Search and filtering
* Profile recommendations
* Mobile responsive enhancements

---

# Authors

Developed as an alumni-student networking platform for IUT students and alumni.

Built using Spring Boot, Thymeleaf, HTMX, and Tailwind CSS.

# LinkIUT 🎓 — Professional Networking & Career Intelligence Platform

LinkIUT is an end-to-end professional networking and career intelligence platform engineered specifically for the students and alumni of the Islamic University of Technology (IUT). It bridges academic life and industry by pairing social networking with live analytics, department-focused hubs, and an automated AI Job Readiness engine.

---

## 📽️ Project Presentation & Video Demonstration

- **Final Presentation Video**: [Watch on YouTube](https://www.youtube.com/watch?v=kUIHjygAu6w)  
  *(Covers Project Overview, Live Feature Demonstrations, Technical Implementation Architecture, and Group Member Contribution Breakdown)*

---

## 👥 Team Members & Contribution Breakdown

| Member Name | Student ID | Core Responsibilities & Modules |
| :--- | :--- | :--- |
| **MD. Jahidul Islam** | **230041248** | **Core Architecture, Real-Time Systems & Community Interaction**<br>• Repository architecture, database schema design, and Spring Data JPA integration.<br>• Real-time WebSocket messaging engine (STOMP/SockJS) for 1-on-1 chats, group messaging, and browser voice audio recording.<br>• Dynamic HTMX interactive feed, interactive community polls, and live event handling. |
| **Shafeen Sufian Meead** | **230041206** | **Security, Networking Hub, Career Analytics & AI Engine**<br>• Spring Security authentication with BCrypt hashing and role-based onboarding (Student vs. Alumni).<br>• Network Hub discovery filters (by department, batch, and skills) and Connection Management (request lifecycle & notification tray).<br>• **AI Job Readiness Tracker**: NLP-lite keyword skill extraction, fuzzy skill matching algorithm, dynamic circular readiness gauge, and AI recommendation generation.<br>• **Career Analytics Dashboard**: Real-time aggregation of community statistics visualized via Chart.js.<br>• **Global Search & Department Hubs**: Cross-entity search (peers, posts, jobs) and segmented department communities (CSE, EEE, MCE, CEE). |
| **Ilhamul Azam** | **230041262** | **Professional Identity, Social Engagement & Multimedia**<br>• Professional Profile System: Experience timelines, project portfolio showcase, and live repository links.<br>• Multipart media upload service for custom profile avatars and cover banners.<br>• Community engagement features: Feed reactions (likes/dislikes), threaded comments, and `@mention` user tagging.<br>• Peer skill endorsement subsystem and profile visit activity tracker. |

---

## ✨ Key Platform Features

### 1. Intelligent Career & Analytics Suite *(Advanced Modules)*
- **AI Job Readiness Tracker**:
  - Automatically compares job requirements against candidate profiles using NLP-lite keyword extraction (50+ tech terms) and fuzzy matching.
  - Dynamically computes a percentage readiness score displayed with an animated circular gauge.
  - Color-coded skill breakdown: Matched skills (green ✅) vs. missing skill gaps (red ❌), along with actionable AI recommendations.
- **Career Analytics Dashboard**:
  - Live data aggregation powered by Java Streams and Spring Data JPA.
  - Dynamic interactive visualizations (Chart.js) showing real-time department distributions and top hiring companies.
- **Global Search**:
  - Instant unified search engine indexing peers, posts, and job opportunities from a single bar.
- **Department Hubs**:
  - Dedicated academic hubs (CSE, EEE, MCE, CEE) allowing members to filter, discover, and collaborate within their specific disciplines.

### 2. Onboarding, Identity & Networking
- **Role-Based Authentication**: Secure onboarding distinguishing Current Students from Alumni, collecting academic parameters (Student ID, Batch, Department) secured with Spring Security & BCrypt.
- **Comprehensive Profiles**: Rich profiles showcasing headline, bio, experience timeline, and project portfolio with GitHub links.
- **Connection Management & Notifications**: Send, accept, decline, or withdraw connection requests with a synchronized notification tray.

### 3. Community Engagement & Real-Time Communication
- **Dynamic Community Feed**: Asynchronous, zero-reload interactions powered by HTMX (post creation, polls, reactions, and threaded comments).
- **Peer Endorsements & Profile Activity**: Credibility-building peer endorsements and profile visit tracking.
- **Real-Time Messaging Engine**:
  - 1-on-1 direct messaging and multi-user group chats.
  - Rich media attachments (photos, videos) and native in-browser voice note recording.
  - Sub-second delivery powered by Spring WebSockets (STOMP / SockJS).

---

## 🚀 Tech Stack

- **Backend**: Java 17, Spring Boot, Spring Security, Spring Data JPA (Hibernate), Spring WebSocket
- **Frontend**: Thymeleaf, Tailwind CSS, HTMX, Chart.js, JavaScript
- **Database**: MySQL Server
- **Build & Dependency Management**: Apache Maven

---

## 🛠️ Local Installation & Setup

### Prerequisites
- **Java Development Kit (JDK)**: Version 17 or higher
- **MySQL Server**: Installed and running locally

### 1. Database Setup
Create the MySQL database:
```sql
CREATE DATABASE linkiut;

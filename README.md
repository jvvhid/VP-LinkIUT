# LinkIUT 🎓

LinkIUT is a dedicated networking platform built for students and alumni of the Islamic University of Technology (IUT). It bridges the gap between current students and alumni by providing a robust ecosystem for professional networking, mentoring, and community engagement.

## ✨ Features

- **Role-Based Authentication:** Dedicated onboarding for Students and Alumni with specialized profiles.
- **Dynamic Interactive Feed:** Share posts, run polls, and interact with the community using likes and comments without page reloads (powered by HTMX).
- **Real-Time Messaging:**
  - One-on-one direct messaging and Group chats (up to 99 members).
  - Rich media support: Send photos, videos, and **record voice messages directly in the browser**.
  - Powered by WebSockets (STOMP/SockJS) for instant delivery.
- **Professional Profiles:** Showcase your headline, skills, education, and experience. Upload profile and cover photos.
- **Networking System:** Connect with alumni for mentorship or students for guidance.
- **Modern UI/UX:** A stunning, responsive isometric design built with Tailwind CSS.
- **Real-time Notifications:** Get notified when someone connects with you, likes your post, or comments on your thread.

## 🚀 Tech Stack

- **Backend:** Java, Spring Boot, Spring Security, Spring WebSocket
- **Frontend:** Thymeleaf, Tailwind CSS, HTMX, JavaScript
- **Database:** MySQL, Spring Data JPA (Hibernate)

## 🛠️ How to Run Locally

Follow these steps to run LinkIUT on any device.

### Prerequisites

1. **Java Development Kit (JDK):** Version 17 or higher.
2. **MySQL Server:** Ensure MySQL is installed and running on your local machine.

### 1. Database Setup

Create a new MySQL database named `linkiut`:

```sql
CREATE DATABASE linkiut;
```

*Note: The application is configured to connect to `jdbc:mysql://localhost:3306/linkiut` with the username `root` and password `root`. If your MySQL credentials differ, update the `application.properties` file.*

### 2. Configure Application Properties

Open `link-iut/src/main/resources/application.properties` and verify your database credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/linkiut?createDatabaseIfNotExist=true&useSSL=false
spring.datasource.username=root
spring.datasource.password=your_password_here
```

### 3. Build and Run

You can run the application directly using the Maven Wrapper included in the repository.

Navigate to the `link-iut` directory in your terminal:

**On Windows:**
```cmd
cd link-iut
.\mvnw.cmd clean spring-boot:run
```

**On macOS/Linux:**
```bash
cd link-iut
./mvnw clean spring-boot:run
```

### 4. Access the Application

Once the server has started, open your web browser and navigate to:
`http://localhost:8080`

The application comes with an automatic `DataLoader` that populates the database with demo users, posts, and chats on the first run.

## 🤝 Contributing

We welcome contributions to LinkIUT! Please ensure you branch off from `main`, make your feature changes, and submit a pull request for review.

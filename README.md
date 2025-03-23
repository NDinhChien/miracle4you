# Miracle4You

**Miracle4You** is a collaborative platform where language lovers can come together to create subtitles for meaningful videos, helping them reach a wider audience. The platform is designed to facilitate discussions, streamline the subtitling process, and present the work beautifully.

This project also serves as a hands-on opportunity to build a scalable and performant Java backend server using the Spring Boot framework.

## ✨ Features

### 🚀 Backend (Java + Spring Boot)

- **Framework**: Spring Boot, Java
- **Database**: PostgreSQL with Hibernate, Data Projections, and Custom Queries
- **Caching**: L2 Hibernate Cache, Caffeine Cache
- **Authentication**: Spring Security with JWT and Google Login
- **Real-time Communication**: WebSocket with SockJS & StompJS for Global, Group, and Private Chat, etc
- **File Storage**: AWS S3 Integration for Avatar Uploads and Sending Attachments
- **Email Service**: JavaMail with Thymeleaf Templates
- **API Documentation**: Swagger

### 🌐 Frontend (Next.js)

A Next.js app was built to test Google Login and WebSocket features. You can check it out here: [Miracle4You Web App](https://www.miracle4you.vercel.app)

![App](./images/app.png)

## 📚 API Documentation

Detailed API documentation is available via Swagger after running the project.
List of currently supported APIs:

![API](./images/api.png)

## ⚙️ How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/NDinhChien/miracle4you.git
   cd miracle4you
   ```
2. Set up your database and environment variables.
3. Run the backend server:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Access the API at:
   ```
   http://localhost:8000
   ```

## 📌 Upcoming Features

The project is still under development. There are many functions I want to add in the near future:

- Complete Notification system
- Enhance Project managements
- Add Subtitle, Comment, Post relating features
- Integrate new collaboration tools: Real-time editing
- .etc..

## 💌 Contact

If you’d like to contribute, give feedback, or just say hi, feel free to reach out!

Thanks for visiting my project — have a nice day! 🌼

# Project Report: College Club Management System (ClubApp)

## 1. INTRODUCTION

**What the project is**
The ClubApp is a full-stack, web-based club management system designed to centralize and automate the administration of college or university student clubs. It provides a dedicated platform for students, club coordinators, and administrators to interact, manage memberships, and organize events.

**Problem it solves**
Traditionally, college clubs manage memberships, event registrations, and attendance manually using spreadsheets, paper registers, or disjointed communication channels. This leads to data inconsistency, poor tracking of student participation, and administrative overhead. ClubApp solves this by providing a unified, secure platform to track all club-related activities in real-time.

**Purpose of the system**
The core purpose is to digitize student engagement. It aims to provide students with an easy way to explore campus life, request to join clubs, and register for events, while simultaneously giving coordinators the tools to manage their members, create events, and accurately track attendance. Administrators get a bird's-eye view of all platform activities to ensure smooth operations.

**Technologies used**
- **Backend:** Java 17, Spring Boot, Spring Security (JWT authentication), Spring Data JPA (Hibernate)
- **Frontend:** HTML5, CSS3, Vanilla JavaScript
- **Database:** MySQL (Relational Database)

---

## 2. OBJECTIVES

**Main goals of the project**
- To digitize and streamline the workflow of college clubs.
- To create a robust, secure, and scalable platform that replaces flat-file or manual data storage with a relational database system.

**Functional objectives**
- Implement secure Role-Based Access Control (RBAC) with distinct privileges for Administrators, Coordinators, and Students.
- Provide end-to-end event management, from creation and poster uploading to user registration.
- Facilitate an automated club join request workflow (Pending -> Accepted/Rejected).
- Track and maintain accurate attendance records for club events.

**User-related objectives**
- Offer students a seamless, centralized dashboard to view their memberships, ongoing events, and application statuses.
- Empower club coordinators with dedicated management tools to oversee their club's members and activities without requiring global admin access.

---

## 3. CLASS DIAGRAM

The system's core logic revolves around the following entity classes mapping to database schemas:

**1. `User`**
- **Role:** Represents all individuals in the system (Admin, Coordinator, Student).
- **Attributes:** `id`, `name`, `email`, `password`, `role`, `department`, `year`, `bio`, `joinedClubs`
- **Methods:** Standard getters/setters, Spring Security `UserDetails` implementations (`getAuthorities`, etc.)
- **Relationships:** Many-to-Many with `Club`, Many-to-Many with `Event`, One-to-Many with `Attendance`, One-to-Many with `ClubJoinRequest`

**2. `Club`**
- **Role:** Represents a student club or organization.
- **Attributes:** `id`, `name`, `description`, `vision`, `mission`, `department`, `foundedYear`, `logoImage`, `coordinator`, `members`, `events`, `joinRequests`
- **Relationships:** Many-to-One with `User` (Coordinator), One-to-Many with `Event`, One-to-Many with `ClubJoinRequest`, Many-to-Many with `User` (Members)

**3. `Event`**
- **Role:** Represents an activity hosted by a club.
- **Attributes:** `id`, `name`, `description`, `venue`, `date`, `time`, `posterImage`, `membersOnly`, `maxAttendees`, `club`, `attendees`
- **Relationships:** Many-to-One with `Club`, Many-to-Many with `User` (Attendees), One-to-Many with `Attendance`

**4. `Attendance`**
- **Role:** Tracks presence or absence of a user at a specific event.
- **Attributes:** `id`, `event`, `user`, `status`
- **Relationships:** Many-to-One with `Event`, Many-to-One with `User`

**5. `ClubJoinRequest`**
- **Role:** Manages the workflow of a user requesting to join a club.
- **Attributes:** `id`, `user`, `club`, `status`, `message`, `requestedAt`, `respondedAt`
- **Relationships:** Many-to-One with `User`, Many-to-One with `Club`

**6. `CollegeDetails`**
- **Role:** Stores global institution branding and administrative configuration.
- **Attributes:** `id`, `collegeName`, `location`, `established`, `tneaCode`, `principalName`, `website`, `vision`, `mission`, `about`, `logoImage`, `bannerImage`

**Enums**
- `Role`: ADMIN, COORDINATOR, STUDENT
- `AttendanceStatus`: PRESENT, ABSENT
- `JoinRequestStatus`: PENDING, ACCEPTED, REJECTED

---

## 4. ER DIAGRAM

**Database Tables & Relationships:**
- **users (1) --- (M) clubs**: A user can be a coordinator for multiple clubs (though functionally constrained), but a club has exactly one coordinator.
- **clubs (1) --- (M) events**: A club can host multiple events.
- **users (M) --- (M) clubs**: Students can join multiple clubs; clubs have multiple student members. (Resolved via `club_members` join table).
- **users (M) --- (M) events**: Users can register for multiple events; events have multiple attendees. (Resolved via `event_attendees` join table).
- **events (1) --- (M) attendance**: An event has multiple attendance records.
- **users (1) --- (M) attendance**: A user has multiple attendance records across different events.
- **users (1) --- (M) club_join_requests**: A user can make multiple join requests.
- **clubs (1) --- (M) club_join_requests**: A club receives multiple join requests.
- **college_details**: Independent table holding global system configuration (1-1 logic for the application scope).

---

## 5. TABLE STRUCTURES

**1. `users` Table**
- Purpose: Stores system credentials and profiles.
- `id` (BIGINT, Primary Key)
- `name` (VARCHAR, Not Null)
- `email` (VARCHAR, Unique, Not Null)
- `password` (VARCHAR, Not Null)
- `role` (VARCHAR, Enum representation, Not Null)
- `department` (VARCHAR)
- `year` (VARCHAR)
- `bio` (TEXT)

**2. `clubs` Table**
- Purpose: Stores club details.
- `id` (BIGINT, Primary Key)
- `name` (VARCHAR, Unique, Not Null)
- `description` (TEXT)
- `vision` (TEXT)
- `mission` (TEXT)
- `department` (VARCHAR)
- `founded_year` (INT)
- `logo_image` (VARCHAR)
- `coordinator_id` (BIGINT, Foreign Key referencing `users.id`)

**3. `events` Table**
- Purpose: Stores event metadata.
- `id` (BIGINT, Primary Key)
- `name` (VARCHAR, Not Null)
- `description` (TEXT)
- `venue` (VARCHAR)
- `date` (VARCHAR)
- `time` (VARCHAR)
- `poster_image` (VARCHAR)
- `members_only` (BOOLEAN, Not Null)
- `max_attendees` (INT)
- `club_id` (BIGINT, Foreign Key referencing `clubs.id`)

**4. `attendance` Table**
- Purpose: Tracks user presence at events.
- `id` (BIGINT, Primary Key)
- `event_id` (BIGINT, Foreign Key referencing `events.id`)
- `user_id` (BIGINT, Foreign Key referencing `users.id`)
- `status` (VARCHAR, Enum representation, Not Null)
- *Constraint: Unique combination of (event_id, user_id).*

**5. `club_join_requests` Table**
- Purpose: Logs requests to join a club.
- `id` (BIGINT, Primary Key)
- `user_id` (BIGINT, Foreign Key referencing `users.id`)
- `club_id` (BIGINT, Foreign Key referencing `clubs.id`)
- `status` (VARCHAR, Enum representation, Not Null)
- `message` (TEXT)
- `requested_at` (TIMESTAMP)
- `responded_at` (TIMESTAMP)

**6. `college_details` Table**
- Purpose: Institutional configuration.
- `id` (BIGINT, Primary Key)
- `college_name`, `location`, `established`, `tnea_code`, `principal_name`, `website` (VARCHAR)
- `vision`, `mission`, `about` (TEXT)
- `logo_image`, `banner_image` (VARCHAR)

**7. `club_members` (Join Table)**
- Purpose: Maps the M:M relationship between clubs and members.
- `club_id` (BIGINT, Foreign Key referencing `clubs.id`)
- `user_id` (BIGINT, Foreign Key referencing `users.id`)

**8. `event_attendees` (Join Table)**
- Purpose: Maps the M:M relationship between events and registered users.
- `event_id` (BIGINT, Foreign Key referencing `events.id`)
- `user_id` (BIGINT, Foreign Key referencing `users.id`)

---

## 6. MODULES DESCRIPTION

**1. Authentication Module**
- **Purpose:** Handles application security, identity verification, and token generation.
- **Features:** Registration for students/admins, secure login, JWT issuance and validation.
- **APIs:** `/api/auth/login`, `/api/auth/register`, `/api/auth/register/admin`

**2. Admin Module**
- **Purpose:** Centralized oversight of the entire system.
- **Features:** View all clubs, assign coordinators, monitor global event data, manage all user accounts.
- **APIs:** `/api/admin/overview`, `/api/admin/clubs`, `/api/admin/users`, `/api/admin/events/*`

**3. Coordinator Module**
- **Purpose:** Allows a coordinator to manage their specific club.
- **Features:** View club statistics, manage club profile, view members, promote members.
- **APIs:** `/api/clubs/my-club`, `/api/join-requests/my-club/*`

**4. Student Module**
- **Purpose:** Primary interface for general users.
- **Features:** Browse active clubs, view ongoing events, track personal memberships and event participation.
- **APIs:** `/api/users/me`, `/api/clubs`

**5. Event Management System**
- **Purpose:** Complete lifecycle management of activities.
- **Features:** Create/Update/Delete events, register for an event, toggle "Members Only" restrictions, upload promotional posters.
- **APIs:** `/api/clubs/{clubId}/events`, `/api/events/{id}`, `/api/events/{id}/register`

**6. Join Request System**
- **Purpose:** Approval workflow for club memberships.
- **Features:** Submit join requests with a message, view pending requests, coordinators can accept or reject applications.
- **APIs:** `/api/join-requests/{clubId}/join-request`, `/api/join-requests/requests/{id}/accept`

**7. Attendance System**
- **Purpose:** Track participation.
- **Features:** Mark attendees as PRESENT or ABSENT, ensuring no duplicate entries.
- **APIs:** `/api/attendance` (POST, GET)

**8. Profile System**
- **Purpose:** Personal user data management.
- **Features:** Update bio, change passwords, view current department and year.
- **APIs:** `/api/users/me` (PUT)

**9. College Settings System**
- **Purpose:** White-labeling and institutional branding.
- **Features:** Admins can update the college name, vision, mission, and institutional logos that appear on the public landing page.
- **APIs:** `/api/college-settings`

---

## 7. IMPLEMENTATION & SCREENSHOTS

**Implementation Details (Backend + Frontend Flow)**
The system is implemented utilizing a decoupled architecture. The Spring Boot backend exposes RESTful APIs that handle business logic, data validation, and database operations. The backend is secured via Spring Security; all restricted endpoints require a valid JWT passed in the `Authorization: Bearer` header. 
The frontend is constructed using static HTML, styled with CSS, and made fully dynamic via Vanilla JavaScript `fetch` API calls. When a user logs in, the JWT is stored in `localStorage` and utilized to authenticate subsequent requests, determining which UI components (like the dashboard) to render based on the user's encoded role.

**Page-wise Functionality**
- **Landing Page (`index.html`):** The public face of the application. It fetches and displays the `CollegeDetails` to showcase institutional branding. It acts as an exploratory page displaying a list of active clubs and an overview of public ongoing events, prompting users to log in for full access.
- **Login/Register (`login.html`):** Handles authentication. Captures user credentials, communicates with the Auth module, stores the resulting JWT, and routes the user to the correct dashboard layout.
- **Dashboard (`dashboard.html`):** A context-aware application interface. 
  - *For Students:* Displays their joined clubs, upcoming events they are registered for, and the status of their join requests.
  - *For Coordinators:* Displays a management view of their assigned club, controls to approve/reject incoming members, tools to create or modify events, and access to the attendance tracker.
- **Admin Panel (`admin.html`):** Restricted solely to the ADMIN role. Provides forms to modify college details (vision, mission, logos), a table to manage user roles and promote students to coordinators, and overriding control over all clubs and events.

**Screenshots to be Captured**
To complete the visual documentation, the following screenshots must be captured and embedded:
1. **Landing Page Overview** - Showing the college banner and active clubs.
2. **Login/Registration Interface** - Showing the authentication form.
3. **Student Dashboard** - Displaying "My Clubs" and "Registered Events".
4. **Coordinator Dashboard** - Displaying the club management tools and pending join requests.
5. **Event Creation/Edit Modal** - Demonstrating the event parameter inputs (Venue, Date, Time, Max Attendees).
6. **Attendance Tracking View** - Showing the list of members and the Present/Absent toggles.
7. **Admin Panel (System Settings)** - Showing the college details modification form.
8. **Admin Panel (User Management)** - Showing the global list of users and coordinator assignment interface.

---

## 8. CONCLUSION

**Summary of the system**
The ClubApp provides a comprehensive, digital solution for managing student organizations within a college environment. By leveraging a modern tech stack (Spring Boot, MySQL, JavaScript), the project replaces manual tracking mechanisms with a robust, centralized web application.

**What was achieved**
- Successfully migrated from flat-file or manual tracking to a relational database system.
- Implemented secure, stateless authentication using JSON Web Tokens (JWT).
- Established a distinct hierarchy with Role-Based Access Control (Admin, Coordinator, Student).
- Delivered a dynamic frontend interface that changes context based on the authenticated user's role.

**Advantages**
- **Data Integrity:** Relationships between users, clubs, and events are strictly enforced by the database, preventing orphaned records.
- **Efficiency:** Drastically reduces the administrative burden on coordinators by automating registrations and centralizing attendance.
- **Security:** Passwords are encrypted, and APIs are protected against unauthorized access.
- **Accessibility:** Students have a unified portal to engage with campus life.

**Possible future improvements**
- Integration of automated Email/SMS notifications for event reminders or join request updates.
- Implementation of a dedicated mobile application (e.g., using React Native or Flutter) for easier access on the go.
- Integration with a payment gateway to facilitate paid event registrations or club membership fees.
- Advanced analytics and reporting dashboards to help the college administration gauge student engagement metrics.

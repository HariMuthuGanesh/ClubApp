You are a senior Java Spring Boot developer.

I already have a partially working Club Management System project.

Tech stack:

* Backend: Java Spring Boot (Maven, MySQL)
* Frontend: HTML, CSS, Vanilla JavaScript ONLY (no frameworks)

Your task is to FIX and COMPLETE the existing project.

Do NOT rebuild from scratch.
Do NOT change architecture.
Only extend and correct the current implementation.

---

OBJECTIVE

Make the system fully working with:

* No compilation errors
* No missing UI
* All features functional
* Proper role-based behavior

---

CRITICAL FIXES (DO FIRST)

1. Fix compilation errors:

* AttendanceResponse builder mismatch
* Any missing fields or methods

2. Ensure backend runs without errors

---

MISSING FEATURES TO IMPLEMENT

1. PROFILE (ALL USERS)

* Add profile modal (not new page)
* Fields:

  * name
  * department (student)
  * year (student)
* Change password:

  * current password
  * new password

No profile image. Use initials only.

---

2. COLLEGE SETTINGS (ADMIN ONLY)

Create full UI + backend.

Fields:

* collegeName
* location
* established
* tneaCode
* vision
* mission
* principalName
* website
* logo (file upload)

Store in DB.

Used in:

* landing page
* navbar
* sidebar

---

3. LANDING PAGE (index.html)

Before login:

* show college details
* show few clubs
* show upcoming events
* login + register buttons

---

4. JOIN REQUEST SYSTEM

Student:

* send join request with message

Coordinator:

* accept / reject
* view message

Allow re-request after rejection

---

5. EVENT SYSTEM FIX

Coordinator:

* create event
* upload poster (file)

Fields:

* name
* date
* time
* venue
* description
* membersOnly

Rules:

* Open → anyone can register
* Members-only → only accepted members

---

6. ATTENDANCE SYSTEM

* Only allowed on event day
* Coordinator marks present/absent

Ensure backend + UI both work

---

7. ADMIN PANEL FIX

Sidebar must have:

* Overview
* All Clubs
* All Users
* College Settings
* Logout

Add missing College Settings UI

---

8. COORDINATOR DASHBOARD FIX

* Show ONLY their club
* Remove "All Clubs"
* Add:

  * join requests
  * members
  * events
  * attendance

---

9. STUDENT DASHBOARD FIX

Sections:

* Overview
* All Clubs
* My Clubs

Fix visibility + behavior

---

10. FILE UPLOAD (IMPORTANT)

Implement correctly:

* event poster → /uploads/events/
* club logo → /uploads/clubs/
* college logo → /uploads/college/

Store filename in DB
Serve images properly

---

11. ERROR HANDLING

* Fix all broken API calls
* Add proper JSON responses
* Show alerts/toasts in frontend

---

STRICT RULES

* Do NOT use React or any framework
* Use only Java + Spring Boot backend
* Use only HTML, CSS, JS frontend
* Do NOT rewrite whole project
* Only fix and complete missing parts

---

OUTPUT

1. Identify issues
2. Fix backend (file by file)
3. Fix frontend (file by file)
4. Ensure project runs successfully

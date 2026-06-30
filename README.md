# JOB AI — Automated Job Application Platform

JOB AI is a full-stack platform that automates the job search process by scraping jobs, scoring them against your profile using AI, generating highly-tailored ATS-friendly PDF resumes, and autonomously applying using headless browsers.

## Architecture
- **Frontend**: React + Vite + Tailwind CSS + Firebase Auth
- **Backend API**: Java Spring Boot + PostgreSQL (Neon) + AWS Bedrock
- **Automation Agent**: Node.js + Playwright + TypeScript

---

## 🚀 Deployment Guide

The platform is designed to be deployed across three services: **Neon** (Database), **Render** (Backend & Agent), and **Firebase** (Frontend).

### Phase 1: Database (Neon)
*Status: Completed!* 
The Neon Serverless Postgres database has been successfully initialized with all necessary tables and schemas via Flyway.

### Phase 2: Backend & Playwright Agent (Render)
Render connects directly to your GitHub repository to build and host the backend services.

1. **Push to GitHub**:
   Initialize a git repository in this folder and push it to GitHub:
   ```bash
   git init
   git add .
   git commit -m "Initial commit of JOB AI"
   git branch -M main
   git remote add origin https://github.com/yourusername/job-ai.git
   git push -u origin main
   ```
2. **Deploy on Render**:
   - Go to [Render Dashboard](https://dashboard.render.com/) and click **New +** -> **Blueprint**.
   - Connect your GitHub repository. Render will automatically detect the `render.yaml` file.
   - Fill in the required environment variables:
     - `DB_URL`: `jdbc:postgresql://ep-crimson-rice-atd9vzq7-pooler.c-9.us-east-1.aws.neon.tech/neondb?sslmode=require`
     - `DB_USERNAME`: `neondb_owner`
     - `DB_PASSWORD`: `npg_ao7Xxgwnqm5d`
     - `BEDROCK_ACCESS_KEY`: Your AWS Access Key
     - `BEDROCK_SECRET_KEY`: Your AWS Secret Key
   - Click **Apply**.
3. **Get your API URL**:
   Once deployed, Render will give you a public URL for your web service (e.g., `https://job-ai-backend.onrender.com`). Copy this URL.

### Phase 3: Frontend (Firebase Hosting)
The React dashboard will be hosted globally on Firebase CDN.

1. **Set Production API URL**:
   In the terminal, build the production bundle of the frontend, making sure to inject the Render backend URL you got from the previous step:
   ```bash
   cd frontend
   VITE_API_URL=https://<your-render-app-url>/api npm run build
   ```
2. **Deploy to Firebase**:
   Initialize and deploy using the Firebase CLI:
   ```bash
   npx firebase login
   npx firebase deploy --only hosting
   ```

You are now live! 🎉

---

## 💻 Running Locally for Development

If you want to run the stack on your local machine:

**1. Start the Spring Boot Backend**
```bash
cd backend
mvn spring-boot:run
```
*(By default, this connects to localhost:5432. To connect to Neon locally, run: `SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run`)*

**2. Start the React Frontend**
```bash
cd frontend
npm run dev
```

**3. Start the Playwright Agent**
```bash
cd playwright-agent
npm run build
node dist/index.js
```

# 🦊 GitLab CI/CD Setup Guide for sample-java-ci

Complete guide to set up and run GitLab CI/CD pipeline for your Java Maven project.

---

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [GitLab Configuration](#gitlab-configuration)
- [Environment Variables Setup](#environment-variables-setup)
- [GitLab Runner Setup](#gitlab-runner-setup)
- [Pipeline Overview](#pipeline-overview)
- [Running the Pipeline](#running-the-pipeline)
- [Troubleshooting](#troubleshooting)

---

## 🛠️ Prerequisites

Before setting up GitLab CI/CD, ensure you have:

- ✅ **GitLab Account** (gitlab.com or self-hosted)
- ✅ **GitLab Repository** (project uploaded to GitLab)
- ✅ **DockerHub Account** (for storing Docker images)
- ✅ **GitLab Runner** (registered and active)
- ✅ **Docker** installed on the runner machine

---

## 🔧 GitLab Configuration

### 1️⃣ Create GitLab Repository

```bash
# Initialize Git (if not already done)
cd D:\Java-Pipeline
git init
git add .
git commit -m "Initial commit: Java Maven CI/CD project"

# Add GitLab remote
git remote add origin https://gitlab.com/<your-username>/sample-java-ci.git

# Push to GitLab
git branch -M main
git push -u origin main
```

### 2️⃣ Verify `.gitlab-ci.yml` File

Ensure `.gitlab-ci.yml` is present in the root directory of your project. This file defines the CI/CD pipeline.

---

## 🔐 Environment Variables Setup

### Configure in GitLab UI

**Navigate to:** `Settings → CI/CD → Variables`

Add the following variables:

| Variable Name | Value | Protected | Masked |
|--------------|-------|-----------|--------|
| `DOCKERHUB_USERNAME` | Your DockerHub username | ✅ | ❌ |
| `DOCKERHUB_PASSWORD` | Your DockerHub password or access token | ✅ | ✅ |

**Steps:**

1. Go to your GitLab project
2. Click **Settings** → **CI/CD**
3. Expand **Variables** section
4. Click **Add variable**
5. Enter variable name and value
6. Check **Protect variable** and **Mask variable** (for password)
7. Click **Add variable**

**Security Note:** Use a DockerHub **Access Token** instead of your password:
- Go to DockerHub → Account Settings → Security → New Access Token
- Copy the token and use it as `DOCKERHUB_PASSWORD`

---

## 🏃 GitLab Runner Setup

### Option 1: Using Shared Runners (Easiest)

If your GitLab instance has shared runners enabled, skip runner installation.

**Enable Shared Runners:**
1. Go to **Settings → CI/CD → Runners**
2. Enable **Shared runners**

### Option 2: Install Your Own Runner

#### On Windows (PowerShell as Administrator):

```powershell
# 1. Create GitLab Runner directory
New-Item -ItemType Directory -Force -Path "C:\GitLab-Runner"
cd C:\GitLab-Runner

# 2. Download GitLab Runner
Invoke-WebRequest -Uri "https://gitlab-runner-downloads.s3.amazonaws.com/latest/binaries/gitlab-runner-windows-amd64.exe" -OutFile "gitlab-runner.exe"

# 3. Register the runner
.\gitlab-runner.exe register

# Follow the prompts:
# - Enter GitLab instance URL: https://gitlab.com/
# - Enter registration token: (Get from Settings → CI/CD → Runners)
# - Enter runner description: windows-docker-runner
# - Enter tags: docker,windows
# - Enter executor: docker
# - Enter default Docker image: eclipse-temurin:20-jdk

# 4. Install and start the runner service
.\gitlab-runner.exe install
.\gitlab-runner.exe start
```

#### On Linux/Ubuntu:

```bash
# 1. Download and install GitLab Runner
curl -L "https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.deb.sh" | sudo bash
sudo apt-get install gitlab-runner

# 2. Register the runner
sudo gitlab-runner register

# Follow the prompts (same as Windows)

# 3. Start the runner
sudo gitlab-runner start
```

### Get Registration Token

1. Go to your GitLab project
2. Navigate to **Settings → CI/CD → Runners**
3. Expand **Specific runners** section
4. Copy the **registration token**

---

## 📊 Pipeline Overview

The GitLab CI/CD pipeline consists of 5 stages:

```
┌─────────────────────────────────────────────────────────────┐
│                    GitLab CI/CD Pipeline                     │
└─────────────────────────────────────────────────────────────┘

Stage 1: BUILD
   ├─> Compile Java source code
   ├─> Package application
   └─> Create JAR artifact

Stage 2: TEST
   ├─> Run JUnit tests
   ├─> Generate test reports
   └─> Upload test results

Stage 3: DOCKER BUILD
   ├─> Build Docker image
   ├─> Tag with 'latest' and pipeline ID
   └─> Verify image creation

Stage 4: DOCKER PUSH
   ├─> Login to DockerHub
   ├─> Push image with 'latest' tag
   ├─> Push image with pipeline ID tag
   └─> Logout from DockerHub

Stage 5: DEPLOY (Manual)
   ├─> Stop existing container
   ├─> Pull latest image
   ├─> Run new container on port 8080
   └─> Verify deployment
```

### Pipeline Features

- ✅ **Caching** - Maven dependencies cached for faster builds
- ✅ **Artifacts** - JAR files and test reports saved
- ✅ **Test Reports** - JUnit results displayed in GitLab UI
- ✅ **Multi-tagging** - Images tagged with 'latest' and pipeline ID
- ✅ **Manual Deployment** - Deploy stage requires manual approval
- ✅ **Branch Filters** - Only runs on main/master/develop branches

---

## 🚀 Running the Pipeline

### Automatic Pipeline Trigger

The pipeline runs automatically when you push code to GitLab:

```powershell
# Make changes to your code
git add .
git commit -m "Update application"
git push origin main
```

### Manual Pipeline Trigger

1. Go to **CI/CD → Pipelines**
2. Click **Run pipeline**
3. Select branch (e.g., `main`)
4. Click **Run pipeline**

### Monitor Pipeline Progress

1. Go to **CI/CD → Pipelines**
2. Click on the pipeline number
3. View each stage's progress and logs
4. Check test results in **Tests** tab

### Manual Deployment

The deployment stage requires manual approval:

1. Wait for all previous stages to complete
2. Go to the pipeline view
3. Click **▶ Play** button on the **deploy** stage
4. Confirm deployment

---

## 🔍 Verify Deployment

After successful deployment:

### Check Container Status

```bash
# SSH to your runner machine
ssh user@runner-machine

# Check running containers
docker ps | grep sample-java-ci

# View container logs
docker logs sample-java-ci

# Test application
curl http://localhost:8080
```

### View Application

Open browser and navigate to:
```
http://your-runner-ip:8080
```

---

## 🐛 Troubleshooting

### Pipeline Fails at Build Stage

**Error:** `mvn: command not found`

**Solution:** The pipeline uses Maven Wrapper (`mvnw`). Ensure the files exist:
```bash
# Check if Maven Wrapper exists
ls -la mvnw
ls -la .mvn/wrapper/
```

### Pipeline Fails at Docker Build

**Error:** `docker: command not found`

**Solution:** Ensure Docker is installed on the GitLab Runner:
```bash
docker --version
```

### Pipeline Fails at Docker Push

**Error:** `unauthorized: authentication required`

**Solution:** Verify DockerHub credentials in GitLab variables:
1. Go to **Settings → CI/CD → Variables**
2. Check `DOCKERHUB_USERNAME` and `DOCKERHUB_PASSWORD`
3. Ensure password is an access token, not your actual password

### Runner Not Picking Up Jobs

**Solution:** Check runner status:
```bash
# On runner machine
gitlab-runner verify
gitlab-runner status

# Restart if needed
gitlab-runner restart
```

### Java Version Mismatch

**Error:** `release version 20 not supported`

**Solution:** Update `.gitlab-ci.yml` to use correct Java version:
```yaml
image: eclipse-temurin:20-jdk
```

### Docker Permission Denied

**Error:** `permission denied while trying to connect to Docker daemon`

**Solution:** Add GitLab Runner user to docker group:
```bash
sudo usermod -aG docker gitlab-runner
sudo systemctl restart docker
```

---

## 📝 Pipeline Configuration Details

### Modify Docker Image Name

Edit `.gitlab-ci.yml`:

```yaml
variables:
  DOCKER_IMAGE: "your-dockerhub-username/sample-java-ci"
```

### Change Deployment Port

Edit `.gitlab-ci.yml` in the deploy stage:

```yaml
script:
  - docker run -d --name sample-java-ci -p 9090:8080 $DOCKER_IMAGE:$DOCKER_TAG
```

### Add More Stages

You can add additional stages like security scanning:

```yaml
stages:
  - build
  - test
  - security
  - docker-build
  - docker-push
  - deploy

security-scan:
  stage: security
  image: aquasec/trivy:latest
  script:
    - trivy image $DOCKER_IMAGE:$DOCKER_TAG
```

---

## 🎯 Best Practices

1. ✅ **Use Access Tokens** - Never use plain passwords
2. ✅ **Enable Protected Branches** - Limit deployment to main/master
3. ✅ **Manual Deployment** - Require approval for production deployments
4. ✅ **Cache Dependencies** - Speed up builds with Maven cache
5. ✅ **Tag Images** - Use pipeline ID for versioning
6. ✅ **Monitor Logs** - Check pipeline logs for issues
7. ✅ **Test Locally** - Test Docker builds locally before pushing

---

## 📚 Useful GitLab Commands

```bash
# View pipeline status
git log --oneline

# Check last pipeline
# (View in GitLab UI: CI/CD → Pipelines)

# Clean up local Docker images
docker system prune -a

# View GitLab Runner logs
gitlab-runner --debug run
```

---

## 🔗 Additional Resources

- [GitLab CI/CD Documentation](https://docs.gitlab.com/ee/ci/)
- [GitLab Runner Installation](https://docs.gitlab.com/runner/install/)
- [Docker Documentation](https://docs.docker.com/)
- [Maven Documentation](https://maven.apache.org/guides/)

---

## 📞 Support

If you encounter issues:

1. Check **CI/CD → Pipelines** for error messages
2. Review **Job logs** for detailed errors
3. Verify **Settings → CI/CD → Variables**
4. Ensure **GitLab Runner** is active
5. Check **Docker** is installed and running

---

**Happy GitLab CI/CD! 🎉**

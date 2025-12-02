# 🦊 GitLab CI/CD Setup Guide

Complete guide for setting up GitLab Runner on AWS EC2 and configuring GitLab CI/CD pipeline for Java Maven Docker project.

## 📋 Table of Contents

- [EC2 Instance Setup](#-ec2-instance-setup-for-gitlab-runner)
- [GitLab Runner Installation](#-gitlab-runner-installation)
- [GitLab Runner Configuration](#-gitlab-runner-configuration)
- [GitLab Project Configuration](#-gitlab-project-configuration)
- [Pipeline Variables](#-pipeline-variables)
- [Testing the Pipeline](#-testing-the-pipeline)
- [Troubleshooting](#-troubleshooting)

---

## 🖥️ EC2 Instance Setup for GitLab Runner

### Instance Details
- **EC2 Type**: t3.large or c7i-flex.large (minimum 2 vCPU, 8GB RAM)
- **AMI**: Amazon Linux 2
- **Key**: gitlab-runner.pem
- **SG Inbound Rules**: 
  - SSH (Port 22) - Your IP
  - HTTP (Port 80) - Optional
  - Custom TCP (Port 8080) - For application testing
- **User**: ec2-user

### Step 1: Connect to EC2 Instance
```bash
cd ~/Downloads
chmod 400 gitlab-runner.pem
ssh -i "gitlab-runner.pem" ec2-user@<EC2-Public-IP>
```

### Step 2: Update System & Install Dependencies
```bash
sudo yum update -y
sudo yum install wget tar tree curl git -y
```

### Step 3: Install Git & Configure
```bash
sudo yum install git -y
git config --global user.name "khushalbhavsar"
git config --global user.email "khushalbhavsar41@gmail.com"
git config --list
```

### Step 4: Install Docker
```bash
# Install Docker
sudo yum install docker -y

# Start and enable Docker
sudo systemctl start docker
sudo systemctl enable docker

# Verify Docker installation
docker --version

# Add ec2-user to docker group
sudo usermod -aG docker ec2-user

# Restart session or run:
newgrp docker

# Test Docker
docker ps
```

### Step 5: Install Java 21 (Amazon Corretto)
```bash
sudo yum install java-21-amazon-corretto.x86_64 -y
java --version
```

### Step 6: Install Maven
```bash
sudo yum install maven -y
mvn -v
```

---

## 🏃 GitLab Runner Installation

### Step 1: Add GitLab Runner Repository
```bash
curl -L "https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.rpm.sh" | sudo bash
```

### Step 2: Install GitLab Runner
```bash
sudo yum install gitlab-runner -y
```

### Step 3: Verify Installation
```bash
gitlab-runner --version
```

### Step 4: Check GitLab Runner Service
```bash
sudo systemctl status gitlab-runner
sudo systemctl enable gitlab-runner
sudo systemctl start gitlab-runner
```

### Step 5: Add GitLab Runner User to Docker Group
```bash
sudo usermod -aG docker gitlab-runner
sudo systemctl restart docker
sudo systemctl restart gitlab-runner
```

### Step 6: Verify Docker Access for GitLab Runner
```bash
sudo -u gitlab-runner docker ps
```

---

## 🔧 GitLab Runner Configuration

### Step 1: Get Registration Token from GitLab

**For Project-Specific Runner:**
1. Go to your GitLab project
2. Navigate to: **Settings → CI/CD → Runners**
3. Expand **Runners** section
4. Copy the **Registration token**

**For Group Runner:**
1. Go to your GitLab group
2. Navigate to: **Settings → CI/CD → Runners**
3. Copy the **Registration token**

### Step 2: Register GitLab Runner
```bash
sudo gitlab-runner register
```

You'll be prompted for the following information:

**Interactive Prompts:**
```
GitLab instance URL: https://gitlab.com/
Registration token: <paste-your-token>
Description: AWS EC2 Docker Runner
Tags: docker,aws,ec2,java,maven
Executor: docker
Default Docker image: maven:3.8.5-openjdk-21
```

**Or use non-interactive registration:**
```bash
sudo gitlab-runner register \
  --non-interactive \
  --url "https://gitlab.com/" \
  --registration-token "YOUR_REGISTRATION_TOKEN" \
  --executor "docker" \
  --docker-image "maven:3.8.5-openjdk-21" \
  --description "AWS EC2 Docker Runner" \
  --tag-list "docker,aws,ec2,java,maven" \
  --run-untagged="true" \
  --locked="false" \
  --docker-privileged="true" \
  --docker-volumes "/var/run/docker.sock:/var/run/docker.sock" \
  --docker-volumes "/cache"
```

### Step 3: Configure Docker Privileged Mode

Edit the GitLab Runner configuration:
```bash
sudo nano /etc/gitlab-runner/config.toml
```

Update the configuration to enable Docker-in-Docker:
```toml
concurrent = 1
check_interval = 0

[session_server]
  session_timeout = 1800

[[runners]]
  name = "AWS EC2 Docker Runner"
  url = "https://gitlab.com/"
  token = "YOUR_RUNNER_TOKEN"
  executor = "docker"
  [runners.custom_build_dir]
  [runners.cache]
    [runners.cache.s3]
    [runners.cache.gcs]
    [runners.cache.azure]
  [runners.docker]
    tls_verify = false
    image = "maven:3.8.5-openjdk-21"
    privileged = true
    disable_entrypoint_overwrite = false
    oom_kill_disable = false
    disable_cache = false
    volumes = ["/var/run/docker.sock:/var/run/docker.sock", "/cache"]
    shm_size = 0
```

### Step 4: Restart GitLab Runner
```bash
sudo systemctl restart gitlab-runner
sudo gitlab-runner verify
```

### Step 5: Check Runner Status
```bash
sudo gitlab-runner status
sudo gitlab-runner list
```

---

## 🔐 GitLab Project Configuration

### Step 1: Add CI/CD Variables

Go to your GitLab project:
**Settings → CI/CD → Variables → Expand → Add Variable**

Add the following variables:

| Key | Value | Type | Protected | Masked |
|-----|-------|------|-----------|--------|
| `DOCKER_USERNAME` | Your DockerHub username | Variable | ✅ | ❌ |
| `DOCKER_PASSWORD` | Your DockerHub password/token | Variable | ✅ | ✅ |

**Steps:**
1. Click **Add variable**
2. Enter **Key**: `DOCKER_USERNAME`
3. Enter **Value**: Your DockerHub username (e.g., `khushalbhavsar`)
4. Check **Protect variable** (optional - for protected branches only)
5. Leave **Mask variable** unchecked
6. Click **Add variable**

Repeat for `DOCKER_PASSWORD`:
1. Click **Add variable**
2. Enter **Key**: `DOCKER_PASSWORD`
3. Enter **Value**: Your DockerHub password or access token
4. Check **Protect variable** (optional)
5. Check **Mask variable** (recommended for security)
6. Click **Add variable**

### Step 2: Enable Auto DevOps (Optional)

**Settings → CI/CD → Auto DevOps**
- Uncheck **Default to Auto DevOps pipeline** (we're using custom `.gitlab-ci.yml`)

### Step 3: Enable Shared Runners (If Needed)

**Settings → CI/CD → Runners**
- Enable **Shared Runners** if you want to use GitLab's shared runners alongside your specific runner

---

## 📝 Pipeline Variables

The `.gitlab-ci.yml` file uses the following variables:

### Predefined GitLab Variables
- `$CI_PROJECT_DIR` - Project directory path
- `$CI_PIPELINE_ID` - Pipeline unique ID (used for Docker tagging)
- `$CI_COMMIT_REF_NAME` - Branch or tag name

### Custom Variables (Set in GitLab)
- `$DOCKER_USERNAME` - DockerHub username
- `$DOCKER_PASSWORD` - DockerHub password/token

### Pipeline-Specific Variables
```yaml
variables:
  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"
  DOCKER_IMAGE: "$DOCKER_USERNAME/sample-java-ci"
  DOCKER_TAG: "latest"
```

---

## ✅ Testing the Pipeline

### Step 1: Commit and Push Changes
```bash
git add .
git commit -m "Add GitLab CI/CD pipeline"
git push origin main
```

### Step 2: Monitor Pipeline Execution

1. Go to your GitLab project
2. Navigate to: **CI/CD → Pipelines**
3. Click on the latest pipeline
4. Monitor each stage: Build → Test → Docker Build → Docker Push → Deploy

### Step 3: View Pipeline Logs

Click on any stage to view detailed logs:
- **Build**: Maven compilation logs
- **Test**: JUnit test results
- **Docker Build**: Docker image build logs
- **Docker Push**: DockerHub push logs
- **Deploy**: Container deployment logs

### Step 4: Manual Deployment

The **Deploy** stage is set to manual trigger:
1. Go to pipeline view
2. Click **Play** button (▶️) on the Deploy stage
3. Confirm deployment
4. Monitor deployment logs

### Step 5: Verify Deployment

On your EC2 instance:
```bash
# Check running containers
docker ps

# View container logs
docker logs sample-java-ci

# Test the application (if it has HTTP endpoint)
curl http://localhost:8080
```

---

## 🐛 Troubleshooting

### Issue 1: Runner Not Picking Up Jobs

**Solution:**
```bash
# Verify runner is active
sudo gitlab-runner verify

# Restart runner
sudo systemctl restart gitlab-runner

# Check runner logs
sudo journalctl -u gitlab-runner -f
```

### Issue 2: Docker Permission Denied

**Error:** `Got permission denied while trying to connect to Docker daemon socket`

**Solution:**
```bash
# Add gitlab-runner to docker group
sudo usermod -aG docker gitlab-runner

# Restart services
sudo systemctl restart docker
sudo systemctl restart gitlab-runner

# Verify docker access
sudo -u gitlab-runner docker ps
```

### Issue 3: Docker-in-Docker Not Working

**Solution:**
Ensure `privileged = true` in `/etc/gitlab-runner/config.toml`:
```bash
sudo nano /etc/gitlab-runner/config.toml
```

Update:
```toml
[runners.docker]
  privileged = true
  volumes = ["/var/run/docker.sock:/var/run/docker.sock", "/cache"]
```

Restart:
```bash
sudo systemctl restart gitlab-runner
```

### Issue 4: Maven Build Fails

**Error:** `Could not resolve dependencies`

**Solution:**
Check internet connectivity and Maven cache:
```bash
# On EC2 instance
curl -I https://repo.maven.apache.org/maven2/

# Clear Maven cache in pipeline
# Add to .gitlab-ci.yml before_script:
- rm -rf .m2/repository/*
```

### Issue 5: DockerHub Login Fails

**Error:** `Error response from daemon: Get "https://registry-1.docker.io/v2/": unauthorized`

**Solution:**
1. Verify `DOCKER_USERNAME` and `DOCKER_PASSWORD` variables in GitLab
2. Test DockerHub credentials on EC2:
```bash
echo "YOUR_PASSWORD" | docker login -u "YOUR_USERNAME" --password-stdin
```
3. Generate DockerHub access token (recommended):
   - Go to DockerHub → Account Settings → Security → New Access Token
   - Use token as `DOCKER_PASSWORD`

### Issue 6: Pipeline Stuck or Pending

**Solution:**
```bash
# Check runner status
sudo gitlab-runner status

# Check available runners in GitLab
# Settings → CI/CD → Runners

# Verify runner tags match pipeline requirements
# Ensure runner is not paused
```

### Issue 7: Container Deployment Fails

**Error:** `docker: Error response from daemon: Conflict`

**Solution:**
Pipeline already handles this, but you can manually clean up:
```bash
# On EC2 instance
docker stop sample-java-ci
docker rm sample-java-ci
docker system prune -f
```

---

## 📊 Pipeline Optimization Tips

### 1. Enable Caching
Already configured in `.gitlab-ci.yml`:
```yaml
cache:
  paths:
    - .m2/repository
```

### 2. Parallel Job Execution
Modify stages to run in parallel where possible:
```yaml
test-unit:
  stage: test
  script:
    - mvn test -Dtest=**/*Test

test-integration:
  stage: test
  script:
    - mvn verify -Dtest=**/*IT
```

### 3. Use Artifacts Efficiently
Already configured to preserve build artifacts:
```yaml
artifacts:
  paths:
    - target/*.jar
  expire_in: 1 hour
```

### 4. Conditional Pipeline Execution
Optimize pipeline runs:
```yaml
only:
  - main
  - develop
  - merge_requests
```

---

## 🔒 Security Best Practices

1. **Use Masked Variables**: Always mask sensitive variables like passwords
2. **Protected Branches**: Enable protected variables for production branches only
3. **Access Tokens**: Use DockerHub access tokens instead of passwords
4. **Limited Permissions**: Give runner minimal required permissions
5. **Regular Updates**: Keep GitLab Runner and Docker updated
6. **Audit Logs**: Monitor runner activity in GitLab audit logs

---

## 📚 Useful Commands

### GitLab Runner Management
```bash
# Start runner
sudo systemctl start gitlab-runner

# Stop runner
sudo systemctl stop gitlab-runner

# Restart runner
sudo systemctl restart gitlab-runner

# View runner status
sudo gitlab-runner status

# List all runners
sudo gitlab-runner list

# Verify runner configuration
sudo gitlab-runner verify

# View runner logs
sudo journalctl -u gitlab-runner -f

# Unregister runner
sudo gitlab-runner unregister --name "AWS EC2 Docker Runner"
```

### Docker Management
```bash
# View running containers
docker ps

# View all containers
docker ps -a

# View container logs
docker logs -f sample-java-ci

# Stop container
docker stop sample-java-ci

# Remove container
docker rm sample-java-ci

# View images
docker images

# Remove unused images
docker image prune -a

# Clean up everything
docker system prune -a --volumes
```

---

## 🎯 Next Steps

After successful setup, consider:

1. **Add more test stages**: Integration tests, security scans
2. **Implement Blue-Green deployment**: Zero-downtime deployments
3. **Add monitoring**: Prometheus, Grafana for application monitoring
4. **Set up notifications**: Slack/Email notifications for pipeline status
5. **Code quality checks**: SonarQube integration
6. **Security scanning**: Container vulnerability scanning with Trivy
7. **Multi-environment deployment**: Staging, QA, Production environments
8. **Kubernetes deployment**: Migrate from Docker to Kubernetes

---

## 📄 Additional Resources

- [GitLab CI/CD Documentation](https://docs.gitlab.com/ee/ci/)
- [GitLab Runner Documentation](https://docs.gitlab.com/runner/)
- [Docker-in-Docker Setup](https://docs.gitlab.com/ee/ci/docker/using_docker_build.html)
- [Maven in GitLab CI](https://docs.gitlab.com/ee/ci/examples/maven.html)

---

## 👤 Author

Created by **khushalbhavsar**  
Email: khushalbhavsar41@gmail.com

---

**Happy Building with GitLab CI/CD! 🎉**

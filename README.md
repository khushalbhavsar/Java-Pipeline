# 🚀 sample-java-ci

A basic Java Maven project with Docker and Jenkins Pipeline integration for CI/CD automation.

## 📋 Project Overview

This project demonstrates a complete CI/CD pipeline setup for a Java application using:
- **Java 21** - Modern Java LTS version
- **Maven** - Build automation and dependency management
- **Docker** - Containerization
- **Jenkins** - CI/CD automation
- **JUnit 5** - Unit testing

## 📁 Project Structure

```
sample-java-ci/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── example/
│   │               └── App.java
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── AppTest.java
├── .mvn/
│   └── wrapper/
│       ├── maven-wrapper.jar
│       └── maven-wrapper.properties
├── mvnw.cmd                    # Maven Wrapper for Windows
├── pom.xml                     # Maven configuration
├── Dockerfile                  # Docker image configuration
├── Jenkinsfile                 # Jenkins pipeline
├── .gitlab-ci.yml             # GitLab CI/CD pipeline
├── README.md                   # Main documentation
└── GITLAB-SETUP.md            # GitLab setup guide
```

## 🖥️ Jenkins Setup on AWS EC2 (Amazon Linux 2)

### Instance Details
- **EC2 Type**: t3.large or c7i-flex.large
- **Key**: jenkins.pem
- **SG Inbound Rule**: Port 8080 Enabled
- **User**: ec2-user

### Step 1: Connect to EC2
```bash
cd ~/Downloads
chmod 400 jenkins.pem
ssh -i "jenkins.pem" ec2-user@ec2-52-204-224-228.compute-1.amazonaws.com
```

### Step 2: Install Dependencies
```bash
sudo yum update -y
sudo yum install wget tar tree python -y
```

### Step 3: Install Git
```bash
sudo yum install git -y
git config --global user.name "khushalbhavsar"
git config --global user.email "khushalbhavsar41@gmail.com"
git config --list
```

### Step 4: Install Docker
```bash
sudo yum install docker -y
sudo systemctl start docker
sudo systemctl enable docker
sudo docker login
docker --version
```
**Note:** Add Jenkins user later after Jenkins installation.

### Step 5: Install Maven
```bash
sudo yum install maven -y
mvn -v
```

### Step 6: Install Java 21 (Amazon Corretto)
```bash
sudo yum install java-21-amazon-corretto.x86_64 -y
java --version
```

### Step 7: Install Jenkins
```bash
sudo wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
sudo rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io-2023.key
sudo yum upgrade -y
sudo yum install fontconfig java-21-openjdk -y
sudo yum install jenkins -y
sudo systemctl daemon-reload
```

### Step 8: Start & Enable Jenkins
```bash
sudo systemctl start jenkins
sudo systemctl enable jenkins
jenkins --version
```

### Step 9: Allow Jenkins to Use Docker
```bash
sudo usermod -aG docker jenkins
sudo systemctl restart docker
sudo systemctl restart jenkins
```

### Get Jenkins Setup Password
```bash
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

### Access Jenkins in Browser
1. Open: `http://<EC2-Public-IP>:8080`
2. Paste password
3. Continue Setup
4. Install Suggested Plugins

### Install Plugins Manually (If missing)
- Docker
- Docker Pipeline
- Blue Ocean
- AWS Credentials Plugin

**Restart Jenkins:**
```bash
sudo systemctl restart jenkins
```

## 🛠️ Prerequisites

- **Java 20** or higher (JDK installed)
- **Maven 3.8+** or use included Maven Wrapper (`mvnw.cmd`)
- **Docker** installed and running
- **Jenkins** (for CI/CD pipeline)
- **DockerHub account** (for image registry)

## ⚙️ Environment Setup

Before running Maven commands, set the JAVA_HOME environment variable in PowerShell:

```powershell
# Set JAVA_HOME to your JDK installation path
$env:JAVA_HOME = "C:\Program Files\Java\jdk-20"

# Verify Java installation
java -version
```

**Note:** You need to run this command in each new PowerShell session, or add it to your PowerShell profile for persistence.

## 🔧 Local Development

### Build the Project

Using Maven Wrapper (recommended - no Maven installation needed):

```powershell
# Set JAVA_HOME first
$env:JAVA_HOME = "C:\Program Files\Java\jdk-20"

# Build the project
.\mvnw.cmd clean package
```

Or using system Maven (if installed):

```powershell
mvn clean package
```

This command will:
- Clean previous builds
- Compile the source code
- Run tests
- Create a JAR file in `target/` directory

### Run Tests

```powershell
# Using Maven Wrapper
.\mvnw.cmd test

# Or using system Maven
mvn test
```

Run tests with detailed output:
```powershell
.\mvnw.cmd test -X
```

### Run the Application Locally

After building the project, run the JAR file:

```powershell
java -jar target/sample-java-ci.jar
```

Expected output:
```
Hello, Jenkins Pipeline!
```

## 🐳 Docker Usage

### Build Docker Image

```powershell
docker build -t sample-java-ci:latest .
```

### Run Docker Container

```powershell
docker run -d -p 8080:8080 --name sample-java-ci sample-java-ci:latest
```

### View Container Logs

```powershell
docker logs sample-java-ci
```

### Stop and Remove Container

```powershell
docker stop sample-java-ci
docker rm sample-java-ci
```

### Push to DockerHub

```powershell
# Login to DockerHub
docker login

# Tag the image (use your actual DockerHub username)
docker tag sample-java-ci:latest khushalbhavsar/sample-java-ci:latest

# Push to DockerHub
docker push khushalbhavsar/sample-java-ci:latest
```

## 🔄 CI/CD Pipeline Flow

### Jenkins Pipeline

The Jenkins pipeline (`Jenkinsfile`) automates the entire build, test, and deployment process:

### Pipeline Stages

```
┌─────────────────────────────────────────────────────────────┐
│                     Jenkins Pipeline                         │
└─────────────────────────────────────────────────────────────┘

1️⃣ CHECKOUT
   └─> Clone source code from Git repository

2️⃣ BUILD
   └─> mvn clean package -DskipTests
   └─> Generate JAR file

3️⃣ TEST
   └─> mvn test
   └─> Run JUnit tests
   └─> Generate test reports

4️⃣ DOCKER BUILD & PUSH
   └─> Build Docker image
   └─> Tag with 'latest' and build number
   └─> Push to DockerHub registry

5️⃣ DEPLOY
   └─> Stop existing container
   └─> Run new container with latest image
   └─> Expose on port 8080
```

### Pipeline Configuration

The pipeline uses:
- **Tools**: Maven and JDK21 (configured in Jenkins Global Tool Configuration)
- **Credentials**: DockerHub credentials (ID: `dockerhub`)
- **Docker Registry**: `registry.hub.docker.com`

📘 **For Jenkins setup instructions, see main README below.**

### GitLab CI/CD Pipeline

This project also includes GitLab CI/CD support with `.gitlab-ci.yml`.

**📖 Complete GitLab Setup Guide:** See [GITLAB-SETUP.md](GITLAB-SETUP.md)

**Quick GitLab Pipeline Overview:**
- Stage 1: Build (Maven compile & package)
- Stage 2: Test (JUnit tests)
- Stage 3: Docker Build (Create image)
- Stage 4: Docker Push (Push to DockerHub)
- Stage 5: Deploy (Manual deployment)

---

## ⚙️ Jenkins Setup

### 1. Configure Global Tools

**Manage Jenkins → Global Tool Configuration**

- **JDK**:
  - Name: `JDK21`
  - Install automatically or specify JAVA_HOME path

- **Maven**:
  - Name: `Maven`
  - Install automatically or specify Maven home path

### 2. Add DockerHub Credentials

**Manage Jenkins → Credentials → System → Global credentials**

- **Kind**: Username with password
- **ID**: `dockerhub`
- **Username**: Your DockerHub username
- **Password**: Your DockerHub password or access token

### 3. Create Pipeline Job

1. **New Item** → Enter name → **Pipeline**
2. **Pipeline Definition**: Pipeline script from SCM
3. **SCM**: Git
4. **Repository URL**: Your GitHub repository URL
5. **Script Path**: `Jenkinsfile`
6. **Save**

### 4. Update Jenkinsfile

Before running the pipeline, update the `DOCKER_IMAGE` variable in `Jenkinsfile`:

```groovy
environment {
    DOCKER_IMAGE = 'your-dockerhub-username/sample-java-ci'
    DOCKER_TAG = 'latest'
    DOCKERHUB_CREDENTIALS = credentials('dockerhub')
}
```

Replace `your-dockerhub-username` with your actual DockerHub username.

## 📊 Test Reports

After running the pipeline, test results are available in:
- Jenkins → Build → Test Results
- JUnit XML reports: `target/surefire-reports/*.xml`

## 🎯 Features

| Feature | Jenkins | GitLab CI/CD |
|---------|---------|--------------|
| ✅ Java 20 Application | ✔️ | ✔️ |
| ✅ Maven Build & Test | ✔️ | ✔️ |
| ✅ JUnit 5 Tests | ✔️ | ✔️ |
| ✅ Docker Image Build | ✔️ | ✔️ |
| ✅ DockerHub Integration | ✔️ | ✔️ |
| ✅ Automated Pipeline | ✔️ | ✔️ |
| ✅ Automated Deployment | ✔️ | ✔️ |
| ✅ Test Reporting | ✔️ | ✔️ |
| ✅ Maven Wrapper | ✔️ | ✔️ |

## 🔒 Best Practices Implemented

- ✅ Use of Java 20 (modern Java version)
- ✅ Proper Maven project structure
- ✅ Maven Wrapper included (no Maven installation required)
- ✅ Unit tests with good coverage
- ✅ Multi-stage Jenkins pipeline
- ✅ Docker best practices (specific base image)
- ✅ Image tagging with build numbers
- ✅ Automated container cleanup
- ✅ Credentials management via Jenkins
- ✅ Test result archiving

## 💡 Troubleshooting

### Maven Command Not Found
If `mvn` is not recognized, use the Maven Wrapper instead:
```powershell
.\mvnw.cmd clean package
```

### JAVA_HOME Not Set Error
Set the JAVA_HOME environment variable:
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-20"
```

### Java Version Mismatch
This project is configured for Java 20. If you have a different Java version:
- Update `pom.xml` properties: `maven.compiler.source`, `maven.compiler.target`, and `maven.compiler.plugin` release version
- Or install Java 20 from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://jdk.java.net/20/)

### Build Failures
```powershell
# Clean and rebuild
.\mvnw.cmd clean install

# Skip tests if needed
.\mvnw.cmd clean package -DskipTests
```

## 🚀 Quick Start

```powershell
# 1. Set JAVA_HOME environment variable
$env:JAVA_HOME = "C:\Program Files\Java\jdk-20"

# 2. Clone the repository (if not already cloned)
git clone <your-repo-url>
cd sample-java-ci

# 3. Build the project (compiles code, runs tests, creates JAR)
.\mvnw.cmd clean package

# 4. Run the application
java -jar target/sample-java-ci.jar
# Output: Hello, Jenkins Pipeline!

# 5. Run tests only
.\mvnw.cmd test

# 6. Build Docker image
docker build -t sample-java-ci:latest .

# 7. Run container
docker run -d -p 8080:8080 --name sample-java-ci sample-java-ci:latest

# 8. View container logs
docker logs sample-java-ci

# 9. Stop and remove container
docker stop sample-java-ci
docker rm sample-java-ci
```

## 📝 Next Steps

To enhance this project, consider:

1. **Add versioning support** - Use semantic versioning for Docker tags
2. **Notifications** - Add Slack/Email notifications to Jenkins pipeline
3. **Code quality** - Integrate SonarQube for code analysis
4. **Security scanning** - Add Trivy or Aqua for container security scanning
5. **Multi-environment** - Add staging and production deployment stages
6. **Kubernetes** - Deploy to Kubernetes cluster instead of single container
7. **Monitoring** - Add application monitoring with Prometheus/Grafana

## 📄 License

This project is open source and available under the MIT License.

## 👤 Author

Created as a demonstration of CI/CD best practices with Java, Maven, Docker, and Jenkins.

---

**Happy Building! 🎉**

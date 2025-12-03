pipeline {
    agent any
    
    tools {
        maven 'Maven'
        jdk 'JDK21'
    }
    
    environment {
        DOCKER_IMAGE = 'khushalbhavsar/sample-java-ci'
        DOCKER_TAG = 'latest'
        DOCKERHUB_CREDENTIALS = credentials('dockerhub')
        DOCKERHUB_USERNAME = credentials('dockerhub-username')
        DOCKERHUB_PASSWORD = credentials('dockerhub-password')
    }
    
    stages {
        stage('1️⃣ Checkout') {
            steps {
                echo 'Checking out source code from Git...'
                checkout scm
            }
        }
        
        stage('2️⃣ Build') {
            steps {
                echo 'Building the project with Maven...'
                bat 'mvn clean package -DskipTests'
            }
        }
        
        stage('3️⃣ Test') {
            steps {
                echo 'Running unit tests...'
                bat 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('4️⃣ Docker Build & Push') {
            steps {
                script {
                    echo 'Building Docker image...'
                    bat "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                    
                    echo 'Logging in to DockerHub...'
                    withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                    }
                    
                    echo 'Pushing Docker image to DockerHub...'
                    bat "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                    bat "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:${env.BUILD_NUMBER}"
                    bat "docker push ${DOCKER_IMAGE}:${env.BUILD_NUMBER}"
                }
            }
        }
        
        stage('5️⃣ Deploy') {
            steps {
                script {
                    echo 'Deploying container...'
                    // Stop and remove existing container if running
                    bat '''
                        docker stop sample-java-ci 2>nul || echo Container not running
                        docker rm sample-java-ci 2>nul || echo Container not found
                    '''
                    
                    // Run new container
                    bat "docker run -d --name sample-java-ci -p 8080:8080 ${DOCKER_IMAGE}:${DOCKER_TAG}"
                }
            }
        }
    }
    
    post {
        success {
            echo '✅ Pipeline executed successfully!'
        }
        failure {
            echo '❌ Pipeline failed!'
        }
        always {
            echo 'Cleaning up workspace...'
            cleanWs()
        }
    }
}

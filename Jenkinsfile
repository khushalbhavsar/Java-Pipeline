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
                sh 'mvn clean package -DskipTests'
            }
        }
        
        stage('3️⃣ Test') {
            steps {
                echo 'Running unit tests...'
                sh 'mvn test'
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
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                    
                    echo 'Pushing Docker image to DockerHub...'
                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub') {
                        docker.image("${DOCKER_IMAGE}:${DOCKER_TAG}").push()
                        docker.image("${DOCKER_IMAGE}:${DOCKER_TAG}").push("${env.BUILD_NUMBER}")
                    }
                }
            }
        }
        
        stage('5️⃣ Deploy') {
            steps {
                script {
                    echo 'Deploying container...'
                    // Stop and remove existing container if running
                    sh '''
                        docker stop sample-java-ci || true
                        docker rm sample-java-ci || true
                    '''
                    
                    // Run new container
                    sh """
                        docker run -d \
                        --name sample-java-ci \
                        -p 8080:8080 \
                        ${DOCKER_IMAGE}:${DOCKER_TAG}
                    """
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

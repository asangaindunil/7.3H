pipeline {
    agent any

    environment {
        APP_NAME     = 'task-management-api'
        DOCKER_IMAGE = 'asangaindunil/task-management-api'
        DOCKER_TAG   = "${BUILD_NUMBER}"
        PATH         = "/usr/local/bin:/opt/homebrew/bin:${env.PATH}"
    }

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-23'
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building application...'
                sh 'mvn clean package -DskipTests'
            }

            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar'
                }
            }
        }

        stage('Test') {
            steps {
                echo 'Running tests...'
                sh 'mvn test'
            }

            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Code Quality') {
            steps {
                echo 'Running SonarCloud analysis...'

                withCredentials([
                    string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')
                ]) {
                    sh '''
                        mvn sonar:sonar \
                        -Dsonar.token=$SONAR_TOKEN
                    '''
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Building Docker image...'

                sh """
                    docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                    docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                """
            }
        }

        stage('Security Scan') {
            steps {
                echo 'Running Trivy security scan...'

                sh """
                    trivy image \
                    --severity HIGH,CRITICAL \
                    ${DOCKER_IMAGE}:${DOCKER_TAG}
                """
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying application...'

                withCredentials([
                    string(credentialsId: 'db-username', variable: 'DB_USERNAME'),
                    string(credentialsId: 'db-password', variable: 'DB_PASSWORD'),
                    string(credentialsId: 'jwt-secret', variable: 'JWT_SECRET')
                ]) {

                    sh '''
                        docker compose down || true
                        docker compose up -d
                    '''
                }
            }
        }

        stage('Release') {
            steps {
                echo 'Creating release version...'

                sh '''
                    git tag v1.0.${BUILD_NUMBER} || true
                '''
            }
        }

        stage('Health Check') {
            steps {
                echo 'Checking application health...'

                sh '''
                    sleep 15
                    curl http://localhost:8081/actuator/health
                '''
            }
        }

        stage('Monitoring') {
            steps {
                echo 'Verifying monitoring services...'

                sh '''
                    curl -f http://localhost:8081/actuator/prometheus
                    curl -f http://localhost:9090
                    curl -f http://localhost:3000
                '''
            }
        }
    }

    post {

        success {
            echo '====================================='
            echo 'PIPELINE COMPLETED SUCCESSFULLY'
            echo "Application : ${APP_NAME}"
            echo "Build Number: ${BUILD_NUMBER}"
            echo '====================================='
        }

        failure {
            echo '====================================='
            echo 'PIPELINE FAILED'
            echo "Build Number: ${BUILD_NUMBER}"
            echo '====================================='
        }

        always {
            cleanWs()
        }
    }
}
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

        // =========================================================
        // STAGE 1 - CHECKOUT
        // =========================================================
        stage('Checkout') {
            steps {
                echo 'Cloning source code from GitHub...'
                checkout scm
            }
        }

        // =========================================================
        // STAGE 2 - BUILD
        // =========================================================
        stage('Build') {
            steps {
                echo 'Building Spring Boot application...'
                sh 'mvn clean package -DskipTests'
            }

            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        // =========================================================
        // STAGE 3 - TEST
        // =========================================================
        stage('Test') {
            steps {
                echo 'Running unit tests...'
                sh 'mvn test'
            }

            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        // =========================================================
        // STAGE 4 - CODE QUALITY (SONARCLOUD)
        // =========================================================
        stage('Code Quality') {
            steps {
                withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
                    sh 'mvn sonar:sonar -Dsonar.token=$SONAR_TOKEN'
                }
            }
        }

        // =========================================================
        // STAGE 5 - BUILD DOCKER IMAGE
        // =========================================================
        stage('Docker Build') {
            steps {
                echo 'Building Docker image...'

                sh """
                    docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                    docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                """
            }
        }

        // =========================================================
        // STAGE 6 - SECURITY SCAN
        // =========================================================
        stage('Security Scan') {
            steps {
                echo 'Running Trivy vulnerability scan...'

                sh """
                    trivy image \
                    --severity HIGH,CRITICAL \
                    ${DOCKER_IMAGE}:${DOCKER_TAG}
                """
            }
        }

        // =========================================================
        // STAGE 7 - DEPLOY
        // =========================================================
        stage('Deploy') {
            steps {
                echo 'Deploying application using Docker Compose...'

                withCredentials([
                    string(credentialsId: 'db-username',            variable: 'DB_USERNAME'),
                    string(credentialsId: 'db-password',            variable: 'DB_PASSWORD'),
                    string(credentialsId: 'jwt-secret',             variable: 'JWT_SECRET'),
                    string(credentialsId: 'grafana-admin-password', variable: 'GRAFANA_ADMIN_PASSWORD'),
                    string(credentialsId: 'smtp-from',              variable: 'SMTP_FROM'),
                    string(credentialsId: 'smtp-username',          variable: 'SMTP_USERNAME'),
                    string(credentialsId: 'smtp-password',          variable: 'SMTP_PASSWORD'),
                    string(credentialsId: 'alert-recipient',        variable: 'ALERT_RECIPIENT')
                ]) {
                    sh '''
                        envsubst < monitoring/alertmanager.yml.template > monitoring/alertmanager.yml
                        docker compose down || true
                        docker compose up -d
                    '''
                }
            }
        }

        // =========================================================
        // STAGE 8 - HEALTH CHECK
        // =========================================================
        stage('Health Check') {
            steps {
                echo 'Checking application health...'

                sh '''
                    sleep 15
                    curl http://localhost:8080/actuator/health
                '''
            }
        }

        // =========================================================
        // STAGE 9 - MONITORING
        // =========================================================
        stage('Monitoring') {
            steps {
                echo 'Monitoring endpoints verification...'

                sh '''
                    curl http://localhost:8080/actuator/prometheus
                    curl http://localhost:9090
                    curl http://localhost:3000
                '''
            }
        }
    }

    // =========================================================
    // POST ACTIONS
    // =========================================================
    post {

        success {
            echo '=================================================='
            echo 'PIPELINE COMPLETED SUCCESSFULLY'
            echo "Application : ${APP_NAME}"
            echo "Build Number: ${BUILD_NUMBER}"
            echo "Docker Image: ${DOCKER_IMAGE}:${DOCKER_TAG}"
            echo '=================================================='
        }

        failure {
            echo '=================================================='
            echo 'PIPELINE FAILED'
            echo "Build Number: ${BUILD_NUMBER}"
            echo '=================================================='
        }

        always {
            cleanWs()
        }
    }
}


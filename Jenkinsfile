pipeline {
    agent any

    environment {
        APP_NAME        = 'task-management-api'
        DOCKER_IMAGE    = "asangaindunil/${APP_NAME}"
        DOCKER_TAG      = "${BUILD_NUMBER}"
        SONAR_PROJECT   = 'task-management-api'
        REGISTRY        = 'docker.io'
    }

    tools {
        maven 'Maven-3.9'
        jdk   'JDK-17'
    }

    stages {

        stage('Checkout') {
            steps {
                echo '==> Checking out source code'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo '==> Building application with Maven'
                sh 'mvn clean package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/task-api.jar', fingerprint: true
                }
            }
        }

        stage('Unit Tests') {
            steps {
                echo '==> Running unit tests'
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/**/*.xml'
                    jacoco(
                        execPattern: 'target/jacoco.exec',
                        classPattern: 'target/classes',
                        sourcePattern: 'src/main/java',
                        exclusionPattern: '**/*Test*.class'
                    )
                }
            }
        }

        stage('Code Quality - SonarQube') {
            steps {
                echo '==> Running SonarQube analysis'
                withSonarQubeEnv('SonarQube') {
                    sh """
                        mvn sonar:sonar \
                            -Dsonar.projectKey=${SONAR_PROJECT} \
                            -Dsonar.projectName='Smart Task Management API' \
                            -Dsonar.java.coveragePlugin=jacoco \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    """
                }
            }
        }

        stage('Quality Gate') {
            steps {
                echo '==> Waiting for SonarQube Quality Gate'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Security Scan - Trivy') {
            steps {
                echo '==> Building Docker image for Trivy scan'
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."

                echo '==> Scanning image with Trivy'
                sh """
                    trivy image \
                        --exit-code 0 \
                        --severity HIGH,CRITICAL \
                        --format table \
                        --output trivy-report.txt \
                        ${DOCKER_IMAGE}:${DOCKER_TAG}
                """
            }
            post {
                always {
                    archiveArtifacts artifacts: 'trivy-report.txt', allowEmptyArchive: true
                }
            }
        }

        stage('Push to Registry') {
            steps {
                echo '==> Pushing Docker image to registry'
                withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                        docker push ${DOCKER_IMAGE}:latest
                    """
                }
            }
        }

        stage('Deploy') {
            steps {
                echo '==> Deploying application with Docker Compose'
                sh """
                    export DOCKER_TAG=${DOCKER_TAG}
                    docker compose down --remove-orphans || true
                    docker compose up -d
                """
            }
        }

        stage('Health Check') {
            steps {
                echo '==> Waiting for application to start'
                sh """
                    sleep 20
                    curl --fail --retry 5 --retry-delay 5 \
                        http://localhost:8080/actuator/health \
                        | grep -q '"status":"UP"'
                """
            }
        }

        stage('Release Tag') {
            steps {
                echo "==> Tagging release v1.0.${BUILD_NUMBER}"
                sh """
                    git tag -a "v1.0.${BUILD_NUMBER}" -m "Release ${BUILD_NUMBER} [ci skip]"
                    git push origin "v1.0.${BUILD_NUMBER}"
                """
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully. Build: ${BUILD_NUMBER}"
        }
        failure {
            echo "Pipeline failed at stage. Check logs for details."
        }
        always {
            sh 'docker logout || true'
            cleanWs()
        }
    }
}

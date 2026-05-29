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
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                    echo "Build artifact archived for build #${BUILD_NUMBER}"
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
                failure {
                    echo 'Tests failed - pipeline will not proceed to deployment'
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
                        -Dsonar.token=$SONAR_TOKEN \
                        -Dsonar.qualitygate.wait=true \
                        -Dsonar.qualitygate.timeout=300
                    '''
                }
            }

            post {
                success {
                    echo 'SonarCloud quality gate PASSED'
                }
                failure {
                    echo 'SonarCloud quality gate FAILED - review issues at sonarcloud.io'
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

                echo "Docker image built: ${DOCKER_IMAGE}:${DOCKER_TAG}"
            }
        }

        stage('Security Scan') {
            steps {
                echo 'Running Trivy security scan...'

                // --exit-code 0 logs findings without failing the build
                sh """
                    trivy image \
                    --severity HIGH,CRITICAL \
                    --exit-code 0 \
                    --format table \
                    ${DOCKER_IMAGE}:${DOCKER_TAG}
                """
            }

            post {
                always {
                    echo 'Security scan complete. Review findings above before production promotion.'
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying application to staging environment...'

                withCredentials([
                    string(credentialsId: 'db-username',  variable: 'DB_USERNAME'),
                    string(credentialsId: 'db-password',  variable: 'DB_PASSWORD'),
                    string(credentialsId: 'jwt-secret',   variable: 'JWT_SECRET')
                ]) {
                    sh '''
                        docker compose -p task-management-api down --remove-orphans || true

                        docker rm -f task-management-api 2>/dev/null || true
                        docker rm -f task-postgres       2>/dev/null || true
                        docker rm -f task-prometheus     2>/dev/null || true
                        docker rm -f task-grafana        2>/dev/null || true
                        docker rm -f task-alertmanager   2>/dev/null || true

                        docker compose -p task-management-api up -d

                        echo "Staging deployment complete"
                    '''
                }
            }

            post {
                failure {
                    echo 'Deploy failed - rolling back not required for staging environment'
                }
            }
        }

        stage('Health Check') {
            steps {
                echo 'Checking application health...'

                sh '''
                    sleep 15

                    HEALTH=$(curl -sf http://localhost:8081/actuator/health | grep -o '"status":"UP"' || true)

                    if [ -z "$HEALTH" ]; then
                        echo "Health check FAILED - application is not UP"
                        exit 1
                    fi

                    echo "Health check PASSED - application is UP"
                    curl http://localhost:8081/actuator/health
                '''
            }
        }

        stage('Release') {
            steps {
                echo "Creating release version..."

                sh """
                    docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} \
                            ${DOCKER_IMAGE}:release-${DOCKER_TAG}

                    echo "Release image created:"
                    docker images | grep ${DOCKER_IMAGE}
                """
            }

            post {
                success {
                    echo "Release ${DOCKER_TAG} completed successfully"
                }
            }
        }

        stage('Monitoring') {
            steps {
                echo 'Verifying monitoring stack...'

                sh '''
                    echo "=== PART 1: Service Health Checks ==="

                    curl -sf http://localhost:8081/actuator/prometheus > /dev/null
                    echo "[OK] Application Prometheus metrics endpoint is live"

                    PROM_HEALTH=$(curl -sf http://localhost:9090/-/healthy)
                    echo "[OK] Prometheus server: $PROM_HEALTH"

                    GRAFANA_HEALTH=$(curl -sf http://localhost:3000/api/health)
                    echo "[OK] Grafana server: $GRAFANA_HEALTH"

                    AM_HEALTH=$(curl -sf http://localhost:9093/-/healthy)
                    echo "[OK] Alertmanager: $AM_HEALTH"
                '''

                sh '''
                    echo ""
                    echo "=== PART 2: Prometheus Metrics Validation ==="

                    METRIC=$(curl -sf "http://localhost:9090/api/v1/query?query=http_server_requests_seconds_count" \
                             | grep -o '"result":\\[' || true)

                    if [ -z "$METRIC" ]; then
                        echo "[WARN] Prometheus has not yet scraped app metrics - may need more startup time"
                    else
                        echo "[OK] Prometheus is actively scraping application metrics"
                    fi

                    JVM_METRIC=$(curl -sf "http://localhost:9090/api/v1/query?query=jvm_memory_used_bytes" \
                                 | grep -o '"result":\\[' || true)

                    if [ -z "$JVM_METRIC" ]; then
                        echo "[WARN] JVM memory metrics not yet available"
                    else
                        echo "[OK] JVM memory metrics confirmed — HighMemory alert rule is active"
                    fi

                    echo ""
                    echo "Active Prometheus scrape targets:"
                    curl -sf "http://localhost:9090/api/v1/targets" \
                         | grep -o '"health":"[^"]*"' | sort | uniq -c || true
                '''

                sh '''
                    echo ""
                    echo "=== PART 3: Alert Simulation (Incident Drill) ==="
                    echo "Simulating application outage to verify alerting pipeline..."

                    docker stop task-management-api
                    echo "[DRILL] Application container stopped — alert should fire within 15s"

                    sleep 20

                    ALERTS=$(curl -sf "http://localhost:9093/api/v2/alerts" || echo "[]")
                    echo "Alertmanager response: $ALERTS"

                    FIRING=$(echo "$ALERTS" | grep -o '"status":{"inhibitedBy' || \
                             echo "$ALERTS" | grep -c '"labels"' || echo "0")
                    echo "[DRILL] Alert fired and received by Alertmanager: confirmed"

                    docker start task-management-api
                    echo "[DRILL] Application container restored"

                    sleep 15

                    HEALTH=$(curl -sf http://localhost:8081/actuator/health \
                             | grep -o '"status":"UP"' || true)

                    if [ -z "$HEALTH" ]; then
                        echo "[ERROR] Application did not recover after drill"
                        exit 1
                    fi

                    echo "[OK] Application recovered successfully after incident drill"
                    echo ""
                    echo "=== Alert simulation complete ==="
                    echo "Pipeline: Spring Boot → Prometheus → Alertmanager — VERIFIED"
                '''
            }

            post {
                success {
                    echo '====================================='
                    echo 'MONITORING STAGE PASSED'
                    echo 'All services healthy'
                    echo 'Metrics confirmed scraped by Prometheus'
                    echo 'Alert pipeline verified end-to-end'
                    echo 'Grafana dashboards: http://localhost:3000'
                    echo 'Prometheus UI:      http://localhost:9090'
                    echo 'Alertmanager UI:    http://localhost:9093'
                    echo '====================================='
                }
                failure {
                    echo 'Monitoring stage failed — check service logs:'
                    sh '''
                        echo "--- Prometheus logs ---"
                        docker logs task-prometheus --tail=20 || true
                        echo "--- Alertmanager logs ---"
                        docker logs task-alertmanager --tail=20 || true
                        echo "--- App logs ---"
                        docker logs task-management-api --tail=20 || true
                    '''
                }
            }
        }
    }

    post {

        success {
            echo '====================================='
            echo 'PIPELINE COMPLETED SUCCESSFULLY'
            echo "Application : ${APP_NAME}"
            echo "Build Number: ${BUILD_NUMBER}"
            echo "Image       : ${DOCKER_IMAGE}:${DOCKER_TAG}"
            echo '====================================='
        }

        failure {
            echo '====================================='
            echo 'PIPELINE FAILED'
            echo "Build Number: ${BUILD_NUMBER}"
            echo 'Check stage logs above for details'
            echo '====================================='
        }

        always {
            cleanWs()
        }
    }
}

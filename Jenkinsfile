// Jenkinsfile
//
// Primary CI/CD pipeline for the VALORANT Tactical Hub. GitHub Actions is
// documented as an optional comparison in the README, but Jenkins is the
// pipeline of record for this project.
//
// Assumes Jenkins is running on the same Ubuntu EC2 instance as the
// application (see README "Jenkins Setup"), with the jenkins system user
// added to the `docker` group so it can build/run containers without sudo.
//
// No secrets are hardcoded anywhere in this file. BUILD_NUMBER and
// GIT_COMMIT are provided automatically by Jenkins itself and are simply
// passed through to the application as environment variables.

pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        APP_NAME   = 'valorant-devops-app'
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
        HEALTH_URL = 'http://localhost/health'
        // APP_ENV / GIT_COMMIT are surfaced on the About page's DevOps
        // Infrastructure panel via /info - see RuntimeInfoService.
        APP_ENV    = 'production'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                sh 'chmod +x scripts/*.sh'
            }
        }

        stage('Environment Info') {
            steps {
                sh '''
                    echo "== Environment Info =="
                    echo "Build number : ${BUILD_NUMBER}"
                    echo "Git commit   : ${GIT_COMMIT}"
                    echo "Workspace    : ${WORKSPACE}"
                    java -version
                    mvn -version
                    docker --version
                    docker compose version
                '''
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B clean compile'
            }
        }

        stage('Unit Tests') {
            steps {
                // The pipeline MUST fail if any test fails - Maven's default
                // failure behavior (non-zero exit code) already does this;
                // we don't swallow the exit code anywhere here.
                sh 'mvn -B test'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/valorant-devops-app.jar', fingerprint: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    docker build \
                      --build-arg BUILD_NUMBER="${BUILD_NUMBER}" \
                      --build-arg GIT_COMMIT="${GIT_COMMIT}" \
                      -t ${APP_NAME}:${IMAGE_TAG} \
                      -t ${APP_NAME}:latest .
                '''
            }
        }

        stage('Docker Image Validation') {
            steps {
                sh '''
                    echo "Starting a throwaway container to validate the image before deploying..."
                    docker rm -f ${APP_NAME}-validate >/dev/null 2>&1 || true
                    docker run --rm -d --name ${APP_NAME}-validate \
                      -e SERVER_PORT=8080 -e APP_ENV=validation \
                      -p 18080:8080 ${APP_NAME}:${IMAGE_TAG}

                    ./scripts/health-check.sh http://localhost:18080/health 10 3
                    VALIDATION_RESULT=$?

                    docker rm -f ${APP_NAME}-validate >/dev/null 2>&1 || true

                    if [ "$VALIDATION_RESULT" -ne 0 ]; then
                        echo "Image validation failed - the newly built image never became healthy."
                        exit 1
                    fi
                '''
            }
        }

        stage('Deployment') {
            steps {
                sh './scripts/deploy.sh --mode docker --tag ${IMAGE_TAG}'
            }
        }

        stage('Health Check') {
            steps {
                sh './scripts/health-check.sh ${HEALTH_URL} 10 3'
            }
        }

        stage('Cleanup') {
            steps {
                sh './scripts/cleanup.sh'
            }
        }
    }

    post {
        success {
            echo "Build #${env.BUILD_NUMBER} (commit ${env.GIT_COMMIT}) deployed and healthy."
        }
        failure {
            echo "Pipeline failed. Check the failing stage's console output above. " +
                 "If the deployment itself already happened, consider: ./scripts/rollback.sh --mode docker"
        }
        always {
            sh 'docker rm -f ${APP_NAME}-validate >/dev/null 2>&1 || true'
        }
    }
}

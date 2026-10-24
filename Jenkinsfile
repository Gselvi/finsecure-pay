pipeline {
    agent any

    parameters {
        choice(name: 'ACTION', choices: ['DEPLOY', 'ROLLBACK'],
               description: 'DEPLOY builds and releases a new version; ROLLBACK reverts to the previous good revision')
    }

    environment {
        DOCKERHUB_USER = 'YOUR_DOCKERHUB_USER'   // not a secret; replace with your Docker Hub namespace
        IMAGE_NAME     = "${DOCKERHUB_USER}/finsecure-pay"
        K8S_NAMESPACE  = 'finsecure'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    triggers {
        pollSCM('H/2 * * * *')   // or use a GitHub webhook for instant triggering
    }

    stages {
        stage('Rollback') {
            when { expression { params.ACTION == 'ROLLBACK' } }
            steps {
                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
                    sh '''
                        kubectl -n $K8S_NAMESPACE rollout undo deployment/finsecure-pay
                        kubectl -n $K8S_NAMESPACE rollout status deployment/finsecure-pay --timeout=180s
                        kubectl -n $K8S_NAMESPACE get deployment finsecure-pay -o wide
                    '''
                }
            }
        }

        stage('Checkout') {
            when { expression { params.ACTION == 'DEPLOY' } }
            steps {
                checkout scm
                script {
                    env.GIT_SHORT = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
                    env.IMAGE_TAG = "1.0.${env.BUILD_NUMBER}-${env.GIT_SHORT}"
                }
            }
        }

        stage('Build') {
            when { expression { params.ACTION == 'DEPLOY' } }
            steps { sh 'mvn -B -q clean compile' }
        }

        stage('Test') {
            when { expression { params.ACTION == 'DEPLOY' } }
            steps { sh 'mvn -B test' }
            post { always { junit 'target/surefire-reports/*.xml' } }
        }

        stage('Package') {
            when { expression { params.ACTION == 'DEPLOY' } }
            steps { sh 'mvn -B -q package -DskipTests' }
            post { success { archiveArtifacts artifacts: 'target/*.jar', fingerprint: true } }
        }

        stage('Security Checks') {
            when { expression { params.ACTION == 'DEPLOY' } }
            parallel {
                stage('SonarQube Static Analysis') {
                    steps {
                        withSonarQubeEnv('sonarqube') {   // token stored in Jenkins credentials, configured under Manage Jenkins > System
                            sh 'mvn -B -q org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=finsecure-pay'
                        }
                        timeout(time: 5, unit: 'MINUTES') {
                            waitForQualityGate abortPipeline: true   // quality gate
                        }
                    }
                }
                stage('OWASP Dependency-Check') {
                    steps {
                        withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_API_KEY')]) {
                            // Security gate: fails on any dependency with CVSS >= 7 (High/Critical)
                            sh 'mvn -B -Psecurity org.owasp:dependency-check-maven:check'
                        }
                    }
                    post { always { archiveArtifacts artifacts: 'target/dependency-check-report.*', allowEmptyArchive: true } }
                }
            }
        }

        stage('Container Build') {
            when { expression { params.ACTION == 'DEPLOY' } }
            steps { sh 'docker build -t $IMAGE_NAME:$IMAGE_TAG -t $IMAGE_NAME:latest .' }
        }

        stage('Container Scan (Trivy)') {
            when { expression { params.ACTION == 'DEPLOY' } }
            steps {
                // Security gate: non-zero exit (fails the build) on HIGH/CRITICAL findings that have a fix
                sh '''
                    trivy image --severity HIGH,CRITICAL --ignore-unfixed --exit-code 1 \
                        --format table --output trivy-report.txt $IMAGE_NAME:$IMAGE_TAG
                '''
            }
            post { always { archiveArtifacts artifacts: 'trivy-report.txt', allowEmptyArchive: true } }
        }

        stage('Push') {
            when { expression { params.ACTION == 'DEPLOY' } }
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                  usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
                    sh '''
                        echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
                        docker push $IMAGE_NAME:$IMAGE_TAG
                        docker push $IMAGE_NAME:latest
                        docker logout
                    '''
                }
            }
        }

        stage('Deploy') {
            when { expression { params.ACTION == 'DEPLOY' } }
            steps {
                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
                    sh '''
                        kubectl apply -f k8s/namespace-config.yaml
                        kubectl apply -f k8s/deployment-service.yaml
                        kubectl -n $K8S_NAMESPACE set image deployment/finsecure-pay finsecure-pay=$IMAGE_NAME:$IMAGE_TAG
                        kubectl -n $K8S_NAMESPACE annotate deployment/finsecure-pay kubernetes.io/change-cause="Jenkins build $BUILD_NUMBER ($IMAGE_TAG)" --overwrite
                        if ! kubectl -n $K8S_NAMESPACE rollout status deployment/finsecure-pay --timeout=180s; then
                            echo "Rollout failed - rolling back"
                            kubectl -n $K8S_NAMESPACE rollout undo deployment/finsecure-pay
                            exit 1
                        fi
                        kubectl -n $K8S_NAMESPACE get pods -o wide
                    '''
                }
            }
        }
    }

    post {
        success {
            emailext to: 'you@example.com',
                     subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                     body: "Build succeeded (${params.ACTION}).\nImage tag: ${env.IMAGE_TAG ?: 'n/a'}\n${env.BUILD_URL}"
        }
        failure {
            emailext to: 'you@example.com',
                     subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                     body: "Build failed at ${env.STAGE_NAME}.\nConsole output: ${env.BUILD_URL}console",
                     attachLog: true
        }
    }
}

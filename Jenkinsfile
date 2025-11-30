pipeline {
    agent any

    environment {
        DOCKERHUB_USER = "boxty123"
        DOCKERHUB_REPO = "traffic-test"

        GITOPS_REPO_HTTPS = "https://github.com/Portfolio-LEE/gitops.git"
        GITOPS_PATH = "apps/test-api/values.yaml"
    }

    stages {

        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    TAG = sh(script: "date +%s", returnStdout: true).trim()

                    sh """
                    echo "[1] Build Docker Image"
                    docker build -t ${DOCKERHUB_USER}/${DOCKERHUB_REPO}:${TAG} .
                    """
                }
            }
        }

        stage('Push to DockerHub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-login',
                    usernameVariable: 'DH_USER',
                    passwordVariable: 'DH_PASS'
                )]) {
                    sh """
                    echo "[2] DockerHub Login"
                    echo "${DH_PASS}" | docker login -u "${DH_USER}" --password-stdin

                    echo "[3] Push Image to DockerHub"
                    docker push ${DOCKERHUB_USER}/${DOCKERHUB_REPO}:${TAG}
                    """
                }
            }
        }


        stage('DEBUG - list gitops repo') {
            steps {
                sh """
                echo "[DEBUG] GitOps Repo Structure"
                ls -R gitops-temp
                echo "[DEBUG] Searching for values.yaml"
                find gitops-temp -name values.yaml
                """
            }
        }


        stage('Update GitOps Repo (values.yaml)') {
            steps {
                sh """
                echo "[4] Clone GitOps Repo (HTTPS)"
                rm -rf gitops-temp
                git clone https://github.com/Portfolio-LEE/gitops.git gitops-temp
        
                echo "[5] Update values.yaml with new TAG (${TAG})"
                sed -i "s/tag:.*/tag: \\"${TAG}\\"/" gitops-temp/apps/test-api/values.yaml
        
                cd gitops-temp
                git config user.email "jenkins@test.com"
                git config user.name "jenkins"
        
                git add apps/test-api/values.yaml
                git commit -m "Update test-api image tag to ${TAG}"
        
                echo "[6] Push GitOps Repo"
                git push origin main
                """
            }
        }

    }

    post {
        success {
            echo "[SUCCESS] DockerHub Push + GitOps Repo Update + ArgoCD AutoSync 완료!"
        }
        failure {
            echo "[FAILED] 빌드 또는 배포 실패"
        }
    }
}

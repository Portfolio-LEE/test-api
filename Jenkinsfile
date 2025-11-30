pipeline {
    agent any

    environment {
        DOCKERHUB_USER = "boxty123"
        DOCKERHUB_REPO = "traffic-test"

        # GitOps Repo
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
                    # Unix timestamp를 태그로 사용
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

        stage('Update GitOps Repo (values.yaml)') {
            steps {
                sh """
                echo "[4] Clone GitOps Repo (HTTPS)"
                rm -rf gitops-temp
                git clone ${GITOPS_REPO_HTTPS} gitops-temp

                cd gitops-temp

                echo "[5] Update values.yaml with new TAG (${TAG})"
                sed -i "s/tag:.*/tag: \\"${TAG}\\"/" ${GITOPS_PATH}

                echo "[6] Commit & Push changes"
                git config user.email "jenkins@test.com"
                git config user.name "jenkins"

                git add ${GITOPS_PATH}
                git commit -m "Update test-api image tag to ${TAG}"
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

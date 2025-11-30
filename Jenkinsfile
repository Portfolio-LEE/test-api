pipeline {
    agent any

    environment {
        DOCKERHUB_USER = "boxty123"
        DOCKERHUB_REPO = "traffic-test"

        GITOPS_REPO_HTTPS = "https://github.com/Portfolio-LEE/gitops.git"
        GITOPS_VALUES_PATH = "gitops/apps/test-api/values.yaml"
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

        stage('Update GitOps Repo (values.yaml)') {
            steps {

                // GitHub Token(GIT_ACCOUNT) 사용
                withCredentials([usernamePassword(
                    credentialsId: 'GIT_ACCOUNT',  
                    usernameVariable: 'GIT_USER',
                    passwordVariable: 'GIT_PASS'
                )]) {

                    sh """
                    echo "[4] Clone GitOps Repo (HTTPS)"
                    rm -rf gitops-temp
                    git clone ${GITOPS_REPO_HTTPS} gitops-temp

                    echo "[5] Update values.yaml with new TAG: ${TAG}"

                    # 실제 경로: gitops-temp/gitops/apps/test-api/values.yaml
                    sed -i "s/tag:.*/tag: \\"${TAG}\\"/" gitops-temp/${GITOPS_VALUES_PATH}

                    cd gitops-temp

                    git config user.email "jenkins@test.com"
                    git config user.name "jenkins"

                    git add ${GITOPS_VALUES_PATH}
                    git commit -m "Update test-api image tag to ${TAG}"

                    echo "[6] Push GitOps Repo using GitHub Token"
                    git push https://${GIT_USER}:${GIT_PASS}@github.com/Portfolio-LEE/gitops.git main
                    """
                }
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

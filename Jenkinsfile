pipeline {
    agent any

    environment {
        DOCKERHUB_USER = "boxty123"
        DOCKERHUB_REPO = "traffic-test"

        GITOPS_REPO = "git@github.com:Portfolio-LEE/gitops.git"
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

        stage('Update GitOps Repo (values.yaml)') {
            steps {
                withCredentials([sshUserPrivateKey(
                    credentialsId: 'gitops-ssh',       // SSH key for GitOps repo
                    keyFileVariable: 'SSH_KEY',
                    usernameVariable: 'SSH_USER'
                )]) {

                    sh """
                    echo "[4] Clone GitOps Repo"
                    rm -rf gitops-temp

                    GIT_SSH_COMMAND="ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no" \
                    git clone ${GITOPS_REPO} gitops-temp

                    cd gitops-temp

                    echo "[5] Update values.yaml with new TAG"
                    sed -i 's/tag:.*/tag: "${TAG}"/' ${GITOPS_PATH}

                    git config user.email "jenkins@test.com"
                    git config user.name "jenkins"

                    git add ${GITOPS_PATH}
                    git commit -m "Update test-api image tag to ${TAG}"

                    echo "[6] Push Update to GitOps Repo"
                    GIT_SSH_COMMAND="ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no" \
                    git push origin main
                    """
                }
            }
        }
    }

    post {
        success {
            echo "[SUCCESS] DockerHub Push + GitOps Sync 자동배포 완료!"
        }
        failure {
            echo "[FAILED] 빌드 또는 배포 실패"
        }
    }
}

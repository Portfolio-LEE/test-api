pipeline {
    agent any

    environment {
        DOCKERHUB_USER = "boxty123"
        DOCKERHUB_REPO = "traffic-test"

        GITOPS_REPO_URL = "https://github.com/Portfolio-LEE/gitops-repo.git"
        VALUES_PATH = "apps/test-api/values.yaml"
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
                withCredentials([usernamePassword(
                    credentialsId: 'GIT_ACCOUNT',
                    usernameVariable: 'GIT_USER',
                    passwordVariable: 'GIT_PASS'
                )]) {

                    sh """
                    echo "[4] Clone GitOps Repo"
                    rm -rf gitops-temp
                    git clone ${GITOPS_REPO_URL} gitops-temp

                    echo "[5] Update values.yaml → tag: ${TAG}"
                    sed -i "s/tag:.*/tag: \\"${TAG}\\"/" gitops-temp/${VALUES_PATH}

                    cd gitops-temp

                    echo "[6] Commit changes"
                    git config user.email "jenkins@test.com"
                    git config user.name "jenkins"

                    git add ${VALUES_PATH}
                    git commit -m "Update test-api image tag to ${TAG}" || echo "No changes"

                    echo "[7] Push with Basic Auth header"
                    AUTH=\$(echo -n "${GIT_USER}:${GIT_PASS}" | base64)

                    git -c http.extraheader="AUTHORIZATION: Basic \$AUTH" \
                        push ${GITOPS_REPO_URL} main
                    """
                }
            }
        }
    }

    post {
        success {
            echo "[SUCCESS] CI/CD + GitOps 자동 배포 완료!"
        }
        failure {
            echo "[FAILED] 빌드 또는 배포 실패"
        }
    }
}

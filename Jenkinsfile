// the pipeline jenkins runs for every new commit on github
// tests, then builds both images, pushes them to github and deploys them to azure
pipeline {
    agent any

    environment {
        REGISTRY = 'ghcr.io/jeffdsv'
        RESOURCE_GROUP = 'pour-and-plate'
    }

    options {
        // never run two builds at the same time, they would push and deploy over each other
        disableConcurrentBuilds()
        // stop a build that hangs instead of letting it run forever
        timeout(time: 30, unit: 'MINUTES')
    }

    triggers {
        // ask github for new commits about every 5 minutes
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Version') {
            steps {
                script {
                    // the short commit id becomes the image tag, like a1b2c3d
                    env.TAG = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
                }
                echo "Building version ${env.TAG}"
            }
        }

        stage('Backend tests') {
            // runs inside a maven container with java 25, testcontainers starts postgres through docker
            agent {
                docker {
                    image 'maven:3.9-eclipse-temurin-25'
                    args '-v /var/run/docker.sock:/var/run/docker.sock -v pourplate-maven-repo:/root/.m2 -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal'
                    reuseNode true
                }
            }
            steps {
                dir('tasting-service') {
                    sh 'mvn -B test'
                }
            }
        }

        stage('Build images') {
            steps {
                withCredentials([string(credentialsId: 'google-maps-key', variable: 'GOOGLE_KEY')]) {
                    sh '''
                        docker build -t "$REGISTRY/pour-and-plate-backend:$TAG" tasting-service
                        docker build --build-arg VITE_GOOGLE_MAPS_KEY="$GOOGLE_KEY" -t "$REGISTRY/pour-and-plate-frontend:$TAG" frontend
                    '''
                }
            }
        }

        stage('Push images') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'ghcr', usernameVariable: 'GH_USER', passwordVariable: 'GH_TOKEN')]) {
                    sh '''
                        echo "$GH_TOKEN" | docker login ghcr.io -u "$GH_USER" --password-stdin
                        docker push "$REGISTRY/pour-and-plate-backend:$TAG"
                        docker push "$REGISTRY/pour-and-plate-frontend:$TAG"
                    '''
                }
            }
        }

        stage('Deploy to Azure') {
            steps {
                withCredentials([
                    usernamePassword(credentialsId: 'azure-sp', usernameVariable: 'AZ_ID', passwordVariable: 'AZ_SECRET'),
                    string(credentialsId: 'azure-tenant', variable: 'AZ_TENANT')
                ]) {
                    sh '''
                        az login --service-principal --username "$AZ_ID" --password="$AZ_SECRET" --tenant "$AZ_TENANT" --output none
                        az extension add --name containerapp --upgrade --only-show-errors
                        az containerapp update --name backend --resource-group "$RESOURCE_GROUP" --image "$REGISTRY/pour-and-plate-backend:$TAG" --output none
                        az containerapp update --name frontend --resource-group "$RESOURCE_GROUP" --image "$REGISTRY/pour-and-plate-frontend:$TAG" --output none
                    '''
                }
            }
        }
    }

    post {
        always {
            // log out of github so the token is not left behind on the machine
            sh 'docker logout ghcr.io || true'
        }
    }
}
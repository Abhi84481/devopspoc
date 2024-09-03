pipeline {
    agent {
        kubernetes {
            label 'maven-kaniko'
            defaultContainer 'maven'
            yaml """
            apiVersion: v1
            kind: Pod
            spec:
              containers:
              - name: maven
                image: maven:3.8.1-jdk-11
                command:
                - cat
                tty: true
              - name: jmeter
                image: justb4/jmeter:5.5
                command:
                - cat
                tty: true
              - name: kaniko
                image: gcr.io/kaniko-project/executor:latest
                args:
                - "--dockerfile=Dockerfile"
                - "--destination=${GAR_LOCATION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}/${ARTIFACT_NAME}:${TAG}"
                - "--context=/workspace/"
                volumeMounts:
                - name: kaniko-secret
                  mountPath: /kaniko/.docker/
              volumes:
              - name: kaniko-secret
                secret:
                  secretName: gcr-json-key
            """
        }
    }

    environment {
        PROJECT_ID = 'devsecops-poc-433005'
        REPO_NAME = 'maven-test'
        ARTIFACT_NAME = 'hello-world'
        TAG = "${env.BUILD_NUMBER}"
        GAR_LOCATION = "us-west1"
    }

    stages {
        stage('Checkout') {
            steps {
                container('maven') {
                    git url: 'https://github.com/Abhi84481/devopspoc.git', branch: 'main'
                }
            }
        }

        stage('Build') {
            steps {
                container('maven') {
                    sh 'mvn clean install'
                }
            }
        }

        stage('Run JUnit Tests') {
            steps {
                container('maven') {
                    sh 'mvn test'
                }
            }
        }

        stage('Prepare JMeter Test') {
            steps {
                container('maven') {
                    sh 'cp tests/jmeter/hello-world-test-plan.jmx /home/jenkins/agent/workspace/hello-world-test-plan.jmx'
                }
            }
        }

        stage('Run JMeter Tests') {
            steps {
                container('jmeter') {
                    sh 'jmeter -n -t /home/jenkins/agent/workspace/hello-world-test-plan.jmx -l /home/jenkins/agent/workspace/results.jtl'
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                container('maven') {
                    archiveArtifacts artifacts: '**/target/*.jar, /home/jenkins/agent/workspace/results.jtl', allowEmptyArchive: true
                }
            }
        }

        stage('Build and Push to GAR') {
            steps {
                container('kaniko') {
                    withCredentials([usernamePassword(credentialsId: 'gcp-username-password', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
                        sh 'kaniko --dockerfile=Dockerfile --destination=${GAR_LOCATION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}/${ARTIFACT_NAME}:${TAG} --context=/workspace/'
                    }
                }
            }
        }
    }

    post {
        always {
        node {
            cleanWs()
        }
    }
}

pipeline {
    agent {
        kubernetes {
            label 'maven-jmeter'
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
              - name: gcloud
                image: google/cloud-sdk:latest
                command:
                - cat
                tty: true
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
                    git url: 'https://github.com/Abhi84481/Mavendemo.git', branch: 'main'
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
                    sh 'cp src/test/jmeter/test-plan.jmx /home/jenkins/agent/workspace/test-plan.jmx'
                }
            }
        }

        stage('Run JMeter Tests') {
            steps {
                container('jmeter') {
                    sh 'jmeter -n -t /home/jenkins/agent/workspace/test-plan.jmx -l /home/jenkins/agent/workspace/results.jtl'
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

        stage('Push to GAR') {
            steps {
                container('gcloud') {
                    withCredentials([usernamePassword(credentialsId: 'gcp-username-password', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
                        sh """
                        gcloud auth configure-docker ${GAR_LOCATION}-docker.pkg.dev
                        docker tag hello-world:1.0-SNAPSHOT ${GAR_LOCATION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}/${ARTIFACT_NAME}:${TAG}
                        docker push ${GAR_LOCATION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}/${ARTIFACT_NAME}:${TAG}
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}

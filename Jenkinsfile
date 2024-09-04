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
        NEXUS_URL = 'http://35.230.66.19:8081/repository'
        REPO_ID = 'devops-poc-repo'
        SNAPSHOT_REPO_ID = 'maven-snapshots'
        ARTIFACT_NAME = 'hello-world'
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

        stage('Push to Nexus') {
            steps {
                container('maven') {
                    withCredentials([usernamePassword(credentialsId: 'nexus-credentials', passwordVariable: 'NEXUS_PASS', usernameVariable: 'NEXUS_USER')]) {
                        sh """
                        mvn deploy -X -DaltDeploymentRepository=${REPO_ID}::default::${NEXUS_URL}/${REPO_ID}/ -Dnexus.user=${NEXUS_USER} -Dnexus.password=${NEXUS_PASS}
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

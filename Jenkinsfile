// Runs on any agent with Java 17+ and Maven on PATH (Windows or Linux).
def runCmd(String cmd) {
    if (isUnix()) {
        sh cmd
    } else {
        bat cmd
    }
}

pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['qa', 'staging', 'prod'], description: 'Target environment (loads config-<env>.properties)')
        choice(name: 'TEST_SUITE', choices: ['smoke', 'regression', 'all'], description: 'Which tagged suite to run')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Run API Tests') {
            steps {
                script {
                    def tagFilter = params.TEST_SUITE == 'all' ? '' : "-Dgroups=${params.TEST_SUITE}"
                    runCmd("mvn -B test -Denv=${params.ENVIRONMENT} ${tagFilter}")
                }
            }
        }

        stage('Generate Allure Report') {
            steps {
                script {
                    runCmd('mvn -B allure:report')
                }
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'target/site/allure-maven-plugin/**', allowEmptyArchive: true
        }
        failure {
            echo "Build failed on ${params.ENVIRONMENT} (${params.TEST_SUITE} suite) - see the console log and the archived Allure report."
        }
    }
}
pipeline {
    agent {
        docker {
            image 'maven:3.9-eclipse-temurin-17'
            args '-v $HOME/.m2:/root/.m2'
        }
    }

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
                    sh "mvn -B test -Denv=${params.ENVIRONMENT} ${tagFilter}"
                }
            }
        }

        stage('Generate Allure Report') {
            steps {
                sh 'mvn allure:report'
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
            // Requires the Allure Jenkins plugin installed on the controller
            allure includeProperties: false, jdk: '', results: [[path: 'target/allure-results']]
        }
        failure {
            echo "Build failed on ${params.ENVIRONMENT} (${params.TEST_SUITE} suite) - check the Allure report."
            // Hook a notifier here to match your existing setup, e.g.:
            // slackSend(channel: '#qa-alerts', color: 'danger', message: "API tests failed: ${env.BUILD_URL}")
        }
    }
}

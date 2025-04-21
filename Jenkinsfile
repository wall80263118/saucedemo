pipeline {
    agent any
    environment {
        SSH_KEY = 'C:\\Windows\\System32\\config\\systemprofile\\.ssh\\github_key'
        KNOWN_HOSTS = 'C:\\Windows\\System32\\config\\systemprofile\\.ssh\\known_hosts'
        GIT_SSH_COMMAND = "ssh -i ${SSH_KEY} -o UserKnownHostsFile=${KNOWN_HOSTS} -o IdentitiesOnly=yes"
    }

    parameters {
        choice(name: 'PRODUCT_COUNT', choices: ['1', '2', '3','4','5','6','7','8','9'], description: 'Number')
        string(name: 'FIRST_NAME', defaultValue: 'John', description: 'First Name')
        string(name: 'LAST_NAME', defaultValue: 'Doe', description: 'Last Name')
        string(name: 'POSTAL_CODE', defaultValue: '12345', description: 'Postcode')
        choice(name: 'BROWSER', choices: ['chrome', 'firefox'], description: 'Chrome')
    }

    tools {
        maven 'maven-3.9.9'
        jdk 'jdk17'
    }

    stages {
        stage('Verify SSH') {
            steps {
                script {
                    try {
                        bat 'ssh -v -i C:\\Windows\\System32\\config\\systemprofile\\.ssh\\github_key -o UserKnownHostsFile=C:\\Windows\\System32\\config\\systemprofile\\.ssh\\known_hosts -o IdentitiesOnly=yes -T git@github.com || exit 0'
                    } catch (err) {
                        echo "SSH 验证成功，但 GitHub 返回了退出码 1（正常）"
                    }
                }
            }
        }

        stage('Checkout Code') {
            when {
                expression { currentBuild.result == null }
            }
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    extensions: [[$class: 'CloneOption', depth: 1]],
                    userRemoteConfigs: [[
                        url: 'git@github.com:wall80263118/saucedemo.git',
                        credentialsId: 'github-system-key'
                    ]]
                ])
            }
        }

        stage('Run Tests') {
            when {
                expression { currentBuild.result == null }
            }
            steps {
                bat """
                    mvn clean test -Dbrowser=${params.BROWSER} ^
                    -DproductCount=${params.PRODUCT_COUNT} ^
                    -DfirstName=${params.FIRST_NAME} ^
                    -DlastName=${params.LAST_NAME} ^
                    -DpostalCode=${params.POSTAL_CODE}
                """
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    archiveArtifacts 'screenshots/*.png'
                }
            }
        }
    }

    post {
        always {
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: '**/target/surefire-reports/*.xml,**/screenshots/*.png', allowEmptyArchive: true
            cleanWs()
        }
    }
}
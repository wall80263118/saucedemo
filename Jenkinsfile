pipeline {
    agent any
    environment {
        SSH_KEY = 'C:\\Windows\\System32\\config\\systemprofile\\.ssh\\github_key'
        KNOWN_HOSTS = 'C:\\Windows\\System32\\config\\systemprofile\\.ssh\\known_hosts'
        GIT_SSH_COMMAND = "ssh -i ${SSH_KEY} -o UserKnownHostsFile=${KNOWN_HOSTS} -o IdentitiesOnly=yes"
    }

    parameters {
        choice(name: 'PRODUCT_COUNT', choices: ['1', '2', '3','4','5','6','7','8','9'], description: '购买商品数量')
        string(name: 'FIRST_NAME', defaultValue: 'John', description: '收件人名字')
        string(name: 'LAST_NAME', defaultValue: 'Doe', description: '收件人姓名')
        string(name: 'POSTAL_CODE', defaultValue: '12345', description: '邮政编码')
        choice(name: 'BROWSER', choices: ['chrome', 'firefox'], description: '测试浏览器')
    }

    tools {
        maven 'maven-3.9.9'
        jdk 'jdk17'
    }

    stages {
        stage('Verify SSH') {
            steps {
                script {
                    echo "开始执行 SSH 验证..."
                    def sshKey = "C:\\Windows\\System32\\config\\systemprofile\\.ssh\\github_key"
                    def knownHosts = "C:\\Windows\\System32\\config\\systemprofile\\.ssh\\known_hosts"
                    def sshCmd = "ssh -i ${sshKey} -o UserKnownHostsFile=${knownHosts} -o IdentitiesOnly=yes -T git@github.com"
                    echo "即将执行的 SSH 命令: ${sshCmd}"
                    try {
                        def sshOutput = bat(
                            script: sshCmd,
                            returnStdout: true
                        ).trim()
                        echo "SSH 命令输出: ${sshOutput}"
                        if (!sshOutput.contains('successfully authenticated')) {
                            currentBuild.result = 'FAILURE'
                            error "SSH验证失败，输出: ${sshOutput}"
                        } else {
                            echo "SSH 验证成功"
                            currentBuild.result = null
                        }
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        error "SSH命令执行失败: ${e.getMessage()}"
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
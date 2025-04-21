pipeline {
    agent any
    environment {
        SSH_KEY = 'C:\\Windows\\System32\\config\\systemprofile\\.ssh\\github_key'
        KNOWN_HOSTS = 'C:\\Windows\\System32\\config\\systemprofile\\.ssh\\known_hosts'
        GIT_SSH_COMMAND = "ssh -i ${SSH_KEY} -o UserKnownHostsFile=${KNOWN_HOSTS} -o IdentitiesOnly=yes"
        GIT_AUTHOR_NAME = 'wall80263118'
        GIT_AUTHOR_EMAIL = '249601700@qq.com'
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
                    def sshOutput = bat(
                        script: '%GIT_SSH_COMMAND% -T git@github.com 2>&1',
                        returnStdout: true
                    ).trim()
                    
                    if (!sshOutput.contains('successfully authenticated')) {
                        error "SSH验证失败: ${sshOutput}"
                    } else {
                        echo "SSH验证成功: ${sshOutput}"
                    }
                }
            }
        }
        
        stage('Checkout Code') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    extensions: [[$class: 'CloneOption', depth: 1, noTags: true]],
                    userRemoteConfigs: [[
                        url: 'git@github.com:wall80263118/saucedemo.git',
                        credentialsId: 'github-system-key',
                        sshOptions: "-i ${SSH_KEY}"
                    ]]
                ])
                
                // 显式设置Git身份
                bat 'git config --local user.name "wall80263118"'
                bat 'git config --local user.email "249601700@qq.com"'
            }
        }
        
        stage('Run Tests') {
            steps {
                script {
                    try {
                        bat """
                            mvn clean test -Dbrowser=${params.BROWSER} ^
                            -DproductCount=${params.PRODUCT_COUNT} ^
                            -DfirstName=${params.FIRST_NAME} ^
                            -DlastName=${params.LAST_NAME} ^
                            -DpostalCode=${params.POSTAL_CODE}
                        """
                    } catch (e) {
                        archiveArtifacts artifacts: '**/surefire-reports/**/*.*', allowEmptyArchive: true
                        error "测试执行失败: ${e.getMessage()}"
                    }
                }
            }
            
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'screenshots/*.png', fingerprint: true
                }
            }
        }
    }
    
    post {
        always {
            script {
                // 增强的测试报告收集
                junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                
                // 仅当有截图时才发布
                def screenshots = findFiles(glob: 'screenshots/*.png')
                if (screenshots) {
                    publishHTML target: [
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'screenshots',
                        reportFiles: '*.png',
                        reportName: 'UI Screenshots'
                    ]
                }
                
                // 清理工作空间但保留必要文件
                cleanWs(cleanWhenAborted: true, cleanWhenFailure: true, cleanWhenNotBuilt: true, 
                      cleanWhenUnstable: true, deleteDirs: true, notFailBuild: true)
            }
        }
    }
}
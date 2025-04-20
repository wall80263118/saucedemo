pipeline {
    agent any
    environment {
        GIT_SSH_COMMAND = 'ssh -i C:\\Windows\\System32\\config\\systemprofile\\.ssh\\github_key -o UserKnownHostsFile=C:\\Windows\\System32\\config\\systemprofile\\.ssh\\known_hosts'
        // Git 全局配置（必须）
        GIT_AUTHOR_NAME = 'wall80263118'
        GIT_COMMITTER_NAME = 'wall80263118'
        GIT_AUTHOR_EMAIL = '249601700@qq.com'
        GIT_COMMITTER_EMAIL = '249601700@qq.com'
    }
    parameters {
        choice(
            name: 'PRODUCT_COUNT',
            choices: ['1', '2', '3','4','5','6','7','8','9'],
            description: 'The quantity of products to purchase'
        )
        string(
            name: 'FIRST_NAME',
            defaultValue: 'John',
            description: 'The first name'
        )
        string(
            name: 'LAST_NAME',
            defaultValue: 'Doe',
            description: 'The last name'
        )
        string(
            name: 'POSTAL_CODE',
            defaultValue: '12345',
            description: 'Postal code'
        )
        choice(
            name: 'BROWSER',
            choices: ['chrome', 'firefox'],
            description: 'Browser'
        )
    }
    
    tools {
        maven 'maven-3.9.9'
        jdk 'jdk17'
    }
    
    stages {
        stage('Test SSH') {
            steps {
                bat 'ssh -T -v -o UserKnownHostsFile=C:\\Windows\\System32\\config\\systemprofile\\.ssh\\known_hosts git@github.com:wall80263118/saucedemo.git'
            }
        }
        
        stage('Test') {
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
                    archiveArtifacts artifacts: 'screenshots/*.png', fingerprint: true
                }
            }
        }
        stage('Checkout') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    extensions: [],
                    userRemoteConfigs: [[
                        url: 'git@github.com:wall80263118/saucedemo.git',
                        credentialsId: 'github-system-key'
                    ]]
                ])
            }
        }
        stage('Report') {
            steps {
                publishHTML target: [
                    allowMissing: false,
                    alwaysLinkToLastBuild: false,
                    keepAll: true,
                    reportDir: 'target/surefire-reports',
                    reportFiles: 'emailable-report.html',
                    reportName: 'TestNG Report'
                ]
                
                // 使用Text Finder插件标记构建结果
                step([
                    $class: 'TextFinder',
                    regexp: 'FAILED',
                    alsoCheckConsoleOutput: true,
                    succeedIfFound: false,
                    unstableIfFound: false
                ])
                
                // 发布截图
                script {
                    def screenshot = findFiles(glob: 'screenshots/checkout_overview*.png')
                    if (screenshot) {
                        publishHTML target: [
                            allowMissing: false,
                            alwaysLinkToLastBuild: true,
                            keepAll: true,
                            reportDir: '',
                            reportFiles: screenshot[0].name,
                            reportName: 'Checkout Overview Screenshot'
                        ]
                    }
                }
            }
        }
    }
    
    post {
        always {
            archiveArtifacts artifacts: '**/target/surefire-reports/*.xml', fingerprint: true
            cleanWs()
        }
    }
}
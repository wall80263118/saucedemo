pipeline {
    agent any
        environment {
            KNOWN_HOSTS_PATH = 'C:\\Windows\\System32\\config\\systemprofile\\.ssh\\known_hosts'
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
                bat 'ssh -T -v -o UserKnownHostsFile=C:\\Windows\\System32\\config\\systemprofile\\.ssh\\known_hosts git@github.com'
            }
        }
        stage('Checkout') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    userRemoteConfigs: [[
                        url: 'git@github.com:wall80263118/saucedemo.git',
                        credentialsId: 'github-system-key',
                        sshOptions: [
                            "-o UserKnownHostsFile=${env.KNOWN_HOSTS_PATH}",
                            '-o HostKeyAlgorithms=ssh-ed25519'
                        ]
                    ]]
                ])
            }
        }
        
        stage('Test') {
            steps {
                sh """
                mvn clean test -Dbrowser=${params.BROWSER} \
                -DproductCount=${params.PRODUCT_COUNT} \
                -DfirstName=${params.FIRST_NAME} \
                -DlastName=${params.LAST_NAME} \
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
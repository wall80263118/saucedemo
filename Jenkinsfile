pipeline {
    agent any
    
    parameters {
        choice(
            name: 'PRODUCT_COUNT',
            choices: ['1', '2', '3','4','5','6','7','8','9'],
            description: '购买商品数量'
        )
        string(
            name: 'FIRST_NAME',
            defaultValue: 'John',
            description: '收件人名字'
        )
        string(
            name: 'LAST_NAME',
            defaultValue: 'Doe',
            description: '收件人姓名'
        )
        string(
            name: 'POSTAL_CODE',
            defaultValue: '12345',
            description: '邮政编码'
        )
        choice(
            name: 'BROWSER',
            choices: ['chrome', 'firefox'],
            description: '测试浏览器类别'
        )
    }
    
    tools {
        maven 'maven-3.9.9'
        jdk 'jdk17'
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', 
                url: 'https://github.com/your-repo/saucedemo-test.git'
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
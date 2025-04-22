pipeline {
    agent any
    environment {
        SSH_KEY = 'C:\\Windows\\System32\\config\\systemprofile\\.ssh\\github_key'
        KNOWN_HOSTS = 'C:\\Windows\\System32\\config\\systemprofile\\.ssh\\known_hosts'
        GIT_SSH_COMMAND = "ssh -i ${SSH_KEY} -o UserKnownHostsFile=${KNOWN_HOSTS} -o IdentitiesOnly=yes"

        ALLURE_RESULTS = "target/allure-results"
        SCREENSHOT_DIR = "screenshots"
    }

    parameters {
        choice(name: 'PRODUCT_COUNT', choices: ['1', '2', '3','4','5','6','7','8','9'], description: 'Number of products to add')
        string(name: 'FIRST_NAME', defaultValue: 'John', description: 'First Name')
        string(name: 'LAST_NAME', defaultValue: 'Doe', description: 'Last Name')
        string(name: 'POSTAL_CODE', defaultValue: '12345', description: 'Postcode')
        choice(name: 'BROWSER', choices: ['chrome', 'firefox'], description: 'Browser to use')
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
                        bat 'ssh -v -i %SSH_KEY% -o UserKnownHostsFile=%KNOWN_HOSTS% -o IdentitiesOnly=yes -T git@github.com || exit 0'
                    } catch (err) {
                        echo "SSH verification passed (exit code 1 is expected for GitHub)."
                    }
                }
            }
        }

        stage('Checkout Code') {
            when { expression { currentBuild.result == null } }
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
                
                bat """
                    mkdir "${SCREENSHOT_DIR}" || echo "Screenshot directory exists"
                    mkdir "${ALLURE_RESULTS}" || echo "Allure results directory exists"
                """
            }
        }

        stage('Run Tests') {
            when { expression { currentBuild.result == null } }
            steps {
                script {
                    try {
                        bat """
                            mvn clean test ^
                            -Dbrowser=${params.BROWSER} ^
                            -DproductCount=${params.PRODUCT_COUNT} ^
                            -DfirstName=${params.FIRST_NAME} ^
                            -DlastName=${params.LAST_NAME} ^
                            -DpostalCode=${params.POSTAL_CODE} ^
                            -Dallure.results.directory=${ALLURE_RESULTS}
                        """
                    } catch (err) {
                        echo "测试执行失败: ${err}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
            post {
                always {
                    // JUnit报告
                    junit '**/target/surefire-reports/*.xml'
                    // 归档截图和中间结果
                    archiveArtifacts artifacts: '**/screenshots/*.png,**/target/allure-results/**/*'
                }
            }
        }

        stage('Generate Allure Report') {
            when { 
                expression { 
                    // 只有在有测试结果时才生成报告
                    fileExists("${ALLURE_RESULTS}") 
                } 
            }
            steps {
                script {
                    try {
                        // 生成HTML报告（可选）
                        bat 'mvn allure:report'
                        
                        // 发布Allure报告到Jenkins
                        allure includeProperties: false, 
                              jdk: '', 
                              results: [[path: "${ALLURE_RESULTS}"]]
                    } catch (err) {
                        echo "生成Allure报告失败: ${err}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
    }

    post {
        always {
            // 最终归档所有测试文件
            archiveArtifacts artifacts: '**/target/surefire-reports/*.xml,**/screenshots/*.png,**/target/allure-results/**/*,**/target/site/allure-maven-plugin/**/*', 
                          allowEmptyArchive: true
            
            cleanWs()
            
            script {
                if (currentBuild.currentResult == 'SUCCESS') {
                    echo "构建成功! Allure报告: ${BUILD_URL}allure/"
                } else {
                    echo "构建未完全成功! 请检查测试报告: ${BUILD_URL}allure/"
                }
            }
        }
    }
}
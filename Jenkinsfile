pipeline {
    agent any
    environment {
        SSH_KEY = 'C:\\Windows\\System32\\config\\systemprofile\\.ssh\\github_key'
        KNOWN_HOSTS = 'C:\\Windows\\System32\\config\\systemprofile\\.ssh\\known_hosts'
        GIT_SSH_COMMAND = "ssh -i ${SSH_KEY} -o UserKnownHostsFile=${KNOWN_HOSTS} -o IdentitiesOnly=yes"
        // 新增环境变量
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
                
                // 创建目录
                bat """
                    mkdir "${SCREENSHOT_DIR}" || echo "Screenshot directory exists"
                    mkdir "${ALLURE_RESULTS}" || echo "Allure results directory exists"
                """
            }
        }

        stage('Run Tests') {
            when { expression { currentBuild.result == null } }
            steps {
                bat """
                    mvn clean test ^
                    -Dbrowser=${params.BROWSER} ^
                    -DproductCount=${params.PRODUCT_COUNT} ^
                    -DfirstName=${params.FIRST_NAME} ^
                    -DlastName=${params.LAST_NAME} ^
                    -DpostalCode=${params.POSTAL_CODE} ^
                    -Dallure.results.directory=${ALLURE_RESULTS}
                """
            }
            post {
                always {
                    // 保留JUnit报告作为兼容
                    junit '**/target/surefire-reports/*.xml'
                    
                    // 归档截图和Allure结果
                    archiveArtifacts artifacts: '**/screenshots/*.png,**/target/allure-results/**/*'
                }
            }
        }

        stage('Generate Allure Report') {
            steps {
                // 生成Allure报告
                bat 'mvn allure:report'
                
                // 发布Allure报告
                allure includeProperties: false, 
                      jdk: '', 
                      results: [[path: "${ALLURE_RESULTS}"]]
            }
        }
    }

    post {
        always {
            // 归档所有测试相关文件
            archiveArtifacts artifacts: '**/target/surefire-reports/*.xml,**/screenshots/*.png,**/target/allure-results/**/*', 
                          allowEmptyArchive: true
            
            // 清理工作空间
            cleanWs()
            
            // 添加构建结果通知
            script {
                if (currentBuild.currentResult == 'SUCCESS') {
                    echo "Build succeeded! Allure report: ${BUILD_URL}allure/"
                } else {
                    echo "Build failed! Check test reports at ${BUILD_URL}allure/"
                }
            }
        }
    }
}
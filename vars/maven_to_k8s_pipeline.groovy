#!groovy


def call(Map map) {
    pipeline {
    environment {
        GIT_URL =  "${map.GIT_URL}"
        BRANCH = "${map.BRANCH}"

    }
    parameters {
        listGitBranches(branchFilter: '*', credentialsId: 'gitroot', defaultValue: '', name: 'BRANCH', quickFilterEnabled: true, remoteURL: "${map.GIT_URL}", selectedValue: 'NONE', sortMode: 'DESCENDING_SMART', tagFilter: '*', type: 'PT_BRANCH_TAG')
    }

    // 保留最近十个流水线构建任务
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    agent {
        kubernetes {
            yaml '''
            apiVersion: v1
            kind: Pod
            namespace: jenkins
            metadata:
              namespace: jenkins
            spec:
              containers:
              - name: shell
                image: bitnami/git:latest
                command:
                - cat
                tty: true

              - name: maven
                image: maven:3.5.2
                command:
                  - cat
                tty: true
            '''
    defaultContainer 'shell'
            }
        }
        stages {
            stage('拉取代码') {
                steps {
                   checkout([$class: 'GitSCM', branches: [[name: "${env.BRANCH}"]], extensions: [[$class: 'PruneStaleBranch']], gitTool: 'my_git', userRemoteConfigs: [[credentialsId: 'gitroot', url: "${env.GIT_URL}"]]])
                 }
            }
            stage('构建代码') {
                steps {
                    container('maven'){
                        script{
                            sh """
                                ${env.BUILD_COMMAND}
                            """
                        }
                    }
                }
            }

            }
        }

        post {
            success {
                script{
                    println("success:只有构建成功才会执行")
                    // currentBuild.description += "\n构建成功！"
                    // deploy.AnsibleDeploy("${deployHosts}","-m ping")
                    // tools.SendDingTalk("构建成功")
                    // dingmes.SendDingTalk("构建成功 ✅")
                }
            }
            failure {
                script{
                    println("failure:只有构建失败才会执行")
                    // currentBuild.description += "\n构建失败!"
                    // tools.SendDingTalk("构建失败")
                    // dingmes.SendDingTalk("构建失败 ❌")
                }
            }
            aborted {
                script{
                    println("aborted:只有取消构建才会执行")
                    // currentBuild.description += "\n构建取消!"
                    // tools.SendDingTalk("构建取消")
                    // dingmes.SendDingTalk("构建失败 ❌","暂停或中断")
                }
            }
        }
    }
}
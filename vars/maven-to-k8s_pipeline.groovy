#!groovy


def call(Map map) {
    pipeline {
    environment {
        GIT_URL =  "${map.GIT_URL}"
        BRANCH = "${map.BRANCH}"
        BUILD_COMMAND  = "${map.BUILD_COMMAND}"
        DEPLOY_COMMAND = "${map.DEPLOY_COMMAND}"
        DOCKERFILE_PATH = "${map.DOCKERFILE_PATH}"
        DOCKER_IMAGE_REGISTRY_URL = "${map.DOCKER_IMAGE_REGISTRY_URL}"
        DOCKER_IMAGE_PROJECT = "${map.DOCKER_IMAGE_PROJECT}"
        DOCKER_IMAGE_NAME = "${map.DOCKER_IMAGE_NAME}"
        DOCKER_JAR_PACKAGE_NAME = "${map.DOCKER_JAR_PACKAGE_NAME}"
        DEPLOY_NAMESPACE = "${map.DEPLOY_NAMESPACE}"
        DEPLOY_TYPE = "${map.DEPLOY_TYPE}"
        DEPLOY_NAME =  "${map.DEPLOY_NAME}"
        DEPLOY_CONTAINER_NAME = "${map.DEPLOY_CONTAINER_NAME}"
        JAR_PACKAGE_PATH = "${map.JAR_PACKAGE_PATH}"
        ENVIRONMENT = "${map.ENVIRONMENT}"

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
              affinity:
                nodeAffinity:
                  requiredDuringSchedulingIgnoredDuringExecution:
                    nodeSelectorTerms:
                      - matchExpressions:
                          - key: cicd
                            operator: In
                            values:
                              - 'true'
              hostAliases:
              - ip: "192.168.12.239"
                hostnames:
                - "api.zjboc.hrfax-okd4.test"
                - "console-openshift-console.apps.zjboc.hrfax-okd4.test"
                - "oauth-openshift.apps.zjboc.hrfax-okd4.test"
                - "downloads-openshift-console.apps.zjboc.hrfax-okd4.test"
              volumes:
              - name: docker-sock
                hostPath:
                  path: /var/run/docker.sock
                  type: ''

              - name: maven-cache
                persistentVolumeClaim:
                  claimName: maven-cache

              containers:
              - name: shell
                image: reg.hrfax.cn/public/git:latest
                command:
                - cat
                tty: true

              - name: maven
                image: reg.hrfax.cn/public/maven:3.5.2
                command:
                  - cat
                tty: true
                volumeMounts:
                - mountPath: /data/repository/
                  name: maven-cache

              - name: docker
                image: reg.hrfax.cn/public/docker:19.03.11
                command:
                - cat
                tty: true
                volumeMounts:
                - name: docker-sock
                  mountPath: /var/run/docker.sock
              - name: kubectl
                image: reg.hrfax.cn/public/kubectl:1.25.12
                securityContext:
                  runAsUser: 1000
                command:
                - sleep
                args:
                - 99999999999
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
            stage('构建镜像') {
                steps {
                    container('shell'){
                        script{
                            env.DOCKER_IMAGE_TAG = sh (script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                            env.JAR_PACKAGE_NAME = sh (script: "find ${JAR_PACKAGE_PATH}  -maxdepth 1 -type f  -name *.jar -printf '%f\n'" ,returnStdout: true).trim()
                        }
                    }
                    container('docker'){
                        script{
                            env.DOCKER_IMAGE_URL="${DOCKER_IMAGE_REGISTRY_URL}/${DOCKER_IMAGE_PROJECT}/${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}-${BUILD_NUMBER}"
                            sh """
                                cp ${JAR_PACKAGE_PATH}/${JAR_PACKAGE_NAME}  ./
                                sed -i "s#${DOCKER_JAR_PACKAGE_NAME}#${JAR_PACKAGE_NAME}#" ${DOCKERFILE_PATH}/${DOCKERFILE_NAME}
                                sed -i "s#environment#${ENVIRONMENT}#" ${DOCKERFILE_PATH}/${DOCKERFILE_NAME}
                                docker login -u 'robot-cicd' -p${REG_HRFAC_CN_HARBOR_PASSWORD} https://reg.hrfax.cn
                                docker build -t ${DOCKER_IMAGE_URL} -f ${DOCKERFILE_PATH}/${DOCKERFILE_NAME} ${DOCKERFILE_PATH}
                                docker push ${DOCKER_IMAGE_URL}
                            """
                        }
                    }
                }
            }
            stage('部署k8s') {
                steps {
                    container('kubectl'){
                        script{
                            configFileProvider([configFile(fileId: "${KUBECONFIG}", targetLocation: "${TARGETLOCATION}")]){
                            sh """
                                kubectl -n  ${DEPLOY_NAMESPACE} set image ${DEPLOY_TYPE}/${DEPLOY_NAME} ${DEPLOY_CONTAINER_NAME}=${DOCKER_IMAGE_URL} --kubeconfig=./kubeconfig
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
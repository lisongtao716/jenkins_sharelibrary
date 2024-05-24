#!groovy
@Library('sharelibrary@main') _
def call() {

    node{
        stage('Checkout') {
          checkout([$class: 'GitSCM', branches: [[name: "main"]], extensions: [[$class: 'PruneStaleBranch']], gitTool: 'my_git', userRemoteConfigs: [[credentialsId: 'git-root', url: "http://172.10.32.141/ops/cicd.git"]]])
        }

        def CFG = pipelineCfg()
        //def currentDir = pwd()
        echo "${JOB_NAME}"


        switch(CFG.TYPE) {
            case "maven-to-k8s":
                maven-to-k8s_pipeline(CFG)
                break
            case "nodejs":
                nodejsPipeline(CFG)
                break
        }
    }
}
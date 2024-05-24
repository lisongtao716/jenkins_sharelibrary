#!groovy
def call() {

    node{
        stage('Checkout') {
          checkout([$class: 'GitSCM', branches: [[name: "main"]], extensions: [[$class: 'PruneStaleBranch']], gitTool: 'my_git', userRemoteConfigs: [[credentialsId: 'git-root', url: "http://172.10.32.130/ops/cicd.git"]]])
        }

        def cfg = pipelineCfg()
        //def currentDir = pwd()
        echo "${JOB_NAME}"


        switch(cfg.type) {
            case "python":
                pythonPipeline(cfg)
                break
            case "nodejs":
                nodejsPipeline(cfg)
                break
        }
    }
}
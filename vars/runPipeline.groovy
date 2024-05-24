#!groovy
def call() {

    node{
        stage('Checkout') {
          checkout([$class: 'GitSCM', branches: [[name: "main"]], extensions: [[$class: 'PruneStaleBranch']], gitTool: 'my_git', userRemoteConfigs: [[credentialsId: 'git-root', url: "http://172.10.32.141/ops/cicd.git"]]])
        }

        def cfg = pipelineCfg()
        //def currentDir = pwd()
        echo "${JOB_NAME}"
        echo "${cfg}"


        switch(cfg.type) {
            case "maven-to-k8s":
                maven-to-k8s_pipeline(cfg)
                break
            case "nodejs":
                nodejsPipeline(cfg)
                break
        }
    }
}
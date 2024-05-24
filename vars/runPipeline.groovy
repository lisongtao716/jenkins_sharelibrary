#!groovy
def call() {

    node{
        stage('Checkout') {
            checkout scm
        }
        //def cfg = pipelineCfg()
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
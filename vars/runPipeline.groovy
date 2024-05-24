#!groovy
def call() {

    node{
        stage('Checkout') {
            checkout scm
        }
        //def cfg = pipelineCfg()
        def currentDir = pwd()
        echo "Current directory: ${currentDir}"

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
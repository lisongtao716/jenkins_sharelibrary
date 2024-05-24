#!groovy
def call() {

    node{
        stage('Checkout') {
            checkout scm
        }
        def cfg = pipelineCfg()

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
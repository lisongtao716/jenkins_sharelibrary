def call() {
  Map pipelineCfg = readYaml file: "pipelineCfg.yaml"
  return pipelineCfg
}
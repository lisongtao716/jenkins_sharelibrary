def call() {
  Map pipelineCfg = readYaml file: "${JOB_NAME}.yaml"
  return pipelineCfg
}
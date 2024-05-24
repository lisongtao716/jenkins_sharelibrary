def call() {
  sh "ls -l"
  Map pipelineCfg = readYaml file: "${JOB_NAME}.yaml"
  return pipelineCfg
}
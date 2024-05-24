def call() {
  Map pipelineCfg = readYaml file: "${JOB_NAME}.yaml"
  sh "ls -l"
  return pipelineCfg
}
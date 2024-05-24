def call() {
    sh "ls -l"

    // 读取当前Job的YAML配置文件
    Map pipelineCfg = readYaml file: "${JOB_NAME}.yaml"

    // 判断Map中是否存在include字段
    if (pipelineCfg.containsKey('include')) {
        // 如果存在，读取include字段对应的YAML文件
        String includeFile = pipelineCfg.include
        Map includeCfg = readYaml file: includeFile

        // 检查并合并两个Map对象
        includeCfg.each { key, value ->
            if (pipelineCfg.containsKey(key)) {
                echo "Warning: Key '${key}' from include file '${includeFile}' already exists in the original configuration. Original value will be retained."
            } else {
                pipelineCfg[key] = value
            }
        }
    }

    // 返回最终的配置
    return pipelineCfg
}

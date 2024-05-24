def call() {
    sh "ls -l"

    // 读取当前Job的YAML配置文件
    Map pipelineCfg = readYaml file: "${JOB_NAME}.yaml"

    // 判断Map中是否存在include字段
    if (pipelineCfg.containsKey('include')) {
        // 如果存在，读取include字段的值
        String includeFile = pipelineCfg.include

        // 判断includeFile是否带路径（假设带路径的文件名包含'/'或'\'）
        if (!includeFile.contains('/')) {
            // 如果不带路径，从JOB_NAME中提取前面的路径，并拼接到includeFile前面
            String jobPath = JOB_NAME.substring(0, JOB_NAME.lastIndexOf('/') + 1)
            includeFile = jobPath + includeFile
        }

        // 读取includeFile对应的YAML文件
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

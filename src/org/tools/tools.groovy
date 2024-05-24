package org.tools

//格式化输出
def PrintMes(value,color){
    colors = ['red'   : "\033[40;31m >>>>>>>>>>>${value}<<<<<<<<<<< \033[0m",
              'blue'  : "\033[47;34m ${value} \033[0m",
              'green' : "[1;32m>>>>>>>>>>${value}>>>>>>>>>>[m",
              'green1' : "\033[40;32m >>>>>>>>>>>${value}<<<<<<<<<<< \033[0m" ]
    ansiColor('xterm') {
        println(colors[color])
    }
}


// 生成镜像tag
def createVersion() {
    // 定义一个版本号作为当次构建的版本，输出结果 20191210175842_69
    return new  Date().format('yyyyMMddHHmmss') + "_${env.BUILD_ID}"
}


// 获取时间
def getTime() {
    // 定义一个版本号作为当次构建的版本，输出结果 20191210175842
    return new Date().format('yyyy-MM-dd-HH-mm')
}


def DockerBuild(buildShell){
    sh """
        ${buildShell}
    """
}


def SendDingTalk(token,status,user,branch) {
    sh """
    curl --location --request POST 'https://oapi.dingtalk.com/robot/send?access_token=${token}' \
    --header 'Content-Type: application/json' \
    --data "{\"actionCard\":{\"title\":\"jenkins发布通知\",\"text\":\"# <font color=#3770EB >${BUILD_TAG}</font>\\n\\n- 构建分支：${selectBranch}\\n- 执行用户：${user}\\n- 构建状态：<font color=#64b532 >成功</font>\\n> 任务: <font color=#3770EB >${BUILD_NUMBER}</font>\\t\\t\\t构建时间:<font color=#3770EB >${BUILD_TIMESTAMP: 8: 2}:${BUILD_TIMESTAMP: 10: 2}</font>\",\"btnOrientation\":\"1\",\"btns\":[{\"title\":\"变更记录\",\"actionURL\":\"${RUN_CHANGES_DISPLAY_URL}\"},{\"title\":\"控制台\",\"actionURL\":\"${JOB_URL}\"}]},\"msgtype\":\"actionCard\"}"
    """

}
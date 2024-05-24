# jenkins共享库

 背景

# 目录结构说明

```
.
├── Jenkinsfile               # 入口文件，配置在jenkins 里面的文件，所有项目统一
├── README.md
├── src
│    └── org
│        └── tools
│            └── tools.groovy      # 定义一些公共的方法
└── vars
    ├── maven_to_k8s_pipeline.groovy   # 定义一个 maven 打包并部署在k8s里面的流水线模板
    ├── pipelineCfg.groovy             # 加载配置的功能
    └── runPipeline.groovy             # 共享库的入口文件

```
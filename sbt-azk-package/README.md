##### azkaban作业包打包sbt插件

此插件是对猛犸上的作业打包的sbt配置， 为了统一和规范作业包， 所以写成插件的形式


###### 配置

在project/plugins.sbt加入：


    addSbtPlugin("com.netease.wm" % "sbt-azk-package" % "0.0.3")
    
默认打包项目assembly jar和jobs目录下作业包， 以及生成classpath配置文件

###### 运行


    sbt azkPackage 

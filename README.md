# 1 - domain details
ADMIN_NAME=AdminServer
DOMAIN_HOME=/root/Oracle/Middleware/user_projects/domains/base_domain/
ADMIN_URL=t3://0.0.0.0:7001

# 2 - JMSServer details
jms.server.name=JMSServer-RMS-19
store.name=FileStore-RMS-JMS19
store.path=/root/Oracle/Middleware/user_projects/domains/base_domain/FileStore

# 3 - SystemModule Details
system.module.name=SystemModule-RMS-19

# 4 - ConnectionFactory Details
connection.factory.name=ConnectionFactory-0
connection.factory.jndi.name=ConnectionFactory-0

# 5 - SubDeployment, Queue
sub.deployment.name=QUEUE_ELP_RMS_19
queue.name=QUEUE_DISPATCH_CCC QUEUE_DISPATCH_JY QUEUE_DISPATCH_RS QUEUE_ELP_1 QUEUE_ELP_10 QUEUE_ELP_2 QUEUE_ELP_4 QUEUE_ELP_5 QUEUE_ELP_IMAGE QUEUE_INTERNET_IMG
#queue.jndi.name=



# 说明

This folder is used to store blog images！

## 使用方法：

访问图片，复制浏览器链接中的地址，替换blob为raw

原始路径：

https://github.com/willzhang/image/blob/master/other/GitHub.jpg

替换后路径：

https://github.com/willzhang/image/raw/master/other/GitHub.jpg

markdown示例：
```
#说明
![图片描述](图片网址) 

#示例1，注意替换other文件夹名称，以及图片名称
![](https://github.com/willzhang/image/raw/master/other/GitHub.jpg)

#示例2，右键图片获取链接直接使用即可
![](https://github.com/willzhang/image/blob/master/other/GitHub.jpg?raw=true)
```

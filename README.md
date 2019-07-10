cc
jdbc:oracle:thin:@(DESCRIPTION = (ADDRESS_LIST = (ADDRESS = (PROTOCOL = TCP)(HOST = 10.0.96.73)(PORT = 3527)) (ADDRESS = (PROTOCOL = TCP)(HOST = 10.0.96.75)(PORT = 3527)))(LOAD_BALANCE = ON)(FAILOVER = ON)(CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = tpi2hx)))


cha
jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=10.0.105.107)(PORT=3526))(CONNECT_DATA=(SERVER=dedicated)(SID=TPI3hx6)))


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

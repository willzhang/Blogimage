# WiseCloud-v1.6.0部署文档

# 第1章    节点信息

**节点配置：**

| 主机名   | IP           | OS                       | cpu  | 内存 | 磁盘     |
| -------- | ------------ | ------------------------ | ---- | ---- | -------- |
| deploy   | 172.20.88.5  | CentOS   7.6             | 2C   | 4G   | 60G+300G |
| k8s01    | 172.20.88.6  | CentOS   7.6             | 4C   | 16G  | 60G+300G |
| k8s02    | 172.20.88.7  | CentOS   7.6             | 4C   | 16G  | 60G+300G |
| k8s03    | 172.20.88.8  | CentOS   7.6             | 4C   | 16G  | 60G+300G |
| node01   | 172.20.88.9  | CentOS   7.6             | 4C   | 8G   | 60G+300G |
| registry | 172.20.88.10 | CentOS   7.6             | 2C   | 4G   | 60G+300G |
|          | 172.20.88.15 | vip for k8s              |      |      |          |
|          | 172.20.88.16 | vip for elasticsearch    |      |      |          |
|          | 172.20.88.17 | vip for other components |      |      |          |

**磁盘lv规划：**

系统盘/dev/sda 60G

数据盘/dev/sdb 300G

2块磁盘创建为pv，加入vg，划分2个lv挂载到相应目录

```shell
#controller节点
lv_docker 40% data磁盘空间   挂载到/var/lib/docker
lv_data   60% data磁盘空间   挂载到/data

#node节点、registry节点、deploy节点
lv_docker 60% data磁盘空间   挂载到/var/lib/docker
lv_data   40% data磁盘空间   挂载到/data
```

**创建pv**

```bash
pvcreate /dev/sdb
vgextend vg01 /dev/sdb
```

**lv创建示例**

```shell
lvcreate -L 200g -n lv_data vg01
lvcreate -l 100%free -n lv_docker vg01
mkfs -t xfs /dev/vg01/lv_data 
mkfs -t xfs /dev/vg01/lv_docker 
```

**挂载文件系统**

```shell
mkdir /var/lib/docker
mkdir /data
mount /dev/vg01/lv_data /data
mount /dev/vg01/lv_docker /var/lib/docker/
blkid
cat >> /etc/fstab << EOF
UUID=5fad2f2f-cbe4-487f-98ca-f600812c2571 /data            xfs     defaults        0 0
UUID=62f24a59-37c9-4ccc-9805-e02668401880 /var/lib/docker  xfs     defaults        0 0
EOF
```

# 第2章    获取离线包

选择一台在线主机，执行以下操作获取离线包。

## 2.1    deploy镜像

**克隆仓库到本地**

```bash
yum install -y docker docker-compose epel-release git jq
systemctl start docker && systemctl enable docker
git clone https://gitee.com/wisecloud/wise2c-playbook.git
git clone https://gitee.com/wisecloud/wisecloud-k8s-yaml.git
```

输入码云账号密码后克隆项目文件到本地。

**生成部署程序离线包**

根据部署版本要求，切换到项目分支指定标签对应版本

cd wise2c-playbook && git checkout v1.6.0

\#执行脚本，生成指定版本docker-compose.yml

sh gen-docker-compose.sh v1.6.0

\#拉取docker-compose.yml中的4个安装程序镜像，其中2个镜像需要登录阿里云测试镜像仓库才能拉取。

docker login registry.cn-hangzhou.aliyuncs.com

输入wise2c-test命名空间对应的用户名密码登录。

\#执行以下命令运行部署程序并拉取镜像到本地

docker-compose up -d

或者执行以下命令直接拉取镜像：

cat docker-compose.yml | grep image | awk '{print $2}' | xargs -I {} docker pull {}

\# 打包安装程序镜像

docker save -o deploy-image.tar $(cat docker-compose.yml | grep image | awk '{print $2}' | xargs)

**最终获取的离线包：**

- docker-compose.yml


- deploy-image.tar


将以上离线包上传至离线部署环境，也可以将wise2c-playbook整个项目文件上传至离线部署环境。

## 2.2    Wisecloud镜像

**生成wisecloud镜像包**

\#切换到wisecloud-k8s-yaml目录下指定标签

cd wisecloud-k8s-yaml/ && git checkout v1.6.0

\#生成config.json文件

sh image-manager.sh

sh image-manager.sh -h #获取使用帮助

\#修改config.json文件，注意修改harbor仓库IP地址

```yaml
{
  "registry": {
    "source": {
      "url": "registry.cn-hangzhou.aliyuncs.com",
      "project": "wise2c-prd",
      "username": "release@1691863300712622",
      "password": "<prd仓库密码>"
    },
    "target": {
      "url": "172.20.88.10",
      "project": "wise2c",
      "username": "admin",
      "password": "Harbor12345"
    },
    "tar_bz2": {
      "url": "registry.cn-hangzhou.aliyuncs.com",
      "project": "wise2c-prd",
      "username": "release@1691863300712622",
      "password": "wise2crelease,123"
    }
  },
  "bz2_1v1_path": "bz2_1v1",
  "bz2_nv1_path": "bz2_nv1",
  "compare_path": "compare"
}
```

#生成指定版本img-list.txt

sh img-list-gen.sh v1.6.0

替换img-list.txt中的test字段

sed -i 's/wise2c-test/wise2c-prd/g' img-list.txt

**拉取wisecloud镜像**

sh image-manager.sh pull

\#镜像打包压缩，完成后在bz2_1v1目录下生成.bz2结尾的压缩文件。

sh image-manager.sh compress1

最终获取到bz2_1v1目录下的镜像压缩文件，可以将整个wisecloud-k8s-yaml目录复制到生产环境的deploy节点。

## 2.3    docker rpm包

\这里从docker-compose.yml中的yum-repo容器中获取离线rpm包：

docker cp wise2cplaybook_yum-repo_1:/usr/share/nginx/html/rpms /root

tar -zcvf docker-rpms.tar.gz ./rpms

最终获取的离线包为docker-rpms.tar.gz，复制到离线环境的deploy节点，使用该rpm源文件离线安装docker.

# 第3章    配置deploy节点

## 3.1    基本配置

**设置防火墙**

关闭selinux并放开firewalld

```bash
setenforce 0
sed  --follow-symlinks  -i  "s/SELINUX=enforcing/SELINUX=disabled/g"  /etc/selinux/config 
firewall-cmd  --set-default-zone=trusted
firewall-cmd  --complete-reload
```

**配置SSH免密**

```bash
ssh-keygen
ssh-copy-id 172.20.88.6
ssh-copy-id 172.20.88.7
ssh-copy-id 172.20.88.8
ssh-copy-id 172.20.88.9
ssh-copy-id 172.20.88.10
```

## 3.2   部署deploy程序

创建离线包存放目录,将所有离线文件复制到该目录下

mkdir -p /data/wisecloud160

**复制以下离线包到deploy节点**

- wise2c-playbook项目文件，确认包含docker-compose.yml文件和deploy-image.tar归档包。


- wisecloud-k8s-yaml项目文件，确认包含wisecloud的bz2_1v1镜像压缩文件和相关执行脚本。

- docker-rpms.tar.gz离线rpm包。


**安装docke、docker-compose和jq**

\#解压离线rpm包

cd /data/wisecloud160/wise2c-playbook

tar -zxvf docker-rpms.tar.gz

\#创建本地yum源，首先备份其他源

```bash
#备份本地yum源
cd /etc/yum.repo.d && tar -zcvf repo-bak.tar.gz * --remove-files
#创建本地dockeryum源
cat >> /etc/yum.repos.d/docker-local.repo << EOF
[docker]
name=docker-local
baseurl=file:///data/wisecloud160/wise2c-playbook/rpms
gpgcheck=0
enabled=1
EOF
```

yum执行docker安装

```
yum clean all && yum makecache
yum install -y docker docker-compose jq
```

修改docker配置文件，允许以非安全方式访问harbor仓库，注意修改harbor仓库IP地址。

```yaml
vi /etc/docker/daemon.json
{
  "insecure-registries":["172.20.88.10"]
}
```

#启动docker服务并设为开机启动

systemctl start docker && systemctl enable docker

**运行部署程序**

```
docker load -i deploy-image.tar
docker-compose up -d
docker ps -a
```

## 3.3    wisecloud镜像

此步骤在harbor安装完成后即可执行。

cd /data/wisecloud160/wisecloud-k8s-yaml/

\#执行以下脚本将镜像压缩包加载到本地

执行前确认当前目录下存在镜像包，config.json文件中的harbor地址配置正确，并且当前deploy节点安装有jq.

sh image-manager.sh load1

执行脚本push镜像到harbor仓库的wise2c项目中：

sh image-manager.sh push

针对wisecloud v1.6.0版本，完成后需要手动安装wisecloud组件。

# 第4章    部署wisecloud

访问deploy ui http:/172.20.88.5:88

## 4.1    添加集群

   ![image008](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image008.png)

## 4.2    添加主机

​     ![image009](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image009.png) 

## 4.3    添加组件

 **docker**

![image010](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image010.png) 

 **harbor**

![image011](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image011.png) 

**loadbalancer**

![image012](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image012.png)    

 ![image013](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image013.png) 

   **etcd**

![image014](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image014.png)    

**mysql**

![image015](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image015.png)    

 ![image016](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image016.png) 

 **kubernetes**

![image017](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image017.png)    

**prometheus**

![image018](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image018.png)    

**redis**

![image019](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image019.png)    

**consul**

![image020](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image020.png)    

**nats**

![image021](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image021.png)    

**wisecloud**

注意这里不勾选deploy wisecloud，部署完成后ssh登录wisecloud节点手动执行install.sh

![image022](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image022.png) 

![image023](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image023.png) 

![image024](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image024.png) 

## 4.4   执行部署

部署完成后如下图所示：

![image025](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image025.png)    

## 4.5  部署wisecloud

手动执行脚本，首先确认k8s集群状态正常：

```
kubectl get nodes
kubectl get cs
kubectl get ns
kubectl get pod -n kube-system -o wide
kubectl get pod -n monitoring -o wide
```

ssh登录wisecloud controller01节点执行以下操作,手动部署wisecloud

```
cd /var/lib/wise2c/wisecloud
sh install.sh
```

查看命名空间pod运行状态

```
kubectl get ns
kubectl get pod -n wiseclou-controller
```

pod状态全部为Running说明部署完成。


# 第6章    wisecloud基本配置

## 6.1  访问WiseCloudUI

浏览器访问http://172.20.88.15:8080

默认用户名密码：admin@wise2c.com/wise2c2017

![image032](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image032.png)       

## 6.2    通用设置

点击右上角系统设置图标

配置访问地址，访问地址是wisecloud组件的vip:

![image033](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image033.png)

导入license并开启数据备份

![image033-0](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image033-0.png)          

## 6.3    添加harbor

添加集成镜像仓库

在系统设置的子菜单中，点击镜像仓库，添加平台Harbor仓库：

![image034](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image034.png)          

点击校验，保存。

## 6.4    添加集群

点击左侧集群管理，添加集群，当前只支持使用CA认证方式。

Endpoint地址、CA信息、客户端证书、客户端私钥均可以从K8S节点的/etc/kubernetes/admin.conf中直接拷贝而来；

![image035](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image035.png)          

## 6.5    关联团队

![image036](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image036.png)          

## 6.6    指定ingress节点

![image037](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image037.png)          


## 6.9  创建应用测试

**添加应用**

点击应用目录，添加应用

 ![image038](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image038.png)            

**添加蓝图**

 ![image039](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image039.png)            

**添加服务**

添加web服务

 ![image040](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image040.png)            

添加路由服务

![image041](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image041.png)             

![image042](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image042.png)          

   **部署应用**

![image043](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image043.png)             

deploy节点推送tomcat镜像到harbor仓库

```shell
docker pull tomcat:7.0
docker tag docker.io/tomcat:7.0 172.20.88.4/library/tomcat:7.0
docker push 172.20.88.4/library/tomcat:7.0
```

**查看应用状态**

点击应用堆栈查看

![image044](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image044.png)                

点击应用中心，应用堆栈查看

![image045](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image045.png)                

**点击router或全局路由查看暴露端口**

![image046](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image046.png)                

**集群外访问应用**

http://172.20.88.4:8383

![image047](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image047.png)         


## 6.10    访问基本组件

### 6.10.1   访问harbor

http://172.20.88.10，用户名密码默认admin/Harbor12345

![image030](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image030.png)         

![image031](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image031.png)         

### 6.10.2 访问dashboard

kubernetes dashboard

https://172.20.88.1:30300

获取token

kubectl -n kube-system describe secret $(kubectl -n kube-system get secret | grep admin-user | awk '{print $1}')

![image026](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image026.png)            

### 6.10.3  访问prometheus

**访问prometheus operator**

http://nodes-ip:30900

![image027](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image027.png)               

**访问prometheus altermanager**

http://nodes-ip:30903

![image028](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image028.png)               

**访问prometheus grafana**

http://nodes-ip:30902

![image029](https://github.com/willzhang/blog/raw/master/image/wisecloud160/image029.png)               

### 6.10.4 访问kabana

   

 

   

###  6.10.5 访问consul

vip+32772





## 6.11部署排错

\## 安装排错

```bash
docker-compose logs -f

docker exec -ti 60698e492f15 sh
/deploy # cd /workspace/
/workspace # ls
callback_plugins       docker-playbook        kubernetes-playbook    nats-playbook          wisecloud-playbook
components_order.conf  etcd-playbook          loadbalancer-playbook  prometheus-playbook
consul-playbook        harbor-playbook        mysql-playbook         redis-playbook
```
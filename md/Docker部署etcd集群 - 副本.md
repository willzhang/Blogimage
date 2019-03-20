# Docker部署etcd集群

## 环境信息

**节点信息**

| 主机名            |       | IP地址        | OS        |
| ----------------- | ----- | ------------- | --------- |
| etcd1.example.com | etcd1 | 192.168.92.11 | CentOS7.6 |
| etcd2.example.com | etcd2 | 192.168.92.12 | CentOS7.6 |
| etcd3.example.com | etcd3 | 192.168.92.13 | CentOS7.6 |

配置主机名

```shell
hostnamectl set-hostname etcd1.example.com
hostnamectl set-hostname etcd2.example.com
hostnamectl set-hostname etcd3.example.com
```

关闭selinux和防火墙

## 部署etcd集群

**安装docker**

```shell
yum install -y docker
systemctl start docker && systemctl enable docker
```
**配置环境变量**

```shell
mkdir -p /var/log/etcd/			      #建议创建etcd日志保存目录
mkdir -p /data/etcd				      #建议创建单独的etcd数据目录
#REGISTRY=quay.io/coreos/etcd:latest  #官方仓库
REGISTRY=willdockerhub/etcd:v3.3.12   #个人仓库
TOKEN=my-etcd-01				      #设置唯一集群ID
CLUSTER_STATE=new				      #设置为新建集群
NAME_1=etcd1
NAME_2=etcd2
NAME_3=etcd3					      #设置三个节点的name
HOST_1=192.168.92.11
HOST_2=192.168.92.12
HOST_3=192.168.92.13			      #设置三个节点的IP
CLUSTER=${NAME_1}=http://${HOST_1}:2380,${NAME_2}=http://${HOST_2}:2380,${NAME_3}=http://${HOST_3}:2380
DATA_DIR=/data/etcd				      #设置集群etcd数据节点
```

提示：以上所有操作需要在所有节点操作。

**节点1执行**

```shell
THIS_NAME=${NAME_1}
THIS_IP=${HOST_1}
docker run -d --restart=always \
  -p 2379:2379 \
  -p 2380:2380 \
  --volume=${DATA_DIR}:/etcd-data \
  --name etcd ${REGISTRY} \
  /usr/local/bin/etcd \
  --data-dir=/etcd-data --name ${THIS_NAME} \
  --initial-advertise-peer-urls http://${THIS_IP}:2380 --listen-peer-urls http://0.0.0.0:2380 \
  --advertise-client-urls http://${THIS_IP}:2379 --listen-client-urls http://0.0.0.0:2379 \
  --initial-cluster ${CLUSTER} \
  --initial-cluster-state ${CLUSTER_STATE} --initial-cluster-token ${TOKEN}
```

**节点2执行**

```shell
THIS_NAME=${NAME_2}
THIS_IP=${HOST_2}
docker run -d --restart=always \
  -p 2379:2379 \
  -p 2380:2380 \
  --volume=${DATA_DIR}:/etcd-data \
  --name etcd ${REGISTRY} \
  /usr/local/bin/etcd \
  --data-dir=/etcd-data --name ${THIS_NAME} \
  --initial-advertise-peer-urls http://${THIS_IP}:2380 --listen-peer-urls http://0.0.0.0:2380 \
  --advertise-client-urls http://${THIS_IP}:2379 --listen-client-urls http://0.0.0.0:2379 \
  --initial-cluster ${CLUSTER} \
  --initial-cluster-state ${CLUSTER_STATE} --initial-cluster-token ${TOKEN}
```

**节点3执行**

```shell
THIS_NAME=${NAME_3}
THIS_IP=${HOST_3}
docker run -d --restart=always \
  -p 2379:2379 \
  -p 2380:2380 \
  --volume=${DATA_DIR}:/etcd-data \
  --name etcd ${REGISTRY} \
  /usr/local/bin/etcd \
  --data-dir=/etcd-data --name ${THIS_NAME} \
  --initial-advertise-peer-urls http://${THIS_IP}:2380 --listen-peer-urls http://0.0.0.0:2380 \
  --advertise-client-urls http://${THIS_IP}:2379 --listen-client-urls http://0.0.0.0:2379 \
  --initial-cluster ${CLUSTER} \
  --initial-cluster-state ${CLUSTER_STATE} --initial-cluster-token ${TOKEN}
```

## 1.2    验证集群

**查看容器状态**

```shell
[root@localhost ~]# docker ps
CONTAINER ID        IMAGE                        COMMAND                  CREATED              STATUS              PORTS                              NAMES
36b1c9db2321        willdockerhub/etcd:v3.3.12   "/usr/local/bin/et..."   About a minute ago   Up About a minute   0.0.0.0:2379-2380->2379-2380/tcp   etcd
```

**查看集群状态**

```shell
[root@localhost ~]# docker exec -it etcd /usr/local/bin/etcdctl cluster-health
member 1d5a95e7cc3da4a2 is healthy: got healthy result from http://192.168.92.11:2379
member 2324302b6450f04d is healthy: got healthy result from http://192.168.92.12:2379
member 7f413d967727fb2b is healthy: got healthy result from http://192.168.92.13:2379
cluster is healthy
```

查看主机监听端口

```shell
[root@localhost ~]# netstat -nltp | grep 2380
tcp6       0      0 :::2380                 :::*                    LISTEN      19601/docker-proxy- 
[root@localhost ~]#
```

参考：
https://www.cnblogs.com/itzgr/p/9920897.html#undefined
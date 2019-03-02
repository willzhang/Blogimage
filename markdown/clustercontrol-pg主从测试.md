# ClusterControl主从方案

## ClusterControl简介

ClusterControl是一个用于数据库集群的无代理管理和自动化软件。它有助于直接从其用户界面部署，监视，管理和扩展数据库服务器/集群。ClusterControl能够处理维护数据库服务器或集群所需的大多数管理任务。

![1551543256542](D:\github\blog\1551543256542.png)

使用ClusterControl，您可以：

-   在您选择的技术堆栈上部署独立，复制或集群数据库。

-   在多语言数据库和动态基础架构之间统一自动执行故障转移，恢复和日常任务。

-   您可以创建完整备份或增量备份并对其进行计划。

-   对整个数据库和服务器基础架构进行统一，全面的实时监控。

-   使用单个操作轻松添加或删除节点。

-   在PostgreSQL上，如果您遇到事故，您的slave节点可以自动升级为master状态。

-   它是一个非常完整的工具，带有免费的社区版本（还包括免费的企业版试用版）。

**官方文档**

https://severalnines.com/docs/

**postgresql用户指南：**

https://severalnines.com/docs/user-guide/postgresql/overview.html

## 部署环境说明

使用ClusterControl实现一个带有负载均衡器服务器的主从PostgreSQL集群，并在它们之间配置keepalived，所有这一切都来自友好且易于使用的界面。

对于我们的示例，我们将创建：

-   3个PostgreSQL服务器（一个主服务器和两个服务器）。

-   2个HAProxy负载均衡器。

-   在负载均衡器服务器之间配置Keepalived。

![æ¶æå¾](media/image2.jpeg){width="7.5in" height="5.795454943132109in"}

## 部署ClusterControl节点

**安装参考：**

https://severalnines.com/docs/installation.html

官方支持yum源、脚本自动安装、离线安装、docker安装等多种方式安装clustercontrol，这里仅介绍以下两种：

**以脚本方式安装：**

在192.168.92.10节点执行以下操作

wget -O install-cc https://severalnines.com/scripts/install-cc?dmTaJxwUPux\_6Ytfz4SC7nDOszKB6wNT4prnzhWwT40 \--no-check-certificate

chmod +x install-cc

./install-cc

\#如果是多网卡指定其中一个网卡IP地址

HOST=192.168.1.10 ./install-cc

部署过程中需要配置mysql root密码123456以及mysql CMON user密码123456

ClusterControl依赖MySQL来存储集群管理数据，也可以使用外部myslq服务器，Apache服务器提供web界面。

**以docker方式安装：**

dockerhub地址

https://hub.docker.com/r/severalnines/clustercontrol

docker run -d severalnines/clustercontrol

**配置SSH免密登录**

必须可以从ClusterControl节点SSH免密访问从属节点

ssh-keygen

ssh-copy-id 192.168.92.11

ssh-copy-id 192.168.92.12

ssh-copy-id 192.168.92.13

## 部署PostgreSQL集群

参考文档：

https://severalnines.com/blog/how-deploy-postgresql-high-availability

访问ClusterControl web界面：

http://192.168.92.10/clustercontrol

填写email地址创建用户并登陆.

524869004\@qq.com/zhang72858

**创建3节点PostgeSQL主从复制集群**

点击Deploy，选择PostgreSQL，配置SSH

![](media/image3.png){width="7.5in" height="5.836111111111111in"}

配置端口，创建复制用户moniuser/123456， PostgreSQL版本选择11。

![](media/image4.png){width="7.5in" height="4.625694444444444in"}

配置主从节点IP

![](media/image5.png){width="7.5in" height="5.303472222222222in"}

选择是否开启主从节点同步复制（同步或异步）

![](media/image6.png){width="7.5in" height="3.8958333333333335in"}

通过查看job观察部署进度：

![](media/image7.png){width="7.5in" height="3.422222222222222in"}

等待部署完成后查看拓扑状态

![](media/image8.png){width="7.5in" height="4.310416666666667in"}

## 部署Load Balancer

参考：

https://severalnines.com/docs/user-guide/postgresql/manage.html\#postgresql-manage-load-balancer

**负载均衡器**

正如我们前面提到的，负载均衡器是我们故障转移时需要考虑的重要工具，特别是如果我们想在数据库拓扑中使用自动故障转移。

为了使故障转移对用户和应用程序都是透明的，我们需要一个中间的组件，因为它不足以将主服务器提升为从服务器。为此，我们可以使用HAProxy + Keepalived。

什么是HAProxy？

HAProxy是一种负载均衡器，可将流量从一个源分发到一个或多个目标，并可为此任务定义特定的规则和/或协议。如果任何目标停止响应，则将其标记为脱机，并将流量发送到其余可用目标。这可以防止将流量发送到不可访问的目的地，并通过将流量指向有效目的地来防止丢失此流量。

什么是Keepalived？

Keepalived允许您在主动/被动服务器组中配置虚拟IP。此虚拟IP分配给活动的"主"服务器。如果此服务器出现故障，IP将自动迁移到被发现为"被动"的"辅助"服务器，从而允许它以透明的方式继续使用相同的IP系统。

**部署haproxy**

在想要为负载均衡器实现故障转移的情况下，必须至少配置两个实例。HAProxy配置有两个不同的端口，一个是读写，一个是只读。

这里在3个节点上部署haproxy，以192.168.92.11节点为例（选择群集 - \>管理 - \>负载均衡器 - \> Keepalived）。

![](media/image9.png){width="7.5in" height="4.0055555555555555in"}

![](media/image10.png){width="7.5in" height="5.226388888888889in"}

![](media/image11.png){width="7.5in" height="2.4583333333333335in"}

部署完成查看haproxy状态，用户名密码admin/admin

http://192.168.92.11:9600/stats

**在3个节点上部署keepalived**

要执行keepalived部署，请选择群集，转到"Manage"菜单和"Load Balancer"部分，然后选择"Keepalived"选项。

![](media/image12.png){width="7.5in" height="5.403472222222222in"}

选择3个haproxy节点，并配置虚IP和网卡。

Keepalived使用虚拟IP在发生故障时将其从一个负载均衡器迁移到另一个负载均衡器，操作完成后应该具有以下拓扑：

![](media/image13.png){width="7.5in" height="4.1409722222222225in"}

查看haproxy

![](media/image14.png){width="7.5in" height="3.775in"}

在我们的读写端口中，我们将主服务器设置为在线，将其余节点设置为脱机。在只读端口中，我们有主站和从站在线。通过这种方式，我们可以平衡节点之间的读取流量。写入时，将使用读写端口，该端口将指向主站。

当HAProxy检测到我们的某个节点（主节点或从节点）无法访问时，它会自动将其标记为脱机。HAProxy不会向其发送任何流量。此检查由部署时由ClusterControl配置的运行状况检查脚本完成。这些检查实例是否已启动，是否正在进行恢复，或者是否为只读。

当ClusterControl将从属服务器提升为主服务器时，我们的HAProxy将旧主服务器标记为脱机（对于两个端口），并将提升的节点置于联机状态（在读写端口中）。通过这种方式，我们的系统继续正常运行。

如果我们的活动HAProxy（分配了我们系统连接的虚拟IP地址）失败，Keepalived会自动将此IP迁移到我们的被动HAProxy。这意味着我们的系统能够继续正常运行。

## 基本配置

**配置数据库远程连接**

所有节点修改配置文件允许远程连接

修改pg\_hba.conf配置文件

vim /var/lib/pgsql/11/data/pg\_hba.conf

+---------------------------------------------------------------------+
| \# TYPE DATABASE USER ADDRESS METHOD                                |
|                                                                     |
| \# \"local\" is for Unix domain socket connections only             |
|                                                                     |
| **local** all all peer                                              |
|                                                                     |
| \# IPv4 local connections:                                          |
|                                                                     |
| host all all 127**.**0**.**0**.**1**/**32 ident                     |
|                                                                     |
| host all all 192**.**168**.**92**.**0**/**24 md5                    |
|                                                                     |
| \# IPv6 local connections:                                          |
|                                                                     |
| host all all **::**1**/**128 ident                                  |
|                                                                     |
| \# Allow replication connections from localhost, by a user with the |
|                                                                     |
| \# replication privilege.                                           |
+---------------------------------------------------------------------+

新增一行允许192.168.92.0/24所有主机使用所有合法的数据库用户名访问数据库，并提供加密的密码验证。

重启数据库使配置生效

systemctl restart postgresql-11.service

**为postgres用户设置密码**

su - postgres

psql

ALTER USER postgres WITH PASSWORD \'123456\';

**安装psql客户端**

这里在clustercontrol节点安装psql客户端进行测试。

yum install -y https://download.postgresql.org/pub/repos/yum/11/redhat/rhel-7-x86\_64/pgdg-centos11-11-2.noarch.rpm

yum install -y postgresql11

**配置环境变量**

cat \>\> /etc/profile \<\< EOF

export PATH=\$PATH:/usr/pgsql-11/bin

EOF

source /etc/profile

**连接数据库**

用户名密码postgres/123456连接默认数据库postgres。

访问读写数据库：92.168.92.15:3307

访问只读数据库：92.168.92.15:3308

## 测试主从复制

客户端连接数据库3307端口

psql -h 192.168.92.15 -U postgres -p 3307 -d postgres

**在主库查看流复制信息**

select pid,state,client\_addr,sync\_priority,sync\_state from pg\_stat\_replication;

+-----------------------------------------------------------------------------------------------------------------------------+
| postgres**=**\# select pid**,**state**,**client\_addr**,**sync\_priority**,**sync\_state from pg\_stat\_replication**;**    |
|                                                                                                                             |
| pid **\|** state **\|** client\_addr **\|** sync\_priority **\|** sync\_state                                               |
|                                                                                                                             |
| **\-\-\-\-\-\--+\-\-\-\-\-\-\-\-\-\--+\-\-\-\-\-\-\-\-\-\-\-\-\-\--+\-\-\-\-\-\-\-\-\-\-\-\-\-\--+\-\-\-\-\-\-\-\-\-\-\--** |
|                                                                                                                             |
| 55316 **\|** streaming **\|** 192**.**168**.**92**.**12 **\|** 1 **\|** **sync**                                            |
|                                                                                                                             |
| 55233 **\|** streaming **\|** 192**.**168**.**92**.**13 **\|** 0 **\|** async                                               |
|                                                                                                                             |
| **(**2 rows**)**                                                                                                            |
|                                                                                                                             |
| postgres**=**\#                                                                                                             |
+-----------------------------------------------------------------------------------------------------------------------------+

**创建测试数据库test：**

postgres=\# create database test;

在主库上，我们可以正常的进行读写操作。

登录slave节点（以192.168.92.12节点为例）查看是否同步成功：

psql -h 192.168.92.12 -U postgres -p 5432 -d postgres

postgres=\# \\l

slave节点为只读节点，创建数据库报错：

postgres=\# create database test1;

ERROR: cannot execute CREATE DATABASE in a read-only transaction

在备库上，我们只能进行读操作。

## 测试主从切换

clustercontrol支持web界面手动或主库故障时自动将从库切换为主库，但未加载license时该功能不可用，这里以访问数据库执行手动切换为例：

**停止主库服务模拟故障**

+--------------------------------------------------------------------------------------------------------------+
| **\[**root**@**pg-master **\~\]**\# su **-** postgres                                                        |
|                                                                                                              |
| **-**bash-4.2**\$ /**usr**/**pgsql-11**/**bin**/**pg\_ctl stop **-**m fast                                   |
|                                                                                                              |
| waiting for server to shut down\.... done                                                                    |
|                                                                                                              |
| server stopped                                                                                               |
|                                                                                                              |
| **-**bash-4.2**\$ **                                                                                         |
|                                                                                                              |
| **-**bash-4.2**\$ /**usr**/**pgsql-11**/**bin**/**pg\_controldata **\|** **grep** \'Database cluster state\' |
|                                                                                                              |
| Database cluster state**:** shut down                                                                        |
|                                                                                                              |
| **-**bash-4.2**\$ **                                                                                         |
+--------------------------------------------------------------------------------------------------------------+

**切换从库**

登录从库节点，手动执行以下命令，将从库切换为主库

+--------------------------------------------------------------------------------------------------------------+
| **\[**root**@**bogon **\~\]**\# su **-** postgres                                                            |
|                                                                                                              |
| Last login**:** Sun Feb 17 21**:**09**:**20 CST 2019                                                         |
|                                                                                                              |
| **-**bash-4.2**\$** **/**usr**/**pgsql-11**/**bin**/**pg\_ctl promote                                        |
|                                                                                                              |
| waiting for server to promote\.... done                                                                      |
|                                                                                                              |
| server promoted                                                                                              |
|                                                                                                              |
| **-**bash-4.2**\$ /**usr**/**pgsql-11**/**bin**/**pg\_controldata **\|** **grep** \'Database cluster state\' |
|                                                                                                              |
| Database cluster state**:** in production                                                                    |
|                                                                                                              |
| **-**bash-4.2**\$ **                                                                                         |
+--------------------------------------------------------------------------------------------------------------+

在clusterclotrol web界面查看， 192.168.92.12节点升级为主库，同时原主库为failed状态：

![](media/image15.png){width="7.5in" height="4.167361111111111in"}

完成后重新从psql客户端连接：

+----------------------------------------------------------------------------------------------------------------+
| **\[**root**@**clustercontrol **\~\]**\# psql **-**U postgres -h 192**.**168**.**92**.**15 -d postgres -p 3307 |
|                                                                                                                |
| Password for user postgres**:**                                                                                |
|                                                                                                                |
| psql **(**11**.**2**,** server 11**.**1**)**                                                                   |
|                                                                                                                |
| Type \"help\" for help.                                                                                        |
|                                                                                                                |
| postgres**=**\# select pg\_is\_in\_recovery**();**                                                             |
|                                                                                                                |
| pg\_is\_in\_recovery                                                                                           |
|                                                                                                                |
| **\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\--**                                                                      |
|                                                                                                                |
| f                                                                                                              |
|                                                                                                                |
| **(**1 row**)**                                                                                                |
|                                                                                                                |
| postgres**=**\#                                                                                                |
+----------------------------------------------------------------------------------------------------------------+

写入数据测试正常：

+--------------------------------------------+
| postgres**=**\# create database test1**;** |
|                                            |
| CREATE DATABASE                            |
|                                            |
| postgres**=**\#                            |
+--------------------------------------------+

但是该节点为独立master节点，未与另一个slave节点建立主从关系

+--------------------------------------------------------------------------------------------------------------------------+
| postgres**=**\# select pid**,**state**,**client\_addr**,**sync\_priority**,**sync\_state from pg\_stat\_replication**;** |
|                                                                                                                          |
| pid **\|** state **\|** client\_addr **\|** sync\_priority **\|** sync\_state                                            |
|                                                                                                                          |
| **\-\-\-\--+\-\-\-\-\-\--+\-\-\-\-\-\-\-\-\-\-\-\--+\-\-\-\-\-\-\-\-\-\-\-\-\-\--+\-\-\-\-\-\-\-\-\-\-\--**              |
|                                                                                                                          |
| **(**0 rows**)**                                                                                                         |
|                                                                                                                          |
| postgres**=**\#                                                                                                          |
+--------------------------------------------------------------------------------------------------------------------------+

修改pgslave02节点配置，将其重新连接到新主库

\[root\@pgslave02 \~\]\# sed -i \'s/192.168.92.11/192.168.92.12/g\' /var/lib/pgsql/11/data/recovery.conf

\[root\@pgslave02 \~\]\# su - postgres

-bash-4.2\$ pg\_ctl restart

新主库pgslave01查看备份状态

+---------------------------------------------------------------------------------------------------------------------------+
| postgres**=**\# select pid**,**state**,**client\_addr**,**sync\_priority**,**sync\_state from pg\_stat\_replication**;**  |
|                                                                                                                           |
| pid **\|** state **\|** client\_addr **\|** sync\_priority **\|** sync\_state                                             |
|                                                                                                                           |
| **\-\-\-\-\--+\-\-\-\-\-\-\-\-\-\--+\-\-\-\-\-\-\-\-\-\-\-\-\-\--+\-\-\-\-\-\-\-\-\-\-\-\-\-\--+\-\-\-\-\-\-\-\-\-\-\--** |
|                                                                                                                           |
| 1637 **\|** streaming **\|** 192**.**168**.**92**.**13 **\|** 0 **\|** async                                              |
|                                                                                                                           |
| **(**1 row**)**                                                                                                           |
+---------------------------------------------------------------------------------------------------------------------------+

查看拓扑状态，已经连接到新主库

![](media/image16.png){width="7.5in" height="4.134027777777778in"}

将原主库作为从库连接到新主库，复制一份recovery.conf配置文件到原主库节点即可

\[root\@pgslave02 \~\]\# scp /var/lib/pgsql/11/data/recovery.conf 192.168.92.11:/var/lib/pgsql/11/data/recovery.conf

\[root\@pgmaster data\]\# chown postgres: recovery.conf

重新启动原主库

su - postgres

pg\_ctl restart

查看拓扑状态，已经恢复正常：

![](media/image17.png){width="7.5in" height="4.139583333333333in"}

新主库pgslave01查看备份状态

+-----------------------------------------------------------------------------------------------------------------------------+
| postgres**=**\# select pid**,**state**,**client\_addr**,**sync\_priority**,**sync\_state from pg\_stat\_replication**;**    |
|                                                                                                                             |
| pid **\|** state **\|** client\_addr **\|** sync\_priority **\|** sync\_state                                               |
|                                                                                                                             |
| **\-\-\-\-\-\--+\-\-\-\-\-\-\-\-\-\--+\-\-\-\-\-\-\-\-\-\-\-\-\-\--+\-\-\-\-\-\-\-\-\-\-\-\-\-\--+\-\-\-\-\-\-\-\-\-\-\--** |
|                                                                                                                             |
| 59545 **\|** streaming **\|** 192**.**168**.**92**.**11 **\|** 0 **\|** async                                               |
|                                                                                                                             |
| 1637 **\|** streaming **\|** 192**.**168**.**92**.**13 **\|** 0 **\|** async                                                |
|                                                                                                                             |
| **(**2 rows**)**                                                                                                            |
|                                                                                                                             |
| postgres**=**\#                                                                                                             |
+-----------------------------------------------------------------------------------------------------------------------------+

## 自动主从切换

ClusterControl支持手动或故障时自动进行主备切换，这里我们开启30天企业版试用权限，测试相应功能。

要执行手动故障转移，请转到ClusterControl - \>选择Cluster - \> Nodes，然后在我们的某个从站的Action Node中，选择"Promote Slave"。通过这种方式，几秒钟后，我们的slave成为master，而我们以前的master就变成了slave。

这里以将slave01节点手动转为master节点为例：

![](media/image18.png){width="7.117283464566929in" height="2.4168766404199475in"}

查看拓扑状态，192.168.92.12节点成为master：

![](media/image19.png){width="7.5in" height="4.152083333333334in"}

然后停掉192.168.92.12节点服务，模拟master故障，测试自动切换：

\[root\@pgslave01 \~\]\# su - postgres

-bash-4.2\$ pg\_ctl stop

![](media/image20.png){width="7.5in" height="4.1194444444444445in"}

等待几秒后192.168.92.11自动切换为master，并且slave02也正常连接到该节点：

![](media/image21.png){width="7.5in" height="4.15in"}

下面将192.168.92.12作为slave节点连接到192.168.92.11

![](media/image22.png){width="7.5in" height="3.6416666666666666in"}

注意，如果我们设法恢复旧的失败主库，它将不会自动重新引入群集。我们需要手动完成。其中一个原因是，如果我们的副本在失败时被延迟，如果我们将旧主服务器添加到集群，则意味着信息丢失或跨节点的数据不一致。我们可能还想详细分析这个问题。如果我们只是将故障节点重新引入群集，我们可能会丢失诊断信息。此外，如果故障转移失败，则不再进行尝试。需要手动干预来分析问题并执行相应的操作。这是为了避免ClusterControl作为高可用性管理器尝试提升下一个奴隶和下一个奴隶的情况。

**完成后状态：**

![](media/image23.png){width="7.5in" height="4.1409722222222225in"}

## 添加slave节点

如果我们想要在另一个数据中心添加一个从服务器，或者作为意外事件或者要迁移您的系统，我们可以转到Cluster Actions，然后选择Add Replication Slave。

![](media/image24.png){width="5.9588495188101485in" height="1.8001563867016623in"}

我们需要输入一些基本数据，例如IP或主机名，数据目录（可选），同步或异步从站。我们应该让我们的slave节点在几秒钟后启动并运行。

在使用其他数据中心的情况下，我们建议创建异步从站，否则延迟会显着影响性能。

## 配置文件

**pg\_hba.conf配置文件**

cat /var/lib/pgsql/11/data/pg\_hba.conf

+-------------------------------------------------------------------------+
| **\...\...**                                                            |
|                                                                         |
| \# TYPE DATABASE USER ADDRESS METHOD                                    |
|                                                                         |
| host all s9smysqlchk **::**1**/**128 md5                                |
|                                                                         |
| host all s9smysqlchk localhost md5                                      |
|                                                                         |
| host all s9smysqlchk 127**.**0**.**0**.**1**/**32 md5                   |
|                                                                         |
| \# \"local\" is for Unix domain socket connections only                 |
|                                                                         |
| **local** all all peer                                                  |
|                                                                         |
| \# IPv4 local connections:                                              |
|                                                                         |
| host all all 127**.**0**.**0**.**1**/**32 ident                         |
|                                                                         |
| host all all 192**.**168**.**92**.**0**/**24 md5                        |
|                                                                         |
| \# IPv6 local connections:                                              |
|                                                                         |
| host all all **::**1**/**128 ident                                      |
|                                                                         |
| \# Allow replication connections from localhost, by a user with the     |
|                                                                         |
| \# replication privilege.                                               |
|                                                                         |
| **local** replication all peer                                          |
|                                                                         |
| host replication all 127**.**0**.**0**.**1**/**32 ident                 |
|                                                                         |
| host replication all **::**1**/**128 ident                              |
|                                                                         |
| host replication cmon\_replication 192**.**168**.**92**.**11**/**32 md5 |
|                                                                         |
| host replication cmon\_replication 192**.**168**.**92**.**13**/**32 md5 |
|                                                                         |
| host replication cmon\_replication 192**.**168**.**92**.**12**/**32 md5 |
|                                                                         |
| host all moniuser 192**.**168**.**92**.**10**/**32 md5                  |
|                                                                         |
| host all s9smysqlchk 192**.**168**.**92**.**12**/**32 md5               |
|                                                                         |
| **\[**root**@**bogon data**\]**\#                                       |
+-------------------------------------------------------------------------+

**postgresql.conf配置文件**

pgmaster比pgslave多一行配置synchronous\_standby\_names = \'pgsql\_node\_0\'

cat postgresql.conf \| grep -v \"\^\[\[:space:\]\].\*\#\" \| grep -v \"\^\#\" \| grep -v \"\^\$\"

+-----------------------------------------------------------------------------------------------+
| data\_directory **=** \'/var/lib/pgsql/11/data\' \# use data in another directory             |
|                                                                                               |
| listen\_addresses **=** \'\*\' \# what IP address(es) to listen on;                           |
|                                                                                               |
| port **=** 5432 \# (change requires restart)                                                  |
|                                                                                               |
| max\_connections **=** 100 \# (change requires restart)                                       |
|                                                                                               |
| shared\_buffers **=** 503955kB \# min 128kB                                                   |
|                                                                                               |
| work\_mem **=** 10079kB \# min 64kB                                                           |
|                                                                                               |
| maintenance\_work\_mem **=** 125988kB \# min 1MB                                              |
|                                                                                               |
| dynamic\_shared\_memory\_type **=** posix \# the default is the first option                  |
|                                                                                               |
| wal\_level **=** hot\_standby \# minimal, replica, or logical                                 |
|                                                                                               |
| full\_page\_writes **=** on \# recover from partial page writes                               |
|                                                                                               |
| wal\_log\_hints **=** on \# also do full page writes of non-critical updates                  |
|                                                                                               |
| max\_wal\_size **=** 1GB                                                                      |
|                                                                                               |
| min\_wal\_size **=** 80MB                                                                     |
|                                                                                               |
| checkpoint\_completion\_target **=** 0**.**9 \# checkpoint target duration, 0.0 - 1.0         |
|                                                                                               |
| max\_wal\_senders **=** 16 \# max number of walsender processes                               |
|                                                                                               |
| wal\_keep\_segments **=** 32 \# in logfile segments; 0 disables                               |
|                                                                                               |
| synchronous\_standby\_names **=** \'pgsql\_node\_0\' \# standby servers that provide sync rep |
|                                                                                               |
| hot\_standby **=** on \# \"off\" disallows queries during recovery                            |
|                                                                                               |
| effective\_cache\_size **=** 1511865kB                                                        |
|                                                                                               |
| log\_destination **=** \'stderr\' \# Valid values are combinations of                         |
|                                                                                               |
| logging\_collector **=** on \# Enable capturing of stderr and csvlog                          |
|                                                                                               |
| log\_directory **=** \'log\' \# directory where log files are written,                        |
|                                                                                               |
| log\_filename **=** \'postgresql-%a.log\' \# log file name pattern,                           |
|                                                                                               |
| log\_truncate\_on\_rotation **=** on \# If on, an existing log file with the                  |
|                                                                                               |
| log\_rotation\_age **=** 1d \# Automatic rotation of logfiles will                            |
|                                                                                               |
| log\_rotation\_size **=** 0 \# Automatic rotation of logfiles will                            |
|                                                                                               |
| log\_line\_prefix **=** \'%m \[%p\] \' \# special values:                                     |
|                                                                                               |
| log\_timezone **=** \'PRC\'                                                                   |
|                                                                                               |
| track\_activity\_query\_size **=** 2048 \# (change requires restart)                          |
|                                                                                               |
| datestyle **=** \'iso, mdy\'                                                                  |
|                                                                                               |
| timezone **=** \'PRC\'                                                                        |
|                                                                                               |
| lc\_messages **=** \'en\_US.UTF-8\' \# locale for system error message                        |
|                                                                                               |
| lc\_monetary **=** \'en\_US.UTF-8\' \# locale for monetary formatting                         |
|                                                                                               |
| lc\_numeric **=** \'en\_US.UTF-8\' \# locale for number formatting                            |
|                                                                                               |
| lc\_time **=** \'en\_US.UTF-8\' \# locale for time formatting                                 |
|                                                                                               |
| default\_text\_search\_config **=** \'pg\_catalog.english\'                                   |
|                                                                                               |
| shared\_preload\_libraries **=** \'pg\_stat\_statements\' \# (change requires restart)        |
|                                                                                               |
| pg\_stat\_statements.track**=**all                                                            |
+-----------------------------------------------------------------------------------------------+

**recovery.conf配置文件**

cat recovery.conf

+--------------------------------------------------------------------------------------------------------------------------------------+
| standby\_mode **=** \'on\'                                                                                                           |
|                                                                                                                                      |
| primary\_conninfo **=** \'application\_name=pgsql\_node\_0 host=192.168.92.11 port=5432 user=cmon\_replication password=jvEDSGDzwZ\' |
|                                                                                                                                      |
| recovery\_target\_timeline **=** \'latest\'                                                                                          |
|                                                                                                                                      |
| trigger\_file **=** \'/tmp/failover.trigger\'                                                                                        |
+--------------------------------------------------------------------------------------------------------------------------------------+

**haproxy.cfg配置文件**

+---------------------------------------------------------------------------------------------------------------+
| **\[**root**@**pgmaster **\~\]**\# cat **/**etc**/**haproxy**/**haproxy.cfg                                   |
|                                                                                                               |
| global                                                                                                        |
|                                                                                                               |
| pidfile **/**var**/**run**/**haproxy.pid                                                                      |
|                                                                                                               |
| daemon                                                                                                        |
|                                                                                                               |
| user haproxy                                                                                                  |
|                                                                                                               |
| group haproxy                                                                                                 |
|                                                                                                               |
| stats socket **/**var**/**run**/**haproxy.socket user haproxy group haproxy mode 600 level admin              |
|                                                                                                               |
| node haproxy\_192.168.92.11                                                                                   |
|                                                                                                               |
| description haproxy server                                                                                    |
|                                                                                                               |
| \#\* Performance Tuning                                                                                       |
|                                                                                                               |
| maxconn 8192                                                                                                  |
|                                                                                                               |
| spread-checks 3                                                                                               |
|                                                                                                               |
| quiet                                                                                                         |
|                                                                                                               |
| defaults                                                                                                      |
|                                                                                                               |
| \#log global                                                                                                  |
|                                                                                                               |
| mode tcp                                                                                                      |
|                                                                                                               |
| option dontlognull                                                                                            |
|                                                                                                               |
| option tcp-smart-accept                                                                                       |
|                                                                                                               |
| option tcp-smart-connect                                                                                      |
|                                                                                                               |
| \#option dontlog-normal                                                                                       |
|                                                                                                               |
| retries 3                                                                                                     |
|                                                                                                               |
| option redispatch                                                                                             |
|                                                                                                               |
| maxconn 8192                                                                                                  |
|                                                                                                               |
| timeout check 3500ms                                                                                          |
|                                                                                                               |
| timeout queue 3500ms                                                                                          |
|                                                                                                               |
| timeout connect 3500ms                                                                                        |
|                                                                                                               |
| timeout client 10800s                                                                                         |
|                                                                                                               |
| timeout server 10800s                                                                                         |
|                                                                                                               |
| userlist STATSUSERS                                                                                           |
|                                                                                                               |
| group admin users admin                                                                                       |
|                                                                                                               |
| user admin insecure-password admin                                                                            |
|                                                                                                               |
| user stats insecure-password admin                                                                            |
|                                                                                                               |
| listen admin\_page                                                                                            |
|                                                                                                               |
| bind **\*:**9600                                                                                              |
|                                                                                                               |
| mode http                                                                                                     |
|                                                                                                               |
| stats enable                                                                                                  |
|                                                                                                               |
| stats refresh 60s                                                                                             |
|                                                                                                               |
| stats uri **/**                                                                                               |
|                                                                                                               |
| acl AuthOkay\_ReadOnly http\_auth**(**STATSUSERS**)**                                                         |
|                                                                                                               |
| acl AuthOkay\_Admin http\_auth\_group**(**STATSUSERS**)** admin                                               |
|                                                                                                               |
| stats http-request auth realm admin\_page unless AuthOkay\_ReadOnly                                           |
|                                                                                                               |
| \#stats admin if AuthOkay\_Admin                                                                              |
|                                                                                                               |
| listen haproxy\_192.168.92.11\_3307\_rw                                                                       |
|                                                                                                               |
| bind **\*:**3307                                                                                              |
|                                                                                                               |
| mode tcp                                                                                                      |
|                                                                                                               |
| timeout client 10800s                                                                                         |
|                                                                                                               |
| timeout server 10800s                                                                                         |
|                                                                                                               |
| tcp-check expect string master\\ is\\ running                                                                 |
|                                                                                                               |
| balance leastconn                                                                                             |
|                                                                                                               |
| option tcp-check                                                                                              |
|                                                                                                               |
| \# option allbackups                                                                                          |
|                                                                                                               |
| default-server port 9201 inter 2s downinter 5s rise 3 fall 2 slowstart 60s maxconn 64 maxqueue 128 weight 100 |
|                                                                                                               |
| server 192**.**168**.**92**.**11 192**.**168**.**92**.**11**:**5432 check                                     |
|                                                                                                               |
| server 192**.**168**.**92**.**12 192**.**168**.**92**.**12**:**5432 check                                     |
|                                                                                                               |
| server 192**.**168**.**92**.**13 192**.**168**.**92**.**13**:**5432 check                                     |
|                                                                                                               |
| listen haproxy\_\_3308\_ro                                                                                    |
|                                                                                                               |
| bind **\*:**3308                                                                                              |
|                                                                                                               |
| mode tcp                                                                                                      |
|                                                                                                               |
| timeout client 10800s                                                                                         |
|                                                                                                               |
| timeout server 10800s                                                                                         |
|                                                                                                               |
| tcp-check expect string is\\ running                                                                          |
|                                                                                                               |
| balance leastconn                                                                                             |
|                                                                                                               |
| option tcp-check                                                                                              |
|                                                                                                               |
| \# option allbackups                                                                                          |
|                                                                                                               |
| default-server port 9201 inter 2s downinter 5s rise 3 fall 2 slowstart 60s maxconn 64 maxqueue 128 weight 100 |
|                                                                                                               |
| server 192**.**168**.**92**.**11 192**.**168**.**92**.**11**:**5432 check                                     |
|                                                                                                               |
| server 192**.**168**.**92**.**12 192**.**168**.**92**.**12**:**5432 check                                     |
|                                                                                                               |
| server 192**.**168**.**92**.**13 192**.**168**.**92**.**13**:**5432 check                                     |
+---------------------------------------------------------------------------------------------------------------+

**keepalived.conf配置文件**

+------------------------------------------------------------------------------------+
| **\[**root**@**pgmaster **\~\]**\# cat **/**etc**/**keepalived**/**keepalived.conf |
|                                                                                    |
| \#haproxy - You can add more types manually after this.                            |
|                                                                                    |
| vrrp\_script chk\_haproxy **{**                                                    |
|                                                                                    |
| script \"killall -0 haproxy\" \# verify the pid existance                          |
|                                                                                    |
| interval 2 \# check every 2 seconds                                                |
|                                                                                    |
| weight 2 \# add 2 points of prio if OK                                             |
|                                                                                    |
| **}**                                                                              |
|                                                                                    |
| vrrp\_instance VI\_HAPROXY **{**                                                   |
|                                                                                    |
| interface ens33 \# interface to monitor                                            |
|                                                                                    |
| state MASTER                                                                       |
|                                                                                    |
| virtual\_router\_id 51 \# Assign one ID for this route                             |
|                                                                                    |
| priority 102                                                                       |
|                                                                                    |
| unicast\_src\_ip 192**.**168**.**92**.**11                                         |
|                                                                                    |
| unicast\_peer **{**                                                                |
|                                                                                    |
| 192**.**168**.**92**.**12                                                          |
|                                                                                    |
| 192**.**168**.**92**.**13                                                          |
|                                                                                    |
| **}**                                                                              |
|                                                                                    |
| virtual\_ipaddress **{**                                                           |
|                                                                                    |
| 192**.**168**.**92**.**15 \# the virtual IP                                        |
|                                                                                    |
| **}**                                                                              |
|                                                                                    |
| track\_script **{**                                                                |
|                                                                                    |
| chk\_haproxy                                                                       |
|                                                                                    |
| **}**                                                                              |
|                                                                                    |
| \# notify /usr/local/bin/notify\_keepalived.sh                                     |
|                                                                                    |
| **}**                                                                              |
|                                                                                    |
| \# DO NOT REMOVE THE NEXT LINE                                                     |
|                                                                                    |
| \#\@S9S\_NEXT\_SECTION@                                                            |
+------------------------------------------------------------------------------------+

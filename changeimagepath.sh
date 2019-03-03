#!/bin/bash

blogfile=/blog/markdown/linux部署PostgreSQL集群.md
imgfile=/blog/image/clustercontrol

sed -n '/media\/image/=' $blogfile >> /blog/read.txt
for $imgname in $(ls ./blog/image/clustercontrol)
do
sed -i 's/$/ $imgname/g' read.txt 
echo 

while read line_num imgname 
#其中a为ip列表中的ip，b为ip列表中的主机名
do
sed "$LINE_NUM"'c  ![$imgname](https://github.com/willzhang/blog/blob/master$imgfile/$imgname' $blogfile 
done </blog/read.txt



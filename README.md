#初始化git本地仓库
git init

#配置全局git提交参数
git config --global user.name "您的账号"

#添加远程仓库地址
git remote add origin http://sso.sensesw.com:7001/sense/IAM.git


#本地和服务器关联
git pull origin master


#删除某个文件夹
git rm -r *


#添加 本地文件
git add *

#提交修改信息
git commit -m "备注"


#推送信息到git远程仓库
git push -u origin master




node  {
   stage('获取git最新代码') {
       checkout scm
   }
   stage('Maven 程序打包') {
       sh 'mvn clean package -DskipTests'
   }
   stage('授权脚本权限') {
       sh 'chmod 777 ./restart.sh'
   }

   stage('执行部署程序') {
       sh './restart.sh'
   }
}

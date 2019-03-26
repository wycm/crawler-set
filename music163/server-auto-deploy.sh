sudo ps -ef |grep music163 | grep -v grep |awk '{print $2}'|xargs kill -9
sudo ps -ef |grep chrome | grep -v grep |awk '{print $2}'|xargs kill -9

cd /alidata/server/workspace/music163
git pull origin master
echo 'pull success'
mvn clean
mvn -Pprod package assembly:single
echo 'mvn success'
export DISPLAY=:99
cd target
java -jar music163-1.0-SNAPSHOT-jar-with-dependencies.jar &

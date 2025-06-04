#!/bin/bash
source /shdy/saas/wes/dev/cd.env

if [ $# -eq 0 ]; then
    echo "请至少传递一个参数"
    exit 1
fi

for arg in "$@"; do
    if [ -z "$arg" ]; then
        echo "参数不能为空"
        exit 1
    fi
done
# 参数 $MODULE_NAME/模块名, MODULE_URL/触发链接 ,MODULE_CODE/模块code ,ENV_NAME/环境 ,VER_NAME/分支 ,GITLAB_USER_NAME/触发用户 ,GITLAB_USER_EMAIL/触发邮箱
MODULE_NAME=$1
MODULE_URL=$2
MODULE_CODE=$3
ENV_NAME=$4
TAG=$5
GITLAB_USER_NAME=$6
GITLAB_USER_EMAIL=$7


# 删除
tmpDir=$ENV_NAME-$APPLICATION_CODE-$APPLICATION_BRANCH
rm -rf $tmpDir
# 拉取指定项目的分支
git clone -b $APPLICATION_BRANCH $GITLAB_URL/$GITLAB_GROUP/$APPLICATION_CODE $tmpDir
if [ $? -ne 0 ]; then
    echo "git clone -b $APPLICATION_BRANCH $GITLAB_URL/$GITLAB_GROUP/$APPLICATION_CODE $tmpDir"
    echo "Git clone失败，请检查错误信息"
    exit 1
fi
cd $tmpDir

# 使用Maven打包构建
mvn -s $MAVEN_SETTING clean package
if [ $? -ne 0 ]; then
    echo "mvn clean package 失败，请检查错误信息"
    exit 1
fi
# 构建Docker镜像
docker build -t $DOCKER_IMAGE:$TAG .

# 推送镜像到Harbor仓库
docker login -u $REGISTRY_USER -p $REGISTRY_PASS $REGISTRY_URL
if [ $? -ne 0 ]; then
    echo "docker login 失败，请检查错误信息"
    exit 1
fi
docker tag $DOCKER_IMAGE:$TAG $REGISTRY_URL/$DOCKER_IMAGE:$TAG

echo "docker tag $DOCKER_IMAGE:$TAG $REGISTRY_URL/$DOCKER_IMAGE:$TAG"

docker push $REGISTRY_URL/$DOCKER_IMAGE:$TAG
if [ $? -ne 0 ]; then
    echo "docker push 失败，请检查错误信息"
    exit 1
fi

if ! docker network ls | grep -q "$DOCKER_NETWORK"; then
    docker network create "$DOCKER_NETWORK"
fi
# 执行docker-compose文件
docker-compose -f $APPLICATION_ROOT/$ENV_NAME/docker-compose.yml down
# 检查镜像是否存在
image_exists=$(docker images -q $REGISTRY_URL/$DOCKER_IMAGE:$TAG)
# 如果镜像存在，则删除
if [ -n "$image_exists" ]; then
    docker rmi -f $image_exists
    echo "已删除镜像：$REGISTRY_URL/$DOCKER_IMAGE:$TAG"
else
    echo "镜像不存在：$REGISTRY_URL/$DOCKER_IMAGE:$TAG"
fi

echo "copy configs to server dev root: cp -r env/$ENV_NAME/* $APPLICATION_ROOT/$ENV_NAME"
cp -r env/$ENV_NAME/* $APPLICATION_ROOT/$ENV_NAME

# 启动容器
docker-compose -f $APPLICATION_ROOT/$ENV_NAME/docker-compose.yml up -d
if [ $? -ne 0 ]; then
    echo "docker-compose up -d 失败"
    exit 1
fi
# 环境归档 /shdy/saas/wes/dev/
cd $APPLICATION_ROOT/$ENV_NAME
mkdir -p $APPLICATION_ROOT/$ENV_NAME/archives
echo "当前目录：$(pwd)"
1ef       current_time=$(date +"%y%m%d%H%M")
tar -czvf \
  $APPLICATION_ROOT/$ENV_NAME/archives/$APPLICATION_NAME-$MODULE_CODE-$ENV_NAME-$TAG-$current_time.tar.gz \
  configs \
  docker-compose.yml \
  cicd.sh \
  ci_webhook.sh \
  cd_webhook.sh \
  readme.md \
  .env
rm -rf $APPLICATION_ROOT/$ENV_NAME/$tmpDir
echo "删除目录：rm -rf $APPLICATION_ROOT/$ENV_NAME/$tmpDir"

#触发webhook
source cd_webhook.sh "$@"



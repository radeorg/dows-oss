#!/bin/bash
source /shdy/saas/wes/dev/cd.env
# 参数 $MODULE_NAME/模块名, MODULE_URL/触发链接 ,MODULE_CODE/模块code ,ENV_NAME/环境 ,VER_NAME/分支 ,GITLAB_USER_NAME/触发用户 ,GITLAB_USER_EMAIL/触发邮箱
args=("$@")
for ((i=0; i<${#args[@]}; i++)); do
  echo "Parameter ${i}: ${args[i]}"
done

MODULE_NAME=${args[0]}
MODULE_URL=${args[1]}
MODULE_CODE=${args[2]}
ENV_NAME=${args[3]}
TAG=${args[5]}
GITLAB_USER_NAME=${args[6]}
CI_COMMIT_AUTHOR=${args[7]}
#状态 成功或失败
STATE=$2


#颜色
COLOR=""
STATE_DESCR=""
if [ "$STATE" = "true" ]; then
  COLOR="green"
  STATE_DESCR="成功"
else
  COLOR="red"
  STATE_DESCR="失败"
  exit 1
fi


title='应用发布'
time="$(date "+%Y-%m-%d")"
times="$(date "+%H:%M:%S")"
xingqi="$(date "+%A")"
public_ip=$(curl -s https://api.ipify.org)
#runner 中需要apt-get install -y ifconfig
ip=$(ifconfig | grep inet | awk 'NR==3{print $2}')
lsblk=$(df -h / | awk '{print $4"/"$2 , $5}' | tail -n 1 )
mem_info=$(free -h) # 运行 free -h 命令并将结果保存到变量 mem_info 中
total_memory=$(echo "$mem_info" | grep "Mem:" | awk '{print $2}') # 从输出结果中提取第二列（Total）的值作为总内存大小
mem=$(free | grep Mem | awk '{print $3/$2 * 100.0}')
cpu=$(top -b -n1 | grep "Cpu(s)" | awk '{print $2}')

#组装URL
gitlab_commit_url="${MODULE_URL}/-/commit/${CI_COMMIT_SHA}"
sonarqube_branch_url="${SONARQUBE_HOST}/dashboard?branch=${CI_COMMIT_REF_NAME}&id=${SONARQUBE_KEY}"
#weixin
#curl --location 'https://open.feishu.cn/open-apis/bot/v2/hook/5d00f1b4-8bfb-4e07-b027-90a31c478464' \
#--header 'Content-Type: application/json' \
#--data '
#{
#  "msgtype": "markdown",
#  "markdown": {
#    "content": "开发环境\n
#      >发布时间: <font color=\"comment\">'"$time $times $xingqi"'</font>
#      >项目名: <font color=\"comment\">'"$PROJECT_NAME"'</font>
#      >分支名: <font color=\"comment\">'"$BRANCH_NAME"'</font>
#      >触发者: <font color=\"comment\">'"$AUTHOR_NAME"'</font>
#      >状态: <font color='"$COLOR"'>'"$STATE"'</font>
#      >HOST: <font color=\"comment\">'"$public_ip:$ip"'</font>
#      >DISK: <font color=\"comment\">'"$lsblk"'</font>
#      >MEM: <font color=\"comment\">'"$total_memory,$mem%"'</font>
#      >CPU: <font color=\"comment\">'"$cpu%"'</font>
#      >描述: <font color='"$COLOR"'>'"$COMMIT"'</font>
#      >变更文件:
#<font color=\"comment\">'"$changedFileList"'</font>"
#  }
#}'
#http://192.168.23.19/shdy/shdy-task/-/commit/6b9fa816dd1e5bfeae1aba0bdf4329a31d017fc1

#feishu
curl -X POST -H "Content-Type: application/json" \
        -d '{
          "msg_type":"post",
          "content": {
            "post": {
              "zh_cn": {
                "title": "发布'"$APPLICATION_CODE/仓储物流"'",
                "content": [
                  [
                    {"tag": "text", "text": "'"触发时间：$time $times $xingqi\n"'"},
                    {"tag": "text", "color": "'"$COLOR"'", "text": "'"状态：$STATE_DESCR\n"'"},
                    {"tag": "text", "text": "'"触发用户：$GITLAB_USER_NAME"'"},
                    {"tag": "a", "href": "'"$CI_COMMIT_AUTHOR"'", "text": "'"($CI_COMMIT_AUTHOR)\n"'"},
                    {"tag": "text", "text": "'"触发模块：$MODULE_CODE/$MODULE_NAME\n"'"},
                    {"tag": "text", "text": "'"触发分支："'"},
                    {"tag": "a", "href": "'"$MODULE_URL"'", "text": "'"$ENV_NAME-$TAG\n"'"},
                    {"tag": "text", "text": "'"HOST:$public_ip:$ip\n"'"},
                    {"tag": "text", "text": "'"DISK:$lsblk\n"'"},
                    {"tag": "text", "text": "'"MEM:$total_memory,$mem%\n"'"},
                    {"tag": "text", "text": "'"CPU:$cpu%\n"'"}
                  ]
                ]
              }
            }
          }
        }' "${WEBHOOK_FEI_SHU}"
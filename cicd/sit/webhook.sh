#!/bin/bash

echo "当前路径: $(pwd)"
# 加载环境变量
source ./cicd.env && source ./webhook.env || { echo "加载环境变量失败"; exit 1; }

# 状态颜色和文字设置
if [ "$ACTIONS_STATUS" = "success" ]; then
    COLOR="green"
    STATE="成功 ✅"
else
    COLOR="red"
    STATE="失败 ❌"
fi

#变更文件换行处理
#changedFileList=$(echo "$CHANGED_FILES" | sed 's/ /\\n/g')
# 处理变更文件换行显示（兼容含空格文件名）
changedFileList=$(echo "$CHANGED_FILES" | tr ' ' '\n' | sed 's/^/ - /')
#打印信息
echo "变更文件: ${CHANGED_FILES}"
echo "变更文件: ${changedFileList}"
echo "代码检测: ${SONARQUBE_HOST}/dashboard?branch=${BRANCH_NAME}&id=${SONARQUBE_KEY}"
echo "参数列表: ${PROJECT_URL} ,${BRANCH_NAME}, ${ACTOR_NAME},${ACTOR_MAIL},${CHANGED_FILES},${COMMIT_MSG},${COMMIT_SHA},${ACTIONS_STATUS}"

#组装URL
COMMIT_URL="${PROJECT_URL}/-/commit/${COMMIT_SHA}"
#sonarqube
SONARQUBE_URL="${SONARQUBE_HOST}/dashboard?branch=${BRANCH_NAME}&id=${SONARQUBE_KEY}"

HOST="$(curl -s https://api.ipify.org || echo "N/A") ($(hostname -I | awk '{print $1}'))"
DISK="$(df -h / | awk 'NR==2{print $4"/"$2 "("$5")"}')"
MEM="$(free -m | awk 'NR==2{printf "%.1fG/%.1fG (%.0f%%)", $3/1024, $2/1024, $3/$2*100}')"
CPU="$(top -b -n1 | grep "Cpu(s)" | awk '{print $2}')"

# 构造消息
MARKDOWN_MSG="### $PROJECT_NAME $STATE\n
---
**📅 发布时间**\n$(date "+%Y-%m-%d %H:%M:%S %A")\n\n
**🔧 项目信息**\n
- 仓库：$PROJECT_URL\n
- 分支：\`$BRANCH_NAME\`\n
- 触发者：$ACTOR_NAME ($ACTOR_MAIL)\n\n
**🖥️ 系统状态**\n
- 主机：$HOST\n
- 磁盘：$DISK\n
- 内存：$MEM\n
- CPU：$CPU\n
**📌 提交信息**\n
- COMMIT-ID：$COMMIT_SHA\n
- 说明：$COMMIT_MSG\n
- [查看提交详情]($COMMIT_URL)\n
- [查看代码检测报告]($SONARQUBE_URL)\n\n
**📌 触发信息**\n
- 触发仓库：$TRIGGER_REPOSITORY\n
- 触发分支：$TRIGGER_BRANCH\n
- 是否触发：**$IS_TRIGGER**\n
**📂 变更文件**\n
$changedFileList\n
---"

# 触发构建
if [ "$IS_TRIGGER" = "1" ]; then
  # 触发构建
  curl -L \
    -X POST \
    -H "Accept: application/vnd.github+json" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    -H "Authorization: Bearer $GH_TOKEN" \
    -d '{"ref": "'$TRIGGER_BRANCH'"}' \
    "$TRIGGER_URL"
fi

# 发送通知
curl -sS -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "msgtype": "markdown",
    "markdown": {
      "title": "'"$PROJECT_NAME 构建通知"'",
      "text": "'"$MARKDOWN_MSG"'"
    }
  }' \
  "$WEBHOOK_DING_TALK"
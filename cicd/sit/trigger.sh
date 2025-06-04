#!/bin/bash
echo "当前路径: $(pwd)"
# 加载环境变量
source ./cicd.env && source ./webhook.env || { echo "加载环境变量失败"; exit 1; }

curl -L \
  -X POST \
  -H "Accept: application/vnd.github+json" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  -H "Authorization: Bearer $GH_TOKEN" \
  -d '{"ref": "'$TRIGGER_BRANCH'"}' \
  "$TRIGGER_URL"
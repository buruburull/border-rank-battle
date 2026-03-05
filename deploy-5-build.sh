#!/bin/bash
# Deploy 5: Build and copy JAR
echo "Building..."
./gradle-8.5/bin/gradle :core-plugin:shadowJar 2>&1 | tail -5

if [ $? -eq 0 ]; then
    cp core-plugin/build/libs/BorderRankBattle-0.1.0-SNAPSHOT.jar ~/minecraft-server/plugins/BorderRankBattle.jar
    rm -f ~/minecraft-server/plugins/BorderRankBattle-0.1.0-SNAPSHOT.jar
    echo "✅ ビルド＆デプロイ完了！サーバーを再起動してください。"
else
    echo "❌ ビルド失敗"
fi

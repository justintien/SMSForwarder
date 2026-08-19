# SMSForwarder

> 讓小號接收到的簡訊自動轉發其它地方 (例如: discord)

目前版本 **1.1.0**，不上架 Google Play，以側載 APK 方式安裝 — 見 [docs/INSTALL.md](docs/INSTALL.md)。

## 功能

- 收到簡訊自動轉發到 Discord webhook
- 可設定多組 webhook，一則簡訊同時送到每一組
- 訊息記錄：搜尋發送者/內容、星號標記與篩選、單筆刪除
- 轉發走 WorkManager，網路不穩時會排隊重試，恢復連線後補送

設定都在 App 內的設定頁完成，**不需要再編輯任何 xml 檔**（舊版曾要求手動改 `res/values/_.xml`，已改掉）。

## 已知問題

如果使用的不是 android 內建的訊息 app，則無法正常監聽 `android.provider.Telephony.SMS_RECEIVED`。

## 開發

### 版本號

版本號集中在 `gradle.properties`：

```properties
VERSION_MAJOR=1
VERSION_MINOR=1
VERSION_PATCH=0
```

`app/build.gradle.kts` 由此推導出 `versionName`（`1.1.0`）和 `versionCode`（`major*10000 + minor*100 + patch` = `10100`）。發版只要改這三個值。

### 簽名與 build

Release 簽章設定放在 `keystore.properties`，該檔已被 gitignore。複製 `keystore.properties.example` 填入自己的 keystore 路徑與密碼即可：

```bash
cp keystore.properties.example keystore.properties
$EDITOR keystore.properties
./gradlew assembleRelease
```

沒有 `keystore.properties` 時 build 仍可執行，只是產出未簽名的 APK。

⚠️ keystore 檔案遺失後就再也發不出能覆蓋更新的版本，使用者只能移除重裝 — 請務必備份。

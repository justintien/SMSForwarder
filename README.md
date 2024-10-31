# SMSForwarder

> 讓小號接收到的簡訊自動轉發其它地方 (例如: discord)

v20241031

## 使用方式

- 需要編輯設置檔: ./app/src/main/res/values/_.xml 
    1. 設置字串 discord_webhook = 你自己想接收訊息的 webhook url
    2. 設置字串 receiver_mobile = 你自己接收簡訊的號碼 (會顯示在標題接收者一欄)

- 已知問題: 如果使用的不是 android 內建的訊息 app，則無法正常監聽 `android.provider.Telephony.SMS_RECEIVED`

## 當前支持

- [x] Discord webhook


### TODO


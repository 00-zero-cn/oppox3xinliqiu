# WatchRecorder — OPPO Watch X3 24小时分段录音

功能：
- 前台服务 + 麦克风前台服务类型，熄屏持续录音
- 每 15 分钟自动切分一个文件（AAC 16kHz 单声道 32kbps，约 7MB/小时）
- 充电时自动暂停、拔掉充电器自动续录（省电策略）
- 界面显示录音状态与累计时长
- 录音文件保存在手表：/sdcard/Android/data/com.example.watchrecorder/files/recordings/

## 方法一（推荐，无需电脑）：GitHub 云端自动构建 APK

1. 注册一个 GitHub 账号（免费）
2. 新建一个仓库（New repository，名字随意，比如 watchrec，选 Public）
3. 在本仓库页面点 "Add file" -> "Upload files"，把本文件夹（WatchRecorder）里的
   所有内容拖进去（Chrome/Edge 可直接拖整个文件夹），点 Commit
4. 上传后点顶部 "Actions" 标签页，允许启用 Workflow，然后运行 "Build APK"
5. 等几分钟（绿色对勾后），点进该次运行，页面底部 "Artifacts" 里下载
   watch-recorder-apk.zip，解压得到 app-debug.apk

## 方法二（有电脑）：Android Studio 构建

1. 安装 Android Studio（官方免费）
2. Open -> 选择 WatchRecorder 文件夹，等待同步完成
3. Build -> Build Bundle(s)/APK(s) -> Build APK(s)
4. 右下角点击 locate，得到 app-debug.apk

## 安装到手表（ADB 侧载）

1. 手表：设置 -> 关于手表 -> 连续点击"版本号"7 次 -> 返回进入"开发者选项" -> 开启"USB调试"
2. 手表放充电底座，数据线连电脑；或在手机装"甲壳虫ADB助手"通过 Wi-Fi 连接手表
   （手表连同一 Wi-Fi，开发者选项里可查看 IP，端口 5555）
3. 安装：adb install app-debug.apk
4. 导出录音：adb pull /sdcard/Android/data/com.example.watchrecorder/files/recordings

## 使用

1. 手表应用列表打开"24h录音"，授予麦克风和通知权限
2. 点"开始录音"即可，可熄屏。下拉通知栏可见录音状态
3. 放充电器上会自动暂停，拔掉自动续录
4. 受物理限制，持续录音续航约 10~20 小时，建议夜间充电、白天录音

## 合规提示

24小时录音可能录入他人谈话，请仅用于记录本人语音备忘、经同意的会议等合法场景，
勿用于窃听或侵犯他人隐私，由此产生的一切法律责任由使用者自行承担。

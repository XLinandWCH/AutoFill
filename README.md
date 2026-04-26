# AutoFill
<div align="center">
  <img src="composeApp/src/jvmMain/resources/drawables/AutoFill.png" width="120" height="120" alt="AutoFill Logo">
  <p><b>自动化问卷填写工具 (问卷星专用)</b></p>

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-blue.svg)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Platform-JVM%20%7C%20Server-orange.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)
</div>

<br>

> 📢 **项目状态：重构中 (Refactoring)**  
> 本项目正在从 Python 向 **Kotlin Multiplatform (KMP)** 迁移重构，旨在提供更稳定、高效的自动化体验。

---

## 📝 项目简介

本项目起源于 **2025年4月**。作者在备战“挑战杯”期间面临巨大的问卷收集压力，深知此类“比赛问卷”的数据收集往往耗费大量社交资源却收效甚微。

最初版本的 `WJXHelper` 基于 Python 开发，受限于当时的技术栈（多线程阻塞、浏览器驱动管理混乱等问题），效率未达预期。因此，项目于 **2026年2月** 正式启动重构，采用 **Kotlin** 重写核心逻辑。

取名 **AutoFill**（自动填充），寓意像流水线一样高效、精准地完成重复性工作。

---

## ✨ 已支持题型

已验证支持的题型如下：

| 题型  | 状态 |
|:----| :--- | 
| 单选题 | ✅ 支持 | 
| 多选题 | ✅ 支持 |
| 填空题 | ✅ 支持 | 
| 排序题 | ✅ 支持 | 
| 量表题 | ✅ 支持 | 
| 下拉框 | ✅ 支持 | 
| 滑动条 | ✅ 支持 | 
| 矩阵题 | ✅ 支持 |


---

## 🚀 使用方法

### 方案一：源码运行 (开发者)

本项目基于 **Kotlin Multiplatform**，使用 Gradle 进行构建管理。

#### 1. 克隆项目
```bash
git clone https://github.com/XLinandWCH/AutoFill.git
cd AutoFill
```

#### 2. 环境准备
- 确保已安装 **JDK 17** 或更高版本。
- 配置好 `JAVA_HOME` 环境变量。

#### 3. 运行程序 (Desktop JVM)
如果你使用的是 macOS/Linux/Windows 终端：
```bash
./gradlew :composeApp:run
```
如果是 Windows PowerShell：
```powershell
gradlew.bat :composeApp:run
```

#### 4. 构建与打包
生成 Native 可执行文件（无 JVM 环境也可运行）：
```bash
./gradlew :composeApp:createDistributable
```
生成的文件位于：`composeApp/build/compose/binaries/main/app/`

### 方案二：打包文件运行 (普通用户)
1. 前往 https://github.com/XLinandWCH/AutoFill/releases 页面下载最新的安装包或压缩包。
2. 解压后双击运行 `AutoFill.exe` (Windows) 或 `AutoFill.app` (macOS)。

---

## ⚙️ 程序配置说明

打开 GUI 界面后，请务必先阅读 **文档 -> 使用说明**。以下是核心参数解释：

| 配置项 | 说明 |
| :--- | :--- |
| **线程数** | 并发浏览器实例数量。<br>例：输入 `3`，则同时打开 3 个窗口并行填写。**建议根据 CPU 性能调整，通常不超过 CPU 核心数。** |
| **问卷数** | 计划提交的总份数。 |
| **开启反爬虫机制** | **强烈建议开启**。<br>开启后：模拟真人行为（随机滚动、鼠标移动、随机停留）。<br>关闭后：极速模式（直连接口/秒点），极易触发风控，**仅限调试使用**。 |
| **开启无头模式** | 开启：浏览器后台运行，无界面（占用资源少）。<br>关闭：可见浏览器操作过程（方便排查元素定位问题）。 |

---

## 🛠️ 技术栈

- **Language**: Kotlin
- **Framework**: Kotlin Multiplatform, Compose Multiplatform (UI)
- **Automation**: Playwright (KMP 封装)
- **Coroutines**: Kotlin Coroutines (解决原 Python 版多线程痛点)

---

## 📬 联系与交流

欢迎加入社区交流反馈，或提交 Issue/PR。

- **Bilibili**: 雁影孑然
- **QQ 交流群**: [1102686162](https://qm.qq.com/cgi-bin/qm/qr?k=1102686162) (新建群，人少但作者常在)

---

## ⚠️ 免责声明

1. **AutoFill** 是一款旨在提高科研效率、减轻无效劳动的工具，**严禁用于商业刷单、恶意攻击或破坏公平竞争环境**。
2. 项目作者开发初衷仅为解决学术竞赛中的问卷收集难题。
3. **项目开源免费供个人学习、非商业使用，如需商业用途（如植入广告、付费分发等）请联系作者协商。**
4. 若因个人或团队的不当使用（包括但不限于滥用反爬机制、恶意攻击服务器）导致的法律后果，**作者概不承担任何责任**。
5. 请合理、合法、遵守平台规则地使用本软件。

---

*祝使用愉快！Happy Coding!* 🎉
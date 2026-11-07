# Camelot

共享库：平台抽象、配置引擎与配置界面、进世界提示、网络管道、界面辅助。

## 提供什么

| 层 | 内容 |
|---|---|
| 平台 | 加载器判定、可选前置探测、配置目录 |
| 配置 | 字段 schema、文件读写、服务端到客户端同步、热加载、带版本的一次性迁移 |
| 界面 | NeoForge 1.21+ 用 `ConfigurationScreen`，其余用 Cloth Config |
| 消息 | 带兜底文案的可翻译组件 |
| 提示 | 进世界提示，可按玩家关闭 |
| 网络 | payload 注册与分发 |
| GUI | 对话框基座、遮罩、按钮描边、文本截断 |

## 目录

```
<分支>/               1.20.X · 1.21.X · 26.X
  build-logic/        约定插件
  common/ fabric/ neoforge/ [forge/]
  targets/<区间>/      逐区间属性与源码覆盖
```

## 构建

```bash
./gradlew -p targets/<区间> build
```

产物位于 `targets/<区间>/build/<模块>/libs/`。

## 许可证

MIT。

# Camelot

Shared library: platform abstraction, config engine and config screens, join-world prompts, network pipeline, UI helpers.

## What it provides

| Layer | Contents |
|---|---|
| Platform | Loader detection, optional dependency probing, config directory |
| Config | Field schemas, file read/write, server-to-client sync, hot reload, one-shot versioned migration |
| Config UI | NeoForge: its own generated screen; Fabric: screen generated from the schema (Cloth Config API, soft dependency) |
| Messages | Translatable components with fallback text |
| Prompts | Join-world prompts, dismissible per player |
| Network | Payload registration and dispatch on one shared channel |

## Layout

```
<branch>/               1.20.X · 1.21.X · 26.X
  build-logic/          convention plugins
  common/ fabric/ neoforge/ [forge/]
  targets/<range>/      per-range properties and source overrides
```

## Build

```bash
./gradlew -p targets/<range> build
```

Artifacts are written to `targets/<range>/build/<module>/libs/`.

## License

MIT.

---

# Camelot

共享库：平台抽象、配置引擎与配置界面、进世界提示、网络管道、界面辅助。

## 提供什么

| 层 | 内容 |
|---|---|
| 平台 | 加载器判定、可选前置探测、配置目录 |
| 配置 | 字段 schema、文件读写、服务端到客户端同步、热加载、带版本的一次性迁移 |
| 配置界面 | NeoForge：用其自带的生成界面；Fabric：按 schema 生成界面（Cloth Config API，软依赖） |
| 消息 | 带兜底文案的可翻译组件 |
| 提示 | 进世界提示，可按玩家关闭 |
| 网络 | payload 注册与分发（全库共用一个通道） |

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
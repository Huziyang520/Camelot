# Camelot

> Shared library for **Only TP** and **AuthCmd**. Local-only: it is never published as a standalone mod.

---

## English

Camelot exists so the same plumbing is not written twice. Both business mods compile against it and ship it
inside their own jar (jar-in-jar), so players install nothing extra.

### Scope

| Layer | What it owns |
|---|---|
| Platform | loader detection, optional-mod presence checks, config directory, side checks |
| Config engine | schema definition, file IO, server → client sync, hot reload, one-shot migrations |
| Config screens | NeoForge `ConfigurationScreen` on 1.21+, Cloth Config on everything else |
| Messaging | translatable components with fallback text, styled chat output |
| Notices | join-world notices, per-player opt-out, missing-dependency hints |
| Networking | payload registration and dispatch behind one loader-agnostic API |
| GUI helpers | dialog base, overlay/scrim helpers, button frames, text fitting, row grid |

It deliberately owns **no business rules**: command filtering, permission lists and game-mode policy stay in the
business mods.

### Layout

```
<branch>/                 26.X · 1.21.X · 1.20.X
  build-logic/            convention plugins (buildSrc is gone)
  common/ fabric/ neoforge/ [forge/]
  targets/<mc range>/     per-range properties + optional source/resource overlay
  gradlew
```

Build one target:

```bash
./gradlew -p targets/26.3 build
```

Outputs land in `targets/<range>/build/<module>/libs/`.

### License

MIT — see `LICENSE`.

---

## 中文

Camelot 存在的意义是同一套接线不写第二遍。两个业务模组都编译期依赖它，并在自己的 jar 里以内置（jar-in-jar）
的形式携带，玩家不需要额外安装任何东西。

### 范围

| 层 | 负责什么 |
|---|---|
| 平台 | 加载器判定、可选前置探测、配置目录、客户端/服务端侧判定 |
| 配置引擎 | schema 定义、文件读写、服务端 → 客户端同步、热加载、一次性迁移 |
| 配置界面 | 1.21+ 用 NeoForge `ConfigurationScreen`，其余用 Cloth Config |
| 消息 | 可翻译组件 + 兜底文案、带样式的聊天输出 |
| 提示 | 进世界提示、按玩家开关、缺前置提示 |
| 网络 | 一套与加载器无关的 payload 注册与分发 API |
| 界面辅助 | 对话框基座、遮罩、按钮描边、文本截断、行网格 |

它刻意**不承载任何业务规则**：命令过滤、名单、游戏模式策略都留在业务模组里。

### 目录

```
<分支>/                   26.X · 1.21.X · 1.20.X
  build-logic/            约定插件（不再用 buildSrc）
  common/ fabric/ neoforge/ [forge/]
  targets/<区间>/          逐区间属性 + 可选源码/资源覆盖
  gradlew
```

构建单个 target：

```bash
./gradlew -p targets/26.3 build
```

产物落在 `targets/<区间>/build/<模块>/libs/`。

### 许可证

MIT，见 `LICENSE`。

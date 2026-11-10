# Camelot

A shared runtime library used by mods built on top of it. It adds no content of its own.

## For players

- It shows up in the mod list as a dependency of the mods that use it. There is nothing to install, configure or click by hand.
- It provides no blocks, items, mobs, dimensions, commands, keybinds, HUD elements, or screens that open on their own.
- It only reads and writes the settings files of the mods that use it, inside the game's `config` folder.
- It talks to nothing external. The only traffic it produces is what those mods already exchange with the server you play on, so that their settings agree on the client and the server. No telemetry, no analytics, no ads, no third-party services.
- It is a dependency, not a feature: removing it breaks the mods that need it.

## For developers

| Area | What it offers |
|---|---|
| Platform | Loader detection and optional-dependency probing behind one API |
| Config | Schema-driven options persisted as TOML, common/client scopes, server-to-client sync, hot reload, one-shot versioned migration |
| Config screens | Screen generated from the schema; the loader's own config screen is used where the loader has one, otherwise Cloth Config when it is installed |
| Messages | Translatable components with fallback text |
| Prompts | Join-world notices, dismissible per player |
| Network | One shared payload channel for registration and dispatch |

Entry points: `CamelotConfig` / `ConfigSchema` for options, `ConfigScreens` for the generated UI, `JoinNotice` for prompts, `CamelotNet` for payloads.

Optional dependencies are declared as optional and never bundled. The public API is additive: existing signatures stay stable.

## License

MIT.

---

# Camelot

共享运行库，供基于它的模组使用。本身不添加任何内容。

## 面向玩家

- 它会作为依赖出现在模组列表里。不需要手动安装、配置或点击任何东西。
- 它不提供方块、物品、生物、维度、命令、按键、HUD 元素，也不会自己弹出任何界面。
- 它只读写使用它的那些模组的设置文件，位置在游戏的 `config` 文件夹里。
- 它不与外部通信。唯一的流量是那些模组与你所连服务器之间本就有的数据，用来让客户端与服务端的设置保持一致。没有遥测、没有统计、没有广告、不接第三方服务。
- 它属于依赖而非玩法内容：删掉它会让依赖它的模组失效。

## 面向开发者

| 方面 | 提供的能力 |
|---|---|
| 平台 | 统一的加载器判定与可选前置探测接口 |
| 配置 | 由 schema 驱动的选项，持久化为 TOML，区分 common / client 作用域，支持服务端到客户端同步、热加载、带版本的一次性迁移 |
| 配置界面 | 按 schema 生成的界面；加载器自带配置界面时用自带的，否则在装有 Cloth Config 时用它生成 |
| 消息 | 带兜底文案的可翻译组件 |
| 提示 | 进世界提示，玩家可逐个关闭 |
| 网络 | 全库共用一个 payload 通道完成注册与分发 |

入口：选项用 `CamelotConfig` / `ConfigSchema`，生成的界面用 `ConfigScreens`，提示用 `JoinNotice`，payload 用 `CamelotNet`。

可选前置按可选声明，不会被打包。公开 API 只增不改：已有签名保持稳定。

## 许可证

MIT。

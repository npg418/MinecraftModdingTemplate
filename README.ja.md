# NPG418's Minecraft modding template

[English](./README.md) | [日本語](./README.ja.md)

[Gradle](https://gradle.org/) + [Kotlin](https://kotlinlang.org/) + [Version catalogs](https://docs.gradle.org/current/userguide/version_catalogs.html)
を使用したマルチローダー対応のModding用テンプレートです。

このプロジェクトはビルドロジックを共有するために [build-logic](./build-logic) のコンポジットビルドを使用しています。

## 対応ローダー

- NeoForge（[ModDevGradle](https://github.com/neoforged/ModDevGradle) 経由）
- Fabric（[Fabric Loom](https://github.com/FabricMC/fabric-loom) 経由）

各ローダー間で共有されるコードは `common` サブプロジェクトに配置されています。各ローダーは `common` に依存する個別のサブプロジェクト（`neoforge`、`fabric`）を持ちます。

## 使い方

1. [settings.gradle.kts](./settings.gradle.kts)の`rootProject.name`を各プロジェクトの名前に置き換える
2. [gradle.properties](./gradle.properties)内のMod情報(`mod_id`, `mod_group`, `mod_name`, `mod_version`,
   `mod_description`, etc.)を更新する
3. `common`、`neoforge`、`fabric` 全体にわたるベースパッケージ（`com.npg418.examplemod`）を自分のModのパッケージにリネームする
4. 各ローダーサブプロジェクトのリソース配下にある `mods.toml` / `fabric.mod.json` テンプレートを自分のModのメタデータで更新する
5. 必要に応じて[`gradle/libs.versions.toml`](./gradle/libs.versions.toml)の依存バージョンを調整する
6. （任意）fabricプロジェクトでデバッグログを有効にしたい場合は、`fabric/log4j-dev.xml`にあるlog4j設定ファイルを更新する
7. Edit'n'Fun!

## 主なGradleタスク

| タスク                                                            | 説明                                           |
|-------------------------------------------------------------------|------------------------------------------------|
| `./gradlew build`                                                 | 全サブプロジェクトをビルドする                 |
| `./gradlew :neoforge:runClient`                                   | NeoForgeローダーでゲームクライアントを起動する |
| `./gradlew :fabric:runClient`                                     | Fabricローダーでゲームクライアントを起動する   |
| `./gradlew :neoforge:runServer` / `./gradlew :fabric:runServer`   | 専用サーバーを起動する                         |
| `./gradlew :neoforge:runDatagen` / `./gradlew :fabric:runDatagen` | データ生成を実行する                           |

## プロジェクト構成

```
.
├── build-logic/      # 共有コンベンションプラグインを提供するコンポジットビルド
├── common/           # ローダーに依存しないModコード
├── neoforge/         # NeoForge固有のエントリーポイントとコード
├── fabric/           # Fabric固有のエントリーポイントとコード
├── gradle/
│   └── libs.versions.toml  # 一元管理されたVersion Catalog
└── settings.gradle.kts
```

## Version Catalog

依存関係とプラグインのバージョンは `gradle/libs.versions.toml` に一元管理されており、ルートビルドと`build-logic` の両方からタイプセーフなアクセサ（`libs.xxx`）を通じて利用されます。`build-logic` 内のコンベンションプラグインは、各ローダーサブプロジェクトに共通設定（Javaツールチェーン、リポジトリ、共通依存関係）を適用し、ローダーごとの`build.gradle.kts`を最小限に保ちます。

## ライセンス

詳細は [LICENSE](./LICENSE.txt) を参照してください。
# Jomini YML プラグイン

Paradox Interactive社のゲームエンジンClausewitzのローカライズファイルをOmegaTで読み込み、作業可能にします。

## インストール

jomini-pluginX.X.jar(Xはバージョン番号)
をインストールしたOmegatTのpluginフォルダに入れてOmegaTを起動します。

## 使い方と設定

インストール成功していれば、設定不要です。
YAMLと同じ拡張子なためOkapi Filters for OmegaTと競合することがあります。Okapi Filtersを無効にしてください。

## OmegaTの設定について注意事項

設定＞文節化規則にて
言語名
初期値
前方の正規表現
[\.\?\!]+
後方の正規表現
\s
にチェックが入っていますので、外して使います。

気を利かせて英語の長文を分割し(文節化)、翻訳後に文の間のスペースを削ってくれています。
がローカライズにおいて不要な設定なので無効にします。
もちろんデフォルト設定のままでも構いませんが、文中でのアイコン表示のように、勝手にスペースが削られ詰められると困ることがあります。

## 専用の文節化規則を追加する

上記のようなデフォルト設定の変更はいじりたくなくて、専用の文節化規則を追加することでも調節可能です。
中国語(Chinese)と初期値の中間にオリジナルな規則集を追加します。

言語名
適当な何か(ここではJominiYML用)
言語コードの正規表現
.*

前方の正規表現
@\w+!
後方の正規表現
\s

これらはOmegaT設定の一例で、状況に応じて自分で決めることができます。

## ライセンス

(c) 2022 yamanaiyuki

GNU general public license version 3もしくはそれ以降です。

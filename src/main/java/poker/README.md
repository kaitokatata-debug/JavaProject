# Texas Hold'em Poker (Java)

Javaで実装されたテキサスホールデムポーカーのゲームロジックライブラリです。
ゲームの進行管理、高度なベッティングロジック（サイドポット対応）、役判定機能を提供します。

## 特徴

*   **ゲーム進行**: プリフロップからショーダウンまでの標準的なテキサスホールデムのフローを実装。
*   **アクション**: ベット、コール、レイズ、チェック、フォールド、オールインに対応。
*   **ポット管理**: 複数のプレイヤーがオールインした場合の複雑なサイドポット計算を自動化。
*   **役判定**: 7枚のカードから最強の5枚を選び、勝者を判定。

## クラス構成

主なクラスは `poker` パッケージに含まれています。

*   `TexasHoldemGame`: ゲームの進行を制御するメインクラス。
*   `Table`: プレイヤー、デッキ、ポット、コミュニティカードなどのテーブル状態を管理。
*   `Player`: プレイヤーの手札、チップ、現在のアクション状態を保持。
*   `Action`: プレイヤーの行動（Bet, Call, Foldなど）を実行し、ゲーム状態を更新。
*   `Pot`: メインポットおよびサイドポットの管理コンテナ。
*   `HandEvaluator`: 手札とコミュニティカードから役を判定するユーティリティ。

## 使用例

```java
// ゲームの初期化
TexasHoldemGame game = new TexasHoldemGame();
game.addPlayer(new Player("Alice", 1000));
game.addPlayer(new Player("Bob", 1000));

// ラウンド開始（カード配布）
game.startNewRound();

// アクション実行
game.getPlayers().get(0).doBet(game, 50); // Alice bets 50
game.getPlayers().get(1).doCall(game);    // Bob calls

// フェーズ進行
game.endBettingRound();
game.advanceState(); // Flop
```
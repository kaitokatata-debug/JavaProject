# Java Web Application 学習用プロジェクト

このプロジェクトは、Java と 埋め込みTomcat (Embedded Tomcat) を使用して作成したシンプルなWebアプリケーションです。
ユーザー名の登録、一覧表示、削除を行うことができます。

## プロジェクトの概要

- **機能**: ユーザー名のCRUD（作成、読み取り、削除）機能
- **データベース**: H2 Database（ファイルとして保存される軽量DB）
- **サーバー**: Apache Tomcat（Javaプログラム内で起動する埋め込みモード）
- **ビルドツール**: Maven

## 環境構築 (Windows)

このプロジェクトを実行するには Java (JDK) と Maven が必要です。
Windows パッケージマネージャーの **Scoop** を使ったインストール方法を以下に記載します。

### 1. Scoop のインストール

PowerShell を開き、以下のコマンドを実行して Scoop をインストールします。
（既にインストール済みの場合はスキップしてください）

```powershell
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser # 実行ポリシーの変更
irm get.scoop.sh | iex
```

### 2. Java (JDK) と Maven のインストール

Scoop を使って必要なツールをインストールします。

```powershell
scoop install java maven
```

### 実行方法

#### 1. コマンドラインから実行する場合

ターミナルで以下のコマンドを実行してサーバーを起動します。

```bash
mvn clean compile exec:java -Dexec.mainClass="App"
```

起動後、ブラウザで `http://localhost:8080/` にアクセスしてください。

#### 2. バッチファイルで簡単に起動する場合 (Windows)

プロジェクトフォルダにある `run.bat` をダブルクリックするだけで、サーバーの起動とブラウザの自動オープンが行われます。

### ファイル構成と役割

このプロジェクトの主要なファイルの役割について解説します。

#### 1. Java ソースコード (`src/main/java/`)

プログラムのロジック部分です。

- **`App.java`** (メインクラス)
    - **役割**: アプリケーションの起動スイッチです。
    - **処理内容**:
        1. H2データベースの初期化（テーブル作成）。
        2. Tomcatサーバーの作成と設定（ポート8080など）。
        3. サーブレット（機能）の登録とURLのマッピング。
        4. サーバーの起動待機。

- **`FormServlet.java`**
    - **URL**: `/submit` (POST)
    - **役割**: ユーザー登録処理を行います。
    - **処理内容**: 入力チェック、重複チェック、DBへの保存、リダイレクト。

- **`ListServlet.java`**
    - **URL**: `/list` (GET)
    - **役割**: 登録済みユーザーの一覧を取得します。
    - **処理内容**: DBから全データを取得し、JSPへ渡します。

- **`DeleteServlet.java`**
    - **URL**: `/delete` (POST)
    - **役割**: ユーザー削除処理を行います。
    - **処理内容**: 指定された名前をDBから削除し、一覧へリダイレクトします。

#### 2. Webリソース (`src/main/webapp/`)

画面表示（フロントエンド）に関するファイルです。

- **`index.jsp`**: トップページ兼入力フォーム。エラーメッセージ表示機能付き。
- **`result.jsp`**: 登録完了画面。

#### 3. 設定ファイル


---

## 2. テキサスホールデムポーカー (Javaライブラリ)

`src/main/java/poker` パッケージに含まれる、テキサスホールデムのゲームロジックライブラリです。
ゲームの進行管理、高度なベッティングロジック（サイドポット対応）、役判定機能を提供します。

### 特徴

*   **ゲーム進行**: プリフロップからショーダウンまでの標準的なテキサスホールデムのフローを実装。
*   **アクション**: ベット、コール、レイズ、チェック、フォールド、オールインに対応。
*   **ポット管理**: 複数のプレイヤーがオールインした場合の複雑なサイドポット計算を自動化。
*   **役判定**: 7枚のカードから最強の5枚を選び、勝者を判定。

### クラス構成

主なクラスは `poker` パッケージに含まれています。

*   `TexasHoldemGame`: ゲームの進行を制御するメインクラス。
*   `Table`: プレイヤー、デッキ、ポット、コミュニティカードなどのテーブル状態を管理。
*   `Player`: プレイヤーの手札、チップ、現在のアクション状態を保持。
*   `Action`: プレイヤーの行動（Bet, Call, Foldなど）を実行し、ゲーム状態を更新。
*   `Pot`: メインポットおよびサイドポットの管理コンテナ。
*   `HandEvaluator`: 手札とコミュニティカードから役を判定するユーティリティ。

### 使用例

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

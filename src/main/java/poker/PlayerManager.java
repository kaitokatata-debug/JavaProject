package poker;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * テーブルに参加しているプレイヤーのリスト、ターン進行、ディーラーボタンの位置など、
 * プレイヤーに関連する状態とロジックを管理するクラス。
 */
public class PlayerManager {
    private final List<Player> players;
    private int dealerButtonPosition;
    private int currentPlayerIndex;

    /**
     * コンストラクタ。
     * プレイヤーリストとインデックスを初期化します。
     */
    public PlayerManager() {
        this.players = new ArrayList<>();
        this.dealerButtonPosition = -1;
        this.currentPlayerIndex = -1;
    }

    /**
     * プレイヤーを管理リストに追加します。
     * @param player 追加するプレイヤー
     */
    public void addPlayer(Player player) {
        players.add(player);
    }

    /**
     * 全てのプレイヤーリストを取得します（フォールドしたプレイヤーやチップがないプレイヤーも含む）。
     * @return プレイヤーのリスト
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * 現在フォールドしていない（アクティブな）プレイヤーのリストを取得します。
     * @return アクティブなプレイヤーのリスト
     */
    public List<Player> getActivePlayers() {
        return players.stream()
                      .filter(p -> !p.isFolded())
                      .collect(Collectors.toList());
    }

    /**
     * 全プレイヤーの手札と状態（ベット額など）をクリアします。
     * 新しいラウンドの開始時に使用されます。
     */
    public void clearHands() {
        for (Player player : players) {
            player.clearHand();
        }
    }
    
    /**
     * マネージャーの状態をリセットします。
     * ディーラーボタンの位置などが初期化されます。
     */
    public void reset() {
        dealerButtonPosition = -1;
    }

    /**
     * ディーラーボタンを次の有効なプレイヤー（チップを持っているプレイヤー）に移動します。
     */
    public void moveDealerButton() {
        if (players.isEmpty()) return;

        int nextIndex = dealerButtonPosition;
        for (int i = 0; i < players.size(); i++) {
            nextIndex = (nextIndex + 1) % players.size();
            if (players.get(nextIndex).getChips() > 0) {
                dealerButtonPosition = nextIndex;
                return;
            }
        }
        dealerButtonPosition = (dealerButtonPosition + 1) % players.size();
    }

    /**
     * ターンを次の有効なプレイヤー（フォールドしておらず、チップを持っているプレイヤー）に進めます。
     * 現在のプレイヤーインデックスを更新します。
     */
    public void nextTurn() {
        if (players.isEmpty()) return;

        int startIndex = currentPlayerIndex;
        if (startIndex < 0 || startIndex >= players.size()) {
            startIndex = dealerButtonPosition;
            currentPlayerIndex = dealerButtonPosition;
        }

        for (int i = 0; i < players.size(); i++) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            Player p = players.get(currentPlayerIndex);
            if (!p.isFolded() && p.getChips() > 0) {
                return;
            }
        }
    }

    /**
     * 現在のターンのプレイヤーを取得します。
     * @return 現在のプレイヤー。インデックスが無効な場合はnull。
     */
    public Player getCurrentPlayer() {
        if (currentPlayerIndex >= 0 && currentPlayerIndex < players.size()) {
            return players.get(currentPlayerIndex);
        }
        return null;
    }

    /**
     * 現在のディーラーボタンの位置（インデックス）を取得します。
     * @return ディーラーボタンのインデックス
     */
    public int getDealerButtonPosition() {
        return dealerButtonPosition;
    }

    /**
     * 現在のターンプレイヤーのインデックスを取得します。
     * @return 現在のプレイヤーインデックス
     */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /**
     * 現在のターンプレイヤーのインデックスを設定します。
     * ラウンド開始時などに特定のプレイヤーから開始させるために使用します。
     * @param index 設定するプレイヤーインデックス
     */
    public void setCurrentPlayerIndex(int index) {
        this.currentPlayerIndex = index;
    }
}
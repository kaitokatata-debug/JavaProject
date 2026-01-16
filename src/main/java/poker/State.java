package poker;

/**
 * ゲームの進行状態を表す列挙型。
 */
public enum State {
    WAITING,    // 待機中
    DEALING,    // カード配布中
    PREFLOP,    // プリフロップ（手札配布後）
    FLOP,       // フロップ（コミュニティカード3枚）
    TURN,       // ターン（コミュニティカード4枚目）
    RIVER,      // リバー（コミュニティカード5枚目）
    SHOWDOWN,   // ショーダウン（勝敗判定）
    CLEANUP     // 次のハンドへの準備
}
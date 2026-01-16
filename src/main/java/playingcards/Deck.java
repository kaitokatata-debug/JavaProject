package playingcards;

import java.util.Collections;

/**
 * 52枚のカードデッキを管理するクラス。
 * シャッフルやカードを引く機能を提供します。
 */
public class Deck extends Cards {
    public Deck() {
        super(52, "デッキ");
        initialize();
    }

    /**
     * デッキを初期化します。
     * 52枚のカードを生成し、シャッフルします。
     */
    public void initialize() {
        clear();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                // 親クラスのprotectedフィールドに直接アクセスして追加
                cards.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(cards);
    }

    /**
     * デッキからカードを1枚引きます。
     * @return 引いたカード
     * @throws IllegalStateException デッキが空の場合
     */
    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("デッキが空です");
        }
        return cards.remove(cards.size() - 1);
    }
}
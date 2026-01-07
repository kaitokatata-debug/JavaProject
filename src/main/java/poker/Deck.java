package poker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 52枚のカードデッキを管理するクラス。
 * シャッフルやカードを引く機能を提供します。
 */
public class Deck {
    private final List<Card> cards = new ArrayList<>();

    public Deck() {
        initialize();
    }

    // 52枚のカードを生成してシャッフル
    public void initialize() {
        cards.clear();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(cards);
    }

    // カードを1枚引く
    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("デッキが空です");
        }
        return cards.remove(cards.size() - 1);
    }
}
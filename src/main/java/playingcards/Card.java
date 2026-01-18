package playingcards;

import java.util.Objects;

/**
 * トランプのカード1枚を表すクラス。
 * スート（マーク）とランク（数字）を持ちます。
 */
public class Card implements Comparable<Card> {
    /**
     * カードのスート（マーク）を表す列挙型
     */
    public enum Suit {
        SPADES("♠"), HEARTS("♥"), DIAMONDS("♦"), CLUBS("♣");
        
        private final String icon;
        Suit(String icon) { this.icon = icon; }
        public String getIcon() { return icon; }
    }

    /**
     * カードのランク（数字・強さ）を表す列挙型
     * 2が一番弱く、Aが一番強い設定です。
     */
    public enum Rank {
        TWO(2, "2"), THREE(3, "3"), FOUR(4, "4"), FIVE(5, "5"),
        SIX(6, "6"), SEVEN(7, "7"), EIGHT(8, "8"), NINE(9, "9"),
        TEN(10, "10"), JACK(11, "J"), QUEEN(12, "Q"), KING(13, "K"), ACE(14, "A");

        private final int value;
        private final String label;
        
        Rank(int value, String label) {
            this.value = value;
            this.label = label;
        }
        public int getValue() { return value; }
        public String getLabel() { return label; }
    }

    // 全カードのインスタンスをキャッシュする配列
    private static final Card[][] CACHE = new Card[Suit.values().length][Rank.values().length];

    static {
        for (Suit s : Suit.values()) {
            for (Rank r : Rank.values()) {
                CACHE[s.ordinal()][r.ordinal()] = new Card(s, r);
            }
        }
    }

    private final Suit suit;
    private final Rank rank;

    /**
     * 指定されたスートとランクのカードインスタンスを取得します。
     * @param suit スート
     * @param rank ランク
     * @return キャッシュされたカードインスタンス
     */
    public static Card valueOf(Suit suit, Rank rank) {
        return CACHE[suit.ordinal()][rank.ordinal()];
    }

    private Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() { return suit; }
    public Rank getRank() { return rank; }

    @Override
    public String toString() {
        return suit.getIcon() + rank.getLabel();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return suit == card.suit && rank == card.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, rank);
    }

    // カードの強さ比較用（数字だけで比較）
    @Override
    public int compareTo(Card other) {
        return Integer.compare(this.rank.getValue(), other.rank.getValue());
    }
}
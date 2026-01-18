package poker.cards;

/**
 * 役の強さを表す列挙型（弱い順）
 */
public enum HandRank {
    /** ハイカード（役なし）。 */
    HIGH_CARD(1, "High Card"),
    /** ワンペア。 */
    ONE_PAIR(2, "One Pair"),
    /** ツーペア。 */
    TWO_PAIR(3, "Two Pair"),
    /** スリーカード。 */
    THREE_OF_A_KIND(4, "Three of a Kind"),
    /** ストレート。 */
    STRAIGHT(5, "Straight"),
    /** フラッシュ。 */
    FLUSH(6, "Flush"),
    /** フルハウス。 */
    FULL_HOUSE(7, "Full House"),
    /** フォーカード。 */
    FOUR_OF_A_KIND(8, "Four of a Kind"),
    /** ストレートフラッシュ。 */
    STRAIGHT_FLUSH(9, "Straight Flush"),
    /** ロイヤルフラッシュ。 */
    ROYAL_FLUSH(10, "Royal Flush");

    private final int power;
    private final String label;

    HandRank(int power, String label) {
        this.power = power;
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    /**
     * 次の（より強い）ランクを取得します。
     * @return 次のランク。すでに最強の場合はnullを返します。
     */
    public HandRank next() {
        int nextIndex = this.ordinal() + 1;
        if (nextIndex < values().length) {
            return values()[nextIndex];
        }
        return null;
    }

    /**
     * 前の（より弱い）ランクを取得します。
     * @return 前のランク。すでに最弱の場合はnullを返します。
     */
    public HandRank previous() {
        int prevIndex = this.ordinal() - 1;
        if (prevIndex >= 0) {
            return values()[prevIndex];
        }
        return null;
    }

    /**
     * 指定されたランクより強いかどうかを判定します。
     * @param other 比較対象のランク
     * @return このランクの方が強い場合はtrue
     */
    public boolean isStrongerThan(HandRank other) {
        return this.power > other.power;
    }

    /**
     * 指定されたランクと同じ強さか判定します。
     * @param other 比較対象のランク
     * @return 同じ強さの場合はtrue
     */
    public boolean isSameRank(HandRank other) {
        return this.power == other.power;
    }
}
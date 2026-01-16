package playingcards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 複数のカードを保持する基底クラス。
 * HoleCardsやCommunityCardsの共通処理を定義します。
 */
public abstract class Cards implements Iterable<Card> {
    protected final List<Card> cards = new ArrayList<>();
    private final int maxSize;
    private final String name;

    /**
     * @param maxSize 保持できるカードの最大枚数
     * @param name エラーメッセージ用の表示名（例: "ホールカード"）
     */
    protected Cards(int maxSize, String name) {
        this.maxSize = maxSize;
        this.name = name;
    }

    public void addCard(Card card) {
        if (cards.size() >= maxSize) {
            throw new IllegalStateException(name + "は" + maxSize + "枚までです。");
        }
        cards.add(card);
    }

    public void addCards(List<Card> newCards) {
        if (cards.size() + newCards.size() > maxSize) {
            throw new IllegalStateException(name + "は" + maxSize + "枚までです。");
        }
        cards.addAll(newCards);
    }

    public void clear() {
        cards.clear();
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    public boolean contains(Card card) {
        return cards.contains(card);
    }

    public int size() {
        return cards.size();
    }

    /**
     * このカードリストからk枚を選ぶ全ての組み合わせを生成します。
     * @param k 選ぶ枚数
     * @return 組み合わせのリスト
     */
    public List<List<Card>> getCombinations(int k) {
        List<List<Card>> combinations = new ArrayList<>();
        combine(k, 0, new ArrayList<>(), combinations);
        return combinations;
    }

    private void combine(int k, int start, List<Card> current, List<List<Card>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < cards.size(); i++) {
            current.add(cards.get(i));
            combine(k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    /**
     * 保持しているカードをランクの強い順（降順）にソートします。
     */
    public void sort() {
        sort(this.cards);
    }

    /**
     * 指定されたカードリストをランクの強い順（降順）にソートします。
     * HandEvaluatorなどで使用します。
     * @param list ソート対象のリスト
     */
    public static void sort(List<Card> list) {
        Collections.sort(list, Collections.reverseOrder());
    }

    @Override
    public String toString() {
        return cards.toString();
    }

    @Override
    public Iterator<Card> iterator() {
        return cards.iterator();
    }
}
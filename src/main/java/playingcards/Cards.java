package playingcards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 複数のカードを保持する基底クラス。
 * HoleCardsやCommunityCardsの共通処理を定義します。
 */
public abstract class Cards implements Iterable<Card> {
    private final List<Card> cards = new ArrayList<>();
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

    /**
     * カードを1枚追加します。
     * @param card 追加するカード
     * @throws NullPointerException cardがnullの場合
     * @throws IllegalStateException 最大枚数を超える場合
     */
    public void addCard(Card card) {
        Objects.requireNonNull(card, "card must not be null");
        if (cards.size() >= maxSize) {
            throw new IllegalStateException(name + "は" + maxSize + "枚までです。");
        }
        cards.add(card);
    }

    /**
     * 複数のカードを追加します。
     * @param newCards 追加するカードのリスト
     * @throws NullPointerException newCardsがnullの場合
     * @throws IllegalStateException 最大枚数を超える場合
     */
    public void addCards(List<Card> newCards) {
        Objects.requireNonNull(newCards, "newCards must not be null");
        if (cards.size() + newCards.size() > maxSize) {
            throw new IllegalStateException(name + "は" + maxSize + "枚までです。");
        }
        cards.addAll(newCards);
    }

    /**
     * 保持しているカードを全て削除します。
     */
    public void clear() {
        cards.clear();
    }

    /**
     * 保持しているカードのリスト（コピー）を返します。
     * @return カードリスト
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    /**
     * 指定されたカードが含まれているか判定します。
     * @param card 判定対象のカード
     * @return 含まれている場合はtrue
     */
    public boolean contains(Card card) {
        return cards.contains(card);
    }

    /**
     * 保持しているカードの枚数を返します。
     * @return カード枚数
     */
    public int size() {
        return cards.size();
    }

    /**
     * 指定されたインデックスのカードを取得します。
     * @param index インデックス
     * @return カード
     */
    public Card get(int index) {
        return cards.get(index);
    }

    /**
     * カードが空かどうかを判定します。
     * @return 空の場合はtrue
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * 保持しているカードのストリームを返します。
     * @return カードのストリーム
     */
    public Stream<Card> stream() {
        return cards.stream();
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
     * 保持しているカードをシャッフルします。
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    protected Card removeCard(int index) {
        return cards.remove(index);
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
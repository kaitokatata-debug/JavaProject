package poker.cards;

import playingcards.Cards;

/**
 * 手札とコミュニティカードを合わせた全カードを表すクラス。
 * 役判定のために使用されます。
 */
public class AllCards extends Cards {
    
    public AllCards(Cards holeCards, Cards communityCards) {
        super(7, "全カード");
        // パッケージが異なるためgetter経由で取得
        addCards(holeCards.getCards());
        addCards(communityCards.getCards());
    }
}
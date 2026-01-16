package poker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ポーカーのポット（賭け金）を管理するクラス。
 * メインポットと、プレイヤーがオールインした場合のサイドポットを扱います。
 * このクラスはポットのコンテナとして機能し、ポットの分割ロジックはゲーム進行クラスが担当します。
 */
public class Pot {

    /**
     * 個々のポット（メインまたはサイド）を表す内部クラス。
     */
    public static class SubPot {
        private int amount;
        private final Set<Player> eligiblePlayers;

        public SubPot() {
            this.amount = 0;
            this.eligiblePlayers = new HashSet<>();
        }

        public void addAmount(int amount) {
            if (amount < 0) {
                throw new IllegalArgumentException("負の金額は追加できません。");
            }
            this.amount += amount;
        }

        public void addPlayer(Player player) {
            this.eligiblePlayers.add(player);
        }
        
        public void addPlayers(Set<Player> players) {
            this.eligiblePlayers.addAll(players);
        }

        public int getAmount() {
            return amount;
        }

        public Set<Player> getEligiblePlayers() {
            return eligiblePlayers;
        }

        @Override
        public String toString() {
            String playerNames = eligiblePlayers.stream()
                                                .map(Player::getName)
                                                .collect(Collectors.joining(", "));
            return "Pot: " + amount + ", Players: [" + playerNames + "]";
        }
    }

    private final List<SubPot> subPots;

    public Pot() {
        this.subPots = new ArrayList<>();
    }

    /**
     * 新しいサブポットを追加します。
     * @param subPot 追加するサブポット
     */
    public void addSubPot(SubPot subPot) {
        this.subPots.add(subPot);
    }

    /**
     * ポットの総額を計算して返します。
     * @return 全てのサブポットの合計金額
     */
    public int getTotalAmount() {
        return subPots.stream().mapToInt(SubPot::getAmount).sum();
    }
    
    /**
     * ポットをクリアし、新しいハンドの準備をします。
     */
    public void clear() {
        subPots.clear();
    }

    /**
     * 全てのサブポットのリストを返します。
     * @return サブポットのリスト
     */
    public List<SubPot> getSubPots() {
        return subPots;
    }

    @Override
    public String toString() {
        return "Total Pot: " + getTotalAmount() + ", SubPots: " + subPots;
    }
}
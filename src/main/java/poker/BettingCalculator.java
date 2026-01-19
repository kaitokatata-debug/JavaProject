package poker;

/**
 * ベット額の計算ロジックを担当するクラス。
 */
public class BettingCalculator {
    private final Table table;

    public BettingCalculator(Table table) {
        this.table = table;
    }

    /**
     * 指定されたプレイヤーのベットオプションを計算します。
     * @param player 対象プレイヤー
     * @return ベットオプション
     */
    public BettingOptions calculate(Player player) {
        int currentHighest = table.getCurrentHighestBet();
        int myBet = player.getCurrentBet();
        int callAmt = currentHighest - myBet;
        
        // 最小レイズ額の計算 (Action.validateのロジックに合わせる)
        // (currentBet + amount) >= currentHighest * 2
        // amount >= (currentHighest * 2) - currentBet
        int minRaiseTotal = (currentHighest == 0) ? 10 : (currentHighest * 2);
        int minAmount = minRaiseTotal - myBet;
        int maxAmount = player.getChips();

        // ポット額の計算
        int totalPot = table.getPot().getTotalAmount();
        for(Player p : table.getPlayers()) {
            totalPot += p.getCurrentBet();
        }

        int halfPotAmt;
        int potAmt;

        if (currentHighest == 0) {
            halfPotAmt = Math.max(minAmount, totalPot / 2);
            potAmt = Math.max(minAmount, totalPot);
        } else {
            // ポットレイズ計算: コール額 + (コール後のポット総額)
            int potAfterCall = totalPot + callAmt;
            halfPotAmt = callAmt + (potAfterCall / 2);
            potAmt = callAmt + potAfterCall;
        }

        // 範囲制限
        halfPotAmt = Math.min(Math.max(halfPotAmt, minAmount), maxAmount);
        potAmt = Math.min(Math.max(potAmt, minAmount), maxAmount);
        
        // minAmountが所持金を超えている場合はAll-Inのみ（maxAmountに合わせる）
        if (minAmount > maxAmount) minAmount = maxAmount;

        return new BettingOptions(minAmount, maxAmount, halfPotAmt, potAmt, callAmt);
    }
}
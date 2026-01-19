package poker;

/**
 * プレイヤーが選択可能なベット額のオプション情報を保持するクラス。
 */
public class BettingOptions {
    private final int minAmount;
    private final int maxAmount;
    private final int halfPotAmount;
    private final int potAmount;
    private final int callAmount;

    public BettingOptions(int minAmount, int maxAmount, int halfPotAmount, int potAmount, int callAmount) {
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.halfPotAmount = halfPotAmount;
        this.potAmount = potAmount;
        this.callAmount = callAmount;
    }

    public int getMinAmount() { return minAmount; }
    public int getMaxAmount() { return maxAmount; }
    public int getHalfPotAmount() { return halfPotAmount; }
    public int getPotAmount() { return potAmount; }
    public int getCallAmount() { return callAmount; }
}
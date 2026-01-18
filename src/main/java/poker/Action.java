package poker;

/**
 * プレイヤーのアクション（ベット、コール、フォールドなど）を定義し、実行するクラス。
 * Player, Game, Tableと連携してゲームの状態を更新します。
 */
public class Action {
    public enum Type {
        FOLD, CHECK, CALL, BET, RAISE, ALL_IN
    }

    private final Player player;
    private final Type type;
    private final int amount;

    /**
     * アクションを生成します。
     * 金額を伴わないアクション（FOLD, CHECK, CALL, ALL_INなど）に使用します。
     * @param player アクションを行うプレイヤー
     * @param type アクションの種類
     */
    public Action(Player player, Type type) {
        this(player, type, 0);
    }

    /**
     * アクションを生成します。
     * 金額を伴うアクション（BET, RAISEなど）に使用します。
     * @param player アクションを行うプレイヤー
     * @param type アクションの種類
     * @param amount ベットまたはレイズする金額
     */
    public Action(Player player, Type type, int amount) {
        this.player = player;
        this.type = type;
        this.amount = amount;
    }

    /**
     * アクションを実行し、ゲームの状態（ベット額、ログなど）を更新します。
     * @param game 対象のゲームインスタンス
     */
    public void execute(TexasHoldemGame game) {
        if (player.isFolded()) return;

        String actionText = null;

        switch (type) {
            case FOLD:
                player.fold();
                actionText = "Fold";
                game.log(player.getName() + " folds");
                break;

            case CALL:
            case CHECK:
                int currentHighest = game.getCurrentHighestBet();
                int amountToCall = currentHighest - player.getCurrentBet();
                if (amountToCall > 0) {
                    player.bet(amountToCall);
                    actionText = "Call";
                    game.log(player.getName() + " calls " + amountToCall);
                } else {
                    actionText = "Check";
                    game.log(player.getName() + " checks");
                }
                break;

            case BET:
            case RAISE:
                player.bet(amount);
                if (player.getCurrentBet() > game.getCurrentHighestBet()) {
                    game.setCurrentHighestBet(player.getCurrentBet());
                }
                actionText = (type == Type.BET ? "Bet " : "Raise ") + amount;
                game.log(player.getName() + " bets " + amount + " (Total: " + player.getCurrentBet() + ")");
                break;

            case ALL_IN:
                int allInAmount = player.getChips();
                player.bet(allInAmount);
                if (player.getCurrentBet() > game.getCurrentHighestBet()) {
                    game.setCurrentHighestBet(player.getCurrentBet());
                }
                actionText = "All In";
                game.log(player.getName() + " goes ALL-IN (" + allInAmount + ")");
                break;
        }
        player.setLastAction(actionText);
    }

    /**
     * アクションが有効かどうかを検証します。
     * @param game 対象のゲームインスタンス
     * @return 有効な場合はtrue、無効な場合はfalse
     */
    public boolean validate(TexasHoldemGame game) {
        if (player.isFolded()) return false;

        switch (type) {
            case FOLD:
                return true;

            case CHECK:
                return player.getCurrentBet() == game.getCurrentHighestBet();

            case CALL:
                return player.getCurrentBet() < game.getCurrentHighestBet();

            case BET:
                return amount > 0 && amount <= player.getChips();

            case RAISE:
                if (amount <= 0 || amount > player.getChips()) {
                    return false;
                }
                return (player.getCurrentBet() + amount) > game.getCurrentHighestBet();

            case ALL_IN:
                return player.getChips() > 0;
        }
        return false;
    }
}
package net.monsterdev.automosreg.enums;

public enum TradeStatus {
    EMPTY(0, "Все"),
    SUGGESTIONS(15, "Прием предложений"),
    AGREEING(20, "Согласование"),
    CONTRACTING(40, "Заключение договора"),
    CONTRACTED(50, "Договор заключен"),
    WITHOUT_PROPOSALS(25, "Нет предложений"),
    CANCELED(30, "Отменена"),
    ARCHIVED(-10, "В архиве");

    private Integer code;
    private String description;

    TradeStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TradeStatus valueOf(int code) {
        switch (code) {
            case 15: return SUGGESTIONS;
            case 20: return AGREEING;
            case 40: return CONTRACTING;
            case 50: return CONTRACTED;
            case 25: return WITHOUT_PROPOSALS;
            case 30: return CANCELED;
            case -10: return ARCHIVED;
            default: return EMPTY;
        }
    }
}

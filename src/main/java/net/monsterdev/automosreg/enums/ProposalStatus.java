package net.monsterdev.automosreg.enums;

public enum ProposalStatus {
    UNDEFINED(-1, "Неопределено"),
    /**
     * Начальное состояние. Для предложения со статусом INITAL еще не создан черновик
     */
    INITIAL(0, "Начальное"),
    /**
     * Предложение активировано, т.е. учавствует в торгах
     */
    ACTIVE(5, "Действительно"),
    /**
     * Предложение отозвано
     */
    REVOKED(10, "Отозвано"),
    /**
     * Для предложения со статусом DRAFT создан черновик
     */
    DRAFT(15, "Черновик");

    private Integer code;
    private String description;

    ProposalStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ProposalStatus valueOf(int status) {
        switch (status) {
            case 0:
                return INITIAL;
            case 5:
                return ACTIVE;
            case 10:
                return REVOKED;
            case 15:
                return DRAFT;
            default:
                return INITIAL;
        }
    }
}

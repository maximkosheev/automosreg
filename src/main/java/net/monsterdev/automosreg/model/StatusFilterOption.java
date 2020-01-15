package net.monsterdev.automosreg.model;

import lombok.Data;

@Data
public class StatusFilterOption {
    // все закупки
    public static final int ALL = 0;
    // закупка по которой ведется наблюдение, но еще не подано предложение
    public static final int OPENED = 1;
    // закупка завершена (аукцион выигран или проигран)
    public static final int CLOSED = 2;
    // закупка, по которой подано предложение, ведется мониторинг и торги
    public static final int ACTIVE = 3;
    // закупка переведена в архив - по ней не ведется мониторинг и торги
    public static final int ARCHIVED = 4;

    private String desciption;
    private int code;

    public StatusFilterOption(int code, String desciption) {
        this.code = code;
        this.desciption = desciption;
    }

    @Override
    public String toString() {
        return desciption;
    }
}

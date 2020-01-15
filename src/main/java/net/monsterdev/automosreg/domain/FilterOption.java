package net.monsterdev.automosreg.domain;

import lombok.Data;
import net.monsterdev.automosreg.enums.FilterType;
import net.monsterdev.automosreg.utils.StringUtil;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "Filters")
@Data
public class FilterOption {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    @Enumerated(EnumType.ORDINAL)
    private FilterType type;

    @Column(name = "options")
    private String options;

    public Map<String, String> getFields() {
        Map<String, String> fields = new HashMap<>();
        String[] pairs = options.split(";");
        for (String pair : pairs) {
            String[] keyvalue = pair.split("=");
            fields.put(keyvalue[0], keyvalue[1]);
        }
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        options = "";
        fields.forEach((key, value) -> {
            options += (key + "=" + value + ";");
        });
        System.out.println(options);
    }

    @Override
    public String toString() {
        return name;
    }

    public static String prepareString(String string)  {
        return StringUtil.removeAll(StringUtil.removeWhitespaces(string), ";", "=");
    }
}

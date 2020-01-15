package net.monsterdev.automosreg.domain;

import lombok.Data;
import org.json.JSONObject;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private long id;
    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;
    @Column(name = "email", length = 50, nullable = false)
    private String email;
    @Column(name = "fax", length = 25)
    private String fax;
    @Column(name = "phone", length = 25, nullable = false)
    private String phone;
    @Column(name = "firstname", length = 20, nullable = false)
    private String firstName;
    @Column(name = "surname", length = 20, nullable = false)
    private String  surname;
    @Column(name = "fathername", length = 20, nullable = false)
    private String fatherName;
    @Column(name = "use_NDS", nullable = false)
    private boolean useNDS;
    @Column(name="NDS")
    private int NDS;
    @Column(name = "cert_name")
    private String certName;
    @Column(name = "cert_hash")
    private String certHash;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "users_documents",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "document_id"))
    private List<Document> documents = new ArrayList<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Trade> trades = new ArrayList<>();
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_dt", updatable = false)
    private Date createDT;

    @Override
    public String toString() {
        return name;
    }

    public JSONObject getContactInfo() {
        JSONObject contactInfo = new JSONObject();
        contactInfo.put("ContactInfoEmail", getEmail());
        contactInfo.put("ContactInfoFax", getFax());
        contactInfo.put("ContactInfoPhone", getPhone());
        contactInfo.put("ContactInfoFirstName", getFirstName());
        contactInfo.put("ContactInfoMiddleName", getFatherName());
        contactInfo.put("ContactInfoLastName", getSurname());
        return contactInfo;
    }

    public void addTrade(Trade trade) {
        trade.setUser(this);
        trades.add(trade);
    }

    public void removeTrade(long tradeId) {
        trades.removeIf(trade -> trade.getTradeId().equals(tradeId));
    }

    public Trade getTrade(long tradeId) {
        for (Trade trade : trades) {
            if (trade.getTradeId().equals(tradeId))
                return trade;
        }
        return null;
    }
}

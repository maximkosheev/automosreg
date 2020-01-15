package net.monsterdev.automosreg.model;

import lombok.Data;
import ru.CryptoPro.JCP.JCP;

import javax.persistence.*;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Data
public class CertificateInfo {
    /**
     * Тип хранилища ключа, например, RuToken, EToken и т.д.
     * Данное приложение работает только с поставщиком JCP.
     * Т.е. поставщик создается только так Security.getProvider("JCP")
     */
    private String storeType;
    private String name;
    private String alias;
    private Date validFrom;
    private Date validTo;
    private String hash;
    private PrivateKey privateKey = null;
    private List<Certificate> chain = new ArrayList<>();
    private String password;

    public String getValidity() {
        DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        return format.format(validFrom) + " - " + format.format(validTo);
    }

    public void loadInfo() throws
            NoSuchProviderException,
            KeyStoreException,
            CertificateException,
            NoSuchAlgorithmException,
            IOException,
            UnrecoverableKeyException
    {
        KeyStore keyStore = KeyStore.getInstance(storeType, "JCP");
        keyStore.load(null, null);
        privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
        chain = Arrays.asList(keyStore.getCertificateChain(alias));
    }

    public String getDigestOid() {

        String privateKeyAlgorithm = privateKey.getAlgorithm();

        if (privateKeyAlgorithm.equalsIgnoreCase(JCP.GOST_EL_2012_256_NAME) ||
                privateKeyAlgorithm.equalsIgnoreCase(JCP.GOST_DH_2012_256_NAME)) {
            return JCP.GOST_DIGEST_2012_256_OID;
        } // if
        else if (privateKeyAlgorithm.equalsIgnoreCase(JCP.GOST_EL_2012_512_NAME) ||
                privateKeyAlgorithm.equalsIgnoreCase(JCP.GOST_DH_2012_512_NAME)) {
            return JCP.GOST_DIGEST_2012_512_OID;
        } // if

        return JCP.GOST_DIGEST_OID;
    }

    public String getPublicKeyOid() {

        String privateKeyAlgorithm = privateKey.getAlgorithm();

        if (privateKeyAlgorithm.equalsIgnoreCase(JCP.GOST_EL_2012_256_NAME) ||
                privateKeyAlgorithm.equalsIgnoreCase(JCP.GOST_DH_2012_256_NAME)) {
            return JCP.GOST_PARAMS_SIG_2012_256_KEY_OID;
        } // if
        else if (privateKeyAlgorithm.equalsIgnoreCase(JCP.GOST_EL_2012_512_NAME) ||
                privateKeyAlgorithm.equalsIgnoreCase(JCP.GOST_DH_2012_512_NAME)) {
            return JCP.GOST_PARAMS_SIG_2012_512_KEY_OID;
        } // if
        else if (privateKeyAlgorithm.equalsIgnoreCase(JCP.GOST_EL_DEGREE_NAME) ||
                privateKeyAlgorithm.equalsIgnoreCase(JCP.GOST_EL_DH_NAME)) {
            return JCP.GOST_EL_KEY_OID;
        } // if

        return privateKeyAlgorithm;
    }

}

package net.monsterdev.automosreg.services.impl;

import net.monsterdev.automosreg.model.CertificateInfo;
import net.monsterdev.automosreg.services.CryptoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.cms.Time;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.util.CollectionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESType;
import ru.CryptoPro.CAdES.exception.CAdESException;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

@Service
@Qualifier("cryptoService")
public class CryptoServiceImpl implements CryptoService {
    private static final String ERROR_COMMON_LOG_MSG = "CryptoServiceImpl error: %s : %s";

    private static Logger LOG = LogManager.getLogger(CryptoServiceImpl.class);
    private List<CertificateInfo> certificateInfos = null;

    /**
     * Возвращает список типов ключевых хранилищ для определенного провайдера
     *
     * @param provider - провайдер
     * @return список ключеных хранилищ, доступных через данный провайдер
     */
    private static List<String> getKeyStoreTypesForProvider(Provider provider) {
        List<String> result = new ArrayList<>();
        for (Provider.Service service : provider.getServices()) {
            if (service.getType().equals("KeyStore")) {
                result.add(service.getAlgorithm());
            }
        }
        return result;
    }

    /**
     * Возвращает хеш-код сертификата
     *
     * @param certificate - сертификат, чей хеш-код нужно вычислить
     * @return строковое представления хеш-кода (символы шестнадцатиричных чисел)
     */
    private static String getCertHash(X509Certificate certificate) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] certDER = certificate.getEncoded();
            messageDigest.update(certDER);
            return DatatypeConverter.printHexBinary(messageDigest.digest());
        } catch (Throwable t) {
            LOG.error(String.format(ERROR_COMMON_LOG_MSG, t.getClass(), t.getMessage()));
            return null;
        }
    }

    @Override
    public List<CertificateInfo> getCertificatesList() {
        if (certificateInfos != null)
            return certificateInfos;

        certificateInfos = new ArrayList<>();

        try {
            /* используется провайдер JCP от КриптоПРО */
            // получаем список типов ключевых хранилищ, доступных через провайдер JCP
            List<String> keyStoreTypes = getKeyStoreTypesForProvider(Security.getProvider("JCP"));
            /* формируем список сертификатов, содержащихся в хранилищах */
            for (String keyStoreType : keyStoreTypes) {
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                Enumeration<String> aliases = keyStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = aliases.nextElement();
                    if (keyStore.isKeyEntry(alias)) {
                        java.security.cert.Certificate cert = keyStore.getCertificate(alias);
                        if (cert.getType().equals("X.509")) {
                            X509Certificate x509Certificate = (X509Certificate) cert;
                            X500Name x500Name = new JcaX509CertificateHolder(x509Certificate).getSubject();
                            RDN cn = x500Name.getRDNs(BCStyle.CN)[0];
                            CertificateInfo certificateInfo = new CertificateInfo();
                            certificateInfo.setName(IETFUtils.valueToString(cn.getFirst().getValue()));
                            certificateInfo.setValidFrom(x509Certificate.getNotBefore());
                            certificateInfo.setValidTo(x509Certificate.getNotAfter());
                            certificateInfo.setStoreType(keyStoreType);
                            certificateInfo.setAlias(alias);
                            certificateInfo.setHash(getCertHash(x509Certificate));
                            certificateInfo.setPassword("12345678");
                            certificateInfos.add(certificateInfo);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            LOG.error(String.format(ERROR_COMMON_LOG_MSG, t.getClass(), t.getMessage()));
        }

        return certificateInfos;
    }

    @PostConstruct
    private void loadCertificateList() {
        certificateInfos = getCertificatesList();
    }

    @Override
    public CertificateInfo getCertificateByHash(String hashCode) {
        if (certificateInfos == null)
            return null;

        return certificateInfos
                .stream()
                .filter(certificate -> certificate.getHash().equalsIgnoreCase(hashCode))
                .findFirst()
                .orElse(null);
    }

    private AttributeTable getSomeSignedAttributes(boolean signTime, boolean email) {

        final Hashtable<ASN1ObjectIdentifier, Attribute> table = new Hashtable<>();

        if (signTime) {
            Attribute attr = new Attribute(CMSAttributes.signingTime, new DERSet(new Time(new Date())));
            table.put(attr.getAttrType(), attr);
        } // if

        if (email) {
            Attribute attr = new Attribute(PKCSObjectIdentifiers.pkcs_9_at_emailAddress, new DERSet(new DERIA5String("some@mail.ru")));
            table.put(attr.getAttrType(), attr);
        } // if
        return new AttributeTable(table);
    }

    private AttributeTable getSignedAttributes() {
        return getSomeSignedAttributes(true, false);
    }

    private AttributeTable getUnsignedAttributes() {
        return getSomeSignedAttributes(false, true);
    }

    @Override
    public byte[] signData(CertificateInfo certificateInfo, String dataForSign) {
        try {
            List<X509Certificate> chain = new ArrayList<>();
            for (Certificate certificate : certificateInfo.getChain()) {
                chain.add((X509Certificate) certificate);
            }

            Collection<X509CertificateHolder> chainHolder = new ArrayList<>();
            for (X509Certificate certificate : chain) {
                chainHolder.add(new X509CertificateHolder(certificate.getEncoded()));
            }

            CAdESSignature cadesSignature = new CAdESSignature(true);
            cadesSignature.setCertificateStore(new CollectionStore(chainHolder));
            // при инстанцировании CAdESSignature еще нужно указать CRL (список отзывов сертификатов - СОС)
            // СОС - это некий файл, который нужно прочитать
            // TODO: Загрузка файла СОС
            cadesSignature.addSigner("JCP",
                    null,
                    //certificateInfo.getDigestOid(),
                    null,
                    //certificateInfo.getPublicKeyOid(),
                    certificateInfo.getPrivateKey(),
                    certificateInfo.getChain(),
                    CAdESType.CAdES_BES,
                    null, false,
                    getSignedAttributes(),
                    null);

            ByteArrayOutputStream outSignatureStream = new ByteArrayOutputStream();

            cadesSignature.open(outSignatureStream);

            byte[] bytesToSing = Base64.getDecoder().decode(dataForSign.getBytes(StandardCharsets.UTF_8));
            cadesSignature.update(bytesToSing);

            // Завершаем создание подписи с двумя подписантами.
            cadesSignature.close();
            outSignatureStream.close();

            return Base64.getEncoder().encode(outSignatureStream.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}

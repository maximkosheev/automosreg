package net.monsterdev.automosreg.services;

import net.monsterdev.automosreg.model.CertificateInfo;

import java.util.List;

public interface CryptoService {
    /**
     * Возвращает список сертификатов установленных в системе
     * @return список сертификатов, установленных в системе
     */
    List<CertificateInfo> getCertificatesList();

    /**
     * Возвращает сертификат по его хеш-код
     * @param hashCode хеш-код сертификата
     * @return сертификат, имеющий указанный хеш-код, или null, если сертификат не найден
     */
    CertificateInfo getCertificateByHash(String hashCode);

    /**
     * Осуществляет подпись некоторой строки
     * @param certificateInfo сертификат ключа, которым выполняется подпись
     * @param dataForSign строка, которую нужно подписать
     * @return полученная подпись
     */
    byte[] signData(CertificateInfo certificateInfo, String dataForSign);
}

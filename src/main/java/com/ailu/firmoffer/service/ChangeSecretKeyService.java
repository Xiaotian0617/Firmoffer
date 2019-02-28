package com.ailu.firmoffer.service;

import com.ailu.firmoffer.dao.bean.FirmOfferKey;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/28 10:56
 */
public interface ChangeSecretKeyService {
    /**
     * 更改用户的key
     *
     * @param userId
     * @param exChange
     * @param key
     * @param secret
     * @return
     */
    boolean changeSecretKeyByUser(Long userId, String exChange, String key, String secret);

    /**
     * @param userId
     * @param exChange
     * @param key
     * @param secret
     * @return
     */
    FirmOfferKey insertSecretKey(Long userId, String exChange, String key, String secret);
}

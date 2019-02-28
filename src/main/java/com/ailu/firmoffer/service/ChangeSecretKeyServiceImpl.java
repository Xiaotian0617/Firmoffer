package com.ailu.firmoffer.service;

import com.ailu.firmoffer.dao.bean.FirmOfferKey;
import com.ailu.firmoffer.dao.bean.FirmOfferKeyExample;
import com.ailu.firmoffer.dao.mapper.FirmOfferKeyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/28 10:56
 */
@Slf4j
@Service
public class ChangeSecretKeyServiceImpl implements ChangeSecretKeyService {

    @Resource
    FirmOfferKeyMapper keyMapper;

    @Override
    public boolean changeSecretKeyByUser(Long userId, String exChange, String key, String secret) {

        FirmOfferKeyExample example = new FirmOfferKeyExample();
        example.or().andUserIdEqualTo(userId).andExChangeEqualTo(exChange);

        List<FirmOfferKey> list = keyMapper.selectByExample(example);
        if (null == list || list.size() == 0) {
            return false;
        }

        FirmOfferKey userKey = list.get(0);
        userKey.setApikey(key);
        userKey.setApikeysecret(secret);
        userKey.setStatus(1);
        int i = keyMapper.updateByExample(userKey, example);
        return i > 0;
    }

    @Override
    public FirmOfferKey insertSecretKey(Long userId, String exChange, String key, String secret) {
        FirmOfferKey obj = new FirmOfferKey();
        obj.setApikey(key);
        obj.setApikeysecret(secret);
        obj.setExChange(exChange);
        obj.setUserId(userId);
        obj.setStatus(1);
        int i = keyMapper.insert(obj);
        if (i > 0) {
            return obj;
        }
        return null;
    }


}

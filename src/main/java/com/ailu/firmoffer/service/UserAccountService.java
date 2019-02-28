package com.ailu.firmoffer.service;

import com.ailu.firmoffer.dao.bean.UserAccountBalance;
import com.ailu.firmoffer.web.vo.UserAssetRatioVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/23 19:49
 */
public interface UserAccountService {

    /**
     * @param userId
     * @return
     */
    List<UserAccountBalance> getUserAccountBalance(Long userId);

    /**
     * @param userId
     * @param exchange
     * @return
     */
    List<UserAccountBalance> getUserExchangeAccountBalance(Long userId, String exchange);


    /**
     * 获取用户的资产配置饼状图。（各个交易所的和）
     *
     * @param userId
     * @return
     */
    List<UserAssetRatioVo> getUserAccoutRatio(Long userId);

    BigDecimal getInitUserBalanceByUserId(Long userId);

    List<UserAssetRatioVo> getUserAccoutRatio(Long userId, String stock);
}

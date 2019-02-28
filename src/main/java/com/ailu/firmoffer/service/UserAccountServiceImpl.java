package com.ailu.firmoffer.service;

import com.ailu.firmoffer.dao.bean.UserAccountBalance;
import com.ailu.firmoffer.dao.mapper.ext.UserAccountExt;
import com.ailu.firmoffer.web.vo.UserAssetRatioVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/24 14:56
 */
@Slf4j
@Service
public class UserAccountServiceImpl implements UserAccountService {

    @Resource
    UserAccountExt userAccountExt;
    @Resource
    MetaService metaService;

    @Override
    public List<UserAccountBalance> getUserAccountBalance(Long userId) {
        List<UserAccountBalance> balance = userAccountExt.getUserAccountBalance(userId);
        return balance;
    }

    @Override
    public List<UserAccountBalance> getUserExchangeAccountBalance(Long userId, String exchange) {
        List<UserAccountBalance> balance = userAccountExt.getUserAccountExchangeBalance(userId, exchange);
        return balance;
    }

    @Override
    public List<UserAssetRatioVo> getUserAccoutRatio(Long userId) {
        List<UserAssetRatioVo> amountlist = getUserCoinAmountByUserIdAndType(userId, null);
        //将币种转换为统一单位的价格
        List<UserAssetRatioVo> pricelist = metaService.getUserAssetRatioVosForPrice(amountlist);
        //计算所有币种的总和
        BigDecimal total = BigDecimal.ZERO;
        for (UserAssetRatioVo priceVo : pricelist) {
            if (null == priceVo || null == priceVo.getPrice()) {
                priceVo.setPrice(BigDecimal.ZERO);
            }
            total = total.add(priceVo.getPrice());
        }
        //计算每个币种对应的比率
        List<UserAssetRatioVo> ratioVoList = new ArrayList<>();
        Map<Integer, BigDecimal> coinmap = new HashMap<>();//币种对应总值
        Map<Integer, String> symbolmap = new HashMap<>();//币种对应名称
        BigDecimal ciontotal = BigDecimal.ZERO;//币总数
        for (UserAssetRatioVo ratioVo : pricelist) {
            int coin = ratioVo.getCoin();//币id
            String symbol = ratioVo.getSymbol();//币名
            BigDecimal price = ratioVo.getPrice();
            if (coinmap.containsKey(coin)) {
                ciontotal = coinmap.get(coin).add(price);
            } else {
                ciontotal = price;
                symbolmap.put(coin, symbol);
            }
            coinmap.put(coin, ciontotal);
        }
        for (Integer cionkey : coinmap.keySet()) {
            BigDecimal ratio = new BigDecimal("0.0");//比率
            BigDecimal ciontotal2 = coinmap.get(cionkey);//币值
            String symbol2 = symbolmap.get(cionkey);//币名
            if (total.compareTo(BigDecimal.ZERO) == 0 || ciontotal2.compareTo(BigDecimal.ZERO) == 0) {
                ratio = BigDecimal.ZERO;
            } else {
                ratio = ciontotal2.divide(total, 4, BigDecimal.ROUND_HALF_DOWN);
            }
            UserAssetRatioVo ratioVo = new UserAssetRatioVo();
            ratioVo.setSymbol(symbol2);//存放对应币种的名字
            ratioVo.setCoin(cionkey);
            ratioVo.setRatio(ratio.multiply(new BigDecimal(100)));
            ratioVo.setPrice(ciontotal2);
            ratioVoList.add(ratioVo);
        }
        return ratioVoList;
    }

    @Override
    public BigDecimal getInitUserBalanceByUserId(Long userId) {
        return userAccountExt.getInitUserBalanceByUserId(userId);
    }

    private List<UserAssetRatioVo> getUserCoinAmountByUserIdAndType(Long userId,String type){
        List<UserAssetRatioVo> amountlist;
        //从数据库获取不同币种对应的总数
        if (StringUtils.hasText(type)){
            amountlist = userAccountExt.getUserCoinAmountByUserIdAndType(userId,type);
        }else {
            amountlist = userAccountExt.getUserCoinAmount(userId);
        }
        return amountlist;
    }

    @Override
    public List<UserAssetRatioVo> getUserAccoutRatio(Long userId, String stock) {
        //从数据库获取不同币种对应的总数
        List<UserAssetRatioVo> amountlist = getUserCoinAmountByUserIdAndType(userId, stock);
        //将币种转换为统一单位的价格
        List<UserAssetRatioVo> pricelist = metaService.getUserAssetRatioVosForPrice(amountlist);
        //计算所有币种的总和
        BigDecimal total = BigDecimal.ZERO;
        for (UserAssetRatioVo priceVo : pricelist) {
            if (null == priceVo || null == priceVo.getPrice()) {
                priceVo.setPrice(BigDecimal.ZERO);
            }
            total = total.add(priceVo.getPrice());
        }
        //计算每个币种对应的比率
        List<UserAssetRatioVo> ratioVoList = new ArrayList<>();
        Map<Integer, BigDecimal> coinmap = new HashMap<>();//币种对应总值
        Map<Integer, String> symbolmap = new HashMap<>();//币种对应名称
        BigDecimal ciontotal = BigDecimal.ZERO;//币总数
        for (UserAssetRatioVo ratioVo : pricelist) {
            int coin = ratioVo.getCoin();//币id
            String symbol = ratioVo.getSymbol();//币名
            BigDecimal price = ratioVo.getPrice();
            if (coinmap.containsKey(coin)) {
                ciontotal = coinmap.get(coin).add(price);
            } else {
                ciontotal = price;
                symbolmap.put(coin, symbol);
            }
            coinmap.put(coin, ciontotal);
        }
        for (Integer cionkey : coinmap.keySet()) {
            BigDecimal ratio = new BigDecimal("0.0");//比率
            BigDecimal ciontotal2 = coinmap.get(cionkey);//币值
            String symbol2 = symbolmap.get(cionkey);//币名
            if (total.compareTo(BigDecimal.ZERO) == 0 || ciontotal2.compareTo(BigDecimal.ZERO) == 0) {
                ratio = BigDecimal.ZERO;
            } else {
                ratio = ciontotal2.divide(total, 4, BigDecimal.ROUND_HALF_DOWN);
            }
            UserAssetRatioVo ratioVo = new UserAssetRatioVo();
            ratioVo.setSymbol(symbol2);//存放对应币种的名字
            ratioVo.setCoin(cionkey);
            ratioVo.setRatio(ratio.multiply(new BigDecimal(100)));
            ratioVo.setPrice(ciontotal2);
            ratioVoList.add(ratioVo);
        }
        return ratioVoList;
    }

}

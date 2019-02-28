package com.ailu.firmoffer.domain;

import com.ailu.firmoffer.dao.bean.FirmOfferCoinContrasts;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/5/8 10:10
 */
@Data
public class CoinContrasts extends FirmOfferCoinContrasts {

    private String childSymbol;

    private String symbol;

    @Override
    public String getSymbol() {
        if (StringUtils.hasText(symbol)) {
            return symbol;
        }
        return "";
    }
}

package com.ailu.firmoffer.web.vo;

import com.ailu.firmoffer.dao.bean.FirmOfferPosition;
import com.ailu.firmoffer.exchange.conversion.BitmexConversion;
import com.ailu.firmoffer.exchange.conversion.OkexV3Conversion;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

import static com.ailu.firmoffer.config.ExchangeName.EX_CHANGE_BITMEX;
import static com.ailu.firmoffer.config.ExchangeName.EX_CHANGE_OKEX;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/11/26 16:54
 */
@Data
public class FirmOfferPositionVo extends FirmOfferPosition {

    /**
     * 数量 （由张换算的结果）
     * <p>
     * 张数和个数是单个合约的数量，张数为基础，通过张数换算个数。算法是 个数=张数*10/对应价格
     * <p>
     * 例如：EOS季度多100张，个数就是100*10/EOS季度多的价格
     * <p>
     * BTC把10换成100
     */
    private BigDecimal amount;

    private String bitmexAmountUnit;

    private String bitMexQtyUnit;

    private int positionStatus;//持仓状态

    private BigDecimal value;


    public FirmOfferPositionVo() {
    }

    public FirmOfferPositionVo(OkexV3Conversion okexV3Conversion, BitmexConversion bitmexConversion) {
        this.okexV3Conversion = okexV3Conversion;
        this.bitmexConversion = bitmexConversion;
    }

    OkexV3Conversion okexV3Conversion;

    BitmexConversion bitmexConversion;

    @Override
    public void setQty(BigDecimal qty) {
        super.setQty(qty);
        if (!StringUtils.hasText(getTradingOn())) {
            return;
        }
        BigDecimal nowPrice = BigDecimal.ZERO;
        if (getExChange().equals(EX_CHANGE_OKEX)) {
            nowPrice = okexV3Conversion.getNowPriceByInstrumentId(getTradingOn());
        }
        if (getExChange().equals(EX_CHANGE_BITMEX)) {
            nowPrice = bitmexConversion.getNowPriceByInstrumentId(getTradingOn());
        }
        if (nowPrice == null || nowPrice.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        if (getTradingOn().contains("BTC-USD") || getTradingOn().contains("XBT")) {
            this.amount = qty.multiply(new BigDecimal(100)).divide(nowPrice, 6, BigDecimal.ROUND_HALF_DOWN);
        } else {
            this.amount = qty.multiply(new BigDecimal(10)).divide(nowPrice, 6, BigDecimal.ROUND_HALF_DOWN);
        }
    }

}

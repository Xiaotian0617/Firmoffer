package com.ailu.firmoffer.web.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/11/26 17:47
 */
@Data
public class UserAccountVo {

    private BigDecimal total;
    private BigDecimal future;
    private BigDecimal stock;
    private BigDecimal wallet;
    private BigDecimal swap;
    private BigDecimal margin;

    public UserAccountVo(BigDecimal total, BigDecimal stock, BigDecimal future, BigDecimal wallet,BigDecimal swap,BigDecimal margin) {
        this.total = total;
        this.future = future;
        this.stock = stock;
        this.wallet = wallet;
        this.swap = swap;
        this.margin = margin;
    }
}

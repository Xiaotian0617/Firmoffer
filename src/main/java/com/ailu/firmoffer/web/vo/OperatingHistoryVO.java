package com.ailu.firmoffer.web.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/4/27 20:36
 */
@Data
public class OperatingHistoryVO {

    private Long userid;
    private String exChange;
    private String side;
    private String symbol;
    private BigDecimal amount;
    private BigDecimal price;
    private Date orderDate;
    private String orderTime;

    public String getOrderTime() {
        Long time = System.currentTimeMillis() - orderDate.getTime();
        if (time.compareTo(3600000L) > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm");
            return dateFormat.format(orderDate.getTime());
        } else {
            int m = (int) (time / 60 / 1000);
            return m + "分钟前";
        }
    }

}

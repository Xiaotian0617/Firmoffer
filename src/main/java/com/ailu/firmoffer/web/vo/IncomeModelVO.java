package com.ailu.firmoffer.web.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 收益走势Model
 */
@Data
public class IncomeModelVO {

    private BigDecimal income;

    private Date ctime;

    public IncomeModelVO() {
    }

    public IncomeModelVO(BigDecimal income, Date time) {
        this.income = income;
        this.ctime = time;
    }

    public String getCtime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(ctime);
    }
}

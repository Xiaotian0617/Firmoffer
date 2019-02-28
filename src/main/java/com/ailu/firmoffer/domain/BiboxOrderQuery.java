package com.ailu.firmoffer.domain;

import lombok.Data;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/5/11 9:36
 */
@Data
public class BiboxOrderQuery {
    private String pair;
    private int accountType;
    private int page;
    private int size;
    private String coinSymbol;
    private String currencySymbol;
    private int orderSide;
    private int hideCancel;
}

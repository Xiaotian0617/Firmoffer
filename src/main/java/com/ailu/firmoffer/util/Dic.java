package com.ailu.firmoffer.util;

/**
 * 与实盘蜘蛛一致的字典表
 */
public class Dic {

    public static final String FUTURE = "future";
    public static final String STOCK = "stock";
    public static final String WALLET = "wallet";
    public static final String SWAP = "swap";
    public static final String MARGIN = "margin";
    //Okex 期货 全仓模式
    public static final String AllSTORE = "crossed";
    //Okex 期货 逐仓模式
    public static final String LIMITSTORE = "fixed";

    public static final String LONG = "long";
    public static final String SHORT = "short";

    public static final String POSITION = "POSITION";

    public static final String ORDER = "ORDER";

    public static final String MATCH = "MATCH";

    public static final String BLANCE = "BLANCE";

    public static final String TRANSFER  = "TRANSFER";

    public static final String DEPOSIT = "DEPOSIT";

    public static final String LEDGER = "LEDGER";

    public static final String SWAP_OKEX = "SWAP";

    /**
     * 告诉实盘需要更新
     */
    public static final String UPDATE = "UPDATE";

    /**
     * 告诉实盘需要替换
     */
    public static final String REPLACE = "REPLACE";

    /**
     * 告诉实盘需要新增
     */
    public static final String INSERT = "INSERT";

    /**
     * 告诉实盘需要清空后新增
     */
    public static final String DELETE = "DELETE";

    public static final String SEPARATOR = "-";

}

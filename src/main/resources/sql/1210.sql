--充值历史
CREATE TABLE `firm_offer_deposit_hist`  (
  `id` int(11) NOT NULL,
  `currency` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '币种名称 如:btc',
  `amount` decimal(32, 8) NULL DEFAULT NULL COMMENT '充值数量',
  `to` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '此笔充值到账地址',
  `txid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '区块转账哈希记录',
  `timestamp` timestamp(0) NULL DEFAULT NULL COMMENT '充值到账时间',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提现状态（0:等待确认;1:确认到账;2:充值成功；）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

SET FOREIGN_KEY_CHECKS = 1;

--账单流水
CREATE TABLE `firm_offer_ledger_hist`  (
  `ledger_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '账单id',
  `exchange` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '交易所',
  `currency` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '币种',
  `balance` decimal(32, 8) NULL DEFAULT NULL COMMENT '余额',
  `amount` decimal(32, 8) NULL DEFAULT NULL COMMENT '变动数量',
  `type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '账单类型',
  `fee` decimal(32, 8) NULL DEFAULT NULL COMMENT '手续费',
  `timestamp` timestamp(0) NULL DEFAULT NULL COMMENT '账单创建时间',
  PRIMARY KEY (`ledger_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE `firm_offer_ledger_hist`
ADD UNIQUE INDEX `uni_idx`(`ledger_id`, `exchange`, `currency`) USING BTREE;
ALTER TABLE `firmoffer`.`firm_offer_position`
ADD COLUMN `position_type` varchar(10) DEFAULT 0 COMMENT '仓位类型 01 交割合约 02 永续合约' ;
ALTER TABLE `firmoffer`.`firm_offer_ledger_hist`
ADD COLUMN `type` varchar(255) COMMENT '发起账户类型' AFTER `type_name`;
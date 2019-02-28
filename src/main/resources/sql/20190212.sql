ALTER TABLE `firmoffer`.`firm_offer_ledger_hist`
ADD COLUMN `status` varchar(10) DEFAULT '00' COMMENT '00 未更改 01 已更改' AFTER `timestamp`;

update `firmoffer`.`firm_offer_ledger_hist` set status = '01';
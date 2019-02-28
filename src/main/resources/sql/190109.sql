ALTER TABLE `firm_offer_match_hist`
ADD COLUMN `match_type`  varchar(50) NULL DEFAULT "match" AFTER `utime`;

ALTER TABLE `firm_offer_exchange_balance`
ADD COLUMN `fee`  decimal(32,12) NULL DEFAULT 0 COMMENT '利息（杠杆模式）' AFTER `freeze`;

ALTER TABLE `firm_offer_exchange_balance_snap`
ADD COLUMN `margin` decimal(32, 12) COMMENT '币币杠杆资产' AFTER `stock`;


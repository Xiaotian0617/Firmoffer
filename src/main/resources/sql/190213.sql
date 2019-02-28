ALTER TABLE `firmoffer`.`user_firm_offer`
ADD COLUMN `trans_time` timestamp(0) DEFAULT '2019-03-01 00:00:00' COMMENT '用户ledger查询最小时间';
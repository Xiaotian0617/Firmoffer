CREATE TABLE `firmoffer`.`init_update`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
`user_id` int(10),
`total` decimal(32, 8) COMMENT '总资产',
`init` decimal(32, 8) COMMENT '初始资产',
`transfer_price` decimal(32, 8) COMMENT '本次计算时 总换算金额',
`cTime` timestamp(0) DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (`id`)
);

ALTER TABLE `firmoffer`.`init_update`
ADD COLUMN `final_init` decimal(32, 8) COMMENT '最后计算结果' AFTER `transfer_price`;
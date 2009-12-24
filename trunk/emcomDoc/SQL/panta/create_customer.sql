CREATE PROCEDURE PANTA_CREATE_CUSTOMERS (beginNum INTEGER, endNum INTEGER, balance INTEGER)
BEGIN
    set @i=beginNum;
    set @j='Liu';
    set @k=100001;
    WHILE @i < endNum DO
    
    set @loginId=CONCAT(@j,SUBSTRING(@k,2));
    set @i=CONCAT('000', @i);
    set @i=SUBSTRING(@i, -8, 8);

INSERT INTO JHF_CUSTOMER_STATUS(CUSTOMER_ID, GROUP_ID, WITHDRAWAL_CONSTRAINT, ACCOUNT_STATUS, LOGIN_CONSTRAINT, OPEN_BUY_CONSTRAINT, OPEN_SELL_CONSTRAINT, CLOSE_BUY_CONSTRAINT, CLOSE_SELL_CONSTRAINT, STRADDLE_OPTION_FLAG, ACCOUNT_ACTIVE_STATUS, ACCOUNT_ACTIVE_STATUS_DATE, LOGIN_ID, LOGIN_PASSWORD, OLD_LOGIN_PASSWORD, ORIGINAL__PASSWORD, PASSWORD_UPDATE_DATE, UPPER_AMOUNT, FUTURE_GROUP_ID, FUTURE_GROUP_CHANGE_DATE, NOTICE_RATIO, WITHDRAWAL_COUNT, TOTAL_CREDIT, LAST_LOGIN_TIME, PASSWORD_FAIL_COUNT, CLOSE_REQUEST_FLAG, COMMISSION_FREE_FLAG, DELIVERY_RECIEPT_CONSTRAINT, DELIVERY_DELIVERY_CONSTRAINT, CONVERSION_IN_CONSTRAINT, CONVERSION_OUT_CONSTRAINT, AFFINITY_KEY, SERVICE_POINT, UPDATE_STAFF_ID, INPUT_STAFF_ID, ACTIVE_FLAG, INPUT_DATE, UPDATE_DATE, VIRTUAL_ACCOUNT_NO, LOSSCUT_MAIL_COUNT, ALERT_MAIL_COUNT, LOSSCUT_RATIO, ALERT_RATIO, LOSSCUT_RATIO_FLAG, ALERT_RATIO_FLAG, LOSSCUT_CONSTRANT) 
    VALUES(@i, 'DEFAULT', 0, 1, 0, 0, 0, 0, 0, 0, 0, '20080716', @loginId, '8ddcff3a80f4189ca1c9d4d902c3c909', 'aEpaN1pucGo=', 
'aEpaN1pucGo=', '20080716', 0, '', '', 0, 0, 0, NOW(), 0, 0, 0, 0, 0, 0, 0, '', 0, 'CS0001', 'CS0001', 1, NOW(), NOW(), '', 0, 0, 0, 0, 0, 0, 0)
;

INSERT INTO JHF_CUSTOMER(CUSTOMER_ID, CORPORATION_TYPE, DOCUMENT_SEND_STATUS, FIRST_NAME, FIRST_NAME_KANA, LAST_NAME, LAST_NAME_KANA, BIRTHDAY_YEAR, BIRTHDAY_MONTH, BIRTHDAY_DAY, 
ZIP_CODE_1, CITY_1, SECTION_1, SECTION_KANA_1, FLAT_1, FLAT_KANA_1, TEL_1, FAX_1, ZIP_CODE_2, CITY_2, SECTION_2, SECTION_KANA_2, BUILDING_NAME, BUILDING_NAME_KANA, ANNUAL_INCOME, 
FINANCIAL_ASSETS, FIRST_TRANSFER_AMOUNT, CONTRACT_PURPOSE, CONTRACT_PURPOSE_COMMENT, MJHF_EXPERIENCE_FLAG, MJHF_EXPERIENCE_YEAR, STOCK_EXPERIENCE_FLAG, STOCK_EXPERIENCE_YEAR, STOCK_MARGIN_EXPERIENCE_FLAG, STOCK_MARGIN_EXPERIENCE_YEAR, 
STOCK_IDX_OPTION_EXP_FLAG, STOCK_IDX_OPTION_EXP_YEAR, PRODUCT_OPTION_EXP_FLAG, PRODUCT_OPTION_EXP_YEAR, OTHER_EXPERIENCE, OTHER_EXPERIENCE_YEAR, APPLICATION_DATE, ACCOUNT_OPEN_STATUS, ACCOUNT_STATUS_CHANGE_DATETIME, MEMO_1, 
MEMO_2, MEMO_3, UPDATE_STAFF_ID, INPUT_STAFF_ID, ACTIVE_FLAG, INPUT_DATE, UPDATE_DATE, AGREE_FLAG, ORIGINAL_DEPOSIT_DATE, DOCUMENT_POST_DATE, 
DOCUMENT_ACCEPT_DATE, ACCOUNT_OPEN_DATE, HOUSE_NUMBER_1, HOUSE_NUMBER_KANA_1, CONTRACT_PURPOSE_1, CONTRACT_PURPOSE_2, CONTRACT_PURPOSE_3, CONTRACT_PURPOSE_4, CONTRACT_PURPOSE_5, CONTRACT_PURPOSE_6, 
CONTRACT_PURPOSE_7, CONTRACT_PURPOSE_8, DISCOVER_SOURCE, DISCOVER_SOURCE_COMMENT, FIT_FLAG, ACCOUNT_OPEN_FINISH_DATE, DAILY_MAIL_FLAG, ACCOUNT_CANCEL_DATE, ACCOUNT_STATUS_CHANGE_DATE)  
VALUES(@i, '0', '0', '????', '?^???E', '?S?p', '?J???Z', '2007','08','13',  '4600003', '???m??', '?s???S?E???????n', '?V?N?O???`???E?\???o???`', '', 
'', '000-000-0000', '', '', '', '', '', '', '', '5', '5', 0, '1', '', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '???L??', 1, '20080716', 5, NOW(), '', 
'', '', 'CS0001', 'CS0019', 1, NOW(), NOW(), 1, '', '', '', '', '', '', 0, 0, 0, 0, 0, 0, 0, 0, 0, '', 0, '', 0, '', '');

INSERT INTO `JHF_CUST_BANK_ACCOUNT` VALUES (@i,'JPY','2','0','1','?G???W?F?C',NULL,NULL,'?O?H?????t?e?i???s',NULL,NULL,'?????????x?X','1111111','CS0001','CS0001','1',NOW(),NOW())
;

INSERT INTO `JHF_MAIL_ADDRESS` VALUES 
(@i,'0','1','zhouhc@bestwiz.cn','CS0019','CS0001','1',NOW(),NOW(), 0, 0),
(@i,'1','1','zhouhc@bestwiz.cn','CS0019','CS0001','1',NOW(),NOW(), 0, 0)
;
INSERT INTO `JHF_PERSONAL` VALUES (@i,'0','001-1111-1111','13','?c??','????','?E??','11','111-111-1111','111-111-1111','1','','0', '', 0, '', 0, 0, 0, '1')

;
INSERT INTO `JHF_CASH_BALANCE` VALUES
(@i,'JPY','0','0',balance,'system','system','1',NOW(),NOW())
;
INSERT INTO `JHF_CUSTOMER_LOCK` VALUES (@i)
;
INSERT INTO `JHF_MAIL_ACTION_MAP` VALUES
(@i,'0','1','1002',NULL,NULL,'1',NOW(),NOW()),
(@i,'0','1','1003',NULL,NULL,'1',NOW(),NOW()),
(@i,'0','1','1004',NULL,NULL,'1',NOW(),NOW()),
(@i,'0','1','1005',NULL,NULL,'1',NOW(),NOW()),
(@i,'0','1','1006',NULL,NULL,'1',NOW(),NOW()),
(@i,'0','1','2003',NULL,NULL,'1',NOW(),NOW()),
(@i,'0','1','3004',NULL,NULL,'1',NOW(),NOW()),
(@i,'0','1','5000',NULL,NULL,'1',NOW(),NOW()),
(@i,'0','1','5001',NULL,NULL,'1',NOW(),NOW()),
(@i,'1','1','1002',NULL,NULL,'1',NOW(),NOW()),
(@i,'1','1','1003',NULL,NULL,'1',NOW(),NOW()),
(@i,'1','1','1004',NULL,NULL,'1',NOW(),NOW()),
(@i,'1','1','1005',NULL,NULL,'1',NOW(),NOW()),
(@i,'1','1','1006',NULL,NULL,'1',NOW(),NOW()),
(@i,'1','1','2003',NULL,NULL,'1',NOW(),NOW()),
(@i,'1','1','3004',NULL,NULL,'1',NOW(),NOW()),
(@i,'1','1','5000',NULL,NULL,'1',NOW(),NOW()),
(@i,'1','1','5001',NULL,NULL,'1',NOW(),NOW())
;
INSERT INTO `JHF_LEVERAGE_GROUP` VALUES
(@i,'A001','USD/JPY','CS0001','0','1',NOW(),NOW()),
(@i,'A002','EUR/JPY','CS0001','0','1',NOW(),NOW()),
(@i,'A003','GBP/JPY','CS0001','0','1',NOW(),NOW()),
(@i,'A004','AUD/JPY','CS0001','0','1',NOW(),NOW()),
(@i,'A005','NZD/JPY','CS0001','0','1',NOW(),NOW()),
(@i,'A006','CHF/JPY','CS0001','0','1',NOW(),NOW()),
(@i,'A007','CAD/JPY','CS0001','0','1',NOW(),NOW()),
(@i,'A008','ZAR/JPY','CS0001','0','1',NOW(),NOW()),
(@i,'A009','EUR/USD','CS0001','0','1',NOW(),NOW()),
(@i,'A010','GBP/USD','CS0001','0','1',NOW(),NOW()),
(@i,'A011','AUD/USD','CS0001','0','1',NOW(),NOW()),
(@i,'A012','NZD/USD','CS0001','0','1',NOW(),NOW()),
(@i,'A013','USD/CHF','CS0001','0','1',NOW(),NOW()),
(@i,'A014','USD/CAD','CS0001','0','1',NOW(),NOW()),
(@i,'A015','EUR/GBP','CS0001','0','1',NOW(),NOW()),
(@i,'A016','GBP/CHF','CS0001','0','1',NOW(),NOW())
;
INSERT INTO `JHF_ORDER_NO_GENERATOR` VALUES (@i,'0','1',NOW(),NOW());

          set @i = @i +1;
          set @k = @k +1;
    END WHILE;
END
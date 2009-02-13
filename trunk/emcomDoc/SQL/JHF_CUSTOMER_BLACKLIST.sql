

-- =============  JMS STUFF  ==============

SELECT * FROM JHF_JMS_VENDOR
;
SELECT * FROM JHF_JMS_PROVIDER
;



UPDATE JHF_JMS_VENDOR SET STATUS = '1';

UPDATE JHF_JMS_PROVIDER SET USING_VENDOR = '1';



-- ===========================================


-- BLACK LIST STUFF
SELECT * FROM JHF_CUSTOMER_BLACKLIST ;
SELECT * FROM JHF_CUSTOMER_BLACKLIST WHERE CUSTOMER_ID = '00006701';
INSERT INTO JHF_CUSTOMER_BLACKLIST(CUSTOMER_ID, REALTIME_ORDER_LONG, REALTIME_ORDER_SHORT, LIMIT_ORDER_LONG, LIMIT_ORDER_SHORT, STOP_ORDER_LONG, STOP_ORDER_SHORT, LOSSCUT_ORDER_LONG, LOSSCUT_ORDER_SHORT, SUMMARY, ACTIVE_FLAG, INPUT_DATE, UPDATE_DATE, INPUT_STAFF_ID, UPDATE_STAFF_ID) 
    VALUES('00000128', 0, 0, 0, 0, 0, 0, 0, 0, '', 1, CURRENT_TIMESTAMP() ,CURRENT_TIMESTAMP() , 'lubq', 'lubq')
;
update JHF_CUSTOMER_BLACKLIST set LOSSCUT_ORDER_LONG = '1' ,LOSSCUT_ORDER_SHORT = '0' where CUSTOMER_ID = '00006701'
update JHF_CUSTOMER_BLACKLIST set LOSSCUT_ORDER_LONG = '0' ,LOSSCUT_ORDER_SHORT = '1' where CUSTOMER_ID = '00006701'
update JHF_CUSTOMER_BLACKLIST set LIMIT_ORDER_LONG = '1'  where CUSTOMER_ID = '00006701'


update JHF_CUSTOMER_BLACKLIST set REALTIME_ORDER_LONG = '0' ,REALTIME_ORDER_SHORT = '0' 
,LIMIT_ORDER_LONG = '1' ,LIMIT_ORDER_SHORT ='1' ,STOP_ORDER_LONG='1' ,STOP_ORDER_SHORT='1' ,LOSSCUT_ORDER_LONG = '1' ,LOSSCUT_ORDER_SHORT = '1'  where CUSTOMER_ID = '00000128' 




INSERT INTO JHF_CUSTOMER_BLACKLIST(CUSTOMER_ID, REALTIME_ORDER_LONG, REALTIME_ORDER_SHORT, LIMIT_ORDER_LONG, LIMIT_ORDER_SHORT, STOP_ORDER_LONG, STOP_ORDER_SHORT, LOSSCUT_ORDER_LONG, LOSSCUT_ORDER_SHORT, SUMMARY, ACTIVE_FLAG, INPUT_DATE, UPDATE_DATE, INPUT_STAFF_ID, UPDATE_STAFF_ID) 
    VALUES('00006701', 1, 0, 0, 0, 0, 0, 0, 0, '', 1, CURRENT_TIMESTAMP() ,CURRENT_TIMESTAMP() , 'lubq', 'lubq')
;
commit;

INSERT INTO JHF_CUSTOMER_BLACKLIST(CUSTOMER_ID, REALTIME_ORDER_LONG, REALTIME_ORDER_SHORT, LIMIT_ORDER_LONG, LIMIT_ORDER_SHORT, STOP_ORDER_LONG, STOP_ORDER_SHORT, LOSSCUT_ORDER_LONG, LOSSCUT_ORDER_SHORT, SUMMARY, ACTIVE_FLAG, INPUT_DATE, UPDATE_DATE, INPUT_STAFF_ID, UPDATE_STAFF_ID) 
    VALUES('00000128', 1, 0, 1, 0, 0, 1, 1, 0, '', 1, CURRENT_TIMESTAMP() ,CURRENT_TIMESTAMP() , 'lubq', 'lubq')
;

delete from JHF_CUSTOMER_BLACKLIST WHERE CUSTOMER_ID = '00006701'


SELECT * FROM JHF_CUSTOMER_BLACKLIST WHERE CUSTOMER_ID = '00006701';
--set realtime long = 1 , realtime short = 1 
update JHF_CUSTOMER_BLACKLIST set LOSSCUT_ORDER_SHORT = '1' ,LOSSCUT_ORDER_LONG = '0'  where CUSTOMER_ID = '00000101';
--set realtime long = 0 , realtime short = 1 
update JHF_CUSTOMER_BLACKLIST set REALTIME_ORDER_LONG = '0' ,REALTIME_ORDER_SHORT = '1' where CUSTOMER_ID = '00000101';
--set realtime long = 1 , realtime short = 0 
update JHF_CUSTOMER_BLACKLIST set REALTIME_ORDER_LONG = '1' ,REALTIME_ORDER_SHORT = '0' where CUSTOMER_ID = '00000101';



update JHF_CUSTOMER_BLACKLIST set LIMIT_ORDER_LONG = '0' ,LIMIT_ORDER_SHORT = '0' where CUSTOMER_ID = '00000101';

update JHF_CUSTOMER_BLACKLIST set STOP_ORDER_LONG = '1' ,STOP_ORDER_SHORT = '0' where CUSTOMER_ID = '00000101';

--set stop long = 1 ,limit long =1 
update JHF_CUSTOMER_BLACKLIST set REALTIME_ORDER_LONG = '0' ,REALTIME_ORDER_SHORT = '0' 
,LIMIT_ORDER_LONG = '1' ,LIMIT_ORDER_SHORT ='1' ,STOP_ORDER_LONG='1' ,STOP_ORDER_SHORT='1' ,LOSSCUT_ORDER_LONG = '1' ,LOSSCUT_ORDER_SHORT = '1'  where CUSTOMER_ID = '00000059' 




SELECT * FROM JHF_CUSTOMER_BLACKLIST WHERE CUSTOMER_ID = '00006701';

SELECT * FROM JHF_APP_PROPERTY WHERE PROP_TYPE = 'BLACKLIST' ;



SELECT * FROM JHF_APP_PROPERTY WHERE PROP_TYPE = 'BLACKLIST';

UPDATE JHF_APP_PROPERTY SET PROPERTY_VALUE = 0  WHERE PROP_TYPE = 'BLACKLIST' AND PROPERTY_KEY = 'BLACKLIST_MODE';
UPDATE JHF_APP_PROPERTY SET PROPERTY_VALUE = 1  WHERE PROP_TYPE = 'BLACKLIST' AND PROPERTY_KEY = 'BLACKLIST_MODE';


SELECT * FROM JHF_STATUS_CONTRL WHERE CURRENCY_PAIR = 'EUR/JPY';
SELECT CURRENCY_PAIR,AUTOHEDGE_SWATICH, AUTOHEDGE_MARKET_BUY,AUTOHEDGE_MARKET_SELL ,STOP_TIME_BEFORE_EOD,STOP_TIME_BEFORE_EOD_SWITCH  FROM JHF_STATUS_CONTRL WHERE CURRENCY_PAIR = 'EUR/JPY';







-- ==================   ORDER  STUFF  ===========================



SELECT * FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID = '00000887' AND CUSTOMER_ORDER_NO IN ('T000000330','T000000319','T000000328') ORDER BY INPUT_DATE DESC LIMIT 100;


SELECT * FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID = '00000887' AND CUSTOMER_ORDER_NO IN ('T000000346','T000000344','T000000341') ORDER BY INPUT_DATE DESC LIMIT 20

SELECT * FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID = '00006701' ORDER BY INPUT_DATE DESC LIMIT 10 ;
SELECT EXECUTION_TYPE FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID = '00006701' ORDER BY INPUT_DATE DESC LIMIT 10 ;

SELECT  FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID = '00006701' ORDER BY INPUT_DATE DESC LIMIT 10 ;
20080506ORD00031214   20080506ORD00031217   20080506ORD00031219


UPDATE JHF_ALIVE_ORDER SET ORDER_STATUS = 1 WHERE ORDER_ID= '20080507ORD00000409'

SELECT * FROM JHF_ORDER_BIND WHERE ORDER_ID IN ( '20080506ORD00031209','20080506ORD00031207','20080506ORD00030804');
SELECT * FROM JHF_ORDER_BIND WHERE ORDER_BIND_ID IN ('2008050600018902','','')

SELECT * FROM JHF_ORDER_BIND ORDER BY INPUT_DATE DESC LIMIT 20;

DELETE FROM JHF_ORDER_BIND WHERE ORDER_BIND_ID IN ('2008050600018905','','');

--=====================================================================================================================

SELECT * FROM JHF_HEDGE_CUSTTRADE WHERE ID = '20080513ORD00022205';

-- === ### DELETE STATUS = 7  ORDER 
DELETE FROM JHF_ALIVE_ORDER  WHERE CUSTOMER_ID = '00006701' AND ORDER_STATUS = 7 ;

-- === ### DELETE STATUS = 7  ORDER'S CONTRACT 
SELECT * FROM JHF_ALIVE_CONTRACT WHERE CUSTOMER_ID = '00000101' ORDER BY INPUT_DATE DESC LIMIT 20;
DELETE FROM JHF_ALIVE_CONTRACT WHERE CUSTOMER_ID = '00000101' ;




SELECT ORDER_ID FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID = '00000101' AND ORDER_STATUS = 7 ;
DELETE FROM JHF_ALIVE_ORDER WHERE ORDER_ID IN ('20080506ORD00031405','');


--=======================            JHF_APP_PROPERTY          ==========================================================
SELECT * FROM JHF_APP_PROPERTY


--=====================================================================================================================

INSERT INTO JHF_ORDER_BIND VALUES ('2008050600018905','20080506ORD00031407','20080506TRAD00031407',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)

SELECT * FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID = '00000101'  AND CURRENCY_PAIR = 'GBP/CHF' ORDER BY INPUT_DATE DESC LIMIT 10

SELECT * FROM JHF_ORDER_BIND WHERE ORDER_ID = '20080418ORD00002612'
SELECT * FROM JHF_ORDER_BIND WHERE ORDER_BIND_ID IN ( '2008042100000307','2008042100000306');
SELECT * FROM JHF_ORDER_BIND WHERE ORDER_ID IN ('20080422ORD00000660','20080422ORD00000661','20080422ORD00000663','20080422ORD00000664','');


SELECT CUSTOMER_ID ,ORDER_STATUS , TRADE_TYPE, INPUT_DATE,UPDATE_DATE FROM JHF_ALIVE_ORDER WHERE ORDER_ID IN ('20080506ORD00031004','20080506ORD00031005','20080506ORD00031006')
DELETE FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID = '00000887' AND CUSTOMER_ORDER_NO IN ('T000000340','T000000339','T000000338');


INSERT INTO JHF_ORDER_BIND VALUES ('2008050600018903','20080506ORD00031405','20080506TRAD00031405',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);

INSERT INTO JHF_ORDER_BIND VALUES ('2008050600018901','20080506ORD00031402','20080506TRAD00031402',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);

INSERT INTO JHF_ORDER_BIND VALUES ('2008050600018901','20080506ORD00031403','20080506TRAD00031403',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);













SELECT * FROM JHF_STATUS_CONTRL WHERE CURRENCY_PAIR = 'USD/JPY';

SELECT CUSTOMER_ORDER_NO ,ORDER_ID ,UPDATE_DATE FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID = '00000101' ORDER BY ORDER_ID DESC



SELECT * FROM JHF_APP_PROPERTY

update JHF_APP_PROPERTY set PROPERTY_VALUE ='3000000' where PROP_TYPE='TOMCAT_SESSION';

SELECT * FROM JHF_FORMULA WHERE CUSTOMER_ID = '00000059';

--make customer losscut ,flag
update JHF_FORMULA set TRADE_IN_RECENT = 1 WHERE CUSTOMER_ID = '00000015' ;
--make customer losscut 
update JHF_CASH_BALANCE SET CASH_BALANCE = '1' WHERE CUSTOMER_ID = '00000142';
--make customer losscut
update JHF_CUSTOMER_STATUS SET ACCOUNT_STATUS = 3 WHERE CUSTOMER_ID = '00000015';



SELECT * FROM JHF_CUSTOMER_STATUS WHERE LOGIN_ID = ''

--============================== 




SELECT * FROM JHF_APP_PROPERTY;
SELECT * FROM JHF_APPLICATION_DATE


UPDATE JHF_APPLICATION_DATE SET FRONT_DATE = '20080530' WHERE DATE_KEY = 'F';
UPDATE JHF_APPLICATION_DATE SET FRONT_DATE = '20080529' WHERE DATE_KEY = 'B';




SELECT * FROM JHF_CASH_BALANCE WHERE CUSTOMER_ID  = '00000128';
select TRADE_IN_RECENT from JHF_FORMULA where CUSTOMER_ID = '00000015' ;

SELECT ACCOUNT_STATUS FROM JHF_CUSTOMER_STATUS WHERE CUSTOMER_ID = '00000059';






SELECT ACCOUNT_STATUS FROM JHF_CUSTOMER_STATUS WHERE CUSTOMER_ID = '00000128';

SELECT * FROM JHF_ALIVE_ORDER WHERE ORDER_ID = '' 

SELECT EXECUTION_TYPE FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID = '00000101'  AND CURRENCY_PAIR = 'GBP/CHF' ORDER BY INPUT_DATE DESC LIMIT 10;




SELECT * FROM JHF_ALIVE_ORDER WHERE ORDER_ID IN (SELECT ORDER_ID FROM JHF_ORDER_BIND WHERE ORDER_BIND_ID = '2008032000583643')


SELECT * FROM JHF_COUNTERPARTY_CURRENCYPAIR ;
SELECT * FROM JHF_COUNTERPARTY ;

UPDATE JHF_COUNTERPARTY_CURRENCYPAIR SET COUNTERPARTY_ID = 'MOCK' WHERE COUNTERPARTY_ID = 'BC' ;

UPDATE  JHF_ALIVE_CONTRACT SET ACTIVE_FLAG = '0' WHERE CUSTOMER_ID = '00000128' AND CURRENCY_PAIR = 'GBP/USD';

SELECT CUSTOMER_ID,LOGIN_ID ,LOGIN_PASSWORD FROM JHF_CUSTOMER_STATUS WHERE LOGIN_PASSWORD = '593c9b4a9390551d53e5cacf28ebd638';

SELECT * FROM JHF_CP_EXECUTION WHERE STATUS=0;

show processlist;

;
SELECT * FROM JHF_CASH_BALANCE WHERE CUSTOMER_ID = '00003373' ;
;

SELECT * FROM JHF_EXECUTION WHERE ORDER_ID IN ('20080530ORD00025208','');
SELECT * FROM JHF_NET_BIND WHERE CUSTTRADE_ID IN  ('20080417ORD00001207','20080417ORD00001208');
SELECT * FROM JHF_EXECUTION_BIND WHERE EXECUTION_ID IN ('20080417CUEX00000304','20080417CUEX00000305');

SELECT * FROM JHF_HEDGE_CUSTTRADE WHERE ID IN ('20080530CUEX00063483','') ORDER BY INPUT_DATE DESC;



SELECT 
JB.CUSTOMER_ID,JCS.LOGIN_ID,JCS.LOGIN_PASSWORD ,JCS.LOGIN_CONSTRAINT,JCS.GROUP_ID ,JB.CURRENCY_CODE,
JB.CASH_BALANCE,JB.ACTIVE_FLAG 
FROM JHF_CASH_BALANCE AS JB ,JHF_CUSTOMER AS JCUST ,JHF_CUSTOMER_STATUS AS JCS
 
WHERE JB.CASH_BALANCE >= 0  AND JB.CUSTOMER_ID = JCUST.CUSTOMER_ID AND JB.CUSTOMER_ID = JCS.CUSTOMER_ID AND JB.CUSTOMER_ID IN (
SELECT JC.CUSTOMER_ID FROM JHF_CUSTOMER_STATUS AS JC WHERE  JC.LOGIN_PASSWORD='593c9b4a9390551d53e5cacf28ebd638'
)

;



SELECT * FROM JHF_CUSTOMER_STATUS WHERE CUSTOMER_ID = '00000101';



DELETE FROM JHF_ALIVE_CONTRACT WHERE CUSTOMER_ID = '00000101' AND CURRENCY_PAIR IN ('EUR/JPY') ;

UPDATE JHF_ALIVE_ORDER SET ORDER_STATUS = 7 WHERE ORDER_ID = '20080508ORD00000904' AND ORDER_STATUS = 1 ;


SELECT * FROM JHF_ALIVE_CONTRACT  WHERE CUSTOMER_ID = '00000142';
SELECT * FROM JHF_ALIVE_CONTRACT  WHERE CUSTOMER_ID = '00000101' AND AMOUNT_NO_SETTLED > 0;



SELECT * FROM JHF_CUSTOMER_STATUS WHERE CUSTOMER_ID = '00000101';



SELECT * FROM JHF_CURRENCY_PAIR;

SELECT * FROM JHF_ALIVE_ORDER ORDER BY INPUT_DATE DESC; 

SELECT * FROM JHF_EXECUTION WHERE ORDER_ID = '20080529ORD00001768';
SELECT * FROM JHF_EXECUTION_BIND WHERE EXECUTION_ID = '20080529CUEX00034834';
SELECT * FROM JHF_CP_EXECUTION WHERE CP_EXECUTION_ID = '20080529CPEX00112968';



SELECT * FROM JHF_CP_TRADE_REQUEST WHERE CP_COVER_ID = '20080529COVR00002179';

SELECT * FROM JHF_ORDER_BIND WHERE ORDER_BIND_ID = '2008053000012703';
SELECT * FROM JHF_ALIVE_ORDER WHERE ORDER_ID = '20080604ORD00002101';


DELETE FROM JHF_ALIVE_CONTRACT WHERE CUSTOMER_ORDER_NO = 'T000000047' AND CUSTOMER_ID = '00000239';



SELECT * FROM JHF_SYS_POSITION_INSERT ;

DELETE FROM JHF_SYS_POSITION_INSERT WHERE FRONT_DATE = '20080604'
SELECT * FROM JHF_SYS_POSITION_INSERT_HIS ORDER BY SEQ_NO DESC;
DELETE FROM JHF_SYS_POSITION_INSERT_HIS WHERE FRONT_DATE = '20080603';
E080000382504
SELECT * FROM JHF_SYS_POSITION_INSERT 
SELECT * FROM JHF_SYS_POSITION_INSERT_HIS 
DELETE FROM JHF_SYS_POSITION_INSERT;
DELETE FROM JHF_SYS_POSITION_INSERT_HIS;
DELETE FROM JHF_HEDGE_CUSTTRADE;

DELETE FROM JHF_EXECUTION_BIND


SELECT * FROM JHF_SYS_POSITION_INSERT WHERE 
SELECT * FROM JHF_EXECUTION ORDER BY EXECUTION_ID DESC;
DELETE FROM JHF_EXECUTION;

SELECT * FROM JHF_APPLICATION_DATE;

-- ============   set best feed  and hedge

SELECT * FROM JHF_STATUS_CONTRL  WHERE STOP_TIME_BEFORE_EOD_SWITCH = 1;

UPDATE JHF_STATUS_CONTRL SET PRI_FEED_SRC = 'MOCK' AND PRI_HEDGE_DEST = 'MOCK' ;
-- =====================

UPDATE JHF_STATUS_CONTRL SET STOP_TIME_BEFORE_EOD_SWITCH = '0' WHERE CURRENCY_PAIR = 'EUR/JPY';

SELECT UNUSUAL_WAIT_TIME FROM JHF_STATUS_CONTRL;


SELECT * FROM JHF_STATUS_CONTRL;

UPDATE JHF_STATUS_CONTRL SET UNUSUAL_WAIT_TIME = '123456000';

UPDATE JHF_STATUS_CONTRL SET SP_MODE = '1' WHERE CURRENCY_PAIR = 'USD/JPY';



SELECT * FROM JHF_CUSTOMER_STATUS WHERE CUSTOMER_ID = '00025366';
SELECT * FROM JHF_CU

SELECT * FROM JHF_COUNTERPARTY



SELECT ACCOUNT_OPEN_STATUS FROM JHF_CUSTOMER WHERE CUSTOMER_ID = '00006701';
SELECT * FROM JHF_CUSTOMER_STATUS WHERE CUSTOMER_ID= '00006701';



SELECT ACCOUNT_OPEN_STATUS FROM JHF_CUSTOMER WHERE CUSTOMER_ID  = '00000120';

-- **************   CUSTOMER STUFF  *************  -- 


-- mj ACCOUNT_OPEN_STATUS = 7
UPDATE JHF_CUSTOMER SET ACCOUNT_OPEN_STATUS = 7  WHERE CUSTOMER_ID  = '00000120';
-- panta ACCOUNT_OPEN_STATUS = 
UPDATE JHF_CUSTOMER SET ACCOUNT_OPEN_STATUS = 8  WHERE CUSTOMER_ID  = '00000120';


UPDATE JHF_CUSTOMER_STATUS SET ACCOUNT_STATUS = 0 WHERE CUSTOMER_ID = '00000120';
UPDATE JHF_CUSTOMER_STATUS SET ACCOUNT_ACTIVE_STATUS = 0 WHERE CUSTOMER_ID = '00000120';
UPDATE JHF_CUSTOMER_STATUS SET STRADDLE_OPTION_FLAG = 0 WHERE CUSTOMER_ID = '00000120';
UPDATE JHF_CUSTOMER_STATUS SET LOGIN_CONSTRAINT = 0 WHERE CUSTOMER_ID = '00000120';
UPDATE JHF_CUSTOMER_STATUS SET OPEN_BUY_CONSTRAINT = 0 ,OPEN_SELL_CONSTRAINT = 0 ,CLOSE_BUY_CONSTRAINT = 0 ,CLOSE_SELL_CONSTRAINT = 0  WHERE CUSTOMER_ID = '00000120';



-- ====================================================


SELECT * FROM JHF_MAIL_ADDRESS WHERE CUSTOMER_ID = '00000128';
UPDATE JHF_MAIL_ADDRESS SET MAIL_ADDRESS = 'lbq@mail2.bestwiz.cn' where CUSTOMER_ID = '00000128';
SELECT * FROM JHF_CUSTOMER



--  QUERY JHF_APP_PROPERTY
SELECT * FROM JHF_APP_PROPERTY;
;

--  UPDATE TIME OUT 
UPDATE JHF_APP_PROPERTY SET PROPERTY_VALUE = 3000000 WHERE PROP_TYPE='TOMCAT_SESSION' ;

-- UPDATE RATE  EXPIRED TIME
SELECT * FROM JHF_APP_PROPERTY WHERE PROP_TYPE = 'RATE' AND  PROPERTY_KEY='ratecacheexpiredtime' ;
;


UPDATE JHF_APP_PROPERTY SET PROPERTY_VALUE = '12000000'  WHERE PROP_TYPE = 'RATE' AND  PROPERTY_KEY='ratecacheexpiredtime' ;





SELECT PRICE_HISTORY_CACHE_SWITCH,PRICE_HISTORY_CACHE_NUM FROM JHF_STATUS_CONTRL WHERE CURRENCY_PAIR = 'EUR/JPY';




show processlist;


---  SP_MODE SET 


SELECT * FROM JHF_STATUS_CONTRL WHERE CURRENCY_PAIR = 'EUR/JPY';
UPDATE JHF_STATUS_CONTRL SET SP_MODE = 2 WHERE CURRENCY_PAIR ='EUR/JPY';






SELECT * FROM JHF_ALIVE_ORDER WHERE ORDER_BIND_ID = '2009021100037402';

SELECT CUSTOMER_ID FROM JHF_ALIVE_ORDER WHERE ORDER_ID = '20080806ORD00015499'


SELECT ORDER_ID ,ORDER_BIND_ID,ORDER_STATUS,CUSTOMER_ID ,INPUT_DATE,UPDATE_DATE,TRADE_PRICE, EXECUTION_PRICE,ORDER_PRICE,SIDE,ORDER_AMOUNT ,PRICE_ID,CHANGE_REASON ,UPDATE_STAFF_ID ,SETTLE_CONTRACT_ID,ACTIVATION_DATE,EXPIRATION_DATE ,REVISION_NUMBER 
FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID = '90000088' ORDER BY INPUT_DATE DESC ;

--   CUSTOMER SESSION 

SELECT  SESSION_ID ,CUSTOMER_ID FROM JHF_CUSTOMER_SESSION WHERE CUSTOMER_ID = '90000007'


--90000005


SELECT ORDER_ID ,ORDER_BIND_ID, CUSTOMER_ORDER_NO,ORDER_STATUS,EXECUTION_TYPE, UPDATE_DATE,CUSTOMER_ID ,INPUT_DATE,UPDATE_DATE,EXECUTION_PRICE,SIDE,ORDER_AMOUNT 
FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID ='90000088' ORDER BY INPUT_DATE DESC

SELECT ORDER_ID ,ORDER_BIND_ID, REVISION_NUMBER, CUSTOMER_ORDER_NO,ORDER_STATUS,EXECUTION_TYPE, UPDATE_DATE,CUSTOMER_ID ,INPUT_DATE,UPDATE_DATE,EXECUTION_PRICE,SIDE,ORDER_AMOUNT 
 FROM JHF_ALIVE_ORDER WHERE ORDER_BIND_ID = '2009021200039403' 

-- ####============================================
--                                                 ======

-- JHF_ALIVE_ORDER

SELECT ORDER_ID ,ORDER_STATUS,CUSTOMER_ID ,INPUT_DATE,UPDATE_DATE,TRADE_PRICE, EXECUTION_PRICE,ORDER_PRICE,SIDE,ORDER_AMOUNT ,PRICE_ID,CHANGE_REASON ,UPDATE_STAFF_ID ,SETTLE_CONTRACT_ID,ACTIVATION_DATE,EXPIRATION_DATE ,REVISION_NUMBER 
FROM JHF_ALIVE_ORDER WHERE ORDER_BIND_ID IN ('2009021200038525','') ORDER BY INPUT_DATE DESC

-- JHF_ORDER_STATUS_HISTORY
SELECT ORDER_ID ,ORDER_STATUS,CUSTOMER_ID ,INPUT_DATE,UPDATE_DATE,TRADE_PRICE,ORDER_PRICE, EXECUTION_PRICE,SIDE,ORDER_AMOUNT ,PRICE_ID,CHANGE_REASON ,UPDATE_STAFF_ID ,SETTLE_CONTRACT_ID,ACTIVATION_DATE,EXPIRATION_DATE ,REVISION_NUMBER 
FROM JHF_ORDER_STATUS_HISTORY WHERE ORDER_BIND_ID IN ('2009021200038525','') ORDER BY INPUT_DATE DESC

 --================ order id ----------
SELECT ORDER_ID ,ORDER_STATUS,CUSTOMER_ID ,INPUT_DATE,UPDATE_DATE,TRADE_PRICE,ORDER_PRICE, EXECUTION_PRICE,SIDE,ORDER_AMOUNT ,PRICE_ID,CHANGE_REASON ,UPDATE_STAFF_ID ,SETTLE_CONTRACT_ID,ACTIVATION_DATE,EXPIRATION_DATE ,REVISION_NUMBER 
FROM JHF_ORDER_STATUS_HISTORY  WHERE ORDER_ID IN('');

-- SON  ORDER 

SELECT ORDER_ID ,ORDER_STATUS,CUSTOMER_ID ,INPUT_DATE,UPDATE_DATE,TRADE_PRICE,ORDER_PRICE, EXECUTION_PRICE,SIDE,ORDER_AMOUNT ,PRICE_ID,CHANGE_REASON ,UPDATE_STAFF_ID ,SETTLE_CONTRACT_ID,ACTIVATION_DATE,EXPIRATION_DATE ,REVISION_NUMBER 
FROM JHF_ALIVE_ORDER WHERE PARENT_ORDER_ID = ( SELECT ORDER_ID FROM JHF_ALIVE_ORDER WHERE ORDER_BIND_ID= '2009021200038520');
-- OCO  ORDER

SELECT ORDER_ID ,ORDER_STATUS,CUSTOMER_ID ,INPUT_DATE,UPDATE_DATE,TRADE_PRICE,ORDER_PRICE, EXECUTION_PRICE,SIDE,ORDER_AMOUNT ,PRICE_ID,CHANGE_REASON ,UPDATE_STAFF_ID ,SETTLE_CONTRACT_ID,ACTIVATION_DATE,EXPIRATION_DATE ,REVISION_NUMBER 
FROM JHF_ALIVE_ORDER WHERE OCO_ORDER_ID = ( SELECT ORDER_ID FROM JHF_ALIVE_ORDER WHERE ORDER_BIND_ID= '2009021200038527');


SELECT ORDER_ID ,OCO_ORDER_ID, ORDER_STATUS,CUSTOMER_ID ,INPUT_DATE,UPDATE_DATE,TRADE_PRICE,ORDER_PRICE, EXECUTION_PRICE,SIDE,ORDER_AMOUNT ,PRICE_ID,CHANGE_REASON ,UPDATE_STAFF_ID ,SETTLE_CONTRACT_ID,ACTIVATION_DATE,EXPIRATION_DATE ,REVISION_NUMBER 
 FROM JHF_ALIVE_ORDER WHERE ORDER_ID IN ('20090212ORD00023717','20090212ORD00023718');

SELECT ORDER_ID ,ORDER_STATUS,CUSTOMER_ID ,INPUT_DATE,UPDATE_DATE,TRADE_PRICE,ORDER_PRICE, EXECUTION_PRICE,SIDE,ORDER_AMOUNT ,PRICE_ID,CHANGE_REASON ,UPDATE_STAFF_ID ,SETTLE_CONTRACT_ID,ACTIVATION_DATE,EXPIRATION_DATE ,REVISION_NUMBER 
FROM JHF_ORDER_STATUS_HISTORY  WHERE ORDER_ID IN(
SELECT ORDER_ID  FROM JHF_ALIVE_ORDER WHERE PARENT_ORDER_ID = ( 
    SELECT ORDER_ID FROM JHF_ALIVE_ORDER WHERE ORDER_BIND_ID= '2009021200038520')
);

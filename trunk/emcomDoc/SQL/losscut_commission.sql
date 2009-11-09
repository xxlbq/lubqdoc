

SELECT * FROM JHF_CUSTOMER_STATUS WHERE CUSTOMER_ID = '90000088';


SELECT * FROM JHF_APP_PROPERTY WHERE PROP_TYPE = 'SYSTEM'


--===================================================
SELECT * FROM JHF_UNREALIZED_CASHFLOW WHERE CASHFLOW_SOURCE_ID IN ('20090825CUEX10339615') ;

SELECT * FROM JHF_UNREALIZED_CASHFLOW WHERE CASHFLOW_SOURCE_ID IN (

    SELECT EXECUTION_ID FROM JHF_ALIVE_EXECUTION WHERE ORDER_ID IN (
        SELECT ORDER_ID FROM JHF_ALIVE_ORDER WHERE ORDER_BIND_ID ='2009091800081316' ));

--SELECT * FROM JHF_UNREALIZED_CASHFLOW  WHERE CASHFLOW_TYPE = 6 ORDER BY INPUT_DATE DESC;


SELECT * FROM JHF_ALIVE_CONTRACT_BIND WHERE EXECUTION_ID IN (SELECT EXECUTION_ID FROM JHF_ALIVE_EXECUTION WHERE ORDER_ID IN (
        SELECT ORDER_ID FROM JHF_ALIVE_ORDER WHERE ORDER_BIND_ID ='2009082400178704' ));

SELECT * FROM JHF_SYS_POSITION_INSERT WHERE POSITION_SOURCE_ID IN (SELECT EXECUTION_ID FROM JHF_ALIVE_EXECUTION WHERE ORDER_ID IN (
        SELECT ORDER_ID FROM JHF_ALIVE_ORDER WHERE ORDER_BIND_ID ='2009082400178704' ));


SELECT ORDER_ID,EXECUTION_ID,COMMISSION ,EXECUTION_AMOUNT FROM JHF_ALIVE_EXECUTION WHERE ORDER_ID IN (
        SELECT ORDER_ID FROM JHF_ALIVE_ORDER WHERE ORDER_BIND_ID ='2009082400178704' );



--\\

SELECT CONTRACT_ID,ORDER_ID,FORCE_RELATION_ID, SETTLEMENT_COMMISSION,OPEN_COMMISSION FROM JHF_ALIVE_CONTRACT WHERE ORDER_ID IN (
    SELECT ORDER_ID FROM JHF_ALIVE_ORDER WHERE ORDER_BIND_ID ='2009031800001406' );

SELECT ORDER_ID FROM JHF_ALIVE_ORDER WHERE ORDER_BIND_ID ='2009031800001006' 

SELECT * FROM JHF_FINISHED_CONTRACT WHERE ORDER_ID = '20090318ORD00001806';


--SELECT ORDER_ID ,TRADE_TYPE FROM JHF_ALIVE_ORDER WHERE ORDER_BIND_ID ='2009031800000901';

--SELECT * FROM JHF_PRODUCT WHERE CURRENCY_PAIR = 'USD/JPY' AND LEVERAGE_LEVEL_ID = ;


show create table JHF_PRODUCT ;


--============

SELECT * FROM JHF_PRODUCT WHERE CURRENCY_PAIR = 'USD/JPY';

UPDATE JHF_PRODUCT SET LOSSCUT_COMMISSION = LEVERAGE_LEVEL_ID WHERE CURRENCY_PAIR = 'USD/JPY';
commit;


UPDATE JHF_PRODUCT SET LOSSCUT_COMMISSION = 500 where PRODUCT_ID = 'A001' and CURRENCY_PAIR = 'EUR/JPY';

SELECT * FROM JHF_ALIVE_CONTRACT WHERE ORDER_ID IN (SELECT ORDER_ID FROM JHF_ALIVE_ORDER WHERE ORDER_BIND_ID IN( '2009040700804003') )

2009040700804003

------



mysql> SELECT CURRENCY_PAIR,LEVERAGE_LEVEL_ID, LOSSCUT_COMMISSION FROM JHF_PRODUCT WHERE CURRENCY_PAIR='USD/JPY'\G;     
*************************** 1. row ***************************
     CURRENCY_PAIR: USD/JPY
 LEVERAGE_LEVEL_ID: 5
LOSSCUT_COMMISSION: 400.00
*************************** 2. row ***************************
     CURRENCY_PAIR: USD/JPY
 LEVERAGE_LEVEL_ID: 10
LOSSCUT_COMMISSION: 400.00
*************************** 3. row ***************************
     CURRENCY_PAIR: USD/JPY
 LEVERAGE_LEVEL_ID: 100
LOSSCUT_COMMISSION: 400.00
*************************** 4. row ***************************
     CURRENCY_PAIR: USD/JPY
 LEVERAGE_LEVEL_ID: 200
LOSSCUT_COMMISSION: 0.00
*************************** 5. row ***************************
     CURRENCY_PAIR: USD/JPY
 LEVERAGE_LEVEL_ID: 25
LOSSCUT_COMMISSION: 400.00
*************************** 6. row ***************************
     CURRENCY_PAIR: USD/JPY
 LEVERAGE_LEVEL_ID: 20
LOSSCUT_COMMISSION: 400.00
*************************** 7. row ***************************
     CURRENCY_PAIR: USD/JPY
 LEVERAGE_LEVEL_ID: 50
LOSSCUT_COMMISSION: 400.00
*************************** 8. row ***************************
     CURRENCY_PAIR: USD/JPY
 LEVERAGE_LEVEL_ID: 1
LOSSCUT_COMMISSION: 400.00
*************************** 9. row ***************************
     CURRENCY_PAIR: USD/JPY
 LEVERAGE_LEVEL_ID: 15
LOSSCUT_COMMISSION: 400.00
*************************** 10. row ***************************
     CURRENCY_PAIR: USD/JPY
 LEVERAGE_LEVEL_ID: 33
LOSSCUT_COMMISSION: 400.00
*************************** 11. row ***************************
     CURRENCY_PAIR: USD/JPY
 LEVERAGE_LEVEL_ID: 40
LOSSCUT_COMMISSION: 400.00
11 rows in set (0.00 sec)

ERROR: 
No query specifie

;
SELECT * FROM JHF_LEVERAGE_GROUP WHERE CUSTOMER_ID = '00000062';
SELECT * FROM JHF_GROUP_PRODUCT_BAND WHERE CURRENCY_PAIR = 'USD/JPY'




UPDATE  JHF_PRODUCT SET  LOSSCUT_COMMISSION  = 0 WHERE CURRENCY_PAIR='USD/JPY' AND LEVERAGE_LEVEL_ID >100





 INSERT INTO JHF_GROUP_PRODUCT_BAND (GROUP_ID,PRODUCT_ID,CURRENCY_PAIR,INPUT_STAFF_ID,UPDATE_STAFF_ID,ACTIVE_FLAG,INPUT_DATE,UPDATE_DATE)
 SELECT B.GROUP_ID,A.PRODUCT_ID,A.CURRENCY_PAIR ,'system','system',1,NOW(),NOW() 
 FROM JHF_PRODUCT A , JHF_GROUP B
 WHERE  A.LEVERAGE_LEVEL_ID IN ('100','200') AND A.ACTIVE_FLAG=1 AND B.ACTIVE_FLAG=1 
 ORDER BY B.GROUP_ID , A.CURRENCY_PAIR ;
 
 
 
 
 
 
 
 
 
 
 
 
 
 
mysql> SELECT * FROM   JHF_GROUP_PRODUCT_BAND WHERE GROUP_ID = 'JUNIOR' AND CURRENCY_PAIR = 'USD/JPY'\G;
*************************** 1. row ***************************
       GROUP_ID: JUNIOR
     PRODUCT_ID: A001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
*************************** 2. row ***************************
       GROUP_ID: JUNIOR
     PRODUCT_ID: B001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
*************************** 3. row ***************************
       GROUP_ID: JUNIOR
     PRODUCT_ID: E001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
*************************** 4. row ***************************
       GROUP_ID: JUNIOR
     PRODUCT_ID: F001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
*************************** 5. row ***************************
       GROUP_ID: JUNIOR
     PRODUCT_ID: G001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
*************************** 6. row ***************************
       GROUP_ID: JUNIOR
     PRODUCT_ID: H001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
*************************** 7. row ***************************
       GROUP_ID: JUNIOR
     PRODUCT_ID: N001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
*************************** 8. row ***************************
       GROUP_ID: JUNIOR
     PRODUCT_ID: P001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
*************************** 9. row ***************************
       GROUP_ID: JUNIOR
     PRODUCT_ID: Q001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
9 rows in set (0.00 sec)

ERROR: 
No query specified

mysql> 
mysql> 
mysql> 
mysql> 
mysql> 
mysql> 
mysql> SELECT * FROM JHF_PRODUCT WHERE CURRENCY_PAIR='USD/JPY'\G;
*************************** 1. row ***************************
                  PRODUCT_ID: A001
                PRODUCT_TYPE: 0
           LEVERAGE_LEVEL_ID: 5
          DISPLAY_SORT_ORDER: 1005
               CURRENCY_PAIR: USD/JPY
                MAX_QUANTITY: 3000000.00
                MIN_QUANTITY: 10000.00
                    LOT_SIZE: 10000.00
                PRODUCT_NAME: ??h??/?~
               TRADE_DEPOSIT: 181440.00
         OPEN_BUY_CONSTRAINT: 0
        OPEN_SELL_CONSTRAINT: 0
        CLOSE_BUY_CONSTRAINT: 0
       CLOSE_SELL_CONSTRAINT: 0
                 USABLE_FLAG: 1
            WEB_AVAILABILITY: 1
               DISPATCH_MODE: NULL
                    MATURITY: 99
                  PRODUCT_CD: NULL
            PRICE_MOVE_LIMIT: 0.00000000
                   AUTO_EXEC: 1
 DELIVERY_RECIEPT_CONSTRAINT: 0
DELIVERY_DELIVERY_CONSTRAINT: 0
          LOSSCUT_COMMISSION: 400.00
             UPDATE_STAFF_ID: system
              INPUT_STAFF_ID: system
                 ACTIVE_FLAG: 1
                  INPUT_DATE: 2007-08-18 16:52:11
                 UPDATE_DATE: 2009-09-18 05:51:06
*************************** 2. row ***************************
                  PRODUCT_ID: B001
                PRODUCT_TYPE: 0
           LEVERAGE_LEVEL_ID: 10
          DISPLAY_SORT_ORDER: 1010
               CURRENCY_PAIR: USD/JPY
                MAX_QUANTITY: 3000000.00
                MIN_QUANTITY: 10000.00
                    LOT_SIZE: 10000.00
                PRODUCT_NAME: ??h??/?~
               TRADE_DEPOSIT: 90720.00
         OPEN_BUY_CONSTRAINT: 0
        OPEN_SELL_CONSTRAINT: 0
        CLOSE_BUY_CONSTRAINT: 0
       CLOSE_SELL_CONSTRAINT: 0
                 USABLE_FLAG: 1
            WEB_AVAILABILITY: 1
               DISPATCH_MODE: NULL
                    MATURITY: 99
                  PRODUCT_CD: NULL
            PRICE_MOVE_LIMIT: 0.00000000
                   AUTO_EXEC: 1
 DELIVERY_RECIEPT_CONSTRAINT: 0
DELIVERY_DELIVERY_CONSTRAINT: 0
          LOSSCUT_COMMISSION: 400.00
             UPDATE_STAFF_ID: system
              INPUT_STAFF_ID: system
                 ACTIVE_FLAG: 1
                  INPUT_DATE: 2007-08-18 16:52:11
                 UPDATE_DATE: 2009-09-18 05:51:06
*************************** 3. row ***************************
                  PRODUCT_ID: C001
                PRODUCT_TYPE: 0
           LEVERAGE_LEVEL_ID: 100
          DISPLAY_SORT_ORDER: 1100
               CURRENCY_PAIR: USD/JPY
                MAX_QUANTITY: 3000000.00
                MIN_QUANTITY: 10000.00
                    LOT_SIZE: 10000.00
                PRODUCT_NAME: ??h??/?~
               TRADE_DEPOSIT: 9072.00
         OPEN_BUY_CONSTRAINT: 0
        OPEN_SELL_CONSTRAINT: 0
        CLOSE_BUY_CONSTRAINT: 0
       CLOSE_SELL_CONSTRAINT: 0
                 USABLE_FLAG: 1
            WEB_AVAILABILITY: 1
               DISPATCH_MODE: NULL
                    MATURITY: 99
                  PRODUCT_CD: NULL
            PRICE_MOVE_LIMIT: 0.00000000
                   AUTO_EXEC: 1
 DELIVERY_RECIEPT_CONSTRAINT: 0
DELIVERY_DELIVERY_CONSTRAINT: 0
          LOSSCUT_COMMISSION: 400.00
             UPDATE_STAFF_ID: system
              INPUT_STAFF_ID: system
                 ACTIVE_FLAG: 1
                  INPUT_DATE: 2007-02-25 06:41:03
                 UPDATE_DATE: 2009-09-18 05:51:06
*************************** 4. row ***************************
                  PRODUCT_ID: D001
                PRODUCT_TYPE: 0
           LEVERAGE_LEVEL_ID: 200
          DISPLAY_SORT_ORDER: 1200
               CURRENCY_PAIR: USD/JPY
                MAX_QUANTITY: 3000000.00
                MIN_QUANTITY: 10000.00
                    LOT_SIZE: 10000.00
                PRODUCT_NAME: ??h??/?~
               TRADE_DEPOSIT: 4536.00
         OPEN_BUY_CONSTRAINT: 0
        OPEN_SELL_CONSTRAINT: 0
        CLOSE_BUY_CONSTRAINT: 0
       CLOSE_SELL_CONSTRAINT: 0
                 USABLE_FLAG: 1
            WEB_AVAILABILITY: 1
               DISPATCH_MODE: NULL
                    MATURITY: 99
                  PRODUCT_CD: NULL
            PRICE_MOVE_LIMIT: 0.00000000
                   AUTO_EXEC: 1
 DELIVERY_RECIEPT_CONSTRAINT: 0
DELIVERY_DELIVERY_CONSTRAINT: 0
          LOSSCUT_COMMISSION: 0.00
             UPDATE_STAFF_ID: system
              INPUT_STAFF_ID: system
                 ACTIVE_FLAG: 1
                  INPUT_DATE: 2007-02-25 06:41:03
                 UPDATE_DATE: 2009-09-18 05:51:06
*************************** 5. row ***************************
                  PRODUCT_ID: E001
                PRODUCT_TYPE: 0
           LEVERAGE_LEVEL_ID: 25
          DISPLAY_SORT_ORDER: 1025
               CURRENCY_PAIR: USD/JPY
                MAX_QUANTITY: 3000000.00
                MIN_QUANTITY: 10000.00
                    LOT_SIZE: 10000.00
                PRODUCT_NAME: ??h??/?~
               TRADE_DEPOSIT: 36288.00
         OPEN_BUY_CONSTRAINT: 0
        OPEN_SELL_CONSTRAINT: 0
        CLOSE_BUY_CONSTRAINT: 0
       CLOSE_SELL_CONSTRAINT: 0
                 USABLE_FLAG: 1
            WEB_AVAILABILITY: 1
               DISPATCH_MODE: NULL
                    MATURITY: 99
                  PRODUCT_CD: NULL
            PRICE_MOVE_LIMIT: 0.00000000
                   AUTO_EXEC: 1
 DELIVERY_RECIEPT_CONSTRAINT: 0
DELIVERY_DELIVERY_CONSTRAINT: 0
          LOSSCUT_COMMISSION: 400.00
             UPDATE_STAFF_ID: system
              INPUT_STAFF_ID: system
                 ACTIVE_FLAG: 1
                  INPUT_DATE: 2009-05-28 09:58:23
                 UPDATE_DATE: 2009-09-18 05:51:06
*************************** 6. row ***************************
                  PRODUCT_ID: F001
                PRODUCT_TYPE: 0
           LEVERAGE_LEVEL_ID: 20
          DISPLAY_SORT_ORDER: 1020
               CURRENCY_PAIR: USD/JPY
                MAX_QUANTITY: 3000000.00
                MIN_QUANTITY: 10000.00
                    LOT_SIZE: 10000.00
                PRODUCT_NAME: ??h??/?~
               TRADE_DEPOSIT: 45360.00
         OPEN_BUY_CONSTRAINT: 0
        OPEN_SELL_CONSTRAINT: 0
        CLOSE_BUY_CONSTRAINT: 0
       CLOSE_SELL_CONSTRAINT: 0
                 USABLE_FLAG: 1
            WEB_AVAILABILITY: 1
               DISPATCH_MODE: NULL
                    MATURITY: 99
                  PRODUCT_CD: NULL
            PRICE_MOVE_LIMIT: 0.00000000
                   AUTO_EXEC: 1
 DELIVERY_RECIEPT_CONSTRAINT: 0
DELIVERY_DELIVERY_CONSTRAINT: 0
          LOSSCUT_COMMISSION: 400.00
             UPDATE_STAFF_ID: system
              INPUT_STAFF_ID: system
                 ACTIVE_FLAG: 1
                  INPUT_DATE: 2007-08-18 16:52:11
                 UPDATE_DATE: 2009-09-18 05:51:06
*************************** 7. row ***************************
                  PRODUCT_ID: G001
                PRODUCT_TYPE: 0
           LEVERAGE_LEVEL_ID: 50
          DISPLAY_SORT_ORDER: 1050
               CURRENCY_PAIR: USD/JPY
                MAX_QUANTITY: 3000000.00
                MIN_QUANTITY: 10000.00
                    LOT_SIZE: 10000.00
                PRODUCT_NAME: ??h??/?~
               TRADE_DEPOSIT: 18144.00
         OPEN_BUY_CONSTRAINT: 0
        OPEN_SELL_CONSTRAINT: 0
        CLOSE_BUY_CONSTRAINT: 0
       CLOSE_SELL_CONSTRAINT: 0
                 USABLE_FLAG: 1
            WEB_AVAILABILITY: 1
               DISPATCH_MODE: NULL
                    MATURITY: 99
                  PRODUCT_CD: NULL
            PRICE_MOVE_LIMIT: 0.00000000
                   AUTO_EXEC: 1
 DELIVERY_RECIEPT_CONSTRAINT: 0
DELIVERY_DELIVERY_CONSTRAINT: 0
          LOSSCUT_COMMISSION: 400.00
             UPDATE_STAFF_ID: system
              INPUT_STAFF_ID: system
                 ACTIVE_FLAG: 1
                  INPUT_DATE: 2007-08-18 16:52:11
                 UPDATE_DATE: 2009-09-18 05:51:06
*************************** 8. row ***************************
                  PRODUCT_ID: H001
                PRODUCT_TYPE: 0
           LEVERAGE_LEVEL_ID: 1
          DISPLAY_SORT_ORDER: 1001
               CURRENCY_PAIR: USD/JPY
                MAX_QUANTITY: 3000000.00
                MIN_QUANTITY: 10000.00
                    LOT_SIZE: 10000.00
                PRODUCT_NAME: ??h??/?~
               TRADE_DEPOSIT: 907200.00
         OPEN_BUY_CONSTRAINT: 0
        OPEN_SELL_CONSTRAINT: 0
        CLOSE_BUY_CONSTRAINT: 0
       CLOSE_SELL_CONSTRAINT: 0
                 USABLE_FLAG: 1
            WEB_AVAILABILITY: 1
               DISPATCH_MODE: NULL
                    MATURITY: 99
                  PRODUCT_CD: NULL
            PRICE_MOVE_LIMIT: 0.00000000
                   AUTO_EXEC: 1
 DELIVERY_RECIEPT_CONSTRAINT: 0
DELIVERY_DELIVERY_CONSTRAINT: 0
          LOSSCUT_COMMISSION: 400.00
             UPDATE_STAFF_ID: system
              INPUT_STAFF_ID: system
                 ACTIVE_FLAG: 1
                  INPUT_DATE: 2007-08-18 16:52:11
                 UPDATE_DATE: 2009-09-18 05:51:06
*************************** 9. row ***************************
                  PRODUCT_ID: N001
                PRODUCT_TYPE: 0
           LEVERAGE_LEVEL_ID: 15
          DISPLAY_SORT_ORDER: 1015
               CURRENCY_PAIR: USD/JPY
                MAX_QUANTITY: 3000000.00
                MIN_QUANTITY: 10000.00
                    LOT_SIZE: 10000.00
                PRODUCT_NAME: ??h??/?~
               TRADE_DEPOSIT: 60480.00
         OPEN_BUY_CONSTRAINT: 0
        OPEN_SELL_CONSTRAINT: 0
        CLOSE_BUY_CONSTRAINT: 0
       CLOSE_SELL_CONSTRAINT: 0
                 USABLE_FLAG: 1
            WEB_AVAILABILITY: 1
               DISPATCH_MODE: NULL
                    MATURITY: 99
                  PRODUCT_CD: NULL
            PRICE_MOVE_LIMIT: 0.00000000
                   AUTO_EXEC: 1
 DELIVERY_RECIEPT_CONSTRAINT: 0
DELIVERY_DELIVERY_CONSTRAINT: 0
          LOSSCUT_COMMISSION: 400.00
             UPDATE_STAFF_ID: system
              INPUT_STAFF_ID: system
                 ACTIVE_FLAG: 1
                  INPUT_DATE: 2009-05-28 09:58:23
                 UPDATE_DATE: 2009-09-18 05:51:06
*************************** 10. row ***************************
                  PRODUCT_ID: P001
                PRODUCT_TYPE: 0
           LEVERAGE_LEVEL_ID: 33
          DISPLAY_SORT_ORDER: 1033
               CURRENCY_PAIR: USD/JPY
                MAX_QUANTITY: 3000000.00
                MIN_QUANTITY: 10000.00
                    LOT_SIZE: 10000.00
                PRODUCT_NAME: ??h??/?~
               TRADE_DEPOSIT: 27491.00
         OPEN_BUY_CONSTRAINT: 0
        OPEN_SELL_CONSTRAINT: 0
        CLOSE_BUY_CONSTRAINT: 0
       CLOSE_SELL_CONSTRAINT: 0
                 USABLE_FLAG: 1
            WEB_AVAILABILITY: 1
               DISPATCH_MODE: NULL
                    MATURITY: 99
                  PRODUCT_CD: NULL
            PRICE_MOVE_LIMIT: 0.00000000
                   AUTO_EXEC: 1
 DELIVERY_RECIEPT_CONSTRAINT: 0
DELIVERY_DELIVERY_CONSTRAINT: 0
          LOSSCUT_COMMISSION: 400.00
             UPDATE_STAFF_ID: system
              INPUT_STAFF_ID: system
                 ACTIVE_FLAG: 1
                  INPUT_DATE: 2009-05-28 09:58:23
                 UPDATE_DATE: 2009-09-18 05:51:06
*************************** 11. row ***************************
                  PRODUCT_ID: Q001
                PRODUCT_TYPE: 0
           LEVERAGE_LEVEL_ID: 40
          DISPLAY_SORT_ORDER: 1040
               CURRENCY_PAIR: USD/JPY
                MAX_QUANTITY: 3000000.00
                MIN_QUANTITY: 10000.00
                    LOT_SIZE: 10000.00
                PRODUCT_NAME: ??h??/?~
               TRADE_DEPOSIT: 22680.00
         OPEN_BUY_CONSTRAINT: 0
        OPEN_SELL_CONSTRAINT: 0
        CLOSE_BUY_CONSTRAINT: 0
       CLOSE_SELL_CONSTRAINT: 0
                 USABLE_FLAG: 1
            WEB_AVAILABILITY: 1
               DISPATCH_MODE: NULL
                    MATURITY: 99
                  PRODUCT_CD: NULL
            PRICE_MOVE_LIMIT: 0.00000000
                   AUTO_EXEC: 1
 DELIVERY_RECIEPT_CONSTRAINT: 0
DELIVERY_DELIVERY_CONSTRAINT: 0
          LOSSCUT_COMMISSION: 400.00
             UPDATE_STAFF_ID: system
              INPUT_STAFF_ID: system
                 ACTIVE_FLAG: 1
                  INPUT_DATE: 2009-05-28 10:08:20
                 UPDATE_DATE: 2009-09-18 05:51:06
11 rows in set (0.00 sec)

ERROR: 
No query specified

mysql> SELECT * FROM   JHF_GROUP_PRODUCT_BAND WHERE GROUP_ID = 'JUNIOR' AND CURRENCY_PAIR = 'USD/JPY'\G;
*************************** 1. row ***************************
       GROUP_ID: JUNIOR
     PRODUCT_ID: A001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
*************************** 2. row ***************************
       GROUP_ID: JUNIOR
     PRODUCT_ID: B001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
*************************** 3. row ***************************
       GROUP_ID: JUNIOR
     PRODUCT_ID: E001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
*************************** 4. row ***************************
       GROUP_ID: JUNIOR
     PRODUCT_ID: F001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
*************************** 5. row ***************************
       GROUP_ID: JUNIOR
     PRODUCT_ID: G001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
*************************** 6. row ***************************
       GROUP_ID: JUNIOR
     PRODUCT_ID: H001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
*************************** 7. row ***************************
       GROUP_ID: JUNIOR
     PRODUCT_ID: N001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
*************************** 8. row ***************************
       GROUP_ID: JUNIOR
     PRODUCT_ID: P001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
*************************** 9. row ***************************
       GROUP_ID: JUNIOR
     PRODUCT_ID: Q001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
9 rows in set (0.00 sec)

ERROR: 
No query specified

mysql> SELECT * FROM   JHF_GROUP_PRODUCT_BAND WHERE PRODUCT_ID = 'D001' AND CURRENCY_PAIR = 'USD/JPY'\G;              
*************************** 1. row ***************************
       GROUP_ID: SENIOR
     PRODUCT_ID: D001
  CURRENCY_PAIR: USD/JPY
 INPUT_STAFF_ID: system
UPDATE_STAFF_ID: system
    ACTIVE_FLAG: 1
     INPUT_DATE: 2009-06-09 20:03:17
    UPDATE_DATE: 2009-06-09 20:03:17
1 row in set (0.00 sec)

ERROR: 
No query specified










UPDATE JHF_CUSTOMER_STATUS SET GROUP_ID = 'SENIOR' WHERE CUSTOMER_ID = '00000062'\G;



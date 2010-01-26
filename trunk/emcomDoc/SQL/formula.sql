

SELECT * FROM JHF_SYSTEM_CONFIGURATION




SELECT * FROM JHF_SYSTEM_CONFIGURATION_HISTORY WHERE FUNC_ID = 'FormulaDiffService'



SELECT * FROM JHF_SYSTEM_CONFIGURATION;


--  FORMULA  DIFF  ����
-- FORMULA SERVICE 
INSERT INTO JHF_SYSTEM_CONFIGURATION(FUNC_MODULE, FUNC_ID, FUNC_VALUE, FUNC_COMMENT, FUNC_GROUP, ACTIVE_FLAG, INPUT_DATE, UPDATE_DATE) 
    VALUES('Formula', 'FormulaDiffService', 'cn.bestwiz.jhf.core.formula.diff.service.FormulaDiffService4Panta', 'Formula Diff', 'Middle', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
;
-- FORMULA BUSINESS
INSERT INTO JHF_SYSTEM_CONFIGURATION(FUNC_MODULE, FUNC_ID, FUNC_VALUE, FUNC_COMMENT, FUNC_GROUP, ACTIVE_FLAG, INPUT_DATE, UPDATE_DATE) 
    VALUES('Formula', 'FormulaDiffBusiness', 'cn.bestwiz.jhf.core.formula.diff.business.FormulaDiffBusiness4Panta', 'Formula Diff', 'Middle', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
;



--  LOSSCUT  DIFF  ����
INSERT INTO JHF_SYSTEM_CONFIGURATION(FUNC_MODULE, FUNC_ID, FUNC_VALUE, FUNC_COMMENT, FUNC_GROUP, ACTIVE_FLAG, INPUT_DATE, UPDATE_DATE) 
    VALUES('Losscut', 'LosscutProcessor', 'cn.bestwiz.jhf.trader.losscut.diff.processor.LosscutProcessor4Panta', 'Losscut Diff', 'Middle', 1, CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
;


--  LOSS CUT DEFAULT   
INSERT INTO JHF_SYSTEM_CONFIGURATION(FUNC_MODULE, FUNC_ID, FUNC_VALUE, FUNC_COMMENT, FUNC_GROUP, ACTIVE_FLAG, INPUT_DATE, UPDATE_DATE) 
    VALUES('Losscut', 'LosscutProcessor', 'cn.bestwiz.jhf.trader.losscut.processor.DefaultLosscutProcessor', 'Losscut Diff', 'Middle', 1, CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
;




-- ɾ�� 
DELETE FROM JHF_SYSTEM_CONFIGURATION WHERE FUNC_MODULE = 'Formula' AND FUNC_ID = 'LosscutProcessor' ;






SELECT * FROM JHF_SYSTEM_CONFIGURATION WHERE FUNC_MODULE = 'Losscut' AND FUNC_ID = 'LosscutProcessor'

SELECT * FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID ='00000101' AND ORDER_STATUS=7;
DELETE FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID ='00000101' AND ORDER_STATUS=7;


--  ����  LOSSCT NOTIC ALERT ��   �� JHF_GROUP ��
UPDATE JHF_GROUP SET LOSSCUT_RATIO = 1.00 , ALERT_RATIO = 1.30 ,NOTICE_RATIO = 1.50 


SELECT SUM(CASHFLOW_AMOUNT)  FROM JHF_UNREALIZED_CASHFLOW WHERE CUSTOMER_ID = '00000101' AND CASHFLOW_TYPE IN (3,4,5,6,9,12);









SELEC

SELECT * FROM JHF_PRODUCT WHERE CURRENCY_PAIR = 'EUR/USD';

UPDATE JHF_PRODUCT SET MIN_QUANTITY = 1000 WHERE CURRENCY_PAIR = 'EUR/USD' AND LEVERAGE_LEVEL_ID = 100;

SELECT GROUP_ID, LOSSCUT_RATIO,ALERT_RATIO,NOTICE_RATIO FROM JHF_CUSTOMER_STATUS 




SELECT * FROM JHF_APP_PROPERTY WHERE PROP_TYPE='RATE'

hedgerate4slipexpiredtime



INSERT INTO JHF_APP_PROPERTY(PROP_TYPE, PROPERTY_KEY, PROPERTY_VALUE, DISPLAY_NAME, NOTE, UPDATE_STAFF_ID, INPUT_STAFF_ID, ACTIVE_FLAG, INPUT_DATE, UPDATE_DATE) 
    VALUES('RATE', 'hedgerate4slipexpiredtime', '20000', '', 'hedgRate expire time', 'system', 'system', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
;





SELECT NOTICE_RATIO ,ALERT_RATIO,LOSSCUT_WARN_RATIO, LOSSCUT_RATIO FROM JHF_GROUP WHERE GROUP_ID  ='DEFAULT'


SELECT PRODUCT_ID,TRADE_DEPOSIT,LEVERAGE_LEVEL_ID FROM JHF_PRODUCT WHERE CURRENCY_PAIR = 'USD/JPY';
SELECT TRADE_DEPOSIT FROM JHF_PRODUCT WHERE CURRENCY_PAIR = 'USD/JPY' AND LEVERAGE_LEVEL_ID = 100 


-- ����ֱֲ�֤���ܶ� 
SELECT SUM(SP.V) FROM (
SELECT CNT.PRODUCT_ID,CNT.AMOUNT_NO_SETTLED ,PDT.LEVERAGE_LEVEL_ID, PDT.TRADE_DEPOSIT 
   ,( CNT.AMOUNT_NO_SETTLED * TRADE_DEPOSIT / PDT.LOT_SIZE) AS V  
FROM JHF_ALIVE_CONTRACT CNT

LEFT OUTER JOIN JHF_PRODUCT PDT ON PDT.PRODUCT_ID =CNT.PRODUCT_ID 

WHERE  CNT.CUSTOMER_ID = '00003242' AND CNT.AMOUNT_NO_SETTLED > 0 AND CNT.STATUS = 1 


) AS SP



-- ����ע�ı�֤���ܶ� 


SELECT SUM(SO.V) FROM (
SELECT OD.PRODUCT_ID,PDT.LEVERAGE_LEVEL_ID, PDT.TRADE_DEPOSIT ,OD.ORDER_AMOUNT ,OD.CURRENCY_PAIR 
   ,( OD.ORDER_AMOUNT * TRADE_DEPOSIT / PDT.LOT_SIZE) AS V  
FROM JHF_ALIVE_ORDER OD

LEFT OUTER JOIN JHF_PRODUCT PDT ON PDT.PRODUCT_ID =OD.PRODUCT_ID 

WHERE  OD.CUSTOMER_ID = '00003242' AND OD.ORDER_STATUS IN (0,1,7)  


) AS SO




-- ���㽨����������  �� #################>>>>> �������� JPY ��β�Ļ��Ҷ� ��
-- ��Ҫ�޸�  CUSTOMER_ID  ��NOW_ASK ��NOW_BID  ��CURRENCY_PAIR

--�I����������~���������s�����ʣ���(-1)��������~���u�����ʵ�Bid
--�ӽ���������~���������s�����ʣ���������~���u�����ʵ�Ask��(-1)

SELECT SUM(PVS.PV) FROM (
SELECT  CNT.SIDE,CNT.EXECUTION_PRICE,CNT.AMOUNT_NO_SETTLED ,

CASE WHEN SIDE = 1 
      THEN ( 1.31220 - CNT.EXECUTION_PRICE) * CNT.AMOUNT_NO_SETTLED 
      ELSE (  CNT.EXECUTION_PRICE - 1.31260) * CNT.AMOUNT_NO_SETTLED  END AS PV

FROM JHF_ALIVE_CONTRACT AS CNT 
WHERE CNT.CUSTOMER_ID = '00003242' AND CNT.AMOUNT_NO_SETTLED > 0 AND CNT.STATUS = 1 AND CURRENCY_PAIR = 'EUR/USD'
) AS PVS





-- ���㽨����������  �� #################>>>>> �������� ��JPY ��β�Ļ��Ҷ� ��
-- ��Ҫ�޸�  CUSTOMER_ID  ��NOW_ASK ��NOW_BID  ��CURRENCY_PAIR
SELECT SUM(PVS.PV) FROM (
SELECT  CNT.SIDE,CNT.EXECUTION_PRICE,CNT.AMOUNT_NO_SETTLED ,

CASE WHEN SIDE = 1 
      THEN ( 1.31220 - CNT.EXECUTION_PRICE) * CNT.AMOUNT_NO_SETTLED 
      ELSE (  CNT.EXECUTION_PRICE - 1.31260) * CNT.AMOUNT_NO_SETTLED  END AS PV

FROM JHF_ALIVE_CONTRACT AS CNT 
WHERE CNT.CUSTOMER_ID = '00003242' AND CNT.AMOUNT_NO_SETTLED > 0 AND CNT.STATUS = 1 AND CURRENCY_PAIR = 'EUR/USD'
) AS PVS






SELECT * FROM JHF_CUSTOMER_STATUS
SELECT * FROM JHF_GROUP_PRODUCT_BAND;


SELECT * FROM JHF_APP_PROPERTY WHERE PROP_TYPE ='WITHDRAW';



---  ��ѯ CASH BALANCE 

SELECT CASH_BALANCE FROM JHF_CASH_BALANCE WHERE CUSTOMER_ID = '00003242';


-- ��������� (����Ԥ����)
SELECT WITHDRAWAL_AMOUNT FROM JHF_WITHDRAWAL WHERE CUSTOMER_ID = '00003242'

-- ��� contract �� swap ���� �ܺ�  

SELECT SUM(SWAP_INTEREST) FROM JHF_ALIVE_CONTRACT WHERE CUSTOMER_ID = '00000101' AND AMOUNT_NO_SETTLED > 0 AND STATUS = 1 ;
SELECT * FROM JHF_ALIVE_CONTRACT WHERE CUSTOMER_ID = '00000101' AND AMOUNT_NO_SETTLED > 0 AND STATUS = 1 ;


UPDATE JHF_ALIVE_CONTRACT SET SWAP_INTEREST = 88 WHERE CONTRACT_ID = '20100105CONT00414227'

SELECT SUM(CASHFLOW_AMOUNT) FROM JHF_UNREALIZED_CASHFLOW WHERE CUSTOMER_ID = '00000101' 
 AND CASHFLOW_TYPE IN (3,4,5,6,9,10,12) 
 AND ACTIVE_FLAG = 1 ;


--- ������趨�~      �ֽ�������Ϊȡ���p��9�9�����ρ9�9swap����������ΪT+1���ֽ���

SELECT SUM(CASHFLOW_AMOUNT) FROM JHF_UNREALIZED_CASHFLOW WHERE CUSTOMER_ID = '00003242' 
 AND CASHFLOW_TYPE IN (3,4,5,6,9,10,12) 
 AND ACTIVE_FLAG = 1 AND VALUE_DATE = '20100119';

--- �țQ�g�p��       �ֽ�������Ϊȡ���p��9�9�����ρ9�9swap����������ΪT+2���ֽ���

SELECT SUM(CASHFLOW_AMOUNT) FROM JHF_UNREALIZED_CASHFLOW WHERE CUSTOMER_ID = '00003242' 
 AND CASHFLOW_TYPE IN (3,4,5,6,9,10,12) 
 AND ACTIVE_FLAG = 1 AND VALUE_DATE = '20100120';


SELECT VALUE_DATE FROM JHF_UNREALIZED_CASHFLOW WHERE CUSTOMER_ID = '00000101' 
 AND CASHFLOW_TYPE IN (3,4,5,6,9,10,12) 
 AND ACTIVE_FLAG = 1 ;


DELETE  FROM JHF_UNREALIZED_CASHFLOW WHERE CUSTOMER_ID = '00000101' AND VALUE_DATE = '20091210';

SELECT DAYOFWEEK('20100112');

SELECT * FROM JHF_HOLIDAY


SELECT * FROM JHF_APPLICATION_DATE
SELECT * FROM JHF_TRADE_CONSTRAINT
--- ===========================



SELECT * FROM JHF_MARGIN_ACCOUNT WHERE CUSTOMER_ID = '00003242';
UPDATE JHF_MARGIN_ACCOUNT SET ACCOUNT_STATUS =0 WHERE CUSTOMER_ID = '00003242';





SELECT * FROM JHF_ALIVE_CONTRACT WHERE CUSTOMER_ID = '00003242' AND AMOUNT_NO_SETTLED > 0 AND AMOUNT_SETTLING > 0 ;
DELETE FROM JHF_ALIVE_CONTRACT WHERE CONTRACT_ID = '20100113CONT00415471';



SELECT * FROM JHF_ALIVE_ORDER WHERE ORDER_STATUS = 1 AND CUSTOMER_ID = '00003242' AND ORDER_AMOUNT = '10000';
DELETE FROM JHF_ALIVE_ORDER WHERE ORDER_ID='20100113ORD00811501' AND ORDER_STATUS=7;


SELECT * FROM JHF_PRODUCT WHERE CURRENCY_PAIR = 'USD/JPY'

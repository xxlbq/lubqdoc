SELECT count(*) FROM JHF_ALIVE_ORDER WHERE ORDER_ID LIKE 'TstORD%'

SELECT * FROM JHF_ALIVE_ORDER WHERE ORDER_ID LIKE 'TstORD%' AND ORDER_STATUS <> 3 ORDER BY INPUT_DATE DESC;

SELECT * FROM JHF_ALIVE_ORDER WHERE ORDER_ID LIKE 'TstORD%' AND ORDER_STATUS = 1 ORDER BY INPUT_DATE DESC;

SELECT count(*) FROM JHF_ALIVE_ORDER WHERE ORDER_ID LIKE 'TstORD%' AND ORDER_STATUS = 1 ORDER BY INPUT_DATE DESC;

SELECT count(*) FROM JHF_ALIVE_ORDER WHERE ORDER_ID LIKE 'TstORD%' AND ORDER_STATUS <> 3 ORDER BY INPUT_DATE DESC;


SELECT count(*) FROM JHF_ALIVE_ORDER WHERE ORDER_ID LIKE 'TstORD%' AND ORDER_STATUS = 7 ORDER BY INPUT_DATE DESC;

SELECT count(*) FROM JHF_ALIVE_ORDER WHERE ORDER_ID LIKE 'TstORD%' AND ORDER_STATUS = 3 ORDER BY INPUT_DATE DESC;

SELECT COUNT(*) FROM JHF_ORDER_BIND WHERE ORDER_ID LIKE 'TstORD%';

DELETE FROM JHF_ALIVE_CONTRACT WHERE CUSTOMER_ID = '00006701' AND CURRENCY_PAIR = 'EUR/JPY';


;
select distinct ORDER_BIND_ID from JHF_ORDER_BIND where ORDER_ID in (SELECT order_id FROM JHF_ALIVE_ORDER WHERE ORDER_ID LIKE 'TstORD%' AND ORDER_STATUS = 7);


SELECT order_id FROM JHF_ALIVE_ORDER WHERE ORDER_ID LIKE 'TstORD%' AND ORDER_STATUS = 7  order by INPUT_DATE;
select order_id,CUSTOMER_ID from JHF_ALIVE_ORDER where order_id in (select ORDER_ID from JHF_ORDER_BIND where ORDER_BIND_ID = '2008070200003137');

SELECT * FROM JHF_CUSTOMER_BLACKLIST


SELECT * FROM JHF_ALIVE_ORDER ORDER BY INPUT_DATE DESC ;
SELECT * FROM JHF_ALIVE_ORDER WHERE ORDER_ID LIKE 'TstORD%' ORDER BY ORDER_ID DESC ;

SELECT * FROM JHF_ALIVE_ORDER  WHERE ORDER_ID ='TstORD103' AND PRICE_ID ;

SELECT * FROM JHF_ALIVE_ORDER WHERE PRICE_ID = '20080617MKPD000000081022' AND CUSTOMER_ID = '00000081';

;
SELECT * FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID = '00000081' ;

SELECT * FROM JHF_ORDER_BIND WHERE ORDER_ID = 'TstORD103';

SELECT * FROM JHF_ORDER_BIND WHERE ORDER_BIND_ID = '2008070200003137';

SELECT ORDER_ID,CUSTOMER_ID,INPUT_DATE,UPDATE_DATE FROM JHF_ALIVE_ORDER WHERE ORDER_ID IN (SELECT ORDER_ID FROM JHF_ORDER_BIND WHERE ORDER_BIND_ID = '2008070200003137');


show full processlist;

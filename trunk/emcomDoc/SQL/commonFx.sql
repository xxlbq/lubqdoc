


SELECT *  FROM JHF_ALIVE_ORDER WHERE ORDER_BIND_ID = '2009072800062009' ;



SELECT * FROM JHF_HEDGE_CUSTTRADE WHERE ID IN (
SELECT EXECUTION_ID FROM JHF_ALIVE_EXECUTION WHERE ORDER_ID IN (
SELECT ORDER_ID  FROM JHF_ALIVE_ORDER WHERE ORDER_BIND_ID = '2009072800062009'));













CURRENCY_PAIR = 'USD/JPY';







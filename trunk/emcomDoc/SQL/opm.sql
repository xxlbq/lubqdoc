
-- OPM ´¥·¢SQL

SELECT * FROM JHF_ALIVE_ORDER ORD   
 WHERE ORD.ACTIVE_FLAG = 1  
   AND ORD.CURRENCY_PAIR = 'USD/JPY'  
   AND (   (ORD.EXPIRATION_TYPE = 3 AND ORD.ACTIVATION_DATE <= CURRENT_TIMESTAMP) 
        OR (    ORD.EXPIRATION_TYPE <> 3 
            AND (ORD.ACTIVATION_DATE <= CURRENT_TIMESTAMP AND ORD.EXPIRATION_DATE >= CURRENT_TIMESTAMP) 
           ) 
       ) 
   AND (   (    ORD.EXECUTION_TYPE = 0  
            AND (   (ORD.SIDE = -1 AND ORD.ORDER_PRICE <= '88.800') 
                 OR (ORD.SIDE = 1 AND ORD.ORDER_PRICE >= '19.965') 
                ) 
           ) 
        OR (    ORD.EXECUTION_TYPE = 1 
            AND (   (   (ORD.SIDE = -1 AND ORD.ORDER_PRICE >= '88.800') 
                     OR (ORD.SIDE = 1 AND ORD.ORDER_PRICE <= '89.200') 
                    ) 
                 OR ORD.STOPORDER_RETRY_FLAG = 9 
                ) 
           ) 
       ) 
--   AND ( ORD.CUSTOMER_ID mod 3 = 1 ) 
   AND ( ORD.UPDATE_DATE <= CURRENT_TIMESTAMP )   AND ORD.ORDER_STATUS = 1 AND CUSTOMER_ID = '00022263' 
   order by  ORD.ORDER_ID asc ;



--==================


SELECT COUNT(*) FROM JHF_ALIVE_ORDER  WHERE SIDE = 1 AND ORDER_ID LIKE '%TstORD%' ORDER BY UPDATE_DATE DESC LIMIT 5;
ORDER_ID LIKE 'TstORD%'
SELECT * FROM JHF_ALIVE_ORDER  WHERE  ORDER_ID LIKE '%TstORD%' ;



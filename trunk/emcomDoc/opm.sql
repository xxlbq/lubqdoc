18:20
(2009-11-25 18:22:03) Nicholas.张振强: 
OPM的数据SQL: 
SELECT * 
  FROM jhf_alive_order 
 WHERE this_.active_flag = ? 
   AND this_.currency_pair = ? 
   AND (   (this_.expiration_type = ? AND this_.activation_date <= ?) 
        OR (    this_.expiration_type <> ? 
            AND (this_.activation_date <= ? AND this_.expiration_date >= ?) 
           ) 
       ) 
   AND (   (    this_.execution_type = ? 
            AND (   (this_.side = ? AND this_.order_price >= ?) 
                 OR (this_.side = ? AND this_.order_price <= ?) 
                ) 
           ) 
        OR (    this_.execution_type = ? 
            AND (   (   (this_.side = ? AND this_.order_price <= ?) 
                     OR (this_.side = ? AND this_.order_price >= ?) 
                    ) 
                 OR this_.stoporder_retry_flag = ? 
                ) 
           ) 
       ) 
   AND ( CUSTOMER_ID mod 2 = ? ) 
   AND ( updateDate <= ? )  
   order by asc id.orderId       
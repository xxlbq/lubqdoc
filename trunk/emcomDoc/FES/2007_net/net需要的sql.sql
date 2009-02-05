
select * from JHF_ALIVE_CONTRACT where customer_id = '00001394'

select * from JHF_CONTRACT_NETBIND where CUSTOMER_ID ='00001394'

select * from JHF_REALIZED_CASHFLOW where CUSTOMER_ID ='00001394'

select * from JHF_REALIZED_CASHFLOW where CUSTOMER_ID ='00001394' AND CASHFLOW_SOURCE_ID IN ('20071218CONT00044941','20071218CONT00044942','20071218CONT00044943','20071218CONT00044944','20071218CONT00044945','20071218CONT00044946')

select * from JHF_UNREALIZED_CASHFLOW where CUSTOMER_ID ='00001394'

select * from JHF_ASSIGN_NET_CONTRACT where CUSTOMER_ID ='00001394'






SELECT * FROM JHF_ALIVE_CONTRACT where CUSTOMER_ID = '00001332'
;

UPDATE JHF_ALIVE_CONTRACT SET SWAP_INTEREST='2000' WHERE CONTRACT_ID = '20080109CONT00045522';

UPDATE JHF_ALIVE_CONTRACT SET SWAP_INTEREST='400' WHERE CONTRACT_ID = '20080109CONT00045523';

UPDATE JHF_ALIVE_CONTRACT SET SWAP_INTEREST='600' WHERE CONTRACT_ID = '20080109CONT00045524';






 CONTRACT_ID           TRADE_TYPE     CUSTOMER_ORDER_NO     ORIGINAL_CONTRACT_DATE     CONTRACT_DATE     SUB_NO     ASSIGN_NET_AMOUNT     CONTRACT_TYPE     SIDE     CONTRACT_CURRENCY_CODE     COUNTER_CURRENCY_CODE     PRODUCT_ID     TRADE_ID              ORDER_ID             REVISION_NUMBER     CURRENCY_PAIR     CP_PRICE     CUSTOMER_ID     SWAP_INTEREST     SETTLEMENT_COMMISSION     PREVIOUS_SWAP_INTEREST     PRE_PREVIOUS_SWAP_INTEREST     ORIGINAL_SETTLE_DATE     SETTLE_DATE     AMOUNT_NO_SETTLED     CURRENT_UNREALIZED_PL     PREVIOUS_UNREALIZED_PL     EXECUTION_DATETIME     ORDER_PRICE     TRADE_PRICE     EXECUTION_PRICE     AMOUNT     ORDER_DATETIME      AMOUNT_SETTLED     OPEN_COMMISSION     AMOUNT_SETTLING     TODAY_ADDED_SWAP     FORCE_RELATION_FLAG     FORCE_RELATION_ID     STATUS     AGENT_STAFF_ID     UPDATE_STAFF_ID     INPUT_STAFF_ID     ACTIVE_FLAG     INPUT_DATE          UPDATE_DATE         AMOUNT_NO_SETTLED_SOD    
 --------------------  -------------  --------------------  -------------------------  ----------------  ---------  --------------------  ----------------  -------  -------------------------  ------------------------  -------------  --------------------  -------------------  ------------------  ----------------  -----------  --------------  ----------------  ------------------------  -------------------------  -----------------------------  -----------------------  --------------  --------------------  ------------------------  -------------------------  ---------------------  --------------  --------------  ------------------  ---------  ------------------  -----------------  ------------------  ------------------  -------------------  ----------------------  --------------------  ---------  -----------------  ------------------  -----------------  --------------  ------------------  ------------------  ------------------------ 
 20080109CONT00045522  0              T000000038            20080109                   20080109          (null)     0                     1                 1        USD                        JPY                       B001           20080109TRAD00000825  20080109ORD00000825  1                   USD/JPY           0            00001332        2000              (null)                    0                          0                              20080111                 20080111        100000                0                         0                          2008-1-9 下午2:59:11     109.54          109.54          109.54              100000     2008-1-9 下午2:59:10  0                  10000               0                   (null)               0                       20080109000000159417  1          (null)             system              system             1               2008-1-9 下午2:59:11  2008-1-9 下午6:29:01  (null)                   
 20080109CONT00045523  0              T000000039            20080109                   20080109          (null)     0                     1                 -1       USD                        JPY                       B001           20080109TRAD00000826  20080109ORD00000826  1                   USD/JPY           0            00001332        400               (null)                    0                          0                              20080111                 20080111        20000                 0                         0                          2008-1-9 下午2:59:24     90.7            90.7            90.7                20000      2008-1-9 下午2:59:23  0                  2000                0                   (null)               0                       20080109000000159419  1          (null)             system              system             1               2008-1-9 下午2:59:24  2008-1-9 下午6:29:01  (null)                   
 20080109CONT00045524  0              T000000040            20080109                   20080109          (null)     0                     1                 -1       USD                        JPY                       B001           20080109TRAD00000827  20080109ORD00000827  1                   USD/JPY           0            00001332        600               (null)                    0                          0                              20080111                 20080111        30000                 0                         0                          2008-1-9 下午2:59:38     90.66           90.66           90.66               30000      2008-1-9 下午2:59:37  0                  3000                0                   (null)               0                       20080109000000159421  1          (null)             system              system             1               2008-1-9 下午2:59:38  2008-1-9 下午6:29:01  (null)                   

 3 record(s) selected [Fetch MetaData: 0/ms] [Fetch Data: 16/ms] 

 [Executed: 08-1-9 下午05时30分07秒 ] [Execution: 0/ms] 





UPDATE JHF_ALIVE_CONTRACT SET SWAP_INTEREST='40.0000' WHERE CONTRACT_ID = '20080109CONT00045530';

UPDATE JHF_ALIVE_CONTRACT SET SWAP_INTEREST='15.0000' WHERE CONTRACT_ID = '20080109CONT00045531';

UPDATE JHF_ALIVE_CONTRACT SET SWAP_INTEREST='5.0000'  WHERE CONTRACT_ID = '20080109CONT00045532';








--指定net sql





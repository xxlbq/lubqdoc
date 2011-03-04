
SELECT DISTINCT CUSTOMER_ID FROM JHF_ALIVE_CONTRACT A,JHF_PRODUCT P 
WHERE A.AMOUNT_NO_SETTLED > 0  AND A.CURRENCY_PAIR IN ('GBP/USD','EUR/USD','EUR/JPY') AND A.PRODUCT_ID = P.PRODUCT_ID AND A.STATUS = 1 
AND A.ACTIVE_FLAG = 1 AND P.ACTIVE_FLAG = 1  ;



-- DELETE INVALIDATE  CONTRACT 
DELETE FROM JHF_ALIVE_CONTRACT WHERE CUSTOMER_ID IN ('00016406',
'91000000',
'00016901',
'00007301',
'00001731'
)

--DELETE FROM JHF_ALIVE_CONTRACT WHERE CUSTOMER_ID = '00006701' AND CURRENCY_PAIR = 'EUR/JPY' ;
--DELETE FROM JHF_ALIVE_CONTRACT WHERE AMOUNT_SETTLING > 0 AND CURRENCY_PAIR IN ('GBP/USD','EUR/USD','EUR/JPY');
SELECT * FROM JHF_ALIVE_CONTRACT WHERE AMOUNT_NO_SETTLED > 0 AND CURRENCY_PAIR IN ('GBP/USD','EUR/USD','EUR/JPY');

--
SELECT * FROM JHF_ALIVE_CONTRACT WHERE CUSTOMER_ID = '00018906' AND AMOUNT_NO_SETTLED > 0 AND CURRENCY_PAIR IN ('USD/JPY');


SELECT CUSTOMER_ID, AMOUNT,AMOUNT_NO_SETTLED,AMOUNT_SETTLING,AMOUNT_SETTLED FROM JHF_ALIVE_CONTRACT A,JHF_PRODUCT P 
WHERE A.AMOUNT_NO_SETTLED > 0  AND A.CURRENCY_PAIR IN ('GBP/USD','USD/CHF')  AND A.PRODUCT_ID = P.PRODUCT_ID AND A.STATUS = 1 
AND A.ACTIVE_FLAG = 1 AND P.ACTIVE_FLAG = 1  ;



---- temp


SELECT * FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID = '00000239' AND CURRENCY_PAIR = 'EUR/USD' 


SELECT SIDE FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID = '00000239' AND CURRENCY_PAIR ='GBP/USD';

SELECT CUSTOMER_ID,CUSTOMER_ORDER_NO FROM JHF_ALIVE_ORDER WHERE ORDER_ID = '20110301ORD00162039'

SELECT ORDER_ID,ORDER_AMOUNT ,ORDER_DATETIME FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID = '00000239' and CUSTOMER_ORDER_NO = 'T000002943'
SELECT ORDER_ID,TRADE_ID,CUSTOMER_ORDER_NO FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID = '00000239' and CURRENCY_PAIR = 'GBP/USD' ORDER BY INPUT_DATE DESC;


SELECT * FROM JHF_WL_SYS_POSITION WHERE CLIENT_ID = 'WL0004' AND CURRENCY_PAIR = 'USD/JPY' ;

SELECT CONTRACT_ID,CURRENCY_PAIR,AMOUNT ,AMOUNT_NO_SETTLED,AMOUNT_SETTLED,AMOUNT_SETTLING FROM JHF_ALIVE_CONTRACT WHERE ORDER_ID = '20110301ORD00162039';




-DconfigPath=D:\appConfs\mj_dev_forcecut_conf



(2011-02-24 14:23:10) 王岩: 

(Thu Feb 24 14:09:50 2011,2)(wangyan@localhost)[MAIN]>select count(*) from JHF_ALIVE_CONTRACT where AMOUNT_NO_SETTLED>0 and currency_pair in (
    -> 'EUR/AUD','GBP/AUD','EUR/NZD',
    -> 'GBP/NZD','AUD/NZD','EUR/CAD',
    -> 'GBP/CAD','AUD/CAD','NZD/CAD',
    -> 'EUR/CHF','GBP/CHF','AUD/CHF',
    -> 'NZD/CHF','CAD/CHF','USD/ZAR',
    -> 'EUR/ZAR','GBP/ZAR');
+----------+
| count(*) |
+----------+
|      713 | 
+----------+
1 row in set (0.22 sec)

(Thu Feb 24 14:11:21 2011,3)(wangyan@localhost)[MAIN]>select customer_id,count(*) from JHF_ALIVE_CONTRACT where AMOUNT_NO_SETTLED>0 and currency_pair in ( 'EUR/AUD','GBP/AUD','EUR/NZD', 'GBP/NZD','AUD/NZD','EUR/CAD', 'GBP/CAD','AUD/CAD','NZD/CAD', 'EUR/CHF','GBP/CHF','AUD/CHF', 'NZD/CHF','CAD/CHF','USD/ZAR', 'EUR/ZAR','GBP/ZAR') group by customer_id;
+-------------+----------+
| customer_id | count(*) |
+-------------+----------+
| 00000289    |        1 | 
| 00000443    |        1 | 
| 00000458    |        2 | 
| 00000492    |        5 | 
| 00000695    |        1 | 
| 00000712    |        2 | 
| 00000942    |        2 | 
| 00001005    |       21 | 
| 00001093    |        1 | 
| 00001306    |        4 | 
| 00001469    |        2 | 
| 00001843    |        1 | 
| 00001884    |        1 | 
| 00001969    |        1 | 
| 00001971    |        2 | 
| 00002412    |        3 | 
| 00003990    |        1 | 
| 00004679    |        4 | 
| 00004861    |        6 | 
| 00004922    |        2 | 
| 00005342    |        1 | 
| 00005366    |        2 | 
| 00005969    |        2 | 
| 00006474    |        2 | 
| 00006598    |        1 | 
| 00007083    |        4 | 
| 00007514    |        1 | 
| 00007747    |        1 | 
| 00007830    |        5 | 
| 00008499    |        6 | 
| 00009012    |        2 | 
| 00009083    |        5 | 
| 00010180    |        2 | 
| 00010818    |        1 | 
| 00010875    |        1 | 
| 00012208    |        2 | 
| 00013387    |        1 | 
| 00013420    |        1 | 
| 00013421    |        3 | 
| 00013708    |        5 | 
| 00013903    |        1 | 
| 00015502    |        1 | 
| 00015940    |        1 | 
| 00016450    |        3 | 
| 00016661    |        1 | 
| 00017369    |        2 | 
| 00017476    |        6 | 
| 00017591    |        1 | 
| 00018089    |        1 | 
| 00018096    |        3 | 
| 00019122    |        1 | 
| 00019952    |        2 | 
| 00020636    |       19 | 
| 00022013    |        2 | 
| 00022526    |        1 | 
| 00022648    |        3 | 
| 00023080    |        1 | 
| 00023390    |        2 | 
| 00023691    |        5 | 
| 00023820    |        1 | 
| 00023836    |        7 | 
| 00024136    |        1 | 
| 00024326    |        1 | 
| 00024351    |        5 | 
| 00024630    |        4 | 
| 00026370    |        5 | 
| 00026420    |        1 | 
| 00026524    |        2 | 
| 00026869    |        1 | 
| 00027200    |        1 | 
| 00027461    |        1 | 
| 00027491    |        1 | 
| 00027588    |        2 | 
| 00027975    |        1 | 
| 00029016    |        1 | 
| 00029176    |        2 | 
| 00030178    |        1 | 
| 00030188    |        4 | 
| 00030404    |        1 | 
| 00030601    |        3 | 
| 00030725    |        1 | 
| 00031193    |        1 | 
| 00033172    |        1 | 
| 00033284    |        4 | 
| 00033907    |        1 | 
| 00035139    |        5 | 
| 00036641    |        6 | 
| 00037807    |        1 | 
| 00038609    |        1 | 
| 00039133    |        1 | 
| 00039261    |        3 | 
| 00039343    |        1 | 
| 00040154    |       17 | 
| 00040576    |        2 | 
| 00040759    |       42 | 
| 00041131    |        1 | 
| 00041624    |        1 | 
| 00041625    |        3 | 
| 00042438    |        1 | 
| 00042673    |        1 | 
| 00042826    |        1 | 
| 00043263    |        1 | 
| 00043381    |        2 | 
| 00043625    |        2 | 
| 00043861    |        2 | 
| 00044155    |        3 | 
| 00047875    |        2 | 
| 00048669    |        1 | 
| 00049223    |        1 | 
| 00049955    |        5 | 
| 00050326    |       16 | 
| 00051796    |        3 | 
| 00051797    |        2 | 
| 00052127    |        1 | 
| 00052700    |        2 | 
| 00052845    |        3 | 
| 00055374    |        6 | 
| 00055461    |        2 | 
| 00056805    |        1 | 
| 00056823    |        2 | 
| 00057092    |        2 | 
| 00059239    |        2 | 
| 00059802    |        1 | 
| 00060217    |        2 | 
| 00061025    |        1 | 
| 00061388    |        3 | 
| 00061889    |        3 | 
| 00062300    |        1 | 
| 00062403    |        7 | 
| 00063186    |       10 | 
| 00063315    |        1 | 
| 00064326    |       17 | 
| 00064391    |        1 | 
| 00064791    |        9 | 
| 00065335    |        2 | 
| 00065700    |        3 | 
| 00065806    |        2 | 
| 00066510    |        1 | 
| 00067140    |        4 | 
| 00068691    |        4 | 
| 00068901    |        5 | 
| 00068904    |        1 | 
| 00069646    |        1 | 
| 00071363    |        2 | 
| 00071438    |        5 | 
| 00071572    |        2 | 
| 00072065    |        1 | 
| 00072154    |        3 | 
| 00072193    |        1 | 
| 00072194    |        5 | 
| 00073056    |        2 | 
| 00074539    |       12 | 
| 00075364    |        1 | 
| 00075955    |        2 | 
| 00076403    |        2 | 
| 00076481    |        4 | 
| 00077000    |        2 | 
| 00077122    |        2 | 
| 00077164    |        1 | 
| 00077180    |        9 | 
| 00077822    |        2 | 
| 00078841    |        1 | 
| 00078897    |        1 | 
| 00079773    |        1 | 
| 00080016    |        3 | 
| 00080139    |        4 | 
| 00080785    |        2 | 
| 00081151    |        4 | 
| 00082193    |        2 | 
| 00083064    |       11 | 
| 00084531    |        5 | 
| 00084664    |        2 | 
| 00085557    |        1 | 
| 00085915    |        9 | 
| 00086306    |        1 | 
| 00086777    |        1 | 
| 00087021    |        3 | 
| 00088774    |        1 | 
| 00089055    |        2 | 
| 00089130    |        3 | 
| 00089185    |        1 | 
| 00089218    |        1 | 
| 00089239    |        2 | 
| 00089441    |        3 | 
| 00090137    |        1 | 
| 00091848    |        2 | 
| 00092140    |        2 | 
| 00092781    |        1 | 
| 00093514    |        7 | 
| 00093563    |        1 | 
| 00093998    |        7 | 
| 00094420    |        2 | 
| 00094530    |        3 | 
| 00094746    |        1 | 
| 00095063    |        6 | 
| 00095410    |        1 | 
| 00095997    |        2 | 
| 00096031    |        4 | 
| 00096114    |       13 | 
| 00096334    |        7 | 
| 00096663    |        4 | 
| 00097340    |        1 | 
| 00097565    |        5 | 
| 00097980    |        1 | 
| 00098244    |        1 | 
| 00098829    |        1 | 
| 00099266    |        1 | 
| 00099280    |        2 | 
| 00099402    |        3 | 
| 00099648    |        2 | 
| 00100101    |        1 | 
| 00100169    |        4 | 
| 00100506    |        1 | 
| 00100621    |        1 | 
| 00101300    |        3 | 
| 00101312    |        1 | 
| 00101408    |        2 | 
| 00101692    |        4 | 
| 00101750    |        2 | 
| 00102124    |        1 | 
| 00102972    |        4 | 
| 00103103    |        1 | 
| 00103322    |        4 | 
| 00104787    |        1 | 
| 00105114    |        1 | 
| 00105223    |        1 | 
| 00105362    |        2 | 
| 00105484    |        1 | 
| 00105940    |        1 | 
| 00105944    |        1 | 
| 00105976    |        2 | 
| 00106337    |        4 | 
| 00106349    |        4 | 
| 00106768    |        6 | 
| 00107092    |        1 | 
| 00109632    |        1 | 
| 00109811    |        1 | 
| 00110052    |        1 | 
| 00110126    |        4 | 
| 00110343    |        1 | 
+-------------+----------+
240 rows in set (0.23 sec)
(2011-02-24 14:23:17) 王岩: 
MJ本番现状的情况


















FRONT 用户登录

SELECT COUNT(*) FROM JHF_CUSTOMER_SESSION ;


POSI


BACKYARD   https://admin.cfd.dmm.com 


注文数据量

SELECT COUNT(*) FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID LIKE '9%' 

opm 完成数量


SELECT COUNT(*) FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID LIKE '9%' AND EXECUTION_TYPE = 0 AND ORDER_STATUS = 3; 


LOSSCUT 取消
 SELECT COUNT(*) FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID LIKE '9%' AND EXECUTION_TYPE = 0 AND ORDER_STATUS =11;
 
 
 
LOSSCUT 成功 	
 
 
成行成功的注文: 
 SELECT COUNT(*) FROM JHF_ALIVE_ORDER WHERE CUSTOMER_ID LIKE '9%' AND EXECUTION_TYPE = 12  AND ORDER_STATUS =3;
 
 
 
 
 
 
 
 SELECT COUNT(*) FROM JHF_ALIVE_ORDER WHERE EXECUTION_TYPE = 0  AND ORDER_STATUS = 3  GROUP BY CUSTOMER_ID;                
 



1 ） 01->1050,02->260,03->190,04->140,05->90,06->90,07->20,08->280,09->20,10->10

2 ） 01->1400,02->340,03->250,04->180,05->120,06->120,07->20,08->370,09->20,10->10 

3 ） 01->1750,02->430,03->320,04->230,05->150,06->150,07->30,08->470,09->20,10->10
 
4 ） 01->2100,02->510,03->380,04->270,05->180,06->180,07->30,08->560,09->30,10->20
 
 
 
1050,
260,
190,
140,
90,
90,
20,
280,
20,
10
 
 
 
 SELECT COUNT(*) FROM JHF_ALIVE_ORDER WHERE EXECUTION_TYPE = 0 AND CUSTOMER_ID LIKE '9%' AND ORDER_STATUS = 3G;
 
 
700
170
125
90
60


60
10
85
8
4


1312

=====================


1050             22
==============
260
190
====================
140
90
90

20
=================
280
20
10






1=========
USER_FETCH_BEGIN = 20001             20000-21200
USER_FETCH_SIZE = 21050

2=========
USER_FETCH_BEGIN = 20701
USER_FETCH_BEGIN = 21751             21201-22400



3=========
USER_FETCH_BEGIN = 22401              22401-23600
USER_FETCH_BEGIN = 22660 

USER_FETCH_BEGIN = 22661 
USER_FETCH_BEGIN = 22851

4=========
USER_FETCH_BEGIN =              23601-24800
5=========
USER_FETCH_BEGIN =              24801-26000





================
25001-25016




http://www.xpdesk.cn/Code/upload88/Pro9/xpdesk.cn2008513225032125b.jpg

+-------------+----------+
| 90025001    |        1 | 
| 90025002    |        1 | 
| 90025003    |        1 | 
| 90025004    |        2 | 
| 90025005    |        2 | 
| 90025006    |        5 | 
| 90025007    |        5 | 
| 90025008    |       10 | 
| 90025009    |       10 | 
| 90025010    |       10 | 
| 90025011    |        3 | 
| 90025012    |        5 | 
| 90025013    |        5 | 
| 90025014    |        5 | 
| 90025015    |        5 | 
| 90025016    |        5




 1
 1
 1
 2
 2
 5
 5
10
10
10
 3
 5
 5
 5
 5
 5









=====================
2100
510
380
270
180
180
30
560
30
20

=======
4260

150
4410
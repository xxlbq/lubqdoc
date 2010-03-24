SELECT * FROM JHF_APPLICATION_DATE

UPDATE JHF_APPLICATION_DATE SET FRONT_DATE = '20100312' , FRONT_END_DATETIME = DATE_ADD(CURRENT_TIMESTAMP, interval 3 day) WHERE DATE_KEY IN ('F','S');
UPDATE JHF_APPLICATION_DATE SET NEXT_EOD_START_TIME = DATE_ADD(CURRENT_TIMESTAMP, interval 3 day) WHERE DATE_KEY IN ('F','S');


UPDATE JHF_APPLICATION_DATE SET FRONT_DATE = '20090828' WHERE DATE_KEY IN ('F','S');



UPDATE JHF_ALIVE_ORDER SET ACTIVATION_DATE = DATE_SUB(CURRENT_TIMESTAMP, interval 10 HOUR) WHERE ORDER_ID='TstORD_0'







hibernate_batch.properties      --->没有
hibernate_backyard.properties   --->没有

hibernate_history.properties    --->MAIN/36zn+MsoQ
[E0A712DAD9B282280300D32799E6F423]

hibernate_info.properties --->INFO/36zn+MsoQ
[E0A712DAD9B282280300D32799E6F423]

hibernate_main.properties --->MAIN/36zn+MsoQ
[E0A712DAD9B282280300D32799E6F423]

hibernate_main_back.properties --->MAIN/36zn+MsoQ
[E0A712DAD9B282280300D32799E6F423]

hibernate_register.properties --->REGISTER/OalCrd08
[AF3E335138F34ED3020B8E2862B9D5C5]

hibernate_sns.properties --->没有

hibernate_unique.properties --->MAIN/36zn+MsoQ
[E0A712DAD9B282280300D32799E6F423]


jdbc_unique.properties MAIN/36zn+MsoQ
[E0A712DAD9B282280300D32799E6F423]

jdbc_main.properties MAIN/36zn+MsoQ
[E0A712DAD9B282280300D32799E6F423]

jdbc_info.properties INFO/36zn+MsoQ
[E0A712DAD9B282280300D32799E6F423]

jdbc_batch.properties MAIN/36zn+MsoQ
[E0A712DAD9B282280300D32799E6F423]

;

DELETE FROM JHF_ALIVE_ORDER WHERE CURRENCY_PAIR = 'GBP/JPY';


DELETE FROM JHF_ALIVE_CONTRACT WHERE CUSTOMER_ID = '00000128' AND CURRENCY_PAIR = 'USD/JPY'


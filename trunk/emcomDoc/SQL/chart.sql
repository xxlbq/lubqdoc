

SELECT * FROM JHF_5MINUTELY_RATE_SUMMARY;


SELECT * FROM JHF_MINUTELY_RATE_SUMMARY WHERE CLOSE_PRICE_TIME = DATE_format('2009-09-27 18:00:00','%Y-%m%M');

show create table JHF_MINUTELY_RATE_SUMMARY


SELECT * FROM JHF_MINUTELY_RATE_SUMMARY WHERE CLOSE_PRICE_TIME < '2009-10-20 16:00:00' AND CURRENCY_PAIR = 'EUR/JPY'\G;

SELECT * FROM JHF_5MINUTELY_RATE_SUMMARY


SELECT * FROM JHF_MINUTELY_RATE_SUMMARY 
WHERE CLOSE_PRICE_TIME < '2009-10-20 16:00:00' 
AND CLOSE_PRICE_TIME > '2009-10-20 15:55:00' 
AND CURRENCY_PAIR = 'EUR/JPY'\G;



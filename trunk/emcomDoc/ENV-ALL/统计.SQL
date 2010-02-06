

======================  OPM 统计  =========================



select hostname,min(date_time) MINT , (select max(date_time) from opm where type='send message'AND opm.hostname='opm1' GROUP BY  opm.hostname 
order by 1 ) MAXT , (extract(day_microsecond from trim(max(date_time))) - extract(day_microsecond from trim(min(date_time)))) DIV 1000  
AS USED ,count(*) as COUNT from opm where type ='used time' AND opm.hostname='opm1' GROUP BY 1 order by 1;






======================  TRADER 统计  =======================

select hostname,min(date_time),max(date_time) , 

(extract(day_microsecond from trim(max(date_time))) - extract(day_microsecond from trim(min(date_time)))) DIV 1000  
AS USED 
from trader  AS TRD WHERE TRD.hostname = 'trader1' GROUP BY 1 order by 1;









11527














/**    ����master ������    **/
/usr/local/mysql/bin/mysqldump -h10.15.2.244 -uMAIN -pbestwiz  --opt  --hex-blob --master-data MAIN >/tmp/MAIN.0720.sql
-- ����ĳ������
/usr/local/mysql/bin/mysqldump -h10.15.2.244 -uroot -pmj_abc  --opt  --hex-blob --master-data MAIN JHF_STATUS_CONTRL>/tmp/MAIN.0720.sql  



/**   ��½mysql   **/
/usr/local/mysql/bin/mysql -h10.15.2.243 -uroot -pmj_abc

/usr/local/mysql/bin/mysql -h10.15.2.244 -uroot -pmj_abc



/**    ����master �����ݵ�slave    **/
/usr/local/mysql/bin/mysql MAIN </tmp/MAIN.xxx.sql 



/usr/local/mysql/bin/mysqladmin  shutdown -uzhangwc -pemcom2oo9


/usr/local/mysql/bin/mysqld_safe �Cuser=mysql &

*========================================*
*=========*   mysql shutdown   *=========*
*========================================*




/usr/local/mysql/bin/mysqladmin -uroot -pmj_abc -h10.15.2.206 shutdown
OR
/usr/local/mysql/bin/mysqladmin  shutdown



*========================================*
*=========*   mysql ����       *=========*
*========================================*

/usr/local/mysql/bin/mysqld_safe  --user=mysql &





/usr/local/mysql/bin/mysqlbinlog mysql-bin.000001 |more














/**    ��ʾmysql master slave �Ƿ�ͬ��   **/
show slave status\G;


/**    mysql���޸�my.cnf���������ú� ��Ҫɾ�����ļ���Ȼ������   **/
mv /usr/local/mysql/var/master.info /usr/local/mysql/var/master.info.bak001





 SET GLOBAL SQL_SLAVE_SKIP_COUNTER =1;



/* ==========  mysql commond ========== */

-- �г�����
mysql> show tables;
-- �г����ݿ���
mysql> show databases;
-- ʹ��ĳ���ݿ�
mysql> use XXX;







$ mysqlbinlog binlog.000001 >  /tmp/statements.sql






























IT168 ������ѧԺ���ߡ�����MySQLĿ¼

����MySQLĬ�ϵ������ļ��洢Ŀ¼Ϊ/var/lib/mysql������Ҫ��Ŀ¼�Ƶ�/home/data����Ҫ�������漸����

����1��homeĿ¼�½���dataĿ¼
����cd /home
����mkdir data

����2����MySQL�������ͣ���� 
����mysqladmin -u root -p shutdown

����3����/var/lib/mysql����Ŀ¼�Ƶ�/home/data
����mv /var/lib/mysql��/home/data/
���������Ͱ�MySQL�������ļ��ƶ�����/home/data/mysql�� 

����4���ҵ�my.cnf�����ļ�
�������/etc/Ŀ¼��û��my.cnf�����ļ����뵽/usr/share/mysql/���ҵ�*.cnf�ļ�����������һ����/etc/������Ϊmy.cnf)�С��������£�
���� [root@test1 mysql]# cp /usr/share/mysql/my-medium.cnf��/etc/my.cnf

����5���༭MySQL�������ļ�/etc/my.cnf
����Ϊ��֤MySQL�ܹ�������������Ҫָ��mysql.sock�ļ��Ĳ���λ�á� �޸�socket=/var/lib/mysql/mysql.sockһ���еȺ��ұߵ�ֵΪ��/home/mysql/mysql.sock ���������£�
���� vi�� my.cnf������ (��vi���߱༭my.cnf�ļ����ҵ����������޸�֮)
���� # The MySQL server
������ [mysqld]
������ port������= 3306
������#socket�� = /var/lib/mysql/mysql.sock��ԭ���ݣ�Ϊ�˸������á�#��ע�ʹ��У�
������ socket�� = /home/data/mysql/mysql.sock�����������ϴ��У�

����6���޸�MySQL�����ű�/etc/rc.d/init.d/mysql
���������Ҫ�޸�MySQL�����ű�/etc/rc.d/init.d/mysql��������datadir=/var/lib/mysqlһ���У��Ⱥ��ұߵ�·���ĳ������ڵ�ʵ�ʴ��·����home/data/mysql��
����[root@test1 etc]# vi��/etc/rc.d/init.d/mysql
����#datadir=/var/lib/mysql����������ע�ʹ��У�
����datadir=/home/data/mysql���� �����ϴ��У�

����7����������MySQL����
����/etc/rc.d/init.d/mysql��start
��������reboot��������Linux
����������������ƶ��ͳɹ��ˣ��������ǰ���7���ټ��һ�¡�

�����ˡ�MySQL�ĳ��ò���

����ע�⣺MySQL��ÿ�������Ҫ�Էֺţ���β��

��





=========================================
    mysqlbinlog [options] log_file ... 

     ��ѡ����, ����ָ��һЩ��������, ������������õĶ���, ������ѡ����:

    --database=db_name, -d db_name
    --offset=N, -o N
    --[start|stop]-datetime=datetime
    --[start|stop]-position=N

     ������ľ���һ��һ��SQL�����, ����Щ���ִ��һ��, �͵��������ָ���, ���Ʋ����ð󶨱�����, ������MySQL���ǲ��ǰ󶨱������Ǻ���Ҫ��. ��Ȼ��Ҫ��һ���ǲ�Ҫ���ж������ȥ��, ��Ϊ�����Ļ�, ˳��͵ò�����֤��. ������ʾ:

    $ mysqlbinlog binlog.000001 >   /tmp/statements.sql
    $ mysqlbinlog binlog.000002 >> /tmp/statements.sql
    $ mysql -e "source /tmp/statements.sql" 










11527














/**    导出master 的数据    **/
/usr/local/mysql/bin/mysqldump -h10.15.2.244 -uMAIN -pbestwiz  --opt  --hex-blob --master-data MAIN >/tmp/MAIN.0720.sql
-- 单独某个表名
/usr/local/mysql/bin/mysqldump -h10.15.2.244 -uroot -pmj_abc  --opt  --hex-blob --master-data MAIN JHF_STATUS_CONTRL>/tmp/MAIN.0720.sql  



/**   登陆mysql   **/
/usr/local/mysql/bin/mysql -h10.15.2.243 -uroot -pmj_abc

/usr/local/mysql/bin/mysql -h10.15.2.244 -uroot -pmj_abc



/**    导入master 的数据到slave    **/
/usr/local/mysql/bin/mysql MAIN </tmp/MAIN.xxx.sql 



/usr/local/mysql/bin/mysqladmin  shutdown -uzhangwc -pemcom2oo9


/usr/local/mysql/bin/mysqld_safe –user=mysql &

*========================================*
*=========*   mysql shutdown   *=========*
*========================================*




/usr/local/mysql/bin/mysqladmin -uroot -pmj_abc -h10.15.2.206 shutdown
OR
/usr/local/mysql/bin/mysqladmin  shutdown



*========================================*
*=========*   mysql 重启       *=========*
*========================================*

/usr/local/mysql/bin/mysqld_safe  --user=mysql &





/usr/local/mysql/bin/mysqlbinlog mysql-bin.000001 |more














/**    显示mysql master slave 是否同步   **/
show slave status\G;


/**    mysql在修改my.cnf或其他配置后 需要删除此文件，然后重启   **/
mv /usr/local/mysql/var/master.info /usr/local/mysql/var/master.info.bak001





 SET GLOBAL SQL_SLAVE_SKIP_COUNTER =1;



/* ==========  mysql commond ========== */

-- 列出表名
mysql> show tables;
-- 列出数据库名
mysql> show databases;
-- 使用某数据库
mysql> use XXX;







$ mysqlbinlog binlog.000001 >  /tmp/statements.sql






























IT168 服务器学院】七、更改MySQL目录

　　MySQL默认的数据文件存储目录为/var/lib/mysql。假如要把目录移到/home/data下需要进行下面几步：

　　1、home目录下建立data目录
　　cd /home
　　mkdir data

　　2、把MySQL服务进程停掉： 
　　mysqladmin -u root -p shutdown

　　3、把/var/lib/mysql整个目录移到/home/data
　　mv /var/lib/mysql　/home/data/
　　这样就把MySQL的数据文件移动到了/home/data/mysql下 

　　4、找到my.cnf配置文件
　　如果/etc/目录下没有my.cnf配置文件，请到/usr/share/mysql/下找到*.cnf文件，拷贝其中一个到/etc/并改名为my.cnf)中。命令如下：
　　 [root@test1 mysql]# cp /usr/share/mysql/my-medium.cnf　/etc/my.cnf

　　5、编辑MySQL的配置文件/etc/my.cnf
　　为保证MySQL能够正常工作，需要指明mysql.sock文件的产生位置。 修改socket=/var/lib/mysql/mysql.sock一行中等号右边的值为：/home/mysql/mysql.sock 。操作如下：
　　 vi　 my.cnf　　　 (用vi工具编辑my.cnf文件，找到下列数据修改之)
　　 # The MySQL server
　　　 [mysqld]
　　　 port　　　= 3306
　　　#socket　 = /var/lib/mysql/mysql.sock（原内容，为了更稳妥用“#”注释此行）
　　　 socket　 = /home/data/mysql/mysql.sock　　　（加上此行）

　　6、修改MySQL启动脚本/etc/rc.d/init.d/mysql
　　最后，需要修改MySQL启动脚本/etc/rc.d/init.d/mysql，把其中datadir=/var/lib/mysql一行中，等号右边的路径改成你现在的实际存放路径：home/data/mysql。
　　[root@test1 etc]# vi　/etc/rc.d/init.d/mysql
　　#datadir=/var/lib/mysql　　　　（注释此行）
　　datadir=/home/data/mysql　　 （加上此行）

　　7、重新启动MySQL服务
　　/etc/rc.d/init.d/mysql　start
　　或用reboot命令重启Linux
　　如果工作正常移动就成功了，否则对照前面的7步再检查一下。

　　八、MySQL的常用操作

　　注意：MySQL中每个命令后都要以分号；结尾。

　





=========================================
    mysqlbinlog [options] log_file ... 

     在选项中, 可以指定一些过滤条件, 来解出你所想用的东西, 这样的选项有:

    --database=db_name, -d db_name
    --offset=N, -o N
    --[start|stop]-datetime=datetime
    --[start|stop]-position=N

     解出来的就是一条一条SQL语句了, 将这些语句执行一下, 就等于增量恢复了, 估计不是用绑定变量的, 可能在MySQL中是不是绑定变量不是很重要了. 当然重要的一点是不要运行多个进程去跑, 因为这样的话, 顺序就得不到保证了. 如下所示:

    $ mysqlbinlog binlog.000001 >   /tmp/statements.sql
    $ mysqlbinlog binlog.000002 >> /tmp/statements.sql
    $ mysql -e "source /tmp/statements.sql" 










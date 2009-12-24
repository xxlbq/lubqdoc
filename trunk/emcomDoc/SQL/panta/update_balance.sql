CREATE PROCEDURE PANTA_UPDATE_CASH_BALANCE (beginNum INTEGER, endNum INTEGER, balance INTEGER, accountStatus INTEGER)
BEGIN
    set @i=beginNum;
    set @i=CONCAT('000', @i);
    set @i=SUBSTRING(@i, -8, 8);
    WHILE @i < endNum DO
         UPDATE  `JHF_CASH_BALANCE` SET CASH_BALANCE = balance  WHERE CUSTOMER_ID = @i ;
         UPDATE  `JHF_MARGIN_ACCOUNT`  SET ACCOUNT_STATUS = accountStatus  WHERE CUSTOMER_ID = @i ;
         set @i = @i +1;
    END WHILE;
END
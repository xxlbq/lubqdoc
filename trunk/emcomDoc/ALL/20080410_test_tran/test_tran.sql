



UPDATE test_tran set NAME='JACK' WHERE NO = 1 ;


COMMIT;



















SELECT * FROM test_tran WHERE NO=1 FOR UPDATE
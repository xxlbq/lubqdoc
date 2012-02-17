package jp.emcom.adv.fx.completor.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.emcom.adv.fx.completor.dao.WLCompletorDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveContract;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrder;
import cn.bestwiz.jhf.core.dao.util.DbSessionFactory;
import cn.bestwiz.jhf.core.idgenerate.IdGenerateFacade;
import cn.bestwiz.jhf.core.idgenerate.exception.IdGenerateException;

public class WLCompletorService {
	
	private static final Logger log = LoggerFactory.getLogger(WLCompletorService.class);
	private WLCompletorDao dao;
	public void setDao(WLCompletorDao dao) {
		this.dao = dao;
	}

	
	
	public  List<JhfWlOrder> queryCompletorOrders(List<String> cid) {
		List<JhfWlOrder> list = null;
		try {
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			list = dao.queryCompletorOrder(cid);
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		} catch (Exception e) {
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
			log.error("DB operate Errors: ", e);
		}
		return list;

	}

	public String queryProductId(String customerId,String ccy){
		String productId = null;
		try {
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			productId = dao.queryProductId(customerId,ccy);
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		} catch (Exception e) {
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
			log.error("DB operate Errors: ", e);
		}
		return productId;
	}

	public List<JhfAliveContract> queryContract(String customerId,String ccy, BigDecimal side){
		List<JhfAliveContract> list = null;
		try {
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			list = dao.queryContract(customerId,ccy,side);
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		} catch (Exception e) {
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
			log.error("DB operate Errors: ", e);
		}
		return list;
	}
	
	public static List<JhfWlOrder> mockCompletorOrders(
			int count,String ccy,String clientId,String customerId,String orderStatus,
			BigDecimal side) throws IdGenerateException{
		
		List<JhfWlOrder> ll = new ArrayList<JhfWlOrder>();
		
		for (int i = 0; i < count; i++) {
			
			ll.add(new JhfWlOrder(
					IdGenerateFacade.getOrderId(), "WHOAMI", "", customerId, orderStatus, 
					side, ccy, new BigDecimal("10000"),  new BigDecimal("100.000"), 1 , 
					new Date(), new Date()));
		}

		return ll;
	}


	
}

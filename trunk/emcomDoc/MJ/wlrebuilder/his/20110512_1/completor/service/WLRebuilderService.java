package jp.emcom.adv.fx.completor.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.emcom.adv.fx.completor.biz.AutoBuildInfo;
import jp.emcom.adv.fx.completor.service.dao.WLCompletorDao;
import jp.emcom.adv.fx.completor.service.dao.WLRebuilderDao;
import cn.bestwiz.jhf.core.dao.bean.main.JhfApplicationDate;
import cn.bestwiz.jhf.core.dao.bean.main.JhfSysPositionInsert;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrder;
import cn.bestwiz.jhf.core.dao.util.DbSessionFactory;
import cn.bestwiz.jhf.core.util.CollectionsUtil;

public class WLRebuilderService {
	
	private WLCompletorDao wlCompletorDao ;
	private WLRebuilderDao wlRebuilderDao ;
	
	
	
	public WLRebuilderDao getWlRebuilderDao() {
		return wlRebuilderDao;
	}

	public void setWlRebuilderDao(WLRebuilderDao wlRebuilderDao) {
		this.wlRebuilderDao = wlRebuilderDao;
	}

	public WLCompletorDao getWlCompletorDao() {
		return wlCompletorDao;
	}

	public void setWlCompletorDao(WLCompletorDao wlCompletorDao) {
		this.wlCompletorDao = wlCompletorDao;
	}

	public Map<String, Object> findCloseRate(AutoBuildInfo info,String currencyPair, JhfApplicationDate date) throws Exception {
		
		List<Map<String, Object>> col = null;
		long msgTime = date.getFrontEndDatetime().getTime() - info.getTradableEndInterval().longValue();
		try {
			DbSessionFactory.beginTransaction(DbSessionFactory.INFO);
			col = this.wlRebuilderDao.findCloseRate(info.getClientId(),currencyPair,msgTime);
			DbSessionFactory.commitTransaction(DbSessionFactory.INFO);
		} catch (Exception e) {
			DbSessionFactory.rollbackTransaction(DbSessionFactory.INFO);
			throw e;
		}
		
		if(null == col  ||  col.size() == 0){
			return null;
		}
		return (Map<String, Object>)col.get(0);
	}

	public List<String> findUsingCurrencyPair(String clientId) 
	throws Exception {
			
		List<String> usingCurrencyPair = null;
		List<String> col = null;
		try {
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			col = this.wlCompletorDao.findUsingCurrencyPair(clientId);
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		} catch (Exception e) {
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
			throw e;
		}
		if(CollectionsUtil.isNotEmpty(col)){
			usingCurrencyPair = new ArrayList<String>();
			for (String ccp : col) {
				usingCurrencyPair.add(ccp);
			}
		}
		return usingCurrencyPair;
	}

	public BigDecimal sumRebuildAmount(String customerId, String currencyPair) throws Exception {
		BigDecimal sumAmount = BigDecimal.ZERO ;
		try {
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			sumAmount = this.wlCompletorDao.sumRebuildAmount(customerId,currencyPair);
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		} catch (Exception e) {
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
			throw e;
		}
		
		
		
		return sumAmount ;
	}


	public void posRebuild(JhfWlOrder rebuildSettleOrder,
			JhfSysPositionInsert jspiSettle, JhfWlOrder rebuildOpenOrder,
			JhfSysPositionInsert jspiOpen) throws Exception {
		try {
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			wlCompletorDao.saveWLOrder(rebuildSettleOrder);
			wlCompletorDao.saveJhfSysPositionInsert(jspiSettle);
			wlCompletorDao.saveWLOrder(rebuildOpenOrder);
			wlCompletorDao.saveJhfSysPositionInsert(jspiOpen);
			
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		} catch (Exception e){
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
			throw e;
		}
		
	}

	public boolean hadRebuildeBefore(String customerId, String currencyPair, String frontDate) throws Exception {
		long rebuildCount = 0L ;
		try {
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			rebuildCount = this.wlCompletorDao.haveRebuildedPos(customerId,currencyPair,frontDate);
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		} catch (Exception e) {
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
			throw e;
		}
		return rebuildCount > 0 ? true : false;
	}

	public boolean completorNotFinishedYet(String customerId,
			String currencyPair, String frontDate) throws Exception {
		long notFinishCount = 0L ;
		try {
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			notFinishCount = this.wlCompletorDao.haveCompletionOrder(customerId,currencyPair,frontDate);
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		} catch (Exception e) {
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
			throw e;
		}
		return notFinishCount > 0 ? true : false;
	}

}

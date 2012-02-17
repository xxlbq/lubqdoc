package jp.emcom.adv.fx.completor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jp.emcom.adv.fx.completor.biz.AutoBuildInfo;
import jp.emcom.adv.fx.completor.service.dao.WLCompletorDao;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlClientConfig;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrder;
import cn.bestwiz.jhf.core.dao.util.DbSessionFactory;
import cn.bestwiz.jhf.core.util.CollectionsUtil;

/**
 *
 * 
 */
public class WLCompletorService {
	//
	private WLCompletorDao wlCompletorDao;
	private final AtomicBoolean verbose = new AtomicBoolean(true);

	
	/**
	 * 
	 */
	public boolean isVerbose() {
		return verbose.get();
	}
	
	public void setVerbose(boolean verbose) {
		this.verbose.set(true);
	}
	
	public void setWlCompletorDao(WLCompletorDao dao) {
		this.wlCompletorDao = dao;
	}
	
	/**
	 * 
	 */
	public List<JhfWlOrder> findCompletionCandidates(boolean readonly, List<String> clientIds)
	throws Exception {
		if(readonly) {
			return findCompletionCandidatesOnSlave(clientIds);
		} else {
			return findCompletionCandidatesOnMaster(clientIds);
		}
	}
	
	/**
	 * 
	 */
	private List<JhfWlOrder> findCompletionCandidatesOnMaster(List<String> clientIds)
	throws Exception {
		List<JhfWlOrder> list = null;
		try {
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			list = this.wlCompletorDao.findCompletionCandidates(clientIds);
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		} catch (Exception e) {
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
			throw e;
		}
		return list;
	}
	
	private List<JhfWlOrder> findCompletionCandidatesOnSlave(List<String> clientIds)
	throws Exception {
		List<JhfWlOrder> list = null;
		try {
			DbSessionFactory.beginReadOnlyTransaction(DbSessionFactory.MAIN);
			list = this.wlCompletorDao.findCompletionCandidates(clientIds);
			DbSessionFactory.commitReadOnlyTransaction(DbSessionFactory.MAIN);
		} catch (Exception e) {
			DbSessionFactory.rollbackReadOnlyTransaction(DbSessionFactory.MAIN);
			throw e;
		}
		return list;
	}

	public List<AutoBuildInfo> findAutoRebuildClientId(List<String> clientIds)
	throws Exception {
		List<AutoBuildInfo> infos = null;
		List<JhfWlClientConfig> cfgs = null;
		try {
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			cfgs = this.wlCompletorDao.findAutoRebuildWorkingId(clientIds);
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		} catch (Exception e) {
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
			throw e;
		}
		
		if(CollectionsUtil.isEmpty(cfgs)){
			return null;
		}
		infos = new ArrayList<AutoBuildInfo>();
		for (JhfWlClientConfig jhfWlClientConfig : cfgs) {
			infos.add(new AutoBuildInfo(
					jhfWlClientConfig.getClientId(),
					jhfWlClientConfig.getCustomerId(),
					jhfWlClientConfig.getPositionAutoRebuildConstraint(),
					jhfWlClientConfig.getPositionAutoRebuildEndInterval(),
					jhfWlClientConfig.getTradableEndInterval()));
		} 
		
		return infos;
	}
}

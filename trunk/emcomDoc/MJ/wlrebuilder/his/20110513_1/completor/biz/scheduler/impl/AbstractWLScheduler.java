package jp.emcom.adv.fx.completor.biz.scheduler.impl;

import java.util.List;

import jp.emcom.adv.fx.completor.biz.executor.ParExecutor;
import jp.emcom.adv.fx.completor.biz.executor.WLExecutor;
import jp.emcom.adv.fx.completor.biz.scheduler.WLScheduler;
import jp.emcom.adv.fx.completor.service.WLCompletorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bestwiz.jhf.core.application.impl.AbstractLifecycle;
import cn.bestwiz.jhf.core.bo.enums.DateKeyEnum;
import cn.bestwiz.jhf.core.dao.bean.main.JhfApplicationDate;
import cn.bestwiz.jhf.core.service.ServiceFactory;

/**
 * 
 * @author Jingqi Xu
 */
public abstract class AbstractWLScheduler extends AbstractLifecycle implements WLScheduler {
	//
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWLScheduler.class);
	
	//
	protected WLExecutor wlExecutor;
	protected ParExecutor wlParExecutor;

	protected List<String> clientIds;
	protected WLCompletorService wlCompletorService;
	
	/**
	 * 
	 */
	protected boolean isValidToSchedule() throws Exception {
		//
		final JhfApplicationDate jad = ServiceFactory.getConfigService().getApplicationDate(DateKeyEnum.FRONT_DATE_KEY_ENUM.getName());
		if (System.currentTimeMillis() >= jad.getFrontEndDatetime().getTime() - 60 * 1000) { // One minute
			LOGGER.info("rollover is about to start in one minute");
			return false;
		} 
		
		//
		if(!ServiceFactory.getCoreService().isMarketOpen()) {
			LOGGER.info("market is NOT open");
			return false;
		}
		return true;
	}
	
	public void setWlParExecutor(ParExecutor wlParExecutor) {
		this.wlParExecutor = wlParExecutor;
	}

	public void setWlExecutor(WLExecutor executor) {
		this.wlExecutor = executor;
	}

	public void setClientIds(List<String> clientIds) {
		this.clientIds = clientIds;
	}

	public void setWlCompletorService(WLCompletorService service) {
		this.wlCompletorService = service;
	}
}

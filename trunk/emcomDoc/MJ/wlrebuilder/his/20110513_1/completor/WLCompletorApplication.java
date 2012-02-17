package jp.emcom.adv.fx.completor;

import cn.bestwiz.jhf.core.wlratecache.impl.WlRataCacheFactory;
import jp.emcom.adv.fx.commons.application.DefaultApplication;
import jp.emcom.adv.fx.commons.application.LifecycleUtils;
import jp.emcom.adv.fx.completor.biz.scheduler.WLScheduler;

/**
 *
 * 
 */
public class WLCompletorApplication extends DefaultApplication{
	
	/**
	 * 
	 */
	@Override
	protected void doStart() throws Exception {
		//
		super.doStart();
		
//		WlRataCacheFactory.getRateCache();
		//
		((WLScheduler)getContext().getBean("wlScheduler")).start();
	}

	@Override
	protected void doStop(long timeout) throws Exception {
		//
		timeout = LifecycleUtils.stopQuietly(((WLScheduler) getContext().getBean("wlScheduler")), timeout);
		
		//
		super.doStop(timeout);
	}
}

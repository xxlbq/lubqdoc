package jp.emcom.adv.fx.completor.main;


import jp.emcom.adv.fx.commons.application.DefaultApplication;
import jp.emcom.adv.fx.commons.handler.BusinessHandler;

import org.apache.commons.logging.Log;

import cn.bestwiz.jhf.core.util.LogUtil;

public class WLCompletorApplicationStartup extends DefaultApplication{

	private final static Log m_log = LogUtil.getLog(WLCompletorApplicationStartup.class);
	private BusinessHandler handler;
//	private AbstractLifecycle ws ;
	@Override
	protected void doStart() throws Exception {
		try {
			super.doStart();
			m_log.info("===[TraderServerStartup]=== Start Process: ");
			handler = (BusinessHandler)getContext().getBean("orderCompletorHandler");
			handler.businessHandle();
			m_log.info("===[TraderServerStartup]=== Start Process Success: ");

		} catch (Exception ex) {
			ex.printStackTrace();
			m_log.error("==[TraderServerStartup]=== Start Process Errors: ",ex);
			System.exit(1);
		}
		
	}

	@Override
	protected void doStop(long timeout) throws Exception {
		super.doStop(timeout);
		
	}
}

package cn.bestwiz.jhf.ratectrl.ratemanager.listener;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.logging.Log;
import cn.bestwiz.jhf.core.bo.enums.AppPropertyKey;
import cn.bestwiz.jhf.core.bo.enums.AppPropertyType;
import cn.bestwiz.jhf.core.bo.enums.CurrencyPairUserTypeEnum;
import cn.bestwiz.jhf.core.configcache.ConfigService;
import cn.bestwiz.jhf.core.dao.bean.main.JhfCurrencyPair;
import cn.bestwiz.jhf.core.jms.SimpleCallback;
import cn.bestwiz.jhf.core.jms.bean.CpSpotRateInfo;
import cn.bestwiz.jhf.core.service.ServiceFactory;
import cn.bestwiz.jhf.core.service.exception.CoreException;
import cn.bestwiz.jhf.core.util.LogUtil;
import cn.bestwiz.jhf.core.util.PropertiesLoader;
import cn.bestwiz.jhf.ratectrl.exceptions.RateCheckerException;
import cn.bestwiz.jhf.ratectrl.ratemanager.RateManager;
import cn.bestwiz.jhf.ratectrl.ratemanager.bussiness.InnerCpRateCache;
import cn.bestwiz.jhf.ratectrl.ratemanager.bussiness.RateManagerChecker;

/**
 * RateManagerListener
 * 1、超时检查
 * 
 * 2、乱序检查
 * 
 * 3、背离检查
 * 
 * 4、频率检查
 * 
 * 5、汇率分发
 * 
 * @author zuolin <zuolin@bestwiz.cn>
 * 
 * @copyright 2006, BestWiz(Dalian) Co.,Ltd
 * 
 */
public class RateManagerListener implements SimpleCallback {
	
	private final static Log m_log = LogUtil.getLog(RateManagerListener.class);

	private ExecutorService m_executor = Executors.newFixedThreadPool(Integer.valueOf(PropertiesLoader.getProperties("rateManager_thread_pool.properties").getProperty("pool_size")).intValue());

	private static ConfigService configService = null;

	private static RateManager m_rateManager = null;

	private static RateManagerChecker m_rateChecker = null;

	private static InnerCpRateCache m_innerCpRateCache = null;

	public RateManagerListener() throws Exception {
		try {
			configService = ServiceFactory.getConfigService();
			m_rateManager = RateManager.getInstance();
			m_rateChecker = m_rateManager.getRateChecker();
			m_innerCpRateCache = m_rateManager.getInnerCpRateCache();
		} catch (Exception e) {
			m_log.error("RateManagerListener init Errors: ", e);
		}
	}

	/**接受cp汇率进行汇率检查及分发
	 * @author zuolin <zuolin@bestwiz.cn>
	 * 
	 */
	public void onMessage(Serializable message) {
		if (null == message) {
			m_log.error("====== RateManagerListener Message From Provider Is Null");
			return;
		}
		if (!(message instanceof CpSpotRateInfo)) {
			m_log.error("====== RateManagerListener Message From Provider Is Not Instance Of CpSpotRateInfo");
			return;
		}
		final CpSpotRateInfo cpSpotRateInfo = (CpSpotRateInfo) message;
		m_log.info("R:" +cpSpotRateInfo.getCurrencyPair()+","+cpSpotRateInfo.getFxPriceId());
		m_executor.execute(new Runnable() {
			public void run() {
				long startTime = System.currentTimeMillis();
				try {
					
					//汇率汇率用于背离检查
					m_innerCpRateCache.putParityCheckRate(cpSpotRateInfo);
					
					//通用检查
					//1、超时检查
					//2、乱序检查
					if (!(m_rateChecker.commonCheck(cpSpotRateInfo))) {
						m_log.info("====== RateManagerListener commonCheck false!" + cpSpotRateInfo);
						return;
					}
					
					//背离检查
					if (!m_rateChecker.parityCheck4Dealer(cpSpotRateInfo)) {
						m_log.info("====== RateManagerListener parityCheck false!" + cpSpotRateInfo);
						return;
					}
					
					//保存汇率，供修改汇率状态重发等
					m_innerCpRateCache.putRateInfo(cpSpotRateInfo);
					
					//汇率分发
					sendToCpRateCache(cpSpotRateInfo);
					
//					if (isSlowEnough(cpSpotRateInfo)) {
//						sendToAgentSpotRate(cpSpotRateInfo);
//					}
				} catch (CoreException e) {
					m_log.error("====== RateManagerListener Call CoreService Errors: " + cpSpotRateInfo, e);
				} catch (RateCheckerException e) {
					m_log.error("======= CpSpotRateListener call RateChecker.CommonCheck to check rates errors return: " + cpSpotRateInfo, e);
				} catch (Exception e) {
					m_log.error("====== RateManagerListener to Check Rates Errors: " + cpSpotRateInfo, e);
				}
				m_log.info("S:"+(System.currentTimeMillis() - startTime)+ ","+cpSpotRateInfo.getCurrencyPair()+","+cpSpotRateInfo.getFxPriceId());
			}
		});
	}

	/**校验汇率发送时间，用以控制汇率处理频率
	 * @param cpSpotRateInfo
	 * @return
	 * @throws Exception
	 */
//	private boolean isSlowEnough(CpSpotRateInfo cpSpotRateInfo) throws Exception {
//		boolean ret = false;
//			Date lastMessageTime = m_innerCpRateCache.getLastSendTimebyCounIdAndCurrPair(cpSpotRateInfo.getCounterPartyId(), cpSpotRateInfo.getCurrencyPair());
//			long sendMinTimeForTrader = Long.valueOf(configService.obtainAppProperty(AppPropertyType.RATE, AppPropertyKey.rateSendToAgentMinTime));
//			if (null == lastMessageTime || cpSpotRateInfo.getMessageTime().getTime() - lastMessageTime.getTime() > sendMinTimeForTrader) {
//				ret = true;
//				m_innerCpRateCache.putLastSendTimebyCounIdAndCurrPair(cpSpotRateInfo);
//			}else{
//				m_log.info("rate comes too fast,sendMinTimeForTrader:" + sendMinTimeForTrader + " ms;" 
//						+ cpSpotRateInfo.getCurrencyPair() 
//						+ " new rate time ="+ cpSpotRateInfo.getMessageTime() 
//						+ ";old rate time ="+ lastMessageTime);
//			}
//		
//		return ret;
//	}
	

	/**
	 * 发送JMS JMS消息
	 * 
	 * @param cpSpotRateInfo
	 * @throws Exception
	 */
	private void sendToCpRateCache(CpSpotRateInfo cpSpotRateInfo) throws Exception {
		m_rateManager.getToCpRateSender(cpSpotRateInfo.getCounterPartyId()).sendMessageWithAliveTime(cpSpotRateInfo);
		m_log.info("SC:" + cpSpotRateInfo.getCurrencyPair()+","+cpSpotRateInfo.getFxPriceId());
	}


	/**
	 * 发送JMS方法 给rateagent发JMS消息
	 * 
	 * @param cpSpotRateInfo
	 * @param isPartyCheckOK
	 * @return 是否发送
	 * @throws Exception
	 */
//	private void sendToAgentSpotRate(CpSpotRateInfo cpSpotRateInfo) throws Exception {
//		JhfCurrencyPair jhfCurrencyPair = configService.getJhfCurrencyPair(cpSpotRateInfo.getCurrencyPair());
//		if (jhfCurrencyPair != null){
//			int whoUse = jhfCurrencyPair.getWhoUse().intValue();
//			if (whoUse == CurrencyPairUserTypeEnum.ALL_USER_ENUM.getValue() || whoUse == CurrencyPairUserTypeEnum.TRADER_USER_ENUM.getValue()) {
//				m_rateManager.geToTradeRateSender(cpSpotRateInfo.getCounterPartyId()).sendMessageWithAliveTime(cpSpotRateInfo);
//				m_log.info("ST:" +cpSpotRateInfo.getCurrencyPair()+","+cpSpotRateInfo.getFxPriceId());
//			}
//		}else{
//			m_log.error("not ccp:" + cpSpotRateInfo.getCurrencyPair()+","+cpSpotRateInfo.getFxPriceId());
//			return;
//		}
//
//	}

	/**
	 * 关闭listener 如果当前进程没有全部结束，不能关闭.
	 * 
	 * @throws Exception
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	public void close() throws Exception {
		m_executor.shutdown();
	}

}

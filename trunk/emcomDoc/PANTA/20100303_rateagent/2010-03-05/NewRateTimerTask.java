package cn.bestwiz.jhf.ratectrl.traderateagent.bussiness;

import java.util.Map;
import org.apache.commons.logging.Log;
import cn.bestwiz.jhf.core.bo.enums.AppPropertyKey;
import cn.bestwiz.jhf.core.bo.enums.AppPropertyType;
import cn.bestwiz.jhf.core.jms.bean.FxSpotRateInfo;
import cn.bestwiz.jhf.core.service.CoreService;
import cn.bestwiz.jhf.core.service.ServiceFactory;
import cn.bestwiz.jhf.core.util.LogUtil;
import cn.bestwiz.jhf.ratectrl.traderateagent.TradeRateAgent;
import cn.bestwiz.jhf.ratectrl.traderateagent.listener.TradeRateAgentListener;

/**
 * 每秒检查是否有汇率被最小间隔时间检查过滤
 * 如果有并且当前没有该货币对的新汇率则发送它
 * @author qym
 *
 */
public class NewRateTimerTask implements Runnable{
	private static Log m_log = LogUtil.getLog(NewRateTimerTask.class);
	private static InnerRateCache innerRateCache = InnerRateCache.getInstance();
	private static long period = 1000;
	 /**
     * The action to be performed by this timer task.
     */
    public void run() {
    	CoreService coreService = ServiceFactory.getCoreService();
		while (true) {
			if(!TradeRateAgentListener.m_initFlag){
				try {
					if (coreService.isMarketOpen()) {
						period = Long.parseLong(coreService.obtainAppProperty(AppPropertyType.RATE, AppPropertyKey.newRateCheckMinTime));
						Map<String, FxSpotRateInfo> newRateMap = innerRateCache.getNewRateInfo();
						if (newRateMap.size() > 0) {
							FxSpotRateInfo[] newRateArray = newRateMap.values().toArray(new FxSpotRateInfo[0]);
							for (FxSpotRateInfo newFxSpotRateInfo : newRateArray) {
								String currencypair = newFxSpotRateInfo.getCurrencyPair();
								
								m_log.info("RN:" + newFxSpotRateInfo.getCurrencyPair() +","+newFxSpotRateInfo.getPriceId());
								FxSpotRateInfo fxSpotRateInfo = innerRateCache.getRateInfo(currencypair);
								if (fxSpotRateInfo != null ) {
									if (fxSpotRateInfo.getReferenceTime().getTime() < newFxSpotRateInfo.getReferenceTime().getTime()) {
										newFxSpotRateInfo.setSendFrom("NewRateTimer");
										
//										if(innerRateCache.getRateInfoMap() != null
//												&& innerRateCache.getRateInfoMap().containsKey(currencypair)
//												&& innerRateCache.getRateInfoMap().get(currencypair) != null 
//												&& innerRateCache.getRateInfoMap().get(currencypair).getPriceId() != null 
//												&& innerRateCache.getRateInfoMap().get(currencypair).getPriceId().equals(fxSpotRateInfo.getPriceId())){
//											m_log.info("RATE NO SEND ======%"+fxSpotRateInfo.getSendFrom()+" sending rate priceId ["+fxSpotRateInfo.getPriceId()
//													+ "] equal new rate priceId ["+innerRateCache.getRateInfoMap().get(currencypair).getPriceId()+"]");
//											return;
//										}
										
										TradeRateAgent.getInstance().doSendRate(newFxSpotRateInfo,false);
										
										m_log.info("SN:"+newFxSpotRateInfo.getCurrencyPair() +","+newFxSpotRateInfo.getPriceId());
									}
								}
								innerRateCache.removeNewRateInfo(currencypair);
							}
						}
					} 
				} catch (Exception e) {
					m_log.error("NewRateCompare  error", e);
				}
			}
			
			try {
				Thread.sleep(period);
			} catch (InterruptedException e) {
				m_log.error("NewRateCompare, this  Thread was Interrupted when it is sleeping", e);
			}
		}
	}
    
     

     
}

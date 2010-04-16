package cn.bestwiz.jhf.ratectrl.traderateagent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import cn.bestwiz.jhf.core.bo.bean.CurrencyPairInfo;
import cn.bestwiz.jhf.core.bo.enums.CurrencyPairWhoUseEnum;
import cn.bestwiz.jhf.core.bo.enums.ParityCheckerTypeEnum;
import cn.bestwiz.jhf.core.bo.enums.SysMailActionIdEnum;
import cn.bestwiz.jhf.core.cpratecache.CpRateCacheFactory;
import cn.bestwiz.jhf.core.dao.util.DbSessionFactory;
import cn.bestwiz.jhf.core.jms.DestinationConstant;
import cn.bestwiz.jhf.core.jms.SimpleReceiver;
import cn.bestwiz.jhf.core.jms.SimpleSender;
import cn.bestwiz.jhf.core.jms.bean.FxSpotRateInfo;
import cn.bestwiz.jhf.core.jms.bean.FxSpotRateInfoMap;
import cn.bestwiz.jhf.core.jms.exception.JMSException;
import cn.bestwiz.jhf.core.service.ProductService;
import cn.bestwiz.jhf.core.service.ServiceFactory;
import cn.bestwiz.jhf.core.util.LogUtil;
import cn.bestwiz.jhf.ratectrl.bussiness.Lock;
import cn.bestwiz.jhf.ratectrl.bussiness.SysMailSender;
import cn.bestwiz.jhf.ratectrl.exceptions.RateCtrlException;
import cn.bestwiz.jhf.ratectrl.traderateagent.bussiness.CrossRateTimerTask;
import cn.bestwiz.jhf.ratectrl.traderateagent.bussiness.InnerRateCache;
import cn.bestwiz.jhf.ratectrl.traderateagent.bussiness.ManualDynSpreadHandler;
import cn.bestwiz.jhf.ratectrl.traderateagent.bussiness.NewRateTimerTask;
import cn.bestwiz.jhf.ratectrl.traderateagent.bussiness.RateGenerator;
import cn.bestwiz.jhf.ratectrl.traderateagent.bussiness.RateInitlizer;
import cn.bestwiz.jhf.ratectrl.traderateagent.bussiness.TradeRateAgentChecker;
import cn.bestwiz.jhf.ratectrl.traderateagent.listener.AdminMessageListener;
import cn.bestwiz.jhf.ratectrl.traderateagent.listener.DlManualRateListener;
import cn.bestwiz.jhf.ratectrl.traderateagent.listener.ResendSpotRateListener;
import cn.bestwiz.jhf.ratectrl.traderateagent.listener.TradeRateAgentListener;

/**
 * TradeRateAgent	
 * (1).启动各种Jms接受类和发送类，初始化RateCache模块,初始化汇率RateManagerChecker以及MailSender等
 * 
 * (2).初始化RateInitlizer
 * 
 * (3).接收RateManager发送的汇率
 * 
 * (4).接收admin消息 		
 * @author  zuolin <zuolin@bestwiz.cn> 			
 * 				
 * @copyright 2006, BestWiz(Dalian) Co.,Ltd		
 * 	
 */
public class TradeRateAgent {
	private TradeRateAgent(){};
	private static TradeRateAgent  m_instance = new TradeRateAgent();
	public static TradeRateAgent getInstance() {
		return m_instance ;
	}
	private final static Log m_log = LogUtil.getLog(TradeRateAgent.class);
	// the receiver of CpSpotRateInfo from RateManger
	private HashMap<String,SimpleReceiver> receiverMap = new HashMap<String,SimpleReceiver>();
	
	// the adminsubcriber
	private  SimpleReceiver m_adminSubscriber = null ;  
	private  AdminMessageListener m_adminListener = null ;
	
	// the dl StatusMessage
	private SimpleReceiver  m_dlReceiver = null;
	private DlManualRateListener m_dlManualListener = null;
	
	// the RateCache receiver for resend rate
	private SimpleReceiver  m_ResendReceiver = null;
	private ResendSpotRateListener m_ResendListener = null;
	
	// the bussiness 
	private InnerRateCache   m_innerRateCache = null ;
	private RateGenerator    m_rateGenerator = null ;
	private SysMailSender    m_sysMailSender = null ;
    private RateInitlizer    m_rateInitlizer = null;
    private TradeRateAgentChecker m_rateChecker  = null ;
    
	// the sender of fxspotrateinfo to other model
	private SimpleSender  m_toCustomerModuleRateTopicSender = null;
	private SimpleSender  m_toCustomerRateTopicSender = null;
	private SimpleSender  m_toResendSpotRateResponseSender = null;
	
	// 
	private CrossRateTimerTask  longTimeNoRateChecker = null;
    /**
	 * TradeRateAgent 启动 
	 * (1).初始化CpRateCache用于取得GW原始汇率进行背离检查
	 * (2).初始化对RateCache的JMS　sender
	 * (3).初始化对Opm的JMS　sender
	 * (4).初始化Other的 JMS sender
	 * (5).初始化Rpm的 JMS sender
	 * (6).初始化innerRateCache,用于保留汇率
	 * (7).初始化rateGenerator,用于处理汇率
	 * (8).初始化系统邮件的sender,用于发送系统邮件
	 * (9).初始化rateChecker,用于汇率检查
	 * (10).初始化背离检查，从属性文件取得相关信息
	 * (11).注册RateManager发送JMS消息的receiver
	 * (12).初始化一个listener并加入到receiver中
	 * (13).注册Admin发送JMS消息的admin receiver
	 * (14).初始化一个listener并加入到admin receiver中
	 * @throws RateCtrlException
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	public void startProcess() throws RateCtrlException {
        try {
        	CpRateCacheFactory.initlizeForDealer();
        	//1. initlize all other sender 
        	if (null == m_toCustomerModuleRateTopicSender) {
        		m_toCustomerModuleRateTopicSender = SimpleSender.getInstance(
        				DestinationConstant.CustomerModuleRateTopic);
        	}
        	if (null == m_toCustomerRateTopicSender) {
        		m_toCustomerRateTopicSender = SimpleSender.getInstance(
        				DestinationConstant.CustomerRateTopic);
        	}
        	if (null == m_toResendSpotRateResponseSender) {
        		m_toResendSpotRateResponseSender = SimpleSender.getInstance(
        				DestinationConstant.resendSpotRateResponseTopic);
        	}
        	
        	//2. initlize the bussiness 
        	if (null == m_innerRateCache) {
        		m_innerRateCache = InnerRateCache.getInstance();
        	}
        	if (null == m_rateGenerator) {
        		m_rateGenerator = RateGenerator.getInstance();;
        	}
        	if (null == m_sysMailSender) {
        		m_sysMailSender = SysMailSender.getInstance();
        	}
        	if (null == m_rateChecker) {
        		m_rateChecker = TradeRateAgentChecker.getInstance();
        	}
        	if (null == m_rateInitlizer) {
        		m_rateInitlizer = RateInitlizer.getInstance();
        	}
            
            //3. call rateChecker.initlize() to initlize parityChecker  
        	m_rateChecker.initlize(ParityCheckerTypeEnum.MOUDLENAME_TRADER.getValue());
            //4. call initlizePot 
            initlizePot(); 
			//5.创建TradeRateAgentListener，根据货币对来启动线程,定时检查cpRateCache
            TradeRateAgentListener agent = new TradeRateAgentListener();
            agent.listen();
            
            //7. create a AdminMessageListener receiver
            if (null == m_adminSubscriber){
            	m_adminSubscriber = new SimpleReceiver(
            			DestinationConstant.AdminTopic);
            }
            
            //8. register a AdminMessageListener listener
            if (null == m_adminListener){
                m_adminListener = new AdminMessageListener();
                m_adminSubscriber.addCallback(m_adminListener);
                m_log.debug("===== adminSubscriber addCallback start: ");
            }
            
            //9. create a DlManualRateListener receiver
            if (null == m_dlReceiver) {
            	m_dlReceiver = new SimpleReceiver(
            			DestinationConstant.DlManualRateQueue);
            }
            //10.register a DlManualRateListener listener
            if (null == m_dlManualListener) {
            	m_dlManualListener = new DlManualRateListener();
            	m_dlReceiver.addCallback(m_dlManualListener);
            }
            
            if (null == m_ResendReceiver) {
            	m_ResendReceiver = new SimpleReceiver(
            			DestinationConstant.ResendSpotRateQueue);
            }
            
            //
            if (null == m_ResendListener) {
            	m_ResendListener = new ResendSpotRateListener();
            	m_ResendReceiver.addCallback(m_ResendListener);
            }
            Thread newRateThread = new Thread(new NewRateTimerTask());
            newRateThread.setDaemon(true);
            newRateThread.start();
            
            // TODO 添加对长时间没汇率做CrossRate配信的检查
    		longTimeNoRateChecker = CrossRateTimerTask.getInstance();
            
        } catch (JMSException e) {
            m_log.error("===[TradeRateAgent]=== JMS Receiver Or Sander Errors: ",e);
            throw new RateCtrlException(e);
        } catch (Exception e) {
            m_log.error("===[TradeRateAgent]=== Initlize Errors: ");
            throw new RateCtrlException(e);
        }
	}
	
	/**
	 * 1、按货币对初始化spreadinfo
	 * 2、按货币对初始化innerratecache的锁
	 * 3、for every jhf_currency_pair , 
	 * do following steps:
	 *(1) RateInitlizer.initializePot(currencyPair) get latest rate
	 *(2) call InnerRateCache.put( String , Map) to 
	 *    put this FxSpotRateInfo rate to  innercache 
	 *(3) send this FxSpotRateInfo by toRateCacheRateSender
	 * @throws RateCtrlException
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	public void initlizePot() throws RateCtrlException {
        m_log.debug("===[TradeRateAgent]=== InitlizePot Start: " );
        long lbegin = System.currentTimeMillis();
        List<CurrencyPairInfo> lstRows = null;
        FxSpotRateInfo pateInfo = null;
        ProductService productService = ServiceFactory.getProductService(); 
        try {
            lstRows = productService.obtainCurrencyPairs(
            		CurrencyPairWhoUseEnum.CUSTOMER_USE_ENUM,true);
	        // 初始化
            m_log.info("init CurrencyPairs counts = " + lstRows.size());
            boolean infoDownFlag = false; 
            try {
                DbSessionFactory.beginTransaction(DbSessionFactory.INFO);
                DbSessionFactory.commitTransaction(DbSessionFactory.INFO);
            } catch (Exception e) {
            	DbSessionFactory.rollbackTransaction(DbSessionFactory.INFO);
            	infoDownFlag = true;
            	m_log.info("info db down");
            }
			for (int i = 0, size = lstRows.size(); i < size; i++ ) {
	        	long lsigleBegin = System.currentTimeMillis();
	        	String currencyPair = lstRows.get(i).getCode();
	        	//按货币对初始化spreadinfo
	            ManualDynSpreadHandler.put(currencyPair);
	            //按货币对初始化innerratecache的锁
	            Lock.put(currencyPair);
	        	pateInfo = m_rateInitlizer.initializePot(currencyPair,infoDownFlag);
	            // 给RateCache发送JMS
	            m_innerRateCache.putRateInfo(currencyPair, pateInfo);
	            m_toCustomerRateTopicSender.sendMessage(RateGenerator.getInstance().convert(pateInfo));
	            long lsigleEnd = System.currentTimeMillis();
	            m_log.info("single init time is " + (lsigleEnd - lsigleBegin));
	        }
	        
	        
	        // 成功后发送邮件
	        m_sysMailSender.sendInitlizePotMail(
	        		SysMailActionIdEnum.TRADEAGENT_INITLIZEPOT_MAIL_ENUM.getName());
	        
	        long lend = System.currentTimeMillis();
	        m_log.info("initlizePot time is " + (lend - lbegin));
        } catch (Exception e) {
            m_log.error("===[TradeRateAgent]=== InitlizePot Errors: ");
            throw new RateCtrlException(e);
        }
        m_log.debug("===[TradeRateAgent]=== InitlizePot End: " );
	}
	
	/**
	 * TradeRateAgent 关闭
	 * (1).关闭listener
	 * (2).关闭receiver
	 * (3).关闭admin listener
	 * (4).关闭admin receiver
	 * (5).关闭rateChecker
	 * (6).关闭innerRateCache
	 * (7).关闭rateGenerator
	 * (8).关闭sysMailSender
	 * (9).关闭rateInitlizer
	 * (10).关闭RateCache
	 * (11).关闭Opm的JMS　sender
	 * (12).关闭Other的JMS　sender
	 * (13).关闭Rpm的JMS　sender
	 * (14).关闭CpRateCache
	 * (15).退出JVM 
	 * @throws RateCtrlException
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	public void stopPropcess() throws RateCtrlException {
		try {
			//1. close listener
			for(SimpleReceiver r :receiverMap.values()){
				r.close();
			}
			//2. close adminSubscriber
			if( null != m_adminSubscriber ){
				m_adminSubscriber.close();
				m_adminSubscriber= null ;
			}
	        //3. close adminListener
	        if (null != m_adminListener) {
	            m_adminListener.close();
	            m_adminListener = null;
	        }
	        //4. close dlManualListener 
	        if (null != m_dlManualListener) {
	        	m_dlManualListener.close();
	        	m_dlManualListener = null;
	        }
	        //5. close m_dlManual Receiver
	        if (null != m_dlReceiver){
	        	m_dlReceiver.close();
	        	m_dlReceiver = null;
	        }
			//6. close all source defined in attributes.
	        if (null!= m_rateChecker) {
	            m_rateChecker.close();
	            m_rateChecker = null;
	        }

	        if (null != m_innerRateCache){
	            m_innerRateCache = null;
	        }
	        
	        if (null != m_rateGenerator) {
	            m_rateGenerator = null;
	        }
	        
	        if (null != m_sysMailSender) {
	        	m_sysMailSender.close();
	            m_sysMailSender = null;
	        }
	        
	        if (null != m_rateInitlizer) {
	            m_rateInitlizer = null;
	        }
	        
	        if (null != m_toCustomerModuleRateTopicSender){
	            m_toCustomerModuleRateTopicSender.close();
	            m_toCustomerModuleRateTopicSender = null;
	        }
	        
	        
	        if (null != m_toCustomerRateTopicSender) {
	            m_toCustomerRateTopicSender.close();
	            m_toCustomerRateTopicSender = null;
	        }
	        
	        if (null != m_toResendSpotRateResponseSender) {
	        	m_toResendSpotRateResponseSender.close();
	        	m_toResendSpotRateResponseSender = null;
	        }
	        
	        // TODO
	        if (null != longTimeNoRateChecker){
	        	longTimeNoRateChecker.close();
	        }
	        
	        m_log.debug("===[TradeRateAgent]=== stopPropcess() Success: ");
        } catch (Exception e) {
            m_log.error("===[TradeRateAgent]=== startProcess() Errors: ");
            throw new RateCtrlException(e);
        }
	}
	
	/**
	 * 取得InnerRateCache
	 * @return m_innerRateCache
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	public final InnerRateCache getInnerRateCache() {
		return m_innerRateCache;
	}

	/**
	 * 取得InnerRateCache
	 * @return m_innerRateCache
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	public final TradeRateAgentChecker getRateChecker() {
		return m_rateChecker;
	}

	/**
	 * 取得RateGenerator
	 * @return m_rateGenerator
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	public final RateGenerator getRateGenerator() {
		return m_rateGenerator;
	}

	/**
	 * 取得SysMailSender
	 * @return m_sysMailSender
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	public final SysMailSender getSysMailSender() {
		return m_sysMailSender;
	}
    
	/**
	 * 取得RateInitlizer
	 * @return m_rateInitlizer
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
    public final RateInitlizer getRateInitlizer() {
        return m_rateInitlizer;
    }
	
	
	/**
	 * 取得SimpleSender
	 * @return m_toOtherrateSender
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	public final SimpleSender getToCustomerRateTopicSender() {
		return m_toCustomerRateTopicSender;
	}
	
	/**
	 * 取得SimpleSender
	 * @return m_toRateCacheRateSender
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	public final SimpleSender getToCustomerModuleRateTopicSender() {
		return m_toCustomerModuleRateTopicSender;
	}
    
	/**
	 * 取得SimpleSender
	 * @return m_toRateCacheRateSender
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	public final SimpleSender getToResendSpotRateResponseSender() {
		return m_toResendSpotRateResponseSender;
	}
	
//	/**
//	 * 正常发送
//	 * @param fxSpotRateInfo
//	 * @throws Exception
//	 */
//	public void doSendRate(FxSpotRateInfo fxSpotRateInfo) throws Exception {
//		String currencyPair = fxSpotRateInfo.getCurrencyPair();
//		boolean isFirstRate = m_innerRateCache.isPreviousDateRateInfo(currencyPair);
//		synchronized (Lock.getLock(currencyPair)) {
//			RateGenerator.getInstance().setRelativeFields(currencyPair, fxSpotRateInfo,isFirstRate,false);
//			m_innerRateCache.putRateInfo(currencyPair, fxSpotRateInfo);
//			FxSpotRateInfoMap mapSpotRate = m_rateGenerator.convert(fxSpotRateInfo);
//			m_toCustomerRateTopicSender.sendMessageWithAliveTime(mapSpotRate);
//			m_toCustomerModuleRateTopicSender.sendMessageWithAliveTime(mapSpotRate);
//		}
//	}
	
	/**
	 * 配信发送(CrossRate 传入true)
	 * @param fxSpotRateInfo
	 * @throws Exception
	 */
	public void doSendRate(FxSpotRateInfo fxSpotRateInfo,boolean crossRateFlg) throws Exception {
		String currencyPair = fxSpotRateInfo.getCurrencyPair();
		boolean isFirstRate = m_innerRateCache.isPreviousDateRateInfo(currencyPair);
		synchronized (Lock.getLock(currencyPair)) {
			RateGenerator.getInstance().setRelativeFields(currencyPair, fxSpotRateInfo,isFirstRate,crossRateFlg);
			
			if(m_innerRateCache.getRateInfoMap() != null
					&& m_innerRateCache.getRateInfoMap().containsKey(currencyPair)
					&& m_innerRateCache.getRateInfoMap().get(currencyPair) != null 
					&& m_innerRateCache.getRateInfoMap().get(currencyPair).getPriceId() != null 
					&& m_innerRateCache.getRateInfoMap().get(currencyPair).getPriceId().equals(fxSpotRateInfo.getPriceId())){
				m_log.info("RATE NO SEND ======%"+fxSpotRateInfo.getSendFrom()+" sending rate priceId ["+fxSpotRateInfo.getPriceId()
						+ "] equal new rate priceId ["+m_innerRateCache.getRateInfoMap().get(currencyPair).getPriceId()+"]");
				return;
			}
			m_innerRateCache.putRateInfo(currencyPair, fxSpotRateInfo);
			FxSpotRateInfoMap mapSpotRate = m_rateGenerator.convert(fxSpotRateInfo);
			Map<String, FxSpotRateInfo> nInfoMap = InnerRateCache.getInstance().getNewRateInfo();
			if(nInfoMap == null 
					|| (!nInfoMap.containsKey(fxSpotRateInfo.getCurrencyPair())) 
					|| null == nInfoMap.get(fxSpotRateInfo.getCurrencyPair()) ){
				
				m_log.info("RATE HAS SEND #### sending rate priceId ["+fxSpotRateInfo.getPriceId()+ "] ");
				m_toCustomerRateTopicSender.sendMessageWithAliveTime(mapSpotRate);
				m_toCustomerModuleRateTopicSender.sendMessageWithAliveTime(mapSpotRate);
				return;
			}
			
			if(! fxSpotRateInfo.getPriceId().equals(
					nInfoMap.get(fxSpotRateInfo.getCurrencyPair()).getPriceId())){
				
				m_log.info("RATE CHECKED SEND ==>> sending rate priceId ["+fxSpotRateInfo.getPriceId()
						+ "] equal new rate priceId ["+nInfoMap.get(fxSpotRateInfo.getCurrencyPair()).getPriceId()+"]");

				
				m_toCustomerRateTopicSender.sendMessageWithAliveTime(mapSpotRate);
				m_toCustomerModuleRateTopicSender.sendMessageWithAliveTime(mapSpotRate);
				return;
			}else{
				m_log.info("RATE NO SEND ------%"+fxSpotRateInfo.getSendFrom()+" sending rate priceId ["+fxSpotRateInfo.getPriceId()
						+ "] equal new rate priceId ["+nInfoMap.get(fxSpotRateInfo.getCurrencyPair()).getPriceId()+"]");
			}

		}
	}
	
}







 

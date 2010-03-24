package cn.bestwiz.jhf.ratectrl.ratemanager;

import java.util.HashMap;
import java.util.List;
import org.apache.commons.logging.Log;
import org.dom4j.Element;
import cn.bestwiz.jhf.core.bo.enums.ParityCheckerTypeEnum;
import cn.bestwiz.jhf.core.bo.enums.SysMailActionIdEnum;
import cn.bestwiz.jhf.core.dao.util.DbSessionFactory;
import cn.bestwiz.jhf.core.jms.DestinationConstant;
import cn.bestwiz.jhf.core.jms.SimpleReceiver;
import cn.bestwiz.jhf.core.jms.SimpleSender;
import cn.bestwiz.jhf.core.jms.bean.CpSpotRateInfo;
import cn.bestwiz.jhf.core.jms.exception.JMSException;
import cn.bestwiz.jhf.core.service.CoreService;
import cn.bestwiz.jhf.core.service.ServiceFactory;
import cn.bestwiz.jhf.core.util.CpDestinationProcessor;
import cn.bestwiz.jhf.core.util.LogUtil;
import cn.bestwiz.jhf.ratectrl.bean.CounterpartyCurrencypair;
import cn.bestwiz.jhf.ratectrl.bussiness.SysMailSender;
import cn.bestwiz.jhf.ratectrl.exceptions.RateCtrlException;
import cn.bestwiz.jhf.ratectrl.exceptions.RateManagerException;
import cn.bestwiz.jhf.ratectrl.ratemanager.bussiness.CpRateInitlizer;
import cn.bestwiz.jhf.ratectrl.ratemanager.bussiness.CpRateReadService;
import cn.bestwiz.jhf.ratectrl.ratemanager.bussiness.InnerCpRateCache;
import cn.bestwiz.jhf.ratectrl.ratemanager.bussiness.RateConfidenceChecker;
import cn.bestwiz.jhf.ratectrl.ratemanager.bussiness.RateManagerChecker;
import cn.bestwiz.jhf.ratectrl.ratemanager.listener.AdminMessageListener;
import cn.bestwiz.jhf.ratectrl.ratemanager.listener.CpStatusMessageListener;
import cn.bestwiz.jhf.ratectrl.ratemanager.listener.RateManagerListener;
import cn.bestwiz.jhf.ratectrl.ratemanager.listener.ResendCpSpotRateListener;

/**
 * RateManager
 * (1).接收GW发过来的汇率信息
 * 
 * (2).对汇率信息进行相关检查
 * 
 * (3).将检查通过的汇率分别发到CpRateCache以及TradeRateAgent(CpSpotRateInfo)		
 * 		
 * @author  zuolin <zuolin@bestwiz.cn> 			
 * 				
 * @copyright 2006, BestWiz(Dalian) Co.,Ltd		
 * 	
 */
public class RateManager {
	private RateManager(){};
	private static RateManager  m_instance = new RateManager();
	public static RateManager getInstance() {
		return m_instance ;
	}
	private final static Log m_log = LogUtil.getLog(RateManager.class);
	// the receiver of CpSpotRateInfo from GW
	private HashMap<String,SimpleReceiver> fromGWreceiverMap = new HashMap<String,SimpleReceiver>();
	// the sender of CpSpotRateInfo to TradeRateAgent
//	private HashMap<String,SimpleSender> toTradeSenderMap = new HashMap<String,SimpleSender>();
	// the sender of CpSpotRateInfo to CpRatecache
	private HashMap<String,SimpleSender> toCPRateCacheSenderMap = new HashMap<String,SimpleSender>();
	// the bussiness 
	private RateManagerChecker m_rateChecker  = null ;
	private SysMailSender      m_sysMailSender = null ;
	private CpRateInitlizer    m_rateInitlizer = null;
	private InnerCpRateCache   m_innerCpRateCache = null;
	private RateConfidenceChecker m_RateConfidenceChecker = null;
    
	// the receiver of CpSpotRateInfo from CpRatecache
	private SimpleReceiver m_ResendReceiver = null;
	
    private SimpleReceiver m_cpStatusReceiver = null;
    
	// admin
	private  SimpleReceiver m_adminSubscriber = null ; 
	
	/**
	 * RateManager 启动 
	 * (1).初始化CpRateCache用于取得GW原始汇率进行背离检查
	 * (2).初始化对TradeRateAgent的JMS　sender
	 * (3).初始化对CpRateCache的JMS　sender
	 * (4).初始化系统邮件的sender
	 * (5).初始化汇率检查
	 * (6).初始化背离检查，从属性文件取得相关信息
	 * (7).注册GW发送JMS消息的receiver
	 * (8).初始化一个listener并加入到receiver中
	 * @throws RateCtrlException
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	public void startProcess() throws RateCtrlException {
			try {
				CpDestinationProcessor.init();
				//1. initlize all other sender 
				for(Element e:(List<Element>)CpDestinationProcessor.getElements()){
//						toTradeSenderMap.put(e.getName(), SimpleSender.getInstance(e.attributeValue("CpRate4TradeTopic")));
						toCPRateCacheSenderMap.put(e.getName(), SimpleSender.getInstance(e.attributeValue("CpRate4RateCacheTopic")));
				}
		       	//2. initlize the bussiness 
        		m_sysMailSender = SysMailSender.getInstance();
        	
        		m_rateChecker = RateManagerChecker.getInstance();
        	
        		m_innerCpRateCache = InnerCpRateCache.getInstance();
        	
        		m_rateInitlizer = CpRateInitlizer.getInstance();
	        	
	        	//3. call rateChecker.initlize() to initlize dealerparityChecker  
	        	m_rateChecker.initlize(ParityCheckerTypeEnum.MOUDLENAME_DEALER.getValue());
	        	
	        	//4. initlize cp rate
	        	initlizePot();
	        	
	        	//5. 启动cp汇率可用性定时检查器
        		m_RateConfidenceChecker = RateConfidenceChecker.getInstance();
	        	
				//6. create a CpSpotRateListener receiver
	        	for (Element e : (List<Element>) CpDestinationProcessor.getElements()) {
						SimpleReceiver receiver = new SimpleReceiver(e.attributeValue("gwCounterPartyRateTopic"));
						receiver.addCallback(new RateManagerListener());
						fromGWreceiverMap.put(e.getName(), receiver);
				}
	            
        		m_ResendReceiver = new SimpleReceiver(DestinationConstant.ResendCpSpotRateQueue);
				m_ResendReceiver.addCallback(new ResendCpSpotRateListener());
	
				m_adminSubscriber = new SimpleReceiver(DestinationConstant.AdminTopic);
				m_adminSubscriber.addCallback(new AdminMessageListener());
				
                m_cpStatusReceiver = new SimpleReceiver(DestinationConstant.CPStatusMessageTopic);
                m_cpStatusReceiver.addCallback(new CpStatusMessageListener());
                
			} catch (JMSException e) {
	            m_log.error("===[RateManager]=== Initlize JMS Receiver Or Sander Errors: ",e);
	            throw new RateManagerException(e);
			} catch (Exception e) {
				m_log.error("===[RateManager]=== Initlize Errors: ",e);
	            throw new RateManagerException(e);
			}
	}
	
	
	/**
	 * for every jhf_currency_pair , 
	 * do following steps:
	 * 
	 *(1) RateInitlizer.initializePot(currencyPair) get latest rate
	 *(2) call InnerRateCache.put( String , Map) to 
	 *    put this FxSpotRateInfo rate to  innercache 
	 *(3) send this FxSpotRateInfo by toRateCacheRateSender
	 * @throws RateCtrlException
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	public void initlizePot() throws Exception {
        m_log.debug("===[RateManager]=== InitlizePot Start: " );
        List<CounterpartyCurrencypair> lstCounterpartyCurrencypair = new CpRateReadService().obtainCounterpartyCurrencypair();
        // 如果银行全部挂掉抛出异常
        if (lstCounterpartyCurrencypair == null || lstCounterpartyCurrencypair.size() ==0) {
        	m_log.error("Banks is all down ,initlizePot RateManager Failured");
        	throw new Exception("Banks is all down ,initlizePot RateManager Failured");
        }
        boolean infoDownFlag = false; 
        try {
            DbSessionFactory.beginTransaction(DbSessionFactory.INFO);
            DbSessionFactory.commitTransaction(DbSessionFactory.INFO);
        } catch (Exception e) {
        	DbSessionFactory.rollbackTransaction(DbSessionFactory.INFO);
        	infoDownFlag = true;
        	m_log.info("info db down");
        }
		CoreService coreService = ServiceFactory.getCoreService();
		// 取得当前交易日
		String sFrontDate = coreService.getFrontDate();
		// 取得前一交易日
		String sPreviousDate = coreService.getPrevBussinessDate(sFrontDate, 1);
    	try {
    		CpSpotRateInfo cpSpotRateInfo = null;
    		String lastCurrencyPair="";
    		for (CounterpartyCurrencypair counterpartyCurrencypair:lstCounterpartyCurrencypair) {
	        	String currencyPair = counterpartyCurrencypair.getCurrencyPair();
	        	String counterpartyId = counterpartyCurrencypair.getCounterpartyId();
	        	if(lastCurrencyPair.equals(currencyPair)){
	        		cpSpotRateInfo = new CpSpotRateInfo(counterpartyId,cpSpotRateInfo);
	        	}else{
	        		lastCurrencyPair = currencyPair;
	        		cpSpotRateInfo = m_rateInitlizer.initializePot(currencyPair,counterpartyId,infoDownFlag,sFrontDate,sPreviousDate);
	        	}
	        	m_innerCpRateCache.putRateInfo(cpSpotRateInfo);
	        	getToCpRateSender(counterpartyId).sendMessage(cpSpotRateInfo);
	        	m_log.info("=========Send Initial Rate to cprate cache==============="+cpSpotRateInfo.toGrossString());
	        }	
        // 成功后发送邮件 需要增加一个SYS MAIL模板
        m_sysMailSender.sendInitlizePotMail(SysMailActionIdEnum.RATEMANAGER_INITLIZEPOT_MAIL_ENUM.getName());
        } catch (Exception e) {
            m_log.error("===[RateManager]=== InitlizePot() Errors: ",e);
            throw e;
        }
        m_log.debug("===[RateManager]=== InitlizePot End: ");
	}
	
	/**
	 * 取得RateManagerChecker
	 * @return m_rateChecker
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	public final RateManagerChecker getRateChecker() {
		return m_rateChecker;
	}
	
	/**
	 * 取得CpRateSender
	 * @return SimpleSender  (m_toCpRateSender)
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	public final SimpleSender getToCpRateSender(String counterPartyId) {
		return toCPRateCacheSenderMap.get(counterPartyId);
	}
	  
	
	/**
	 * 取得TradeRateSender
	 * @return SimpleSender  (m_toTradeRateSender)
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
//	public final SimpleSender geToTradeRateSender(String counterPartyId) {
//		return toTradeSenderMap.get(counterPartyId);
//	}
	 
	
	/**
	 * 取得SysMailSender
	 * 
	 * @return SysMailSender (m_sysMailSender)
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	public final SysMailSender getSysMailSender() {
		return m_sysMailSender;
	}
	
	/**
	 * 取得InnerCpRateCache
	 * @return SysMailSender  (m_sysMailSender)
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	public final InnerCpRateCache getInnerCpRateCache() {
		return m_innerCpRateCache;
	}
	
	/**
	 * RateManager 关闭
	 * (1).关闭listener
	 * (2).关闭receiver
	 * (3).关闭rateChecker
	 * (4).关闭系统邮件的sender
	 * (5).关闭TradeRateAgent的JMS　sender
	 * (6).关闭CpRateCache的JMS　sender
	 * (7).关闭CpRateCache
	 * (8).退出JVM   
	 * @throws RateCtrlException
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	public void stopPropcess() throws RateCtrlException {
		try {
			for(SimpleReceiver r :fromGWreceiverMap.values()){
				r.close();
			}
			m_adminSubscriber.close();
			
			// close all source defined in attributes.
	        m_rateChecker.close();
	        
	        m_sysMailSender.close();
	        
//        	for(SimpleSender r :toTradeSenderMap.values()){
//				r.close();
//			}
        	for(SimpleSender r :toCPRateCacheSenderMap.values()){
        		r.close();
        	}
        	m_ResendReceiver.close();
	        
	        m_RateConfidenceChecker.close();
	        
		} catch (Exception e) {
			throw new RateCtrlException(e);
		}
	}
}

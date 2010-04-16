package cn.bestwiz.jhf.ratectrl.traderateagent.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import cn.bestwiz.jhf.core.bo.enums.AppPropertyKey;
import cn.bestwiz.jhf.core.bo.enums.AppPropertyType;
import cn.bestwiz.jhf.core.bo.enums.BoolEnum;
import cn.bestwiz.jhf.core.bo.enums.CurrencyPairWhoUseEnum;
import cn.bestwiz.jhf.core.configcache.ConfigService;
import cn.bestwiz.jhf.core.cpratecache.CpRateCacheFactory;
import cn.bestwiz.jhf.core.dao.bean.main.JhfStatusContrl;
import cn.bestwiz.jhf.core.jms.bean.CpSpotRateInfo;
import cn.bestwiz.jhf.core.jms.bean.FxSpotRateInfo;
import cn.bestwiz.jhf.core.service.CoreService;
import cn.bestwiz.jhf.core.service.ServiceFactory;
import cn.bestwiz.jhf.core.service.exception.CoreException;
import cn.bestwiz.jhf.core.util.LogUtil;
import cn.bestwiz.jhf.core.util.PropertiesLoader;
import cn.bestwiz.jhf.core.util.TypeConverter;
import cn.bestwiz.jhf.ratectrl.traderateagent.TradeRateAgent;
import cn.bestwiz.jhf.ratectrl.traderateagent.bussiness.InnerRateCache;
import cn.bestwiz.jhf.ratectrl.traderateagent.bussiness.ManualDynSpreadHandler;
import cn.bestwiz.jhf.ratectrl.traderateagent.bussiness.RateGenerator;
import cn.bestwiz.jhf.ratectrl.traderateagent.bussiness.TradeRateAgentChecker;

/**
 * RateManagerListener
 * (1).接收CP的汇率
 * 
 * (2).对CP的汇率进行背离检查
 * 
 * (3).如果背离通过，根据FxSpotRateConfig和FxSpread的设置产生FxSpotRateInfo， 
 *     然后发送汇率给RateCache，opm,tso,chartgenerator
 *     
 * (4).最终发出去的汇率的位数必须是fx_product中定义的，舍入做法也是由fx_product中定义的。
 * 
 * @author zuolin <zuolin@bestwiz.cn>
 * 
 * @copyright 2006, BestWiz(Dalian) Co.,Ltd
 * 
 */
public class TradeRateAgentListener {
	
    private final static Log m_log = LogUtil.getLog(TradeRateAgentListener.class);
    private static TradeRateAgent m_tradeRateAgent = TradeRateAgent.getInstance();
    private static TradeRateAgentChecker m_rateChecker = m_tradeRateAgent.getRateChecker();
    private static RateGenerator m_rateGenerator = m_tradeRateAgent.getRateGenerator();
    private static InnerRateCache m_innerRateCache = m_tradeRateAgent.getInnerRateCache();
    private static CoreService m_coreService = null;
    private static ConfigService m_configService = null;
    public  static boolean m_initFlag = false;
    private final static String PROPS_FILE_NAME = "rateAgent_thread_pool.properties";
    private long period = 0;
    private volatile boolean isWork = true;
    private List<String> currencyPairList = null;

    
    public TradeRateAgentListener()throws Exception{
    	Properties props = PropertiesLoader.getProperties(PROPS_FILE_NAME);
    	m_coreService = ServiceFactory.getCoreService();
    	m_configService = ServiceFactory.getConfigService();
    	currencyPairList=initialize(props);
    	period = Long.parseLong(props.getProperty("rateagent.period"));
    }
	 
	public void listen(){
		for (String ccp : currencyPairList) {
			new Thread(this.new RateAgtJob(ccp)).start();			
		}
		
	}
	
	class RateAgtJob implements Runnable{
		private String currencyPair;
		public RateAgtJob(String ccp){
			this.currencyPair = ccp;
			m_log.info("RateAgtJob Running  CCP:"+ccp);
		}
		public void run() {
			while(isWork){
				try {
					Thread.sleep(period);
				} catch (InterruptedException e1) {
					m_log.info("RateAgtJob thread was Interrupted CCP:"+currencyPair);
					continue;
				}

				long startTime = System.currentTimeMillis();
				try {
					// 如果市场关闭直接返回
					if (!m_coreService.isMarketOpen()) {
						m_log.info("====== Market Is Closed: ");
						//TODO:market close期间时间是否减缓处理速度
						continue;
					}
					
					Map<String,CpSpotRateInfo> cacheMap = CpRateCacheFactory
						.getCpRatecacheForDealer().getRatebyCurrencyPairWithParityCheck(currencyPair);

					if(cacheMap.isEmpty()){
						continue;
					}
					/** 用于背离检查的汇率 */
	//					m_innerRateCache.putParityCheckRate(receivemsg);
	//					if (!m_rateChecker.parityCheck4Trader(receivemsg)) {
	//						m_log.info("parityCheck4Trader is false " + receivemsg.toString());
	//						return;
	//					}
					JhfStatusContrl jhfStatusContrl = m_coreService.obtainStatusContrl(currencyPair);
					if (jhfStatusContrl == null) {
			    		m_log.error("CurrencyPair dosnt exist in  JhfStatusContrl " + currencyPair);
			    		continue;
		        	}
					CpSpotRateInfo[] cpSpotRateInfos = null;
					if (jhfStatusContrl.getBestFeedSwatich().intValue() == BoolEnum.BOOL_YES_ENUM.getValue()) {
	//						/** 汇率按BestFeed的检查进行处理 */
	//						if(jhfStatusContrl.getFeedGiveupWeightSwitch().intValue()== BoolEnum.BOOL_YES_ENUM.getValue()){
	//							if(!m_coreService.isPrimaryBank(receivemsg.getCounterPartyId())){
	//								RateBandInfo rateBandInfo =  receivemsg.getAskBandInfo(0);
	//								rateBandInfo.setRate(rateBandInfo.getRate().add(jhfStatusContrl.getGiveupWeightAsk()));
	//								rateBandInfo =  receivemsg.getBidBandInfo(0);
	//								rateBandInfo.setRate(rateBandInfo.getRate().add(jhfStatusContrl.getGiveupWeightBid()));
	//							}
	//						}
						
						
	//						m_innerRateCache.putCpRateInfo(receivemsg);
	//						List<CpSpotRateInfo> lstBestFeed = m_innerRateCache.getCpRateInfo(receivemsg.getCounterPartyId(), receivemsg.getCurrencyPair());
						List<CpSpotRateInfo> lstBestFeed = new ArrayList<CpSpotRateInfo>(cacheMap.values());
						
						if (lstBestFeed != null && lstBestFeed.size() > 0) {
							/** 需要选择 bestfeed 汇率 否则用本次收到的汇率 */
	//							lstBestFeed.add(receivemsg);
							/** 取得最优汇率 返回null本次没有得到最优的汇率  */
							cpSpotRateInfos = m_rateChecker.getBestFeed(lstBestFeed, currencyPair,jhfStatusContrl);
							if(cpSpotRateInfos ==null ){
								m_log.info("=== [TradeRateAgentListener] ===  lstBestFeed isEmpty ,no cp rate can bestfeed "+currencyPair);
								continue;
							}
							for(CpSpotRateInfo cpSpotRateInfo:cpSpotRateInfos){
								if ((cpSpotRateInfo == null) || (!m_configService.obtainCounterparty(cpSpotRateInfo.getCounterPartyId()))) {
									m_log.info("=== [TradeRateAgentListener] ===  bestfeed already send or cp not running!!"+(cpSpotRateInfo==null?currencyPair:cpSpotRateInfo));
									continue;
								}
							}
						}
	//						else{
	//							if(jhfStatusContrl.getBestfeedUseuncheckcpWhenCheckedcpng().intValue()==BoolEnum.BOOL_NO_ENUM.getValue()
	//								&& !RatectrlService.canBestfeed(currencyPair,receivemsg.getCounterPartyId())){
	//								m_log.info("=== [TradeRateAgentListener] === There is only one cprate,and it can not bestfeed"+receivemsg);
	//								continue;
	//							}else{
	//								cpSpotRateInfos = new CpSpotRateInfo[]{receivemsg};
	//							}
	//						}
					} else {
						String currentCounterPartyID = getCurrentCounterPartyID(jhfStatusContrl);
						if (!m_rateChecker.commonCheck(currencyPair,currentCounterPartyID,cacheMap,jhfStatusContrl)) {
	//							m_log.info("CommonCheck False!"+currencyPair+","+receivemsg.getFxPriceId());
							m_log.info("CommonCheck False!"+currencyPair+",");
							continue;
						}else{
							cpSpotRateInfos = new CpSpotRateInfo[]{cacheMap.get(currentCounterPartyID) };
							m_log.info("*******************************************************");
						}
					}
					long expirTime = TypeConverter.toLong(m_coreService.obtainAppProperty(AppPropertyType.RATE, AppPropertyKey.agentExpirationQuoteTime));
					for(CpSpotRateInfo cpSpotRateInfo:cpSpotRateInfos){
						if ((System.currentTimeMillis() - cpSpotRateInfo.getMessageTime().getTime()) > expirTime) {
							m_log.info("===Rate Time OverFlow===" + cpSpotRateInfo);
							continue;
						}
					}
	//					String sCurrencyPair = receivemsg.getCurrencyPair();
									
					//cpSpotRateInfo；转换成 FxSpotRateInfo,加Spread值
					FxSpotRateInfo fxSpotRateInfo = m_rateGenerator.convert(cpSpotRateInfos);
	
					if (fxSpotRateInfo == null) {
						continue;
					}
					boolean stopSendFlag = ManualDynSpreadHandler.addManualDynSpread(currencyPair,fxSpotRateInfo);
					if(stopSendFlag){
						for(CpSpotRateInfo cpSpotRateInfo:cpSpotRateInfos){
							m_innerRateCache.putRateMessageTime(cpSpotRateInfo.getCounterPartyId(),currencyPair, cpSpotRateInfo.getMessageTime());
						}
						m_innerRateCache.putReferenceRate(currencyPair, fxSpotRateInfo);
						m_log.info("stopchange or costrate,innerRateCache "+fxSpotRateInfo);
						continue;
					}
					
					/** 取得 old rateinfo 与新的汇率判断是否超过rateSendMinTime */
					FxSpotRateInfo lastFxSpotRateInfo = m_innerRateCache.getRateInfo(currencyPair);
					if (null != lastFxSpotRateInfo) {
						long sendMinTime = Long.valueOf(m_coreService.obtainAppProperty(AppPropertyType.RATE, AppPropertyKey.rateSendMinTime));
							if (fxSpotRateInfo.getReferenceTime().getTime() - lastFxSpotRateInfo.getReferenceTime().getTime() <= sendMinTime) {
								m_log.info("smaller than " + sendMinTime +"," + currencyPair+","+fxSpotRateInfo.getPriceId() + ",new rt:"
										+ fxSpotRateInfo.getReferenceTime() + ",old rt:" + lastFxSpotRateInfo.getReferenceTime());
								m_innerRateCache.putNewRateInfo(currencyPair, fxSpotRateInfo);
								continue;
						}
					}
					for(CpSpotRateInfo cpSpotRateInfo:cpSpotRateInfos){
						Date messageTime = m_innerRateCache.getRateMessageTime(cpSpotRateInfo.getCounterPartyId(), currencyPair);
						if(messageTime!=null){
							if(cpSpotRateInfo.getMessageTime().getTime()<messageTime.getTime()){
								m_log.info("smaller lastTime:"+cpSpotRateInfo.getCurrencyPair()+","+cpSpotRateInfo.getFxPriceId());
								continue;
							}
						}
					}
					for(CpSpotRateInfo cpSpotRateInfo:cpSpotRateInfos){
						m_innerRateCache.putRateMessageTime(cpSpotRateInfo.getCounterPartyId(),currencyPair, cpSpotRateInfo.getMessageTime());
					}
					fxSpotRateInfo.setSendFrom("RateAgent");
					m_tradeRateAgent.doSendRate(fxSpotRateInfo,false);
					m_log.info("S:"+(System.currentTimeMillis()-startTime)+","+fxSpotRateInfo.getCurrencyPair() +","+fxSpotRateInfo.getPriceId());
				} catch (Exception e) {
					m_log.error("=== [TradeRateAgentListener] === onMessage Errors "+currencyPair, e);
				}
			}
		}
	}
	
    
    /**
	 * 关闭线程池等待现有线程全部完成后,退出
	 * 
	 * @throws Exception
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
    public void close() throws Exception { 
    	isWork = false;
    }
    
   
    
	/**
	 * 读取配置文件，初始化
	 * 
	 * @author houdg
	 * @param propsFromFile 从配置文件里读取的属性信息
	 * @return 返回本模块跟踪的货币对信息
	 */
	private static List<String> initialize(Properties propsFromFile) {
		List<String> currencyPairList = null;
		try {

			String currencyPairFromFile = propsFromFile
					.getProperty("rateagent.currencyPairID");
			currencyPairList = getCurrencyPair(currencyPairFromFile);
			m_log.info(currencyPairList.size()
					+ " currency pairs for triggerPool are : "
					+ currencyPairList.toString());
		} catch (NumberFormatException e) {
			m_log.error("[AutoTriggerFactory.initialize()]"
					+ " LIMITTIME or TIMETOSLEEP parseLong(String) error! ", e);
		} 
		m_log.info("Property file loading finished.");

		return currencyPairList;
	}

	/**
	 * 按照从properties读取的内容，格式化currency pair信息
	 * @author houdg
	 * @param properties 从配置文件里读取的属性信息
	 * @return 在配置文件定义的全部货币对
	 */
	private static List<String> getCurrencyPair(String properties) {
		// 访问数据库，获得全部可用货币对列表
		List<String> listDB = new ArrayList<String>();
		try {
			listDB = ServiceFactory
					.getProductService()
					.obtainCurrencyPairNames(
							CurrencyPairWhoUseEnum.CUSTOMER_USE_ENUM, false);
			m_log.debug("Currency pair informations loaded from Database.");
		} catch (Exception e) {
			m_log.error("[AutoTriggerFactory.getCurrencyPair()] error!", e);
		}

		// 获得配置文件定义列表
		String regEx = "([A-Z]{3}|[*])/([A-Z]{3}|[*])";
		properties = properties.toUpperCase().replace("ALL", "*/*");
		m_log.debug(properties);
		Pattern po = Pattern.compile(regEx.toUpperCase());
		Matcher mo = po.matcher(properties.toUpperCase());
		List<String> list = new ArrayList<String>();
		while (mo.find()) {
			list.add(mo.group());
		}
		
		// 获得配置文件与数据库定义的交集
		regEx = list.toString().replace(", ", "|").replace("[", "")
				.replace("]", "").replace("*", "[A-Z]{3}");
		po = Pattern.compile(regEx);
		mo = po.matcher(listDB.toString());
		list.clear();
		while (mo.find()) {
			list.add(mo.group());
		}
		return list;
	}
	
	
	/**prifeed模式下，取得当前负责发送汇率银行的id
	 * @param jhfStatusContrl
	 * @return
	 * @throws CoreException
	 */
	private String getCurrentCounterPartyID(JhfStatusContrl jhfStatusContrl) throws CoreException{
		String counterpartyId = null;
		if(m_configService.obtainCounterparty(jhfStatusContrl.getPriFeedSrc())){
			counterpartyId = jhfStatusContrl.getPriFeedSrc();
		}else if(m_configService.obtainCounterparty(jhfStatusContrl.getSecFeedSrc())){
			counterpartyId = jhfStatusContrl.getSecFeedSrc();
		}else{
			counterpartyId = m_configService.obtainAllCounterparty();
		}
		return counterpartyId;
		
	}
}

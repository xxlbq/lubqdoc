package cn.bestwiz.jhf.ratectrl.traderateagent.bussiness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.logging.Log;

import cn.bestwiz.jhf.core.bo.enums.AppPropertyKey;
import cn.bestwiz.jhf.core.bo.enums.AppPropertyType;
import cn.bestwiz.jhf.core.bo.enums.BestFeedTypeEnum;
import cn.bestwiz.jhf.core.bo.enums.BoolEnum;
import cn.bestwiz.jhf.core.bo.enums.FeedPriorityEnum;
import cn.bestwiz.jhf.core.bo.enums.ParityCheckBoolEnum;
import cn.bestwiz.jhf.core.bo.enums.SysMailActionIdEnum;
import cn.bestwiz.jhf.core.configcache.ConfigService;
import cn.bestwiz.jhf.core.dao.bean.main.JhfCounterparty;
import cn.bestwiz.jhf.core.dao.bean.main.JhfStatusContrl;
import cn.bestwiz.jhf.core.jms.bean.CpSpotRateInfo;
import cn.bestwiz.jhf.core.jms.bean.FxSpotRateInfo;
import cn.bestwiz.jhf.core.jms.bean.FxSpotRateInfoMap;
import cn.bestwiz.jhf.core.jms.bean.RateBandInfo;
import cn.bestwiz.jhf.core.service.CoreService;
import cn.bestwiz.jhf.core.service.ServiceFactory;
import cn.bestwiz.jhf.core.service.exception.CoreException;
import cn.bestwiz.jhf.core.util.DataLogger;
import cn.bestwiz.jhf.core.util.LogUtil;
import cn.bestwiz.jhf.core.util.PropertiesLoader;
import cn.bestwiz.jhf.ratectrl.bean.BestFeedBean;
import cn.bestwiz.jhf.ratectrl.bussiness.ParityChecker;
import cn.bestwiz.jhf.ratectrl.bussiness.SysMailSender;
import cn.bestwiz.jhf.ratectrl.exceptions.RateCheckerException;
import cn.bestwiz.jhf.ratectrl.service.RatectrlService;
import cn.bestwiz.jhf.ratectrl.traderateagent.TradeRateAgent;
/**
 * RateManagerChecker
 * (1).对CP汇率进行有效性检查
 * 
 * (2).对CP汇率进行有效性检查通过的发送到TradeRateAgent中
 * 	   如果没有通过log记录下来,直接返回.
 * 
 * (3).对CP汇率进行Trade的背离检查
 * 		
 * (4).对CP汇率进行Trade的背离检查通过的发送到RateCache中
 *     如果没有通过log记录下来,并发送邮件.
 * 
 * @author  zuolin <zuolin@bestwiz.cn> 			
 * 				
 * @copyright 2006, BestWiz(Dalian) Co.,Ltd		
 * 	
 */
public class TradeRateAgentChecker {
	private TradeRateAgentChecker(){};
	private static TradeRateAgentChecker m_instance = new TradeRateAgentChecker();
	public static TradeRateAgentChecker getInstance(){
		return m_instance;
	}
    private final static Log m_log = LogUtil.getLog(TradeRateAgentChecker.class);
    // 定义系统的log
    private DataLogger m_tradeParityCheckLog = null;
    
    private static HashMap<String, Integer> m_mapTreaderPparityTable = new HashMap<String, Integer>();
    
    private Map<String, Map<String, String>> m_mapIsSend = (Map<String, Map<String, String>>)
    					Collections.synchronizedMap(new HashMap<String, Map<String, String>>());
    private ParityChecker  m_checker4Trader  = null ;
    private SysMailSender m_sysMailSender = null;
    private final static CoreService coreService = ServiceFactory.getCoreService();
    private final static ConfigService configService = ServiceFactory.getConfigService();
    
    // 定义trade背离检查的属性文件名
    private final static String TRADER_CHECK_FILE_NAME = "trader_check.properties";
    private Properties m_treaderProp = null;
    private static int TRADE_PARITY_CHECK_COUNT_INT;
    
    private static TradeRateAgent m_tradeRateAgent = TradeRateAgent.getInstance();  
    private static InnerRateCache m_innerRateCache = m_tradeRateAgent.getInnerRateCache();
    
    private Map<String, Integer> m_mapSendMail = 
    	(Map<String, Integer>) Collections.synchronizedMap(new HashMap<String, Integer>());
    
    private static CoreService m_coreService = ServiceFactory.getCoreService();
    
    /**初始化*/
	public void initlize(int iMoudleName) throws RateCheckerException {
        try {
        	m_sysMailSender = m_tradeRateAgent.getSysMailSender();
        	
    		// trade相关的log定义
        	m_tradeParityCheckLog = DataLogger.getInstance("TradeParityCheck");
    		// 取得trader属性的定义
    		m_treaderProp = PropertiesLoader.getProperties(TRADER_CHECK_FILE_NAME);
    		// 发送系统Mail的条件
    		TRADE_PARITY_CHECK_COUNT_INT = Integer.parseInt(m_treaderProp.getProperty("PARITY_CHECK_COUNT"));
    		// 初始化Treader Checker
    		m_checker4Trader = ParityChecker.getInstance(TRADER_CHECK_FILE_NAME,iMoudleName);   
        } catch (Exception e){
            m_log.error(" initlize() Errors :");
            throw new RateCheckerException(e);
        }
	}
	
	
	/**
	 * bestfeed开关off的情况下检查汇率的银行
	 * 根据CounterpartyCurrencypair表设置判断是否可以对顾客bestfeed
     * 判断汇率的银行与当前负责发送汇率的银行是否一致
	 * 如果PriFeedSrcType为manual作为不一致处理（SEC_FEED_SRC为manual的情况不存在）
	 * 如果找不到当前负责发送汇率的银行（全部不在运行状态）设置汇率(trable = false, inEmgerencyStatus = true,usualable = false )
	 * 并重新汇率发到ratecache中
	 * @param currencyPair
	 * @param counterPartyId
	 * @param jhfStatusContrl
	 * @return
	 * @throws RateCheckerException
	 */
	public  boolean commonCheck(String currencyPair,String currentCounterPartyID,Map<String,CpSpotRateInfo> cacheMap,JhfStatusContrl jhfStatusContrl) throws RateCheckerException {
        try {
        	if (jhfStatusContrl.getPriFeedSrcType().intValue() == FeedPriorityEnum.FEED_MANUAL_ENUM.getValue()) {
                m_log.info(" PriFeedSrcType is manual， receiver dealer rate!"+currencyPair);
        		return false;
        	} 
        	
        	
        	if (currentCounterPartyID == null) {
                m_log.info("InManualStatus: Banks All Down !");
                sendInManualStatusMessage();
        		return false;
        	}
        	if(!cacheMap.containsKey((currentCounterPartyID))){
        		return false;
        	}
			 if(!RatectrlService.canBestfeed(currencyPair,currentCounterPartyID)){
				 return false;
			 }
        	return true;
		} catch (Exception e) {
			m_log.error(" commonCheck() Errors : ",e);
	        throw new RateCheckerException(e);
		}
	}

	
	/**
	 * 对Trader的背离检查
	 * (1).trader_check.properties中判断是否进行Dealer的背离检查(0 return ture; 1 return false )
	 * 
	 * (2).调用ParityChecker.parityCheck进行背离检查
	 * 
	 * (3).CP汇率的背离检查没有通过,记录log,并发送系统邮件;通过则发返回true;
	 *  
	 * */
	public  boolean parityCheck4Trader( CpSpotRateInfo cpSpotRateInfo ) throws Exception{
      // call checker4Traler to paritycheck
		int value = Integer.parseInt(coreService.obtainAppProperty(AppPropertyType.BUGTICK, AppPropertyKey.TRADERATEAGENT));
        if ( value == ParityCheckBoolEnum.BOOLEAN_PARITYCHECK_NO.getValue()) {
            return true;
        }
      
      try {
          List lstResults = null;
          boolean bParityCheckFlag = false;
          BigDecimal bdLastMid = null;
          BigDecimal bdVolatility = null;
          BigDecimal bdSwatchVolatility = null;
          
          lstResults = m_checker4Trader.parityCheck(cpSpotRateInfo);
          
          String sCurrencypair = cpSpotRateInfo.getCurrencyPair();
          bParityCheckFlag = (Boolean) lstResults.get(0);
          bdLastMid = (BigDecimal) lstResults.get(1);
          bdVolatility = ((BigDecimal) lstResults.get(2)).setScale(10,
                  BigDecimal.ROUND_HALF_UP);
          bdSwatchVolatility = ((BigDecimal) lstResults.get(6)).setScale(10,
                  BigDecimal.ROUND_HALF_UP);
          
          
          // 如果返回值为false 并且midRate为0　返回false
          if (!bParityCheckFlag && bdLastMid.compareTo(new BigDecimal(0)) == 0) {
      		m_tradeParityCheckLog.write(" ==== [Parity Check Swatch Not Full] ==== "
      				+ cpSpotRateInfo);
        	// TODO 需要分别记日志?
        	// 写日志
        	m_log.error(" CurrencyPair : " + cpSpotRateInfo.getCurrencyPair()
                    + "Parity Check Swatch Not Full , Please Check it ");
            return false;
          }
          
          if(!bParityCheckFlag){
	    	// TODO 需要分别记日志?
	    	// expire time check false
	    	// 背离检查不通过,写日志
  	    	m_tradeParityCheckLog.write( "==== [parityCheck Check False] ===="
	    	        + "\nCurrencyPair=" + cpSpotRateInfo.getCurrencyPair()
					+ " ; " + "\nCounterParty=" + cpSpotRateInfo.getCounterPartyId()
					+ " ; " + "\nFxPriceId=" + cpSpotRateInfo.getFxPriceId() + " ; "
					+ "\nMID=" + RateGenerator.getMidRate(cpSpotRateInfo) + " ; "
					+ "\nbdVolatility = " + bdVolatility + " ; "
					+ "\nbdSwatchVolatility = " + bdSwatchVolatility + " ; "
					+ "\nbdLastMid = " + bdLastMid + ";"
					+ "\ncpSpotRateInfo = " + cpSpotRateInfo.toGrossString()
					+ "\nFeedTime=" + new Date());
	    	
	   	   //判断PARITY_CHECK_COUNT_INT(trader的配置文件)再发邮件
 		   if (m_mapTreaderPparityTable.containsKey(sCurrencypair)) {
 		      Integer iParityCount = m_mapTreaderPparityTable
 		              .get(sCurrencypair);
 		
 		      iParityCount = new Integer(iParityCount.intValue() + 1);
 		
 		      m_mapTreaderPparityTable.put(sCurrencypair, iParityCount);
 		
 		   } else {
 			  m_mapTreaderPparityTable.put(sCurrencypair, new Integer(1));
 		   }
 		   
		   if (( m_mapTreaderPparityTable.get(sCurrencypair)).intValue() > TRADE_PARITY_CHECK_COUNT_INT) {
 		      // send parity mail to service
 		      m_sysMailSender.sendTraderParityNGMail(SysMailActionIdEnum.PARITY_NG_MAIL_FOR_TRADER_ENUM.getName(), 
 		              cpSpotRateInfo, bdLastMid, bdVolatility, bdSwatchVolatility);
 		     m_mapTreaderPparityTable.put(sCurrencypair, Integer.valueOf(0));
 		   }
 	  		return false;
 	  		
  	    } else {
	    	// 通过背离检查
  	    	m_mapTreaderPparityTable.put(sCurrencypair, Integer.valueOf(0));
	        return true;
  	    }
      } catch (Exception e) {
      	m_log.error("====== TradeRateAgentChecker.parityCheck4Dealer() Errors : ");
      	throw new RateCheckerException(e);
      }
	}
	
	
	 /**关闭*/
	public void close() {
		// close checker4Trader
        if (null != m_checker4Trader) {
            m_checker4Trader.close();
            m_checker4Trader = null;
        }
        if (null != m_mapTreaderPparityTable) {
        	m_mapTreaderPparityTable.clear();
        	m_mapTreaderPparityTable = null;
        }
        DataLogger.clear();
	}
	
	/**
	 * 如果银行全部挂掉,发JMS消息及MAIL
	 * */
	@SuppressWarnings("unchecked")
	private void sendInManualStatusMessage() {
		//银行全部挂掉，发消息
		FxSpotRateInfo fxSpotRateInfo = null;
		String sCacheCurrencyPair = null;
		try {
			List<FxSpotRateInfo> lstAllRate = m_innerRateCache.getAllRateInfo();
			for (int i = 0,size = lstAllRate.size(); i < size; i++) {
			    fxSpotRateInfo = lstAllRate.get(i);
                sCacheCurrencyPair = fxSpotRateInfo.getCurrencyPair();
                fxSpotRateInfo.setTradableFlag(false);
                fxSpotRateInfo.setUsualable(false);
				m_innerRateCache.putRateInfo(sCacheCurrencyPair, fxSpotRateInfo);
				// 修改完状态后，发JMS消息
				FxSpotRateInfoMap mapSpotRate = RateGenerator.getInstance().convert(fxSpotRateInfo);
				m_tradeRateAgent.getToCustomerRateTopicSender().sendMessage(mapSpotRate);
			}
			
			/** 发送邮件需要增加一个邮件模板发送发运营人员和CS人员和dealer人员 
			 *  与日方确认在同一个frontdate内只发一封邮件,所以修改逻辑
			 * */ 
			String sFrontDate = coreService.getFrontDate();
			
			if (m_mapSendMail.containsKey(sFrontDate)) {
				// 当前frontDate已经发完邮件,不需要在发送
			} else {
				// 发送邮件并且保留发送信息
				m_mapSendMail.clear();
				m_mapSendMail.put(sFrontDate, Integer.valueOf(0));
				m_sysMailSender.sendBankInEmergencyStatusMail();
			}
			
		} catch(Exception e) {
			m_log.error("sendInManualStatusMessage Errors : ", e);
		}
	}
    
	/**
	 * 取得最优汇率进行处理后继处理,如果没有返回null,不进行后继的加spread等处理
	 * BestFeedType = CP时 根据spread和其他权重返回一个CpSpotRateInfo
	 * 否则根据bid 和 ask 值返回两个CpSpotRateInfo，CpSpotRateInfo[0]为按照bid选出的，CpSpotRateInfo[1]为按照ask选出的
	 * @param  List<CpSpotRateInfo> lstInfo
	 * @param   String currencyPair
	 * */
	public CpSpotRateInfo[] getBestFeed(List<CpSpotRateInfo> lstInfo, String currencyPair,JhfStatusContrl jhfStatusContrl) throws Exception {
		List<BestFeedBean> lstBestFeed = new ArrayList<BestFeedBean>();
		List<BestFeedBean> tempList = new ArrayList<BestFeedBean>();
		for (int i = 0, size = lstInfo.size(); i < size; i++) {
			boolean tradable = true;
			CpSpotRateInfo tempInfo = lstInfo.get(i);
			
			/** 汇率按BestFeed的检查进行处理 */
			if(jhfStatusContrl.getFeedGiveupWeightSwitch().intValue()== BoolEnum.BOOL_YES_ENUM.getValue()){
				if(!m_coreService.isPrimaryBank(tempInfo.getCounterPartyId())){
					RateBandInfo rateBandInfo =  tempInfo.getAskBandInfo(0);
					rateBandInfo.setRate(rateBandInfo.getRate().add(jhfStatusContrl.getGiveupWeightAsk()));
					rateBandInfo =  tempInfo.getBidBandInfo(0);
					rateBandInfo.setRate(rateBandInfo.getRate().add(jhfStatusContrl.getGiveupWeightBid()));
				}
			}
			
			String counterPartyId = tempInfo.getCounterPartyId();
			/** 判断银行是否可用 如果不可用,continue */
			if (!configService.obtainCounterparty(counterPartyId)) {
				m_log.info("This bank : " + tempInfo.getCounterPartyId() + " was downed");
				/** cp down之后 清空cpRateCache中该cp对应的汇率信息 added by mengfj 2007/12/17 */
				m_innerRateCache.clearCpRateMap(counterPartyId);
				/** add end */
				continue;
			}
			BestFeedBean bean = new BestFeedBean();
			bean.setCounterPartyId(counterPartyId);
			bean.setSpreadValue(tempInfo.getAskBandInfo(0).getRate().subtract(tempInfo.getBidBandInfo(0).getRate()));
			bean.setMessageTime(tempInfo.getMessageTime());
			/**
			 * 
			 * 注意:这里的UNTRADABLE, 以下3个种情况的汇率都算作UNTRADABLE: Tradable=false
			 * Validity時刻超過時 Price=Null
			 */
			if ((!tempInfo.isUsualable()) || (!this.getTradable(tempInfo)) || (!this.checkRatePriceNotNull(tempInfo))) {
				tradable = false;
			}
			bean.setTradable(tradable);
			bean.setFeedWeightPosi(configService.getCounterpartyCurrencypair(counterPartyId, currencyPair).getFeedPriortiy().intValue());
			bean.setCpSpotRateInfo(tempInfo);
			bean.setPriceId(tempInfo.getFxPriceId());
			bean.setAskValue(RateGenerator.getAskRate(tempInfo));
			bean.setBidValue(RateGenerator.getBidRate(tempInfo));
			if(RatectrlService.canBestfeed(currencyPair,counterPartyId)){
				lstBestFeed.add(bean);
			}else{
				tempList.add(bean);
			}
		}
		if((jhfStatusContrl.getBestfeedUseuncheckcpWhenCheckedcpng().intValue()==BoolEnum.BOOL_YES_ENUM.getValue()) && lstBestFeed.isEmpty()){
			lstBestFeed.addAll(tempList);
		}
		if(lstBestFeed.isEmpty()){
			return null;
		}
		return calculatingPoints(lstBestFeed, currencyPair);
	}
	
	/**判断是否银行无效, 
	 * 如果是则判断该货币对是否还有bestFeed权重相同的其他银行的汇率,
	 * 如果有且银行有效则返回该汇率否则返回null
	 */
	private CpSpotRateInfo checkBidCp(List<BestFeedBean> lstBestFeed) throws Exception {
		CpSpotRateInfo retInfo = null;
		int mostHighTotalBidPoint = lstBestFeed.get(0).getTotalBidPoint();
		for (int i = 0, size = lstBestFeed.size(); i < size; i++) {
			BestFeedBean bean = lstBestFeed.get(i);
			if (mostHighTotalBidPoint != bean.getTotalBidPoint()) {
				break;
			}
			if ((configService.obtainCounterparty(bean.getCounterPartyId()))) {
				retInfo = bean.getCpSpotRateInfo();
				break;
			}
		}
		return retInfo;
	}
	
	/**判断是否银行无效, 
	 * 如果是则判断该货币对是否还有bestFeed权重相同的其他银行的汇率,
	 * 如果有且银行有效则返回该汇率否则返回null
	 */
	private CpSpotRateInfo checkAskCp(List<BestFeedBean> lstBestFeed) throws Exception {
		CpSpotRateInfo retInfo = null;
		int mostHighTotalAskPoint = lstBestFeed.get(0).getTotalAskPoint();
		for (int i = 0, size = lstBestFeed.size(); i < size; i++) {
			BestFeedBean bean = lstBestFeed.get(i);
			if (mostHighTotalAskPoint != bean.getTotalAskPoint()) {
				break;
			}
			if ((configService.obtainCounterparty(bean.getCounterPartyId()))) {
				retInfo = bean.getCpSpotRateInfo();
				break;
			}
		}
		return retInfo;
	}
	/**判断是否已经发送或者银行无效, 
	 * 如果是则判断该货币对是否还有bestFeed权重相同的其他银行的汇率,
	 * 如果有且银行有效则返回该汇率否则返回null
	 */
	private CpSpotRateInfo checkCpAndAlreadySend(List<BestFeedBean> lstBestFeed,String currencyPair) throws Exception{
		CpSpotRateInfo retInfo = null;
		String priceId = null;
		String counterPartyId = null;
		String oldPriceId = null;
		Map<String, String> currMap = null;
		int mostHighTotalPoint =lstBestFeed.get(0).getTotalPoint();
		for (int i = 0, size = lstBestFeed.size(); i < size; i++) {
			BestFeedBean bean = lstBestFeed.get(i);
			if(mostHighTotalPoint!=bean.getTotalPoint()){
				break;
			}
			priceId = bean.getPriceId();
			counterPartyId = bean.getCounterPartyId();
			if (!m_mapIsSend.containsKey(counterPartyId)) {
				if ((configService.obtainCounterparty(counterPartyId))) {
					retInfo = bean.getCpSpotRateInfo();
					Map<String, String> tempMap = new HashMap<String, String>();
					tempMap.put(currencyPair, retInfo.getFxPriceId());
					m_mapIsSend.put(counterPartyId,tempMap);
					break;
				}
			} else {
				currMap = m_mapIsSend.get(counterPartyId);
				if ((!currMap.containsKey(currencyPair))) {
					if ((configService.obtainCounterparty(counterPartyId))) {
						retInfo = bean.getCpSpotRateInfo();
						currMap.put(currencyPair, retInfo.getFxPriceId());
						break;
					}
				} else {
					oldPriceId = currMap.get(currencyPair);
					if ((oldPriceId == null) || (!oldPriceId.equals(priceId))) {
						if ((configService.obtainCounterparty(counterPartyId))) {
							retInfo = bean.getCpSpotRateInfo();
							currMap.put(currencyPair, retInfo.getFxPriceId());
							break;
						}
					}
				}
			}
		}

		return retInfo;
	}
	/**
	 * totalpoint 排序器
	 */
	private Comparator<BestFeedBean> totalPointComparator = new Comparator<BestFeedBean>(){
		public int compare(BestFeedBean o1, BestFeedBean o2) {
			if (o2.getTotalPoint()-o1.getTotalPoint() ==0 ) {
				return spreadComparator.compare(o1, o2);
			}
			return o2.getTotalPoint()-o1.getTotalPoint();
		}
	};
	
	/**
	 * totalBidPoint 排序器
	 */
	private Comparator<BestFeedBean> totalBidPointComparator = new Comparator<BestFeedBean>(){
		public int compare(BestFeedBean o1, BestFeedBean o2) {
			if (o2.getTotalBidPoint()-o1.getTotalBidPoint() ==0 ) {
				return bidValueComparator.compare(o1, o2);
			}
			return o2.getTotalBidPoint()-o1.getTotalBidPoint();
		}
	};
	
	/**
	 * totalAskPoint 排序器
	 */
	private Comparator<BestFeedBean> totalAskPointComparator = new Comparator<BestFeedBean>(){
		public int compare(BestFeedBean o1, BestFeedBean o2) {
			if (o2.getTotalAskPoint()-o1.getTotalAskPoint() ==0 ) {
				return askValueComparator.compare(o1, o2);
			}
			return o2.getTotalAskPoint()-o1.getTotalAskPoint();
		}
	};
	
    /**
     * 取得的BidBandInfoList中Tradable
     * @param CpSpotRateInfo (csri)
     * @return boolean
     * @exception Exception
     * @author zuolin<zuolin@bestwiz.cn>
     */
    public boolean getTradable(CpSpotRateInfo csri) throws Exception {
        boolean bBidTradable = false;
        boolean bAskTradable = false;
        try {
            RateBandInfo bidinfo = (RateBandInfo) csri.getBidBandInfoList().get(0);
            RateBandInfo askinfo = (RateBandInfo) csri.getAskBandInfoList().get(0);
            bBidTradable = bidinfo.isTradable();
            bAskTradable = askinfo.isTradable();
            if (bBidTradable && bAskTradable) {
            	return true;
            } 
        } catch (Exception e) {
            m_log.error("=== [RateGenerator] ===  Compute Tradable Eorrors : ",e);
        }
        return false;
    }
    
    /**
     * 判断CpSpotRateInfo中的price是否为空
     * @param CpSpotRateInfo (csri)
     * @return boolean
     * @exception Exception
     * @author zuolin<zuolin@bestwiz.cn>
     */
    public boolean checkRatePriceNotNull(CpSpotRateInfo csri) {
        String BidRate = null;
        String AskRate = null;
        try {
            RateBandInfo bidinfo = (RateBandInfo) csri.getBidBandInfoList().get(0);
            RateBandInfo askinfo = (RateBandInfo) csri.getAskBandInfoList().get(0);
            BidRate = bidinfo.getPriceId();
            AskRate = askinfo.getPriceId();
            if (BidRate==null || AskRate==null) {
            	return false;
            } 
        } catch (Exception e) {
            m_log.error("checkRatePrice Eorrors : ",e);
            return false;
        }
        return true;
    }
    
	
    /**
     * 根据货币对判断哪些元素需要参与BestFeed的计算
     * BestFeedType = CP时 根据spread和其他权重返回一个CpSpotRateInfo
	 * 否则根据bid 和 ask 值返回两个CpSpotRateInfo，CpSpotRateInfo[0]为按照bid选出的，CpSpotRateInfo[1]为按照ask选出的

     * @param List<BestFeedBean> lstBestFeed
     * @param currencyPair
     * @exception Exception
     * @author zuolin<zuolin@bestwiz.cn>
     */
	private CpSpotRateInfo[] calculatingPoints(List<BestFeedBean> lstBestFeed, String currencyPair) throws Exception {
		JhfStatusContrl statusContrl = coreService.obtainStatusContrl(currencyPair);
		CpSpotRateInfo[] retInfo = null;
		List<JhfCounterparty> list = configService.getCounterparty();
		if(list==null){
			m_log.info("There is no cp in RunningStatus");
			return null;
		}
		int bankSize = list.size();
		if (statusContrl.getFeedWeightSwitch().intValue() == BoolEnum.BOOL_YES_ENUM.getValue()) {
			calculatingPtPoints(lstBestFeed, bankSize);
		}

		if (statusContrl.getFeedTradableSwitch().intValue() == BoolEnum.BOOL_YES_ENUM.getValue()) {
			calculatingTradablePoints(lstBestFeed, bankSize);
		}

		if (statusContrl.getFeedDiffergenSwitch().intValue() == BoolEnum.BOOL_YES_ENUM.getValue()) {
			calculatingDifferGenPoints(lstBestFeed, bankSize, statusContrl);
		}

		if (statusContrl.getBestFeedType().intValue() == BestFeedTypeEnum.CP) {
			if (statusContrl.getFeedSpreadSwitch().intValue() == BoolEnum.BOOL_YES_ENUM.getValue()) {
				calculatingSpreadPoints(lstBestFeed, bankSize);
			}
			retInfo = new CpSpotRateInfo[1];
			Collections.sort(lstBestFeed, totalPointComparator);
			retInfo[0] = checkCpAndAlreadySend(lstBestFeed, currencyPair);
		} else {
			if (statusContrl.getFeedSpreadSwitch().intValue() == BoolEnum.BOOL_YES_ENUM.getValue()) {
				calculatingBidOfferPoints(lstBestFeed, bankSize);
			}
			retInfo = new CpSpotRateInfo[2];
			Collections.sort(lstBestFeed, totalBidPointComparator);
			retInfo[0] = checkBidCp(lstBestFeed);
			Collections.sort(lstBestFeed, totalAskPointComparator);
			retInfo[1] = checkAskCp(lstBestFeed);
		}

		return retInfo;
	}
	
	/**
	 * A. FW.PT  =  （(CP个数+1)- CP的FeedWeightPosi）。  
	 * @param hedgeList
	 * @param lstBestFeed
	 * @param BankSize
	 */
	public void calculatingPtPoints(List<BestFeedBean> lstBestFeed ,int BankSize){
		for (BestFeedBean bean : lstBestFeed) {
            bean.setFeedWeightPoint(BankSize+1-bean.getFeedWeightPosi());
			bean.setTotalPoint(bean.getTotalPoint() + bean.getFeedWeightPoint());
			bean.setTotalBidPoint(bean.getTotalPoint());
			bean.setTotalAskPoint(bean.getTotalPoint());
		}
	}
	
	/**
	 * B. Spread.PT=  (CP个数+1)- CP的SPREAD的narrow顺序) × bankSize
	 *  CP的SPREAD的narrow顺序-->
	 *  SPREAD最窄的CP的〔CP的SPREAD的narrow顺序〕为1,
	 *  汇率次NARROW的CP的〔CP的SPREAD的narrow顺序〕为2,
	 *  依次类推,SPREAD相同则〔CP的SPREAD的narrow顺序〕相同。
	 * @param lstBestFeed
	 * @param bankSize
	 * @throws Exception
	 */
	private void calculatingSpreadPoints(List<BestFeedBean> lstBestFeed, int bankSize) throws Exception{
		Collections.sort(lstBestFeed, spreadComparator);
		m_log.debug("lstBestFeed(SpreadPoints) =" + lstBestFeed);
		int narrowNum = 1;
		for (int i = 0; i < lstBestFeed.size(); i++) {
			BestFeedBean bean = lstBestFeed.get(i);
			if (i!=0 && bean.getSpreadValue().compareTo(lstBestFeed.get(i - 1).getSpreadValue())!= 0) {
				bean.setSpreadPoint((bankSize + 1 - ++narrowNum) * bankSize);
			}else{
				bean.setSpreadPoint((bankSize + 1 - narrowNum) * bankSize);
			}
			bean.setTotalPoint(bean.getTotalPoint() + bean.getSpreadPoint());
		}
	}
	/**
	 * B. Spread.PT=  (CP个数+1)- CP的SPREAD的narrow顺序) × bankSize
	 *  CP的SPREAD的narrow顺序-->
	 *  SPREAD最窄的CP的〔CP的SPREAD的narrow顺序〕为1,
	 *  汇率次NARROW的CP的〔CP的SPREAD的narrow顺序〕为2,
	 *  依次类推,SPREAD相同则〔CP的SPREAD的narrow顺序〕相同。
	 * @param lstBestFeed
	 * @param bankSize
	 * @throws Exception
	 */
	private void calculatingBidOfferPoints(List<BestFeedBean> lstBestFeed, int bankSize) throws Exception{
		Collections.sort(lstBestFeed, bidValueComparator);
		m_log.debug("calculatingBidOfferPoints bidValueComparator sort lstBestFeed=" + lstBestFeed);
		int narrowNum = 1;
		for (int i = 0; i < lstBestFeed.size(); i++) {
			BestFeedBean bean = lstBestFeed.get(i);
			if (i!=0 && bean.getBidValue().compareTo(lstBestFeed.get(i - 1).getBidValue())!= 0) {
				bean.setBidPoint((bankSize + 1 - ++narrowNum) * bankSize);
			}else{
				bean.setBidPoint((bankSize + 1 - narrowNum) * bankSize);
			}
			bean.setTotalBidPoint(bean.getTotalBidPoint() + bean.getBidPoint());
			m_log.debug("calculatingBidOfferPoints bid sort BestFeedBean=" + bean);
		}
		Collections.sort(lstBestFeed, askValueComparator);
		m_log.debug("calculatingBidOfferPoints askValueComparator sort lstBestFeed=" + lstBestFeed);
		narrowNum = 1;
		for (int i = 0; i < lstBestFeed.size(); i++) {
			BestFeedBean bean = lstBestFeed.get(i);
			if (i!=0 && bean.getAskValue().compareTo(lstBestFeed.get(i - 1).getAskValue())!= 0) {
				bean.setAskPoint((bankSize + 1 - ++narrowNum) * bankSize);
			}else{
				bean.setAskPoint((bankSize + 1 - narrowNum) * bankSize);
			}
			bean.setTotalAskPoint(bean.getTotalAskPoint() + bean.getAskPoint());
			m_log.debug("calculatingBidOfferPoints ask sort BestFeedBean=" + bean);
		}
	}
	
	/**
	 * spread 排序器
	 */
	private Comparator<BestFeedBean> spreadComparator = new Comparator<BestFeedBean>(){
		public int compare(BestFeedBean o1, BestFeedBean o2) {
			return o1.getSpreadValue().compareTo(o2.getSpreadValue());
		}
	};
	
	/**
	 * spread 排序器
	 */
	private Comparator<BestFeedBean> bidValueComparator = new Comparator<BestFeedBean>(){
		public int compare(BestFeedBean o1, BestFeedBean o2) {
			return o2.getBidValue().compareTo(o1.getBidValue());
		}
	};
	
	/**
	 * spread 排序器
	 */
	private Comparator<BestFeedBean> askValueComparator = new Comparator<BestFeedBean>(){
		public int compare(BestFeedBean o1, BestFeedBean o2) {
			return o1.getAskValue().compareTo(o2.getAskValue());
		}
	};
	
	/**
	 * C. Tradable.PT = ( CP的汇率为tradable)?  1 : ( - CP个数*2)
 	 * 比如某一时刻DB和GS的汇率为TRADABLE,而BC为UNTRADABLE；则
 	 * DB的Tradable.PT ＝ 1
 	 * GS的Tradable.PT ＝ 1
 	 * BC的Tradable.PT = -3 ×2 = -6
	 *
 	 * 注意:这里的UNTRADABLE, 以下3个种情况的汇率都算作UNTRADABLE:
 	 * Tradable=false     
 	 * Validity時刻超過時 
 	 * Price=Null
	 *  
	 * @param hedgeList
	 */
	public void calculatingTradablePoints(List<BestFeedBean> lstBestFeed, int bankSize){
		for (BestFeedBean bean : lstBestFeed) {
            /**
             * C. Tradable.PT = ( CP的汇率为tradable)?  1 : ( - CP个数×CP个数 )
             * modified by mengfj 2008/02/18
             * */
            bean.setTradablePoint(bean.isTradable() ? 1 : bankSize * (bankSize * -1));
			bean.setTotalPoint(bean.getTotalPoint() + bean.getTradablePoint());
			bean.setTotalBidPoint(bean.getTotalPoint());
			bean.setTotalAskPoint(bean.getTotalPoint());
		}
	}
	
	
	/**
	 * D.设DifferGenSeq为DifferGen的汇率的新旧的顺序.
	 * 如果isTradable的汇率：
	 * 最新到达的汇率以及与之相比到达时间小与特定时间的DifferGenSeq为1;
	 * 其余的为bankSize * -1 * bankSize
	 * @param hedgeList
	 */
	public void calculatingDifferGenPoints(List<BestFeedBean> lstBestFeed, 
			int bankSize, JhfStatusContrl statusContrl){
		Collections.sort(lstBestFeed, differGenComparator);
		m_log.debug("lstBestFeed(DifferGenPoints) =" + lstBestFeed);
		long unit = statusContrl.getBestfeedDiffgenTimeunit().longValue();
		long standTime = lstBestFeed.get(0).getMessageTime().getTime();
		for (int i = 0; i < lstBestFeed.size(); i++) {
			BestFeedBean feed = lstBestFeed.get(i);
			if (statusContrl.getFeedTradableSwitch().intValue() == BoolEnum.BOOL_NO_ENUM.getValue()||feed.isTradable()){
					long thisQuoteTime = feed.getMessageTime().getTime();
					if (standTime - thisQuoteTime < unit){
	                    feed.setDiffGenPoint(1);
					}
	                else{
	                    feed.setDiffGenPoint((bankSize * -1) * bankSize);
	                }
			}else{
				feed.setDiffGenPoint((bankSize * -1) * bankSize);
			}
			feed.setTotalPoint(feed.getTotalPoint() + feed.getDiffGenPoint());
			feed.setTotalBidPoint(feed.getTotalPoint());
			feed.setTotalAskPoint(feed.getTotalPoint());
		}		
	}
	
	
	/**
	 * DifferGeneration 排序器
	 */
	private static Comparator<BestFeedBean> differGenComparator = new Comparator<BestFeedBean>(){
		public int compare(BestFeedBean o1, BestFeedBean o2) {
			return -o1.getMessageTime().compareTo(o2.getMessageTime());
		}
	};
	
}

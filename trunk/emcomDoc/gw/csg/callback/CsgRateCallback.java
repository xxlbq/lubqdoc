package cn.bestwiz.jhf.gws.csg.callback;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.logging.Log;

import cn.bestwiz.jhf.core.idgenerate.exception.IdGenerateException;
import cn.bestwiz.jhf.core.jms.bean.CpSpotRateInfo;
import cn.bestwiz.jhf.core.jms.bean.RateBandInfo;
import cn.bestwiz.jhf.core.jms.exception.JMSException;
import cn.bestwiz.jhf.core.util.DateHelper;
import cn.bestwiz.jhf.core.util.LogUtil;
import cn.bestwiz.jhf.gws.common.exception.FXMessageException;
import cn.bestwiz.jhf.gws.common.util.CheckPrice;
import cn.bestwiz.jhf.gws.common.util.GWDataLogger;
import cn.bestwiz.jhf.gws.common.util.IdGenerateFacade;
import cn.bestwiz.jhf.gws.common.util.Round;
import cn.bestwiz.jhf.gws.csg.CsgContext;
import cn.bestwiz.jhf.gws.gkgoh.GKGOHService;

import com.pst.bean.com.CustomerCCYPair;


public class CsgRateCallback extends CsgAbstractCallback{

	private static final Log log = LogUtil.getLog(CsgRateCallback.class);
	private static final String SEPARATE ="/" ;
	private GWDataLogger logger;
	
	public CsgRateCallback(int count) {
		super(count);
		logger = GWDataLogger.getInstance("CpRate");
	}
	

	
	public void dealItem(Object workItem) {
		
		log.info("CsgRateCallback fire :"+workItem);
//		System.out.println("TAKE FROM Q :"+((CustomerCCYPair)workItem).getOamount());
		CustomerCCYPair newRate = (CustomerCCYPair) workItem;
		logger.write(newRate);
		Date nowTime = new Date();
		
		if(nowTime.getTime() -  
				DateHelper.parseYmdHms( newRate.getTimeStamp()).getTime()   
				> CsgContext.getProp().getRateCheckInterval()){
			log.warn("QuoteMessage's dealy time is over" + newRate);
		}
		
		if( ! CsgContext.getProp().getPairs().containsKey(appendPair(newRate))){
			log.warn("Unknown Currency Pair Quotes Received! <===> " + appendPair(newRate));
			return;
		}
		
		boolean isValid = checkField(newRate);
		if(isValid){
			CpSpotRateInfo rateInfo = turnToCpSpotRateInfo(newRate);
			try {
				if (CheckPrice.bugRateCheck(rateInfo)) {
					GKGOHService.getInstance().getRateSender().sendRate(rateInfo);
				}
			} catch (FXMessageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}

	}



	private CpSpotRateInfo turnToCpSpotRateInfo(CustomerCCYPair newRate) {
		
		CpSpotRateInfo cpSpotRate = new CpSpotRateInfo();
		RateBandInfo askRateBandInfo = new RateBandInfo();
		RateBandInfo bidRateBandInfo = new RateBandInfo();
		
		boolean askTradeble = ((Byte)newRate.getOLiquidityFlg()) == 0 ? false : true ;
		boolean bidTradeble = ((Byte)newRate.getBLiquidityFlg()) == 0 ? false : true ;
		
		
		cpSpotRate.setCounterPartyId(CsgContext.getProp().getCounterpartyId());
		
		cpSpotRate.setUsualable(true);
		cpSpotRate.setCurrencyPair(appendPair(newRate));
		cpSpotRate.setMessageTime(DateHelper.getTodaysDate());

//		String ContractCurrency = fxCurrencyPair.substring(0, 3);
//		String CounterCurrency = fxCurrencyPair.substring(4, 7);

		{
			// askRateBandInfo 汇率买价信息初始化
			BigDecimal askprice = newRate.getOffer();
			BigDecimal askLiquidityAmount = Round.floor(newRate.getOamount(),0);
			
			askRateBandInfo.setContractCurrency(newRate.getCcy1());
			askRateBandInfo.setContractLowerAmount("1");
			askRateBandInfo.setContractUpperAmount(askLiquidityAmount);


			askRateBandInfo.setCounterCurrency(newRate.getCcy2());
			askRateBandInfo.setCounterLowerAmount("1");
			askRateBandInfo.setCounterUpperAmount(askLiquidityAmount.multiply(askprice));
//			askRateBandInfo.setPriceId();
			askRateBandInfo.setRate(askprice);

			askRateBandInfo.setTradable(askTradeble);// 该汇率是否可交易
			// askRateBandInfo.setValueDate(askRate.getSettlDate().getValue());
			cpSpotRate.setAskBandInfoList(askRateBandInfo);// 将汇率买价信息对象追加入本系统可识别的买价汇率信息列表对象
		}
		
		{
			// bidRateBandInfo 汇率卖价信息初始化
			BigDecimal bidprice = newRate.getBid();
			BigDecimal bidLiquidityAmount = Round.floor(newRate.getBamount(),0);
			
			bidRateBandInfo.setContractCurrency(newRate.getCcy1());// bid货币名称
			bidRateBandInfo.setContractLowerAmount("1");// bid最小数量
			bidRateBandInfo.setContractUpperAmount(bidLiquidityAmount);// bid最大数量，
			// 需从汇率中取得当前货币对的最大量
			// 。
			bidRateBandInfo.setCounterCurrency(newRate.getCcy2());// ask货币名称
			bidRateBandInfo.setCounterLowerAmount("1");// ask最小数量
			bidRateBandInfo.setCounterUpperAmount(bidLiquidityAmount.multiply(bidprice));
//			bidRateBandInfo.setPriceId(bidRate.getMDEntryID().getValue());
			bidRateBandInfo.setRate(bidprice);// 从汇率对象中取得汇率的交易值，置入汇率买价信息对象
			
			askRateBandInfo.setTradable(bidTradeble);// 该汇率是否可交易
			// bidRateBandInfo.setValueDate(bidRate.getSettlDate().getValue());
			cpSpotRate.setBidBandInfoList(bidRateBandInfo);// 将汇率卖价信息对象追加入本系统可识别的卖价汇率信息列表对象
		}
		
		try {
			cpSpotRate.setFxPriceId(IdGenerateFacade.getFxPriceIdForCSG());
		} catch (IdGenerateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// 生成并设置本系统可识别的汇率ID

		return cpSpotRate;
	}



	private String appendPair(CustomerCCYPair newRate) {
		return newRate.getCcy1() + SEPARATE + newRate.getCcy2();
	}



	private boolean checkField(CustomerCCYPair newRate) {
		
		Byte bliqu = newRate.getBLiquidityFlg();
		Byte oliqu = newRate.getOLiquidityFlg();
		
		if(newRate.getBid() == null || newRate.getOffer() == null 
				|| newRate.getBid().compareTo(BigDecimal.ZERO) <= 0 
				|| newRate.getOffer().compareTo(BigDecimal.ZERO) <= 0 ){
			log.info("Invalid Rate Price Value :" + newRate);
			return false;
		}
		
		if (newRate.getBid().compareTo(new BigDecimal(0)) != 1 || newRate.getOffer().compareTo(new BigDecimal(0)) != 1
				|| newRate.getOffer().compareTo(newRate.getBid()) == -1) {
			log.info("Invalid Rate Price Value :" + newRate);
			return false;
		}
		
		if(newRate.getBamount().compareTo(new BigDecimal(0)) <= 0 && newRate.getOamount().compareTo(new BigDecimal(0)) <= 0 
				|| bliqu.intValue() == 0 
				|| oliqu.intValue() == 0){
			
			log.info("Invalid LiquidityFlg or LiquidityAmount:" + newRate);
			return false;
		}
		
		return true;
	}
	
	

}

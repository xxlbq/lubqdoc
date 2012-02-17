package jp.emcom.adv.fx.completor.biz.executor.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import jp.emcom.adv.fx.completor.biz.AutoBuildInfo;
import jp.emcom.adv.fx.completor.biz.executor.ParExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bestwiz.jhf.core.application.impl.AbstractLifecycle;
import cn.bestwiz.jhf.core.bo.enums.DateKeyEnum;
import cn.bestwiz.jhf.core.dao.bean.main.JhfApplicationDate;
import cn.bestwiz.jhf.core.jms.bean.WLSpotRateInfo;
import cn.bestwiz.jhf.core.service.ServiceFactory;
import cn.bestwiz.jhf.core.util.CurrencyHelper;
import cn.bestwiz.jhf.core.wlratecache.RateCacheKey;
import cn.bestwiz.jhf.core.wlratecache.impl.WlRataCacheFactory;

public class DefaultParWLExecutor  extends AbstractLifecycle implements ParExecutor{

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultParWLExecutor.class);
	private final static String COUNTERCCY_JPY = "JPY";
	private ExecutorService executor;
	private WLRebuilder rebuilder;
	private int executorSize ;
	

	
	public ExecutorService getExecutor() {
		return executor;
	}

	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	public WLRebuilder getRebuilder() {
		return rebuilder;
	}

	public void setRebuilder(WLRebuilder rebuilder) {
		this.rebuilder = rebuilder;
	}

	public int getExecutorSize() {
		return executorSize;
	}

	public void setExecutorSize(int executorSize) {
		this.executorSize = executorSize;
	}

	@Override
	protected void doStart() throws Exception {
		
	}

	@Override
	protected void doStop(long timeout) throws Exception {
		
	}

	@Override
	public void execute(List<AutoBuildInfo> autoBuildInfos) throws Exception {
		Long usetime = 0L;
		List<Future<Long>> futures = new ArrayList<Future<Long>>();
		JhfApplicationDate date = ServiceFactory.getConfigService().getApplicationDate(DateKeyEnum.FRONT_DATE_KEY_ENUM.getName());
		for (AutoBuildInfo  info: autoBuildInfos) {
			final long now = System.currentTimeMillis();
			long rebuildTime = rebuilder.getRebuildTimeByClientId(info,date,60*1000);
			long eodDateTime = date.getFrontEndDatetime().getTime();
			if(inRebuildTime(now,rebuildTime,eodDateTime,info.getPositionAutoRebuildEndInterval().intValue())){//05:41
				//find using ccp ,and close rate
				List<RebuildTask> tasks = toRebuildTasks(info,date);
				for (RebuildTask rebuildTask : tasks) {
					Future<Long> f = executor.submit(rebuildTask);
					futures.add(f);
				}
			}else{
				LOGGER.info("clientId : {} not in rebuild time  rebuildTime :{}  ,eodDateTime : {} ,rebuildEndInterval:{} " ,
						new Object[]{info.getClientId(),rebuildTime,eodDateTime ,info.getPositionAutoRebuildEndInterval()});
				continue;
			}

		} 
		 
		for (Future<Long> future : futures) {
			Long time = future.get();
			usetime+= time;
		}
		LOGGER.info(" rebuiled usetime : {}",usetime);
	}

	private boolean inRebuildTime(long now,long rebuildTime,long eodDateTime,long EndInterval){
//		return now > rebuildTime  &&  now < eodDateTime - EndInterval; 
		//TODO:
		return true; 
	}

	private List<RebuildTask> toRebuildTasks(AutoBuildInfo info,JhfApplicationDate date) throws Exception {
		
		List<RebuildTask> tasks = new ArrayList<RebuildTask>();
		//find useing ccp
		List<String> usingCcp = rebuilder.findUsingCurrencyPair(info.getClientId());

		for (String ccp : usingCcp) {
			//find close rate
			WLSpotRateInfo rate = rebuilder.findCloseRate(info,ccp,date);
			
			if(rate == null){
				rate = WlRataCacheFactory.getRateCache().getRate(new RateCacheKey(ccp, info.getClientId()) );
				if(rate == null){
					LOGGER.info("no rate to rebuild  , currencyPair : {} clientId : {} ",new Object[]{info.getClientId(),ccp});
					continue;
				}
			}
			
			RebuildTask rt = null;
			final String counterCcy = CurrencyHelper.getCounterCurrencyCode(ccp);
			final String convertCcy = counterCcy + "/" + COUNTERCCY_JPY ;
			if(!counterCcy.equals(COUNTERCCY_JPY)) {
				WLSpotRateInfo convertRate = rebuilder.findCloseRate(info,convertCcy,date);
				if(convertRate == null){
					convertRate = WlRataCacheFactory.getRateCache().getRate(new RateCacheKey(convertCcy, info.getClientId()) );
					if(convertRate == null){
						LOGGER.info("no convert rate to rebuild  , convertCurrencyPair : {} clientId : {} ",new Object[]{info.getClientId(),convertCcy});
						continue;
					}
				}
				rt = new RebuildTask(info.getClientId(),info.getCustomerId(),ccp,
						rate.getAskRate(),rate.getBidRate(),rate.getWlRateId(),
						convertRate.getAskRate(),convertRate.getBidRate(),convertRate.getWlRateId(),
						date);
			}else{
				rt = new RebuildTask(info.getClientId(),info.getCustomerId(),ccp,
						rate.getAskRate(),rate.getBidRate(),rate.getWlRateId(),
						BigDecimal.ONE, BigDecimal.ONE, null,
						date);
			}
			LOGGER.info("clientId : {} ,currencyPair : {} ,ask : {} ,bid : {} ,convertAsk:{}  ,convertBid:{} " ,
					new Object[]{info.getClientId(),ccp,rt.getCloseRateBuy(),rt.getCloseRateSell(),rt.getCloseConvertRateBuy(),rt.getCloseConvertRateSell()});
			
			tasks.add(rt);
		}
		return tasks;
	}


	class RebuildTask implements Callable<Long>{
		
		String clientId;
		String customerId;
		String currencyPair;
		BigDecimal closeRateBuy;
		BigDecimal closeRateSell;
		String closeRatePriceId;
		BigDecimal closeConvertRateBuy;
		BigDecimal closeConvertRateSell;
		String closeConvertRatePriceId;
		JhfApplicationDate applicationDate;
		
		public BigDecimal getCloseConvertRateBuy() {
			return closeConvertRateBuy;
		}

		public void setCloseConvertRateBuy(BigDecimal closeConvertRateBuy) {
			this.closeConvertRateBuy = closeConvertRateBuy;
		}

		public BigDecimal getCloseConvertRateSell() {
			return closeConvertRateSell;
		}

		public void setCloseConvertRateSell(BigDecimal closeConvertRateSell) {
			this.closeConvertRateSell = closeConvertRateSell;
		}

		public String getCloseConvertRatePriceId() {
			return closeConvertRatePriceId;
		}

		public void setCloseConvertRatePriceId(String closeConvertRatePriceId) {
			this.closeConvertRatePriceId = closeConvertRatePriceId;
		}

		public String getCloseRatePriceId() {
			return closeRatePriceId;
		}

		public void setCloseRatePriceId(String closeRatePriceId) {
			this.closeRatePriceId = closeRatePriceId;
		}

		public JhfApplicationDate getApplicationDate() {
			return applicationDate;
		}

		public void setApplicationDate(JhfApplicationDate applicationDate) {
			this.applicationDate = applicationDate;
		}

		public BigDecimal getCloseRateBuy() {
			return closeRateBuy;
		}

		public void setCloseRateBuy(BigDecimal closeRateBuy) {
			this.closeRateBuy = closeRateBuy;
		}

		public BigDecimal getCloseRateSell() {
			return closeRateSell;
		}

		public void setCloseRateSell(BigDecimal closeRateSell) {
			this.closeRateSell = closeRateSell;
		}

		public String getClientId() {
			return clientId;
		}

		public String getCurrencyPair() {
			return currencyPair;
		}
		
		
		
		public String getCustomerId() {
			return customerId;
		}

		public void setCustomerId(String customerId) {
			this.customerId = customerId;
		}

		public void setClientId(String clientId) {
			this.clientId = clientId;
		}

		public void setCurrencyPair(String currencyPair) {
			this.currencyPair = currencyPair;
		}



		public RebuildTask(String clientId, String customerId,
				String currencyPair, BigDecimal closeRateBuy,
				BigDecimal closeRateSell, String closeRatePriceId,
				BigDecimal closeConvertRateBuy,
				BigDecimal closeConvertRateSell,
				String closeConvertRatePriceId,
				JhfApplicationDate applicationDate) {
			super();
			this.clientId = clientId;
			this.customerId = customerId;
			this.currencyPair = currencyPair;
			this.closeRateBuy = closeRateBuy;
			this.closeRateSell = closeRateSell;
			this.closeRatePriceId = closeRatePriceId;
			this.closeConvertRateBuy = closeConvertRateBuy;
			this.closeConvertRateSell = closeConvertRateSell;
			this.closeConvertRatePriceId = closeConvertRatePriceId;
			this.applicationDate = applicationDate;
		}

		@Override
		public Long call() throws Exception {
			return execute(this);
		}
		
		private Long execute(RebuildTask rebuildTask) {
			
			if(invalided(rebuildTask)){
				return 0L;
			}
			
			final long now = System.currentTimeMillis();
			rebuilder.rebuild(rebuildTask);
			return System.currentTimeMillis() - now ;
		}
		
		private boolean invalided(RebuildTask task) {
			try {
				//had autorebuilded before
				if(rebuilder.hadRebuildeBefore(task.getCustomerId(),task.getCurrencyPair(),task.getApplicationDate().getId().getFrontDate())){
					LOGGER.error(" had auto rebuild before , clientId : {}  currencyPair : {}" ,task.getClientId(),task.getCurrencyPair() );
					return true;
				}
				
				//completor all finished
				if(rebuilder.completorNotFinished(task.getCustomerId(),task.getCurrencyPair(),task.getApplicationDate().getId().getFrontDate())){
					LOGGER.error(" completor not finished  , clientId : {}  currencyPair : {}" ,task.getClientId(),task.getCurrencyPair() );
					return true;
				}
			} catch (Exception e) {
				LOGGER.error(" check auto rebuild error ",e);
				return false;
			}

			return false;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("RebuildTask [clientId=").append(clientId)
					.append(", currencyPair=").append(currencyPair)
					.append(", closeRateBuy=").append(closeRateBuy)
					.append(", closeRateSell=").append(closeRateSell)
					.append("]");
			return builder.toString();
		}
		
		
	}
}

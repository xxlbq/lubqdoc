package jp.emcom.adv.fx.completor.biz.scheduler.impl;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import jp.emcom.adv.fx.commons.application.LifecycleUtils;
import jp.emcom.adv.fx.completor.biz.AutoBuildInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bestwiz.jhf.core.application.utils.XThreadFactory;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrder;
import cn.bestwiz.jhf.core.util.CollectionsUtil;

/**
 * 
 * @author Jingqi Xu
 */
public class PullingWLScheduler extends AbstractWLScheduler {
	//
	private static final Logger LOGGER = LoggerFactory.getLogger(PullingWLScheduler.class);
	
	//
	private boolean readOnly = true;
	private long schedulingInterval = 5000;
 	private final AtomicReference<ScheduledExecutorService> scheduler;

	/**
	 * 
	 */
	public PullingWLScheduler() {
		this.scheduler = new AtomicReference<ScheduledExecutorService>();
	}
	
	@Override
	protected void doStart() throws Exception {
		// 1
		this.wlExecutor.start();
		this.wlParExecutor.start();
		// 2 
		final ThreadFactory tf2 = new XThreadFactory("scheduler", false);
		this.scheduler.compareAndSet(null, Executors.newScheduledThreadPool(1, tf2)); // One thread only
		this.scheduler.get().scheduleWithFixedDelay(new SchedulerTask(), 0, this.schedulingInterval, TimeUnit.MILLISECONDS);
	}

	@Override
	protected void doStop(long timeout) throws Exception {
		// 2
		final ScheduledExecutorService s = this.scheduler.getAndSet(null);
		if(s != null) s.shutdownNow();
		
		// 1
		timeout = LifecycleUtils.stopQuietly(this.wlExecutor, timeout);
	}
	
	/**
	 * 
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	public void setSchedulingInterval(long schedulingInterval) {
		this.schedulingInterval = schedulingInterval;
	}
	
	/**
	 * 
	 */
	private class SchedulerTask implements Runnable {
		
		/**
		 * 
		 */
		@Override
		public void run() {
			try {
				LOGGER.info("scheduler task started");
				final long now = System.currentTimeMillis();
				schedule();
				LOGGER.info("scheduler task stopped, scheduler delay: {}", (System.currentTimeMillis() - now));
			} catch(Exception e) {
				LOGGER.error("unhandled exception in scheduler task", e);
			}
			
			try {
				autoRebuild();
				
			} catch (Exception e) {
				LOGGER.error("unhandled exception in makeRebuild task", e);
			}
			
		}
		
		/**
		 * 
		 */
		private void autoRebuild() throws Exception {
			//JHF_WL_CLIENT_CONFIG.POSITION_AUTO_REBUILD_CONSTRAINT
			List<AutoBuildInfo> AutoBuildInfos = wlCompletorService.findAutoRebuildClientId(clientIds);
			if(CollectionsUtil.isEmpty(AutoBuildInfos)) {
				LOGGER.info("nobody need autoRebuild .");
				return ;
			}
			for (AutoBuildInfo info : AutoBuildInfos) {
				LOGGER.info("clientId : {} is under pos auto rebuild ",info.getClientId());
			}
			
			wlParExecutor.execute(AutoBuildInfos);
		}

		/**
		 * 
		 */
		private void schedule() throws Exception {
			// Find candidates
			// Note: candidates should be sorted in FIFO order by WLCompletorService
			LOGGER.info("start to find candidates, clientIds: {}", clientIds);
			final List<JhfWlOrder> candidates = wlCompletorService.findCompletionCandidates(readOnly, clientIds);
			if(candidates.size() == 0) {
				LOGGER.info("candidates NOT found, clientIds: {}", clientIds);
				return;
			}
			
			// Validate candidates
			if(!isValidToSchedule()) {
				LOGGER.error("the following candidates are NOT properly scheduled"); // Big trouble!
				for(JhfWlOrder order : candidates) {
					LOGGER.error("customerId: {}, wlOrderId: {}, currencyPair: {}, completionStatus: {}", new Object[]{order.getCustomerId(), order.getWlOrderId(), order.getCurrencyPair(), order.getCompletionStatus()});
				}
				LOGGER.error("there is nothing we can do, just let the rollover do it's job first and we have to correct the data manually later");
				return;
			} else {
				LOGGER.info("found {} candidates to schedule", candidates.size());
				for(JhfWlOrder order : candidates) {
					LOGGER.info("customerId: {}, wlOrderId: {}, currencyPair: {}, completionStatus: {}", new Object[]{order.getCustomerId(), order.getWlOrderId(), order.getCurrencyPair(), order.getCompletionStatus()});
				}
			}
			
			// Execute candidates
			LOGGER.info("start to execute candidates, count: {}", candidates.size());
			wlExecutor.execute(candidates);
		}
	}
}
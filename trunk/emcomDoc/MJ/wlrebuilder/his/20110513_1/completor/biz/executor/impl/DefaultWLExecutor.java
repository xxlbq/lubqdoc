package jp.emcom.adv.fx.completor.biz.executor.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import jp.emcom.adv.fx.commons.application.LifecycleUtils;
import jp.emcom.adv.fx.commons.bo.enums.CompletionStatusEnum;
import jp.emcom.adv.fx.completor.biz.executor.WLExecutor;
import jp.emcom.adv.fx.completor.biz.util.XExecutorCompletionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bestwiz.jhf.core.application.impl.AbstractLifecycle;
import cn.bestwiz.jhf.core.application.utils.XThreadFactory;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrder;


/**
 * 
 * @author Jingqi Xu
 */
public class DefaultWLExecutor extends AbstractLifecycle implements WLExecutor {
	//
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWLExecutor.class);
	
	//
	private static final AtomicLong EXECUTOR_TASK_ID_GENERATOR = new AtomicLong(0);
	
	//
	private int executorPoolSize = 32;
	private long pollingInterval = 1000;
	private long executionTimeThreshold = 5000;
	private final AtomicReference<WLNetter> wlNetter;
	private final AtomicReference<WLCorrector> wlCorrector;
	private final AtomicReference<XExecutorCompletionService<Long>> executor;
	
	/**
	 * 
	 */
	public DefaultWLExecutor() {
		this.wlNetter = new AtomicReference<WLNetter>();
		this.wlCorrector = new AtomicReference<WLCorrector>();
		this.executor = new AtomicReference<XExecutorCompletionService<Long>>();
	}
	
	@Override
	protected void doStart() throws Exception {
		// 1
		this.wlNetter.get().start();
		
		// 2
		this.wlCorrector.get().start();
		
		// 3
		final ThreadFactory tf1 = new XThreadFactory("executor", false);
		this.executor.compareAndSet(null, new XExecutorCompletionService<Long>(Executors.newFixedThreadPool(this.executorPoolSize, tf1)));
	}

	@Override
	protected void doStop(long timeout) throws Exception {
		// 3
		final XExecutorCompletionService<?> executor = this.executor.getAndSet(null);
		if(executor != null) executor.shutdownNow();
		
		// 2
		final WLCorrector corrector = this.wlCorrector.getAndSet(null);
		timeout = LifecycleUtils.stopQuietly(corrector, timeout);
		
		// 3
		final WLNetter netter = this.wlNetter.getAndSet(null);
		timeout = LifecycleUtils.stopQuietly(netter, timeout);
	}
	
	/**
	 * 
	 */
	public void setWlNetter(WLNetter netter) {
		this.wlNetter.set(netter);
	}

	public void setWlCorrector(WLCorrector corrector) {
		this.wlCorrector.set(corrector);
	}
	
	public void setPollingInterval(long pollingInterval) {
		this.pollingInterval = pollingInterval;
	}
	
	public void setExecutorPoolSize(int executorPoolSize) {
		this.executorPoolSize = executorPoolSize;
	}
	
	public void setExecutionTimeThreshold(long executionTimeThreshold) {
		this.executionTimeThreshold = executionTimeThreshold;
	}
	
	/**
	 * 
	 */
	@Override
	public void execute(List<JhfWlOrder> candidates) throws Exception {
		// Submit
		final Map<Long, ExecutorTask> tasks = toExecutorTasks(candidates);
		for(ExecutorTask task : tasks.values()) {
			this.executor.get().submit(task);
			LOGGER.info("submitted a task, taskId: {}, customerId: {}, currencyPair: {}", new Object[]{task.getTaskId(), task.getCustomerId(), task.getCurrencyPair()});
		}
		LOGGER.info("{} tasks were submitted to executor", tasks.size());
		
		// Await
		final long now = System.currentTimeMillis();
		while(tasks.size() > 0) {
			//
			final Future<Long> future = this.executor.get().poll(this.pollingInterval, TimeUnit.MILLISECONDS);
			if(future != null) {
				final Long taskId = future.get();
				final ExecutorTask task = tasks.remove(taskId);
				if(task == null) {
					LOGGER.error("failed to remove task by taskId: {}", taskId);
				}
			}
			
			//
			final long awaitedTime = System.currentTimeMillis() - now;
			if(tasks.size() > 0 && awaitedTime > this.executionTimeThreshold) { 
				LOGGER.warn("the following orders take too much time to process, elapsed time: {}", awaitedTime);
				for(ExecutorTask task : tasks.values()) {
					for(JhfWlOrder order : task.getPendingOrders()) {
						LOGGER.warn("taskId: {}, customerId: {}, wlOrderId: {}, currencyPair: {}", new Object[]{task.getTaskId(), order.getCustomerId(), order.getWlOrderId(), order.getCurrencyPair()});
					}
				}
			}
		}
		LOGGER.info("all tasks were successfully executed");
	}
	
	/**
	 * 
	 */
	private Map<Long, ExecutorTask> toExecutorTasks(List<JhfWlOrder> candidates) {
		//
		final Map<String, Map<String, List<JhfWlOrder>>> orders = new HashMap<String, Map<String, List<JhfWlOrder>>>();
		for(JhfWlOrder order : candidates) {
			//
			Map<String, List<JhfWlOrder>> map = orders.get(order.getCustomerId());
			if(map == null) {
				map = new HashMap<String, List<JhfWlOrder>>();
				orders.put(order.getCustomerId(), map);
			}
			
			//
			List<JhfWlOrder> list = map.get(order.getCurrencyPair());
			if(list == null) {
				list = new ArrayList<JhfWlOrder>();
				map.put(order.getCurrencyPair(), list);
			}
			list.add(order);
		}
		
		//
		final Map<Long, ExecutorTask> tasks = new HashMap<Long, ExecutorTask>();
		for(String customerId : orders.keySet()) {
			final Map<String, List<JhfWlOrder>> map = orders.get(customerId);
			for(String currencyPair : map.keySet()) {
				final ExecutorTask task = new ExecutorTask(customerId, currencyPair, map.get(currencyPair)); 
				tasks.put(task.getTaskId(), task);
			}
		}
		return tasks;
	}
	
	/**
	 * 
	 */
	private class ExecutorTask implements Callable<Long> {
		//
		private final long taskId;
		private final long timestamp;
		private final String customerId;
		private final String currencyPair;
		private final List<JhfWlOrder> orders;

		/**
		 * 
		 */
		public ExecutorTask(String customerId, String currencyPair, List<JhfWlOrder> orders) {
			this.orders = orders;
			this.customerId = customerId;
			this.currencyPair = currencyPair;
			this.timestamp = System.currentTimeMillis();
			this.taskId = EXECUTOR_TASK_ID_GENERATOR.incrementAndGet();
		}
		
		/**
		 * 
		 */
		@Override
		public Long call() throws Exception {
			try {
				LOGGER.info("start to process executor task, taskId: {}, customerId: {}, currencyPair: {}, delay: {}", new Object[]{taskId, customerId, currencyPair, (System.currentTimeMillis() - timestamp)});
				execute(this);
				LOGGER.info("processed an executor task, taskId: {}, customerId: {}, currencyPair: {}, delay: {}", new Object[]{taskId, customerId, currencyPair, (System.currentTimeMillis() - timestamp)});
			} catch(Exception e) {
				LOGGER.error("unhandled exception in executor task, taskId: " + taskId + ", customerId: " + customerId + ", currencyPair: " + currencyPair, e);
			}
			return this.taskId;
		}
		
		/**
		 * 
		 */
		public long getTaskId() {
			return taskId;
		}
		
		public String getCustomerId() {
			return customerId;
		}

		public String getCurrencyPair() {
			return currencyPair;
		}
		
		public List<JhfWlOrder> getOrders() {
			return orders;
		}
		
		public List<JhfWlOrder> getPendingOrders() {
			final List<JhfWlOrder> r = new ArrayList<JhfWlOrder>();
			for(JhfWlOrder order : this.orders) {
				final int cs = order.getCompletionStatus().intValue();
				if(cs == CompletionStatusEnum.PENDING.getValue() 
					|| cs == CompletionStatusEnum.PROGRESSING.getValue()
					|| cs == CompletionStatusEnum.CORRECTED.getValue()) {
					r.add(order);
				}
			}
			return r;
		}
		
		/**
		 * 
		 */
		private void execute(ExecutorTask task) throws Exception {
			for(JhfWlOrder order : task.getOrders()) {
				//
				boolean succeed = false;
				final int cs = order.getCompletionStatus().intValue();
				LOGGER.info("start to process order, taskId: {}, customerId: {}, currencyPair: {}, wlOrderId: {}", new Object[]{task.getTaskId(), order.getCustomerId(), order.getCurrencyPair(), order.getWlOrderId()});
				if(cs == CompletionStatusEnum.SKIP.getValue()) {
					LOGGER.warn("skipped an order, taskId: {}, customerId: {}, currencyPair: {}, wlOrderId: {}", new Object[]{task.getTaskId(), order.getCustomerId(), order.getCurrencyPair(), order.getWlOrderId()});
					succeed = true;
				} else if(cs == CompletionStatusEnum.FAILED.getValue() || cs == CompletionStatusEnum.CORRECTION_FAILED.getValue()) {
					LOGGER.error("invalid completion status, taskId: {}, customerId: {}, currencyPair: {}, wlOrderId: {}, completionStauts: {}", new Object[]{task.getTaskId(), order.getCustomerId(), order.getCurrencyPair(), order.getWlOrderId(), order.getCompletionStatus()});
					succeed = false;
				} else if(cs == CompletionStatusEnum.PENDING.getValue() || cs == CompletionStatusEnum.PROGRESSING.getValue()) {
					succeed = wlNetter.get().net(order);
				} else if(cs == CompletionStatusEnum.CORRECTED.getValue()) {
					succeed = wlCorrector.get().correct(order);
				} else {
					LOGGER.error("unknown completion status, taskId: {}, customerId: {}, currencyPair: {}, wlOrderId: {}, completionStatus: {}", new Object[]{task.getTaskId(), order.getCustomerId(), order.getCurrencyPair(), order.getWlOrderId(), order.getCompletionStatus()});
					succeed = false;
				}
				
				//
				if(!succeed) {
					break;
				}
			}
		}
	}
}

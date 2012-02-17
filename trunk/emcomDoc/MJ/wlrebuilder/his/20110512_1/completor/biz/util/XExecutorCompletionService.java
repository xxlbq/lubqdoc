package jp.emcom.adv.fx.completor.biz.util;

import java.util.List;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author Jingqi Xu
 */
public class XExecutorCompletionService<V> extends ExecutorCompletionService<V> {
	//
	private final ExecutorService executorService;

	/**
	 * 
	 */
	public XExecutorCompletionService(ExecutorService executorService) {
		super(executorService);
		this.executorService = executorService;
	}
	
	/**
	 * 
	 */
	public List<Runnable> shutdownNow() {
		return this.executorService.shutdownNow();
	}
	
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return this.executorService.awaitTermination(timeout, unit);
	}
}

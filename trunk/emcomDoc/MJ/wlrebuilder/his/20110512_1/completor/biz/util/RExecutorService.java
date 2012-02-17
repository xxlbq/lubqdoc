package jp.emcom.adv.fx.completor.biz.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RExecutorService extends ThreadPoolExecutor{

//	public RExecutorService(int corePoolSize, int maximumPoolSize,
//			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
//		
//		// TODO Auto-generated constructor stub
//	}
	
//	public DefaultParWLExecutor() {
//		this.executor = new ThreadPoolExecutor(executorSize, executorSize,
//                0L, TimeUnit.MILLISECONDS,
//                new LinkedBlockingQueue<Runnable>());
//	}
	
	public RExecutorService(int executorSize){
		super(executorSize, executorSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}
	
}

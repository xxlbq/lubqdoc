package jp.emcom.adv.fx.completor.biz.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RExecutorRebuilderService extends ThreadPoolExecutor{

	public RExecutorRebuilderService(int executorSize){
		super(executorSize, executorSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}
}

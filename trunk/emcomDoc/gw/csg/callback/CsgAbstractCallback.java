package cn.bestwiz.jhf.gws.csg.callback;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.jfree.util.Log;

import cn.bestwiz.jhf.gws.common.callback.IMessageCallback;

public abstract class CsgAbstractCallback implements Runnable,IMessageCallback{

	protected BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
	protected ExecutorService executor ;

	public abstract void dealItem(Object workItem) ;
	
	public void run() {
		try {
			while(true){
				Object o = queue.take();
				dealItem(o);
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public CsgAbstractCallback(int count) {
		executor = Executors.newFixedThreadPool(count);
		executor.execute(this);
	}
	
	public void enQueue (Object o){
		System.out.println("enQueue is fired ....");
		try {
			queue.put(o);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	
	public void stop() {
		executor.shutdown();
	}
}

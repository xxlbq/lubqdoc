package cn.bestwiz.jhf.gws.csg.util;

import java.util.Timer;

import org.apache.commons.logging.Log;

import cn.bestwiz.jhf.core.util.LogUtil;
import cn.bestwiz.jhf.gws.csg.CsgContext;

import com.pstcl.clientsys.PSTClientContext;
import com.pstcl.clientsys.timer.HBWatchTask;

public class Times extends java.util.TimerTask {
	private static Log log = LogUtil.getLog(CsgContext.class);
	private Timer timer = null;

	
	private static Times times = null;
	private PSTClientContext context = null;
	private HBWatchTask ht = null;

	public Times(PSTClientContext c) {
		timer = new Timer(false);
		this.context = c ;
		this.ht= new HBWatchTask(context);
	}
	
	protected void start() {

		// 定时提醒：安排指定的任务在指定的延迟后开始进行重复的固定速率执行。
		timer.scheduleAtFixedRate(this, 0, 5 * 1000);

	}

	/**
	 * 停止定时器
	 * 
	 */
	public static void stop() {
		if (times != null) {
			times.timer.cancel();
		}
	}
	public void run() {

		String f = null;
		try {
			f = ht.call();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(f.equalsIgnoreCase(ht.OK)){
			log.info(" HEART BEAT OK ------ result="+f);
		}else{
			log.info(" HEART BEAT NG ------result="+f);
		}
		
		
	}

	public void startTimer() {
		if (times == null) {
			times = new Times(context);			
			times.start();
		}
	}
}
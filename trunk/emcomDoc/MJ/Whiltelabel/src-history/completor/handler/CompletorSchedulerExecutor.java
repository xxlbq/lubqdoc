package jp.emcom.adv.fx.completor.handler;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CompletorSchedulerExecutor implements SchedulerExecutor {
	
	private ScheduledExecutorService scheduledExecutorService;
	
	private	Runnable command ;
	private  long initialDelay;
	private  long period;
	private  TimeUnit unit;
	
	public void schedule() {
		//never consider delay 
		scheduledExecutorService.scheduleAtFixedRate(command, initialDelay, period, unit);
		//do schedule after delay
//		scheduledExecutorService.scheduleWithFixedDelay(command, initialDelay, period, unit);
	}

	public ScheduledExecutorService getScheduledExecutorService() {
		return scheduledExecutorService;
	}

	public void setScheduledExecutorService(
			ScheduledExecutorService scheduledExecutorService) {
		this.scheduledExecutorService = scheduledExecutorService;
	}

	public Runnable getCommand() {
		return command;
	}

	public void setCommand(Runnable command) {
		this.command = command;
	}

	public long getInitialDelay() {
		return initialDelay;
	}

	public void setInitialDelay(long initialDelay) {
		this.initialDelay = initialDelay;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}

	public TimeUnit getUnit() {
		return unit;
	}

	public void setUnit(TimeUnit unit) {
		this.unit = unit;
	}

	
	
}

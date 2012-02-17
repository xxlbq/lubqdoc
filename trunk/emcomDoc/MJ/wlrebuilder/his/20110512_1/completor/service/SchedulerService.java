package jp.emcom.adv.fx.completor.service;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import jp.emcom.adv.fx.completor.biz.scheduler.impl.AbstractWLScheduler;

import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;


public class SchedulerService  extends AbstractWLScheduler{

	private Scheduler scheduler;
	private JobDetail jobDetail;

	
	
	@Override
	protected void doStart() throws Exception {
		// TODO Auto-generated method stub
		Date date = getTriggerTime();
//		List<String> clientIds = findPosRebuildClients();
		schedule(date);
	}
	
	@Override
	protected void doStop(long timeout) throws Exception {
		// TODO Auto-generated method stub
		scheduler.shutdown();
	}
	
	@Override
	protected boolean isValidToSchedule() throws Exception {
		if(!super.isValidToSchedule()){
			return false;
		}

		return true;
	}
	
	
	
	public void setJobDetail(JobDetail jobDetail) {
		this.jobDetail = jobDetail;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	public void schedule(String cronExpression) {
		schedule(null, cronExpression);
	}

	public void schedule(String name, String cronExpression) {
		try {
			schedule(name, new CronExpression(cronExpression));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public void schedule(CronExpression cronExpression) {
		schedule(null, cronExpression);
	}

	public void schedule(String name, CronExpression cronExpression) {
		if (name == null || name.trim().equals("")) {
			name = UUID.randomUUID().toString();
		}

		try {
			scheduler.addJob(jobDetail, true);

			CronTrigger cronTrigger = new CronTrigger(name, Scheduler.DEFAULT_GROUP, jobDetail.getName(),
					Scheduler.DEFAULT_GROUP);
			cronTrigger.setCronExpression(cronExpression);
			scheduler.scheduleJob(cronTrigger);
//			scheduler.rescheduleJob(name, Scheduler.DEFAULT_GROUP, cronTrigger);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}

	public void schedule(Date startTime) {
		schedule(startTime, null);
	}

	public void schedule(String name, Date startTime) {
		schedule(name, startTime, null);
	}

	public void schedule(Date startTime, Date endTime) {
		schedule(startTime, endTime, 0);
	}

	public void schedule(String name, Date startTime, Date endTime) {
		schedule(name, startTime, endTime, 0);
	}

	public void schedule(Date startTime, Date endTime, int repeatCount) {
		schedule(null, startTime, endTime, 0);
	}

	public void schedule(String name, Date startTime, Date endTime, int repeatCount) {
		schedule(name, startTime, endTime, 0, 0L);
	}

	public void schedule(Date startTime, Date endTime, int repeatCount, long repeatInterval) {
		schedule(null, startTime, endTime, repeatCount, repeatInterval);
	}

	public void schedule(String name, Date startTime, Date endTime, int repeatCount, long repeatInterval) {
		if (name == null || name.trim().equals("")) {
			name = UUID.randomUUID().toString();
		}

		try {
			scheduler.addJob(jobDetail, true);

			SimpleTrigger SimpleTrigger = new SimpleTrigger(name, Scheduler.DEFAULT_GROUP, jobDetail.getName(),
					Scheduler.DEFAULT_GROUP, startTime, endTime, repeatCount, repeatInterval);
			scheduler.scheduleJob(SimpleTrigger);
//			scheduler.rescheduleJob(name, Scheduler.DEFAULT_GROUP, SimpleTrigger);

		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}

	
	
	//
	
	
	public Date getTriggerTime(){
		Date startTime = parse("2011-05-09 14:42:00");
		return  startTime ;
	}
	
    private static Date parse(String dateStr){  
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        try {  
            return format.parse(dateStr);  
        } catch (ParseException e) {  
            throw new RuntimeException(e);  
        }  
    }
	
	
}


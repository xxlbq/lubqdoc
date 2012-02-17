package jp.emcom.adv.fx.completor.biz.scheduler;

import java.util.List;

import jp.emcom.adv.fx.completor.service.WLRebuildService;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class RebuildJob  extends QuartzJobBean{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RebuildJob.class);
	
	private WLRebuildService rebuildService;
	protected List<String> clientIds;
	
	public List<String> getClientIds() {
		return clientIds;
	}

	public void setClientIds(List<String> clientIds) {
		this.clientIds = clientIds;
	}

	public WLRebuildService getRebuildService() {
		return rebuildService;
	}

	public void setRebuildService(WLRebuildService rebuildService) {
		this.rebuildService = rebuildService;
	}

	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		// TODO Auto-generated method stub
		
//	   Trigger trigger = context.getTrigger();  
//	   String triggerName = trigger.getName();   
		
		rebuildService.start(clientIds);

	}

	
	
}

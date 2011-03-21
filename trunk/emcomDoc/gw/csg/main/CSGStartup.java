package cn.bestwiz.jhf.gws.csg.main;

import org.apache.commons.logging.Log;

import cn.bestwiz.jhf.core.dao.util.DbSessionFactory;
import cn.bestwiz.jhf.core.jms.DestinationConstant;
import cn.bestwiz.jhf.core.jms.SimpleSender;
import cn.bestwiz.jhf.core.jms.bean.GwCpTradeRequestInfo;
import cn.bestwiz.jhf.core.util.LogUtil;
import cn.bestwiz.jhf.gws.csg.CsgContext;
import cn.bestwiz.jhf.gws.csg.callback.CsgAbstractCallback;
import cn.bestwiz.jhf.gws.csg.callback.CsgFxRequestCallback;
import cn.bestwiz.jhf.gws.csg.config.CsgConfig;
import cn.bestwiz.jhf.gws.csg.trade.CSGFXTradeReceiver;
import cn.bestwiz.jhf.gws.csg.util.CsgUtil;
import cn.bestwiz.jhf.gws.gkgoh.GKGOHService;



/**
 * 服务启动操作类，操作服务的启动和关闭
 *
 * @author JHF Team <jhf@bestwiz.cn>
 * @copyright 2006, BestWiz(Dalian) Co.,Ltd
 * @version 
 */

public class CSGStartup{
	
	private static final Log log = LogUtil.getLog(CSGStartup.class);


	
	/**
	 * 启动GKGOH银行服务
	 *
	 */
	public void startUp()
	{
		
		try
		{
//			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
//			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
			System.out.println("Main Session Creatied!");

			CsgService.
			service.startService();
			
		} catch (Exception e)
		{
			log.error("SERVER COULD NOT RECEIVE ENDSERVICE COMMOND - EXCEPTION THROWN DURING STARTUP", e);
//			service.endService();
			e.printStackTrace();
			System.exit(1);
		}
	
	}

	/**
	 * 程序入口，本程序由此启动
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		CSGStartup startUpObj = new CSGStartup(); 
		startUpObj.startUp(); 
	}

}
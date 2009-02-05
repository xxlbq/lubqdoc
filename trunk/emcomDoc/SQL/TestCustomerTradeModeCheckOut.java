package cn.bestwiz.jhf.core.configcache;


import cn.bestwiz.jhf.core.dao.bean.main.JhfCustomerBlacklist;
import cn.bestwiz.jhf.core.service.CoreService;
import cn.bestwiz.jhf.core.service.ServiceFactory;
import junit.framework.TestCase;

public class TestCustomerTradeModeCheckOut extends TestCase {
	
	String customerId;
	String currencyPair; 
	int side ;
	int executionType ;
	int defaultMode;
	
	
	CoreService cs;
	ConfigService cfgs;
	
	@Override
	protected void setUp() throws Exception {
		
		super.setUp();
//		this.customerId = "00000101";
		this.customerId = null;
		this.currencyPair = "USD/JPY"; 
		this.side= -1 ;
		//成行
		this.executionType= 12 ;
		this.defaultMode = 1;
		
		cs = cn.bestwiz.jhf.core.service.ServiceFactory.getCoreService();
		cfgs = cn.bestwiz.jhf.core.service.ServiceFactory.getConfigService();
	}
	
	public void testCustomerTradeModeCheckOut() throws Exception{
		int mode = cs.customerTradeModeCheckOut(
				customerId, currencyPair, side, executionType,defaultMode);
		
		System.out.println(mode);
		////  0(通常),1(滞留),2(自社)
		assertEquals( 0 , mode);
	}
	
	public void testObtainBlackCustomer() throws Exception{
		JhfCustomerBlacklist blackCustomer = cfgs.obtainBlackCustomer(customerId);
		if(null == blackCustomer){
			System.out.println(" blackCustomer null");
		}else{
			System.out.println(" it is not null");
		}
	}
}

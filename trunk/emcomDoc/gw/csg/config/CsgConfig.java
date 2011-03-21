package cn.bestwiz.jhf.gws.csg.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.io.xml.DomDriver;


@XStreamAlias("config")
public class CsgConfig {
	
	public CsgConfig(String pstBridgeHost, String pstbCommandServerPort,
			String pstcRateserverPort, String pstcResserverPort,
			String pstbOrderServerPort, String customer, String clientSystem,
			String orderQueueSize, String sotimeout) {
		super();
		this.pstBridgeHost = pstBridgeHost;
		this.pstbCommandServerPort = pstbCommandServerPort;
		this.pstcRateserverPort = pstcRateserverPort;
		this.pstcResserverPort = pstcResserverPort;
		this.pstbOrderServerPort = pstbOrderServerPort;
		this.customer = customer;
		this.clientSystem = clientSystem;
		this.orderQueueSize = orderQueueSize;
		this.sotimeout = sotimeout;
	}





	@XStreamAlias("pst_bridge.host")
	private String pstBridgeHost;
	
	@XStreamAlias("pstb_command_server.port")
	private String pstbCommandServerPort;
	
	@XStreamAlias("pstc_rateserver.port")
	private String pstcRateserverPort;
	
	@XStreamAlias("pstc_resserver.port")
	private String pstcResserverPort;
	
	@XStreamAlias("pstb_order_server.port")
	private String pstbOrderServerPort;
	
	@XStreamAlias("customer")
	private String customer;
	
	@XStreamAlias("client_system")
	private String clientSystem;
	
	@XStreamAlias("order.queue.size")
	private String orderQueueSize;
	
	@XStreamAlias("sotimeout")
	private String sotimeout;
	
	
	
	

	@Override
	public String toString() {
		return "CsgConfig [clientSystem=" + clientSystem + ", customer="
				+ customer + ", orderQueueSize=" + orderQueueSize
				+ ", pstBridgeHost=" + pstBridgeHost
				+ ", pstbCommandServerPort=" + pstbCommandServerPort
				+ ", pstbOrderServerPort=" + pstbOrderServerPort
				+ ", pstcRateserverPort=" + pstcRateserverPort
				+ ", pstcResserverPort=" + pstcResserverPort + ", sotimeout="
				+ sotimeout + "]";
	}


	public static synchronized CsgConfig getInstance(Reader reader) {
		XStream xstream = new XStream(new DomDriver());
		xstream.processAnnotations(CsgConfig.class);
		return (CsgConfig)xstream.fromXML(reader);
	}
	
	
	
	
	
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(CsgConfig.class.getResourceAsStream("PSTApi.xml")));
		System.out.println(CsgConfig.getInstance(br));
		br.close();

	}

	
	
}

package cn.bestwiz.jhf.gws.csg.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;

import cn.bestwiz.jhf.core.util.LogUtil;
import cn.bestwiz.jhf.core.util.SystemConstants;
import cn.bestwiz.jhf.gws.csg.config.CsgConfig;

public class CsgUtil {

	private static final Log LOG = LogUtil.getLog(CsgUtil.class);
	private static final String PSTAPI_XML_FILE = "PSTApi.xml";
	
	
	//

	
	public static CsgConfig loadConfig() throws Exception{
		BufferedReader br = null;
		String configPath = System.getProperty(SystemConstants.CONFIG_PATH_KEY);
		if (configPath == null || configPath.length() == 0) {
			throw new IllegalArgumentException("invalid system property: " + SystemConstants.CONFIG_PATH_KEY);
		}
		
		try {
			File file = new File(configPath + File.separator + PSTAPI_XML_FILE);
			if(file.exists()) {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
				return CsgConfig.getInstance(br);				
			} else {
				throw new Exception(PSTAPI_XML_FILE+" not found at :"+(configPath + File.separator + PSTAPI_XML_FILE));
			}
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch(Exception e) {
					LOG.error("===[JMS]=== failed to close reader", e);
				}
			}
		}
	}
	

}

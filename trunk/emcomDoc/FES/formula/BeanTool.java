package cn.bestwiz.jhf.formula.tool;

import java.lang.reflect.Method;

public class BeanTool {
	
	public static String printf(Object bean,String spilt , boolean isPrintClassName){
		
		StringBuffer sb = new StringBuffer();
		
		if(isPrintClassName){
			sb.append(bean.getClass().getName()+"=========\n");
		}
		
		Method[] methods = bean.getClass().getMethods();
		for(Method method : methods){
			
			String methodName = method.getName();
			String returnName = method.getReturnType().getName();
			
			if(!methodName.matches("^get.*")) continue;
		
			String regex = ".*\\.(String|BigDecimal|RatioInfo|MarginInfo;)$";
			
			if(returnName.matches(regex)){
				try {
					
					if(method.getReturnType().isArray()){
						Object[] x =(Object[]) method.invoke(bean, (Object[])null);
						if(x.length != 0){
							sb.append(methodName+"():"+x[0].getClass().getName()+"======>\n");
							for(Object a:x){
								sb.append("        "+a+spilt);
							}
						}
					}else{
						Object x = method.invoke(bean, (Object[])null);
						sb.append(methodName+"():"+x+spilt);
					}
					
					
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}else{
				if(!returnName.matches("java.lang.Class")){
					System.err.println(returnName);
				}
			}
		}
		
		return sb.toString();
		
	}

}

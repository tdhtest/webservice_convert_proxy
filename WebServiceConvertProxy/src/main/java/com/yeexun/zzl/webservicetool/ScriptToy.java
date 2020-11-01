package com.yeexun.zzl.webservicetool;
 
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
 /**
  * 工具类，用来将算法、路径隐射加载到程序中
  * @author michazl
  *
  */
public class ScriptToy implements Invocable{

	private ScriptEngineManager manager;
	private ScriptEngine engine;
	private Invocable in;

	public ScriptToy() {
		super(); 
	}
	public ScriptToy(String scriptFile) {
		super(); 

		  StringBuilder sb = new StringBuilder();
	        byte[] buf = new byte[4096];    
		try {
			InputStream input = ClassLoader.getSystemResourceAsStream(scriptFile); 
			for(;;) {
				int read = input.read(buf);
	        	if(read==buf.length) {
		        	sb.append(new String(buf));
	        	}else {
		        	sb.append(new String(buf,0,read));break;
	        	}
			}
			
		} catch (IOException e) { 
			e.printStackTrace();
		}

		String params = sb.toString();
		  manager = new ScriptEngineManager();
          engine = manager.getEngineByName("javascript"); 
            try {
				engine.eval( params);
                in = (Invocable) engine;
			} catch (ScriptException e) { 
				e.printStackTrace();
			}
//          System.out.println(in.invokeFunction("add",1,1));
	}
 

	@Override
	public Object invokeMethod(Object thiz, String name, Object... args) throws ScriptException, NoSuchMethodException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String invokeFunction(String name, Object... args) {
		name = name .replace('/', '_');
		try {
			return String.valueOf(in.invokeFunction(name, args));
		} catch (NoSuchMethodException | ScriptException e) {
			e.printStackTrace();
			
		}
		return "\"转换异常\"";
	}

	@Override
	public <T> T getInterface(Class<T> clasz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getInterface(Object thiz, Class<T> clasz) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * stream 转 字符串
	 * @param input
	 * @return
	 */
	public static  String  readAll(InputStream input)   {
		StringBuilder sb = new StringBuilder();
		byte[] buf = new byte[4096];   
		try {
		for(;;) {
			int read;
				read = input.read(buf);
			if(read==buf.length) {
		    	sb.append(new String(buf));
			}else {
		    	sb.append(new String(buf,0,read));  
		    	break;
			}
		}
		} catch (IOException e) { 
			e.printStackTrace();
		}
		String str = sb.toString();
		sb.setLength(0);
		return str;
	}
	/**
	 * params map 转字符串
	 * @param params
	 * @param charset
	 * @return
	 */
	  public static String generateParams(Map<String, String[]> params,String charset)  {
	      int flag=0;

	      StringBuffer ret=new StringBuffer();

	      Iterator<Entry<String, String[]>> iter = params.entrySet().iterator();

	      while (iter.hasNext()) {
	          Map.Entry<String,String[]> entry = (Map.Entry<String,String[]>) iter.next();

	          Object key = entry.getKey();

	          String[] vals = (String[]) entry.getValue();
	          String val = vals[0];
	          if(val!=null){
	          if(flag==0){
	            ret.append(key);

	            ret.append("=");

	            if(charset!=null&&!charset.equals("")){
	            try {
					ret.append(URLEncoder.encode(val.toString(), charset));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	            }else{
	               ret.append(val.toString());

	            }

	            flag++;

	          }else{
	            ret.append("&");

	            ret.append(key);

	            ret.append("=");

	            if(charset!=null&&!charset.equals("")){
	              try {
					ret.append(URLEncoder.encode(val.toString(), charset));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	              }else{
	                 ret.append(val.toString());

	              }

	          }

	          }

	      }

	      return ret.toString();

	   }
}

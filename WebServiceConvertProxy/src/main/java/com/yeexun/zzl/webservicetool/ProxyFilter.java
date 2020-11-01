package com.yeexun.zzl.webservicetool;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.Charsets;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
/**
 * 
 * @author michazl
 *可以通过json配置映射 和js配置数据格式转换规则
 *
 *用于前端对接多个webservice ，后端格式不统一的问题。
 */
@WebFilter(description = "firstServlet", urlPatterns = "/*"
		,initParams = { @WebInitParam(name = "excludedExt", value = "jpeg jpg png pdf ico html js")}
		)
public class ProxyFilter implements Filter {
	
	private Map<String, String> urlMap;  
	private ScriptToy funsDown;
	private ScriptToy funsUp;
	private List<String> excludedExt ;

    CloseableHttpClient httpclient ;  
    /**
     * Default constructor. 
     */
    public ProxyFilter() {
        // TODO Auto-generated constructor stub
        super();   
        urlMap = new HashMap<String,String>();
        InputStream site2apiStream = ClassLoader.getSystemResourceAsStream("site2api.json");
        String site2api = ScriptToy.readAll(site2apiStream); 
        JSONObject jsonObject  = JSONObject.parseObject(site2api);
        for(String key :jsonObject.keySet()) {
        	JSONArray apis = jsonObject.getJSONArray(key);
        	for(Object api:apis) { 
        		urlMap.put((String) api, key);
        	}
        };
        funsDown = new ScriptToy("funsDown.js");
        funsUp = new ScriptToy("funsUp.js");
        httpclient = HttpClients.createDefault();  
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		try {
			httpclient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException { 
		
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		String uri = httpRequest.getRequestURI();
				if(excludedExt.contains(uri.substring(uri.lastIndexOf('.')+1))){  
					chain.doFilter(request, response);
					return;
				};
		HttpServletResponse httpResponse = (HttpServletResponse)response;
		String method = httpRequest.getMethod();
		if(method.equalsIgnoreCase("get")) {
			doGet(httpRequest,httpResponse);
			return;
		}if(method.equalsIgnoreCase("post")) {
			doPost(httpRequest,httpResponse);  
			return;
		}  
		return;
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String uri = request.getRequestURI(); 
		String paramString = ScriptToy.generateParams(request.getParameterMap(),Charsets.UTF_8.displayName()); 
		HttpGet get = new HttpGet(urlMap.get(uri) + uri+"?"+paramString);
		copyHeaders(request, get);
		CloseableHttpResponse resp = httpclient.execute(get); 
        doResp(request, response, resp);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	        String params  = ScriptToy.readAll(request.getInputStream());  
				params =  funsUp.invokeFunction(request.getRequestURI(), params); 
    		String url = urlMap.get(request.getRequestURI())+request.getRequestURI();
	        HttpPost post = new HttpPost(url);  
				copyHeaders(request, post);
	        post.setEntity(new StringEntity(params, "UTF-8"));
	  	    CloseableHttpResponse resp =  httpclient.execute(post);
            doResp(request, response, resp);
	}

	private void doResp(HttpServletRequest request, HttpServletResponse response, CloseableHttpResponse resp)
			throws IOException {
		int state = resp.getStatusLine().getStatusCode();
		if (state == HttpStatus.SC_OK) {
		    String   jsonString = EntityUtils.toString(resp.getEntity()); 
		      jsonString =   funsDown.invokeFunction(request.getRequestURI() , jsonString); 
		      copyHeaders(resp,response);
		      resp.close();
		    response.getOutputStream().write(jsonString.getBytes());
		}
		else{
			response.setStatus(state);
		    //logger.error("请求返回:"+state+"("+url+")");
		}   
		response.getOutputStream().flush();response.getOutputStream().close();
	}
/**
 * 添加下游的header,webservice 服务器接收数据
 * @param request
 * @param post
 */
	private void copyHeaders(HttpServletRequest request, HttpRequestBase post) {
		Enumeration<String> itr = request.getHeaderNames();
		while(itr.hasMoreElements()) {
			String name = itr.nextElement();
			if(name.equalsIgnoreCase("Content-Length"))
				continue;
			post.setHeader(name, request.getHeader(name));	
		}
		 
	}
	
/**
 * 添加上游的header ，浏览器接收数据
 * @param resp
 * @param response
 */
	private void copyHeaders( HttpResponse resp,HttpServletResponse response) {
		Header[] headers = resp.getAllHeaders();
		
		for(Header header:headers) {
			response.setHeader(header.getName(),header.getValue());	
		} //指定允许其他域名访问
		response.setHeader("Access-Control-Allow-Origin", "*");//或指定域
		response.setHeader("Access-Control-Allow-Methods", "GET,POST");//响应类型
		response.setHeader("Access-Control-Allow-Headers", "x-requested-with,content-type");//响应头设置
		 
	}
	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		String str = fConfig.getInitParameter("excludedExt");
		excludedExt = Arrays.asList(str.split(" ")); 
	}

}

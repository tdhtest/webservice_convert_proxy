package com.yeexun.zzl.webservicetool;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress; 
import java.util.concurrent.Executors;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;    
/**
 * 
 * @author michazl
 * 用来测试的工具类
 */
public class FakeApiServer {   
    public static void main(String[] args) throws IOException {   
        int port = 8888;
		InetSocketAddress addr = new InetSocketAddress(port);   
        HttpServer server = HttpServer.create(addr, 0);   
  
        server.createContext("/", new MyHandler());   
        server.setExecutor(Executors.newCachedThreadPool());   
        server.start();   
        System.out.println("Server is listening on port "+port);   
    }   
}   
  
class MyHandler implements HttpHandler {   
    public void handle(HttpExchange exchange) throws IOException {   
        String requestMethod = exchange.getRequestMethod();    
        System.out.println(exchange.getRequestURI());//包括get params

        Headers responseHeaders = exchange.getResponseHeaders();    
        responseHeaders.set("Content-Type", "text/plain");   
        responseHeaders.set("AAAAA", "BBBBBB");//响应头设置 
        exchange.sendResponseHeaders(200, 0);    
        if (requestMethod.equalsIgnoreCase("GET")) { 
        } 
        if(requestMethod.equalsIgnoreCase("POST")) {
        	String jsonStr = ScriptToy.readAll(exchange.getRequestBody());
        	System.out.println(jsonStr);
        }
        OutputStream responseBody = exchange.getResponseBody();    
        responseBody.write("4".getBytes());
        responseBody.close();   
    }   
}
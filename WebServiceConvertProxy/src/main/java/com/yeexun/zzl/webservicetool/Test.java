package com.yeexun.zzl.webservicetool;

import java.util.Arrays;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager; 

public class Test {

	public static void main(String[] args) throws Exception {
		

		  List<String> excludedExt = Arrays.asList("jpeg jpg png pdf ico html js".split(" "));
		  for(String str:excludedExt) {
			  System.out.println(str);
		  }
		  ScriptEngineManager manager = new ScriptEngineManager();
          ScriptEngine engine = manager.getEngineByName("javascript"); 
            String script = "function transform(variable){\n" +
		        "if(variable == '4'){\n" +
		        "    return true;\n" +
		        "}else {\n" +
		        "    return false\n" +
		        "  }\n" +
		        "}"
		        + "function abc(){"
		        + "return true}"
		        + "";
			engine.eval(script);
            if (engine instanceof Invocable) {
                  Invocable in = (Invocable) engine;
                  System.out.println(in.invokeFunction("transform",4));
                  System.out.println(in.invokeFunction("abc",1,1));
            }
            System.out.println(script);
	}

}

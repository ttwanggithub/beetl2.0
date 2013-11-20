package org.beetl.core.attr;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.beetl.core.exception.TempException;

public class AAFactory {
	
	//已经为属性生成的访问代理类
	Map<String,AA> pojoCache = new HashMap<String,AA>();	
	Map<String,AA> generalGetCache = new HashMap<String,AA>();
	
	
	MapAA maa = new MapAA();
	
	public AA  build(Object o,int index,String name){
		// map属性
		if(o instanceof Map){
			return maa;
		}
		Class c = o.getClass();
		String className = o.getClass()+"$_$"+name;
		AA aa = pojoCache.get(className);
		if(aa!=null) return aa;
		
		FindResult pojoResult = this.findCommonInterfaceOrClass(c, name);
		if(pojoResult!=null){
			className = pojoResult.c.getClass()+"$_$"+name;
			aa = pojoCache.get(className);
			if(aa!=null){
				return aa;
			}else{
				aa = ASMUtil.instance().createAAClass(pojoResult.c, name, pojoResult.realMethodName, pojoResult.returnType);
				pojoCache.put(className, aa);
			}
			
		}else{
			//General Get	
			className = o.getClass()+"$_$"+name;
			AA aa = pojoCache.get(className);
			FindResult generaGetResult = findResult(c,"get","get");
			if(generaGetResult!=null){
				aa = ASMUtil.instance().createAAClass(generaGetResult.c, "get", "get", Object.class);
				aaCache.put(generaGetResult.c, aa);
			}else{
				//还是没有找到，抛错吧
				throw new TempException("未找到属性访问方法");
			}
		}
		
		
		
		return  aa;
		
		
		
		
		
	}
	
	public FindResult findCommonInterfaceOrClass(Class c,String name){
		StringBuilder mbuffer = new StringBuilder("get");
		mbuffer.append(name.substring(0, 1).toUpperCase()).append(name.substring(1));
		String getName = mbuffer.toString();
		mbuffer = new StringBuilder("is");
		mbuffer.append(name.substring(0, 1).toUpperCase()).append(name.substring(1));
		String isName =  mbuffer.toString();
		FindResult  findResult = findResult(c,getName,isName);
		return findResult;
		
	
		
	}
	
	private FindResult  findResult(Class c,String getName,String isName){
		FindResult result  = null;
		Method[] methods = c.getMethods();
		Method findMethod = null;
		for(Method m:methods){
			String name = m.getName();
			Class[] paras = m.getParameterTypes();
			if(paras.length!=0)continue;
			if(name.equals(getName)){
				findMethod = m; 
				break;
			}else if(name.equals(isName)){
				findMethod = m; 
				break;
			}
		}
		//判断父接口
		Class[] interfaces = c.getInterfaces();
		for(Class inc:interfaces){
			if(inc.getName().startsWith("java.")){
				//java包不需要考虑
				continue;
			}
			result = this.findResult(inc, getName, isName);
			if(result!=null){
				return result;
			}
		}
		
		Class parent = c.getSuperclass();
		if(!parent.getName().startsWith("java.")){
			result = this.findResult(parent, getName, isName);
			if(result!=null){
				return result;
			}
		}
		
		if(findMethod!=null){
			result = new FindResult();
			result.realMethodName = findMethod.getName();
			result.c = c;
			result.returnType = findMethod.getReturnType();
			return result;
		}else{
			return null;
		}
		
		
		
		
	}
	
	
	class FindResult {
		
		String realMethodName;
		Class c;
		Class returnType;
	}
}
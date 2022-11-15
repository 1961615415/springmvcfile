package cn.knet.businesstask.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextUtils implements ApplicationContextAware{
    
    public static ApplicationContext context;
 
    @Override
    public void setApplicationContext(ApplicationContext context)
            throws BeansException {
        SpringContextUtils.context = context;
    }
     
    @SuppressWarnings("unchecked")
	public static <T> T getBean(String beanId){
        return (T) context.getBean(beanId);
    }
    public static <T> T getBean(Class<T> clazz){
        return context.getBean(clazz);
    }
     
    public static ApplicationContext getContext(){
        return context;
    }
 
}
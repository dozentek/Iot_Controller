package cn.rh.iot;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class ContextAwareBeanLoader implements ApplicationContextAware {
	
	private static ApplicationContext context;

	public static <T> T getBean(Class<T> clazz) {
		if (context == null) {
			return null;
		}
		return context.getBean(clazz);
	}

	public static <T> T getBean(Class<T> clazz, String qualifier) {
		if (context == null) {
			return null;
		}
		return context.getBean(qualifier, clazz);
	}
	
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		ContextAwareBeanLoader.context = context;
	}
	
}

package evyframework.container.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class FooInterceptor implements MethodInterceptor {
	
	public FooInterceptor() {
		// System.err.println("FooInterceptor instantiated");
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		// System.err.println("method: " + invocation.getMethod().getName() +
		// " called");
		return invocation.proceed();
	}

}

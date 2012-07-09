package evyframework.common;

import org.junit.Assert;
import org.junit.Test;

public class ClassUtilsTest {
	
	@Test
	public void testIsAssignable() {
		Assert.assertTrue(ClassUtils.isAssignable(Integer.class, Integer.class));
		Assert.assertTrue(ClassUtils.isAssignable(Integer.class, int.class));
		Assert.assertFalse(ClassUtils.isAssignable(Integer.class, Long.class));
		Assert.assertFalse(ClassUtils.isAssignable(Integer.class, long.class));
	}

}

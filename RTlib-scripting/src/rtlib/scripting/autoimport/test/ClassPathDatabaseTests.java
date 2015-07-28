package rtlib.scripting.autoimport.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import rtlib.scripting.autoimport.ClassPathResolver;

public class ClassPathDatabaseTests
{

	@Test
	public void test() throws ClassNotFoundException
	{


		List<String> lListOfFoundClasses = ClassPathResolver.getFullyQualifiedNames("String");
		assertEquals(1, lListOfFoundClasses.size());
		System.out.println(lListOfFoundClasses);

		lListOfFoundClasses = ClassPathResolver.getFullyQualifiedNames("Math");
		assertEquals(1, lListOfFoundClasses.size());
		System.out.println(lListOfFoundClasses);

		lListOfFoundClasses = ClassPathResolver.getFullyQualifiedNames("ScriptingEngine");
		assertEquals(1, lListOfFoundClasses.size());
		System.out.println(lListOfFoundClasses);

	}

}
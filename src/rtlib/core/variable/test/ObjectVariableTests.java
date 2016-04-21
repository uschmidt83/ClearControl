package rtlib.core.variable.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import rtlib.core.variable.Variable;

public class ObjectVariableTests
{

	@Test
	public void DoubleVariableTest()
	{
		final Variable<Double> x = new Variable<Double>("x",
																																0.0);
		final Variable<Double> y = new Variable<Double>("y",
																																0.0);

		x.syncWith(y);
		assertEquals(new Double(0.0), x.get());
		assertEquals(new Double(0.0), y.get());

		x.set(1.0);
		assertEquals(new Double(1.0), x.get());
		assertEquals(new Double(1.0), y.get());

		y.set(2.0);
		assertEquals(new Double(2.0), x.get());
		assertEquals(new Double(2.0), y.get());

	}

}
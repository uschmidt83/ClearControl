package clearcontrol.core.units.test;

import static clearcontrol.core.units.Magnitude.Micro;
import static clearcontrol.core.units.Magnitude.Milli;
import static clearcontrol.core.units.Magnitude.Unit;
import static org.junit.Assert.assertEquals;

import clearcontrol.core.units.Magnitude;

import org.junit.Test;

public class MagnitudesTests
{

  @Test
  public void testShortcutMethods()
  {
    assertEquals(1000, Magnitude.milli2micro(1), 0.1);
    assertEquals(0.001, Magnitude.micro2milli(1), 0.1);
    assertEquals(1e6, Magnitude.unit2micro(1), 0.1);
    assertEquals(1e-6, Magnitude.micro2unit(1), 1e-7);
  }

  @Test
  public void testConvertFrom()
  {
    assertEquals(1000, Micro.convertFrom(1, Milli), 0.1);
    assertEquals(0.001, Milli.convertFrom(1, Micro), 0.1);
    assertEquals(1e6, Micro.convertFrom(1, Unit), 0.1);
    assertEquals(1e-6, Unit.convertFrom(1, Micro), 1e-7);
  }

}

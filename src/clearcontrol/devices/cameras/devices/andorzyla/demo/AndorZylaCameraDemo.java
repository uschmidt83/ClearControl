package clearcontrol.devices.cameras.devices.andorzyla.demo;

import andorsdkj.AndorSdkJ;
import andorsdkj.enums.TriggerMode;
import clearcontrol.core.variable.Variable;
import clearcontrol.devices.cameras.devices.andorzyla.AndorZylaStackCamera;
import clearcontrol.gui.video.video3d.Stack3DDisplay;
import clearcontrol.stack.StackInterface;

import org.junit.Test;

public class AndorZylaCameraDemo
{

  @Test
  public void testAndorZylaStackCameraOpenAndClose() throws Exception
  {
    // initializing andor environment
    AndorSdkJ lAndorEnv = new AndorSdkJ();
    lAndorEnv.open();
    AndorZylaStackCamera lZylaOne =
                                  new AndorZylaStackCamera(0,
                                                           TriggerMode.SOFTWARE);
    lZylaOne.stop();
    lAndorEnv.close();

  }

  @Test
  public void testAndorZyla3DDisplay() throws Exception
  {
    // initializing andor environment
    AndorSdkJ lAndorEnv = new AndorSdkJ();
    lAndorEnv.open();
    AndorZylaStackCamera lZylaOne =
                                  new AndorZylaStackCamera(0,
                                                           TriggerMode.SOFTWARE);

    final Stack3DDisplay lVideoFrame3DDisplay =
                                              new Stack3DDisplay("Test");
    final Variable<StackInterface> lFrameReferenceVariable =
                                                           lVideoFrame3DDisplay.getInputStackVariable();
    lVideoFrame3DDisplay.open();

    StackInterface lStack = lZylaOne.getStackVariable().get();

    lFrameReferenceVariable.set(lStack);

    lZylaOne.stop();
    lAndorEnv.close();

  }
}

package clearcontrol.hardware.stages.hub.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import clearcontrol.hardware.stages.devices.ecc100.ECC100StageDevice;
import clearcontrol.hardware.stages.devices.smc100.SMC100StageDevice;
import clearcontrol.hardware.stages.hub.StageDeviceHub;

import org.junit.Test;

public class StageHubDemo
{

  @Test
  public void test()
  {
    ECC100StageDevice lECC100StageDevice = new ECC100StageDevice();
    SMC100StageDevice lSMC100StageDevice =
                                         new SMC100StageDevice("SMC100",
                                                               "COM1");

    StageDeviceHub lStageHub = new StageDeviceHub("Hub");

    lStageHub.addDOF(lECC100StageDevice, 1);
    lStageHub.addDOF(lSMC100StageDevice, 0);

    assertTrue(lStageHub.open());

    assertEquals(2, lStageHub.getNumberOfDOFs());

    assertTrue(lStageHub.start());

    lStageHub.home(0);
    lStageHub.home(1);

    assertTrue(lStageHub.stop());

    lStageHub.close();

  }

}

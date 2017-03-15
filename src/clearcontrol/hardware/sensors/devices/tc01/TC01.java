package clearcontrol.hardware.sensors.devices.tc01;

import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.variable.Variable;
import clearcontrol.hardware.sensors.TemperatureSensorDeviceBase;
import clearcontrol.hardware.sensors.devices.tc01.bridj.TC01libLibrary;

import org.bridj.Pointer;

public class TC01 extends TemperatureSensorDeviceBase
{

  private NIThermoCoupleType mThermoCoupleNIType =
                                                 NIThermoCoupleType.K;
  private final boolean mIsDevicePresent;
  private final Pointer<Byte> mPhysicalChannelPointer;

  public TC01(String pPhysicalChannel,
              NIThermoCoupleType pNIThermoCoupleType,
              final int pDeviceIndex)
  {
    super("TC01");
    mThermoCoupleNIType = pNIThermoCoupleType;
    mIsDevicePresent =
                     MachineConfiguration.getCurrentMachineConfiguration()
                                         .getIsDevicePresent("ni.tc01",
                                                             pDeviceIndex);

    mPhysicalChannelPointer =
                            Pointer.pointerToCString(pPhysicalChannel);
  }

  @Override
  public boolean loop()
  {
    if (!mIsDevicePresent)
      return false;
    final Variable<Double> lTemperatureInCelciusVariable =
                                                         getTemperatureInCelciusVariable();
    final double lTemperatureInCelcius =
                                       TC01libLibrary.tC01lib(mPhysicalChannelPointer,
                                                              mThermoCoupleNIType.getValue());
    // System.out.println(lTemperatureInCelcius);
    lTemperatureInCelciusVariable.set(lTemperatureInCelcius);
    return true;
  }

  @Override
  public boolean open()
  {
    if (!mIsDevicePresent)
      return false;
    return true;
  }

  @Override
  public boolean close()
  {
    if (!mIsDevicePresent)
      return false;
    return true;
  }

}

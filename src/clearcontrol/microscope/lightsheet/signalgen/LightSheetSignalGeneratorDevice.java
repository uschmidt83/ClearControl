package clearcontrol.microscope.lightsheet.signalgen;

import java.util.concurrent.Future;

import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.device.queue.QueueDeviceInterface;
import clearcontrol.core.log.LoggingInterface;
import clearcontrol.devices.signalgen.SignalGeneratorInterface;
import clearcontrol.devices.signalgen.SignalGeneratorQueue;

/**
 * This device knows how to generate the signals for a light sheet microscope
 * (both detection and illumination signals)
 *
 * @author royer
 */
public class LightSheetSignalGeneratorDevice extends VirtualDevice
                                             implements
                                             QueueDeviceInterface<LightSheetSignalGeneratorQueue>,
                                             LoggingInterface
{

  private final SignalGeneratorInterface mDelegatedSignalGenerator;

  /**
   * Wraps a signal generator with a lightsheet signal generation. This
   * lightsheet signal generator simply adds a layer that translate detection
   * arm and lightsheet parameters to actual signals.
   * 
   * @param pSignalGeneratorInterface
   *          delegated signal generator
   * @return lightsheet signal generator
   */
  public static LightSheetSignalGeneratorDevice wrap(SignalGeneratorInterface pSignalGeneratorInterface)
  {
    return new LightSheetSignalGeneratorDevice(pSignalGeneratorInterface);
  }

  /**
   * Instantiates a lightsheet signal generator that delegates to another signal
   * generator for the actual signal generation. This signal generator simply
   * adds a layer that translate detection arm and lightsheet parameters to
   * actual signals.
   * 
   * @param pSignalGeneratorInterface
   *          delegated signal generator
   */
  public LightSheetSignalGeneratorDevice(SignalGeneratorInterface pSignalGeneratorInterface)
  {
    super("LightSheet" + pSignalGeneratorInterface.getName());
    mDelegatedSignalGenerator = pSignalGeneratorInterface;
  }

  @Override
  public boolean open()
  {
    return super.open() && mDelegatedSignalGenerator.open();
  }

  @Override
  public boolean close()
  {
    return mDelegatedSignalGenerator.close() && super.close();
  }

  @Override
  public LightSheetSignalGeneratorQueue requestQueue()
  {
    return new LightSheetSignalGeneratorQueue(this,
                                              mDelegatedSignalGenerator.requestQueue());
  }

  @Override
  public Future<Boolean> playQueue(LightSheetSignalGeneratorQueue pQueue)
  {
    SignalGeneratorQueue lDelegatedQueue = pQueue.getDelegatedQueue();
    return mDelegatedSignalGenerator.playQueue(lDelegatedQueue);
  }

}

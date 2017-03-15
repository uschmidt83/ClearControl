package clearcontrol.hardware.cameras;

import java.util.concurrent.atomic.AtomicBoolean;

import clearcontrol.core.variable.Variable;
import clearcontrol.device.VirtualDevice;

public abstract class CameraDeviceBase extends VirtualDevice
                                       implements
                                       CameraDeviceInterface
{

  protected Variable<Double> mExposureInMicrosecondsVariable,
      mPixelSizeinNanometersVariable,
      mLineReadOutTimeInMicrosecondsVariable;

  protected Variable<Long> mStackBytesPerPixelVariable,
      mStackWidthVariable, mStackHeightVariable,
      mStackMaxWidthVariable, mStackMaxHeightVariable,
      mStackDepthVariable;

  protected Variable<Boolean> mTriggerVariable;

  protected Variable<Integer> mChannelVariable;

  private AtomicBoolean mReOpenDeviceNeeded =
                                            new AtomicBoolean(false);

  protected Variable<Boolean> mIsAcquiring;

  public CameraDeviceBase(final String pDeviceName)
  {
    super(pDeviceName);
  }

  @Override
  public void setExposure(double pExposureInMicroseconds)
  {
    getExposureInMicrosecondsVariable().set(pExposureInMicroseconds);
  }

  @Override
  public double getExposure()
  {
    return getExposureInMicrosecondsVariable().get();
  }

  @Override
  public void trigger()
  {
    getTriggerVariable().setEdge(false, true);
  }

  public boolean isReOpenDeviceNeeded()
  {
    return mReOpenDeviceNeeded.get();
  }

  public void requestReOpen()
  {
    mReOpenDeviceNeeded.set(true);
  }

  public void clearReOpen()
  {
    mReOpenDeviceNeeded.set(false);
  }

  public abstract void reopen();

  @Override
  public Variable<Integer> getChannelVariable()
  {
    return mChannelVariable;
  }

  public Variable<Long> getStackBytesPerPixelVariable()
  {
    return mStackBytesPerPixelVariable;
  }

  public Variable<Long> getStackWidthVariable()
  {
    return mStackWidthVariable;
  }

  public Variable<Long> getStackHeightVariable()
  {
    return mStackHeightVariable;
  }

  public Variable<Long> getStackDepthVariable()
  {
    return mStackDepthVariable;
  }

  public Variable<Long> getStackMaxWidthVariable()
  {
    return mStackMaxWidthVariable;
  }

  public Variable<Long> getStackMaxHeightVariable()
  {
    return mStackMaxHeightVariable;
  }

  @Override
  public Variable<Double> getExposureInMicrosecondsVariable()
  {
    return mExposureInMicrosecondsVariable;
  }

  @Override
  public Variable<Double> getPixelSizeInNanometersVariable()
  {
    return mPixelSizeinNanometersVariable;
  }

  @Override
  public Variable<Boolean> getIsAcquiringVariable()
  {
    return mIsAcquiring;
  }

  @Override
  public Variable<Double> getLineReadOutTimeInMicrosecondsVariable()
  {
    return mLineReadOutTimeInMicrosecondsVariable;
  }

  @Override
  public Variable<Boolean> getTriggerVariable()
  {
    return mTriggerVariable;
  }

}

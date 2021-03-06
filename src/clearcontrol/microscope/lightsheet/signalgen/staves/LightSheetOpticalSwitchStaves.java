package clearcontrol.microscope.lightsheet.signalgen.staves;

import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.devices.signalgen.movement.Movement;
import clearcontrol.devices.signalgen.staves.ConstantStave;
import clearcontrol.microscope.lightsheet.component.opticalswitch.LightSheetOpticalSwitch;
import clearcontrol.microscope.lightsheet.component.opticalswitch.LightSheetOpticalSwitchQueue;

/**
 * Light sheet microscope optical switch staves. These staves are used when
 * controlling a lightsheeet microscope using digital signals to switch
 * lightsheets on and off.
 *
 * @author royer
 */
public class LightSheetOpticalSwitchStaves
{
  private LightSheetOpticalSwitchQueue mLightSheetOpticalSwitchQueue;

  private final ConstantStave[] mBitStave;

  private int[] mStaveIndex;

  /**
   * Instanciates given a lightsheet optical switch device and default stave
   * index.
   * 
   * @param pLightSheetOpticalSwitchQueue
   *          lightsheet optical switch device queue
   * @param pDefaultStaveIndex
   *          default stave index
   */
  public LightSheetOpticalSwitchStaves(LightSheetOpticalSwitchQueue pLightSheetOpticalSwitchQueue,
                                       int pDefaultStaveIndex)
  {
    super();
    mLightSheetOpticalSwitchQueue = pLightSheetOpticalSwitchQueue;
    int lNumberOfBits =
                      (int) Math.ceil(Math.log(mLightSheetOpticalSwitchQueue.getNumberOfSwitches())
                                      / Math.log(2));
    mBitStave = new ConstantStave[lNumberOfBits];
    mStaveIndex = new int[lNumberOfBits];

    for (int i = 0; i < mBitStave.length; i++)
    {
      mStaveIndex[i] =
                     MachineConfiguration.getCurrentMachineConfiguration()
                                         .getIntegerProperty("device.lsm.switch."
                                                             + getLightSheetOpticalSwitch().getName()
                                                             + i
                                                             + ".index",
                                                             pDefaultStaveIndex);
      mBitStave[i] = new ConstantStave("lightsheet.s." + i, 0);
    }
  }

  private LightSheetOpticalSwitch getLightSheetOpticalSwitch()
  {
    return mLightSheetOpticalSwitchQueue.getLightSheetOpticalSwitch();
  }

  /**
   * Adds staves to staging movements.
   * 
   * @param pBeforeExposureMovement
   *          before exposure movement
   * @param pExposureMovement
   *          exposure movement
   */
  public void addStavesToMovements(Movement pBeforeExposureMovement,
                                   Movement pExposureMovement)
  {
    for (int i = 0; i < mBitStave.length; i++)
    {
      pBeforeExposureMovement.setStave(mStaveIndex[i], mBitStave[i]);
      pExposureMovement.setStave(mStaveIndex[i], mBitStave[i]);
    }
  }

  /**
   * Updates staves
   * 
   * @param pExposureMovement
   *          exposure movement
   * @param pBeforeExposureMovement
   *          before exposure movement
   */
  public void update(Movement pBeforeExposureMovement,
                     Movement pExposureMovement)
  {
    synchronized (this)
    {
      for (int i = 0; i < mBitStave.length; i++)
      {
        mBitStave[i].setValue(getLightSheetOpticalSwitch().getSwitchVariable(i)
                                                          .get() ? 1
                                                                 : 0);
      }
    }
  }
}

package clearcontrol.microscope.lightsheet.signalgen.staves;

import static java.lang.Math.cos;
import static java.lang.Math.round;
import static java.lang.Math.sin;

import java.util.concurrent.TimeUnit;

import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.log.LoggingInterface;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.signalgen.movement.Movement;
import clearcontrol.devices.signalgen.staves.ConstantStave;
import clearcontrol.devices.signalgen.staves.EdgeStave;
import clearcontrol.devices.signalgen.staves.IntervalStave;
import clearcontrol.devices.signalgen.staves.RampSteppingStave;
import clearcontrol.devices.signalgen.staves.StaveInterface;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheet;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheetQueue;
import clearcontrol.microscope.lightsheet.component.lightsheet.si.StructuredIlluminationPatternInterface;

/**
 *
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public class LightSheetStaves implements LoggingInterface
{
  private LightSheetQueue mLightSheetQueue;

  private final BoundedVariable<Double> mLineExposureInMicrosecondsVariable =
                                                                            new BoundedVariable<Double>("LineExposureInMicroseconds",
                                                                                                        10.0);

  private RampSteppingStave mBeforeExposureZStave,
      mBeforeExposureYStave, mExposureYStave, mExposureZStave;

  private ConstantStave mBeforeExposureXStave, mExposureXStave,
      mBeforeExposureBStave, mExposureBStave, mBeforeExposureWStave,
      mExposureWStave, mBeforeExposureLAStave, mExposureLAStave;
  private IntervalStave mNonSIIluminationLaserTriggerStave;

  private EdgeStave mBeforeExposureTStave, mExposureTStave;

  public LightSheetStaves(LightSheetQueue pLightSheetQueue)
  {
    super();
    mLightSheetQueue = pLightSheetQueue;

    mBeforeExposureLAStave =
                           new ConstantStave("laser.beforeexp.am", 0);
    mExposureLAStave = new ConstantStave("laser.exposure.am", 0);

    mBeforeExposureXStave = new ConstantStave("lightsheet.x.be", 0);
    mBeforeExposureYStave = new RampSteppingStave("lightsheet.y.be");
    mBeforeExposureZStave = new RampSteppingStave("lightsheet.z.be");
    mBeforeExposureBStave = new ConstantStave("lightsheet.b.be", 0);
    mBeforeExposureWStave = new ConstantStave("lightsheet.r.be", 0);
    mBeforeExposureTStave = new EdgeStave("trigger.out.be", 1, 1, 0);

    mExposureXStave = new ConstantStave("lightsheet.x.e", 0);
    mExposureYStave = new RampSteppingStave("lightsheet.y.e");
    mExposureZStave = new RampSteppingStave("lightsheet.z.e");
    mExposureBStave = new ConstantStave("lightsheet.b.e", 0);
    mExposureWStave = new ConstantStave("lightsheet.r.e", 0);
    mExposureTStave = new EdgeStave("trigger.out.e", 1, 0, 0);

    mNonSIIluminationLaserTriggerStave =
                                       new IntervalStave("trigger.out",
                                                         0,
                                                         1,
                                                         1,
                                                         0);

  }

  private LightSheet getLightSheet()
  {
    return mLightSheetQueue.getLightSheet();
  }

  public void addStavesToMovements(Movement pBeforeExposureMovement,
                                   Movement pExposureMovement)
  {
    ensureStavesAddedToBeforeExposureMovement(pBeforeExposureMovement);
    ensureStavesAddedToExposureMovement(pExposureMovement);
  }

  public void ensureStavesAddedToBeforeExposureMovement(Movement pBeforeExposureMovement)
  {
    final MachineConfiguration lCurrentMachineConfiguration =
                                                            MachineConfiguration.getCurrentMachineConfiguration();

    // Analog outputs before exposure:
    mBeforeExposureXStave =
                          pBeforeExposureMovement.ensureSetStave(lCurrentMachineConfiguration.getIntegerProperty("device.lsm.lightsheet."
                                                                                                                 + getLightSheet().getName()
                                                                                                                                  .toLowerCase()
                                                                                                                 + ".x.index",
                                                                                                                 2),
                                                                 mBeforeExposureXStave);

    mBeforeExposureYStave =
                          pBeforeExposureMovement.ensureSetStave(lCurrentMachineConfiguration.getIntegerProperty("device.lsm.lightsheet."
                                                                                                                 + getLightSheet().getName()
                                                                                                                                  .toLowerCase()
                                                                                                                 + ".y.index",
                                                                                                                 3),
                                                                 mBeforeExposureYStave);

    mBeforeExposureZStave =
                          pBeforeExposureMovement.ensureSetStave(lCurrentMachineConfiguration.getIntegerProperty("device.lsm.lightsheet."
                                                                                                                 + getLightSheet().getName()
                                                                                                                                  .toLowerCase()
                                                                                                                 + ".z.index",
                                                                                                                 4),
                                                                 mBeforeExposureZStave);

    mBeforeExposureBStave =
                          pBeforeExposureMovement.ensureSetStave(lCurrentMachineConfiguration.getIntegerProperty("device.lsm.lightsheet."
                                                                                                                 + getLightSheet().getName()
                                                                                                                                  .toLowerCase()
                                                                                                                 + ".b.index",
                                                                                                                 5),
                                                                 mBeforeExposureBStave);

    mBeforeExposureWStave =
                          pBeforeExposureMovement.ensureSetStave(lCurrentMachineConfiguration.getIntegerProperty("device.lsm.lightsheet."
                                                                                                                 + getLightSheet().getName()
                                                                                                                                  .toLowerCase()
                                                                                                                 + ".w.index",
                                                                                                                 6),
                                                                 mBeforeExposureWStave);

    mBeforeExposureLAStave =
                           pBeforeExposureMovement.ensureSetStave(lCurrentMachineConfiguration.getIntegerProperty("device.lsm.lightsheet."
                                                                                                                  + getLightSheet().getName()
                                                                                                                                   .toLowerCase()
                                                                                                                  + ".la.index",
                                                                                                                  7),
                                                                  mBeforeExposureLAStave);

    mBeforeExposureTStave =
                          pBeforeExposureMovement.ensureSetStave(lCurrentMachineConfiguration.getIntegerProperty("device.lsm.lightsheet."
                                                                                                                 + getLightSheet().getName()
                                                                                                                                  .toLowerCase()
                                                                                                                 + ".t.index",
                                                                                                                 8 + 7),
                                                                 mBeforeExposureTStave);

  }

  public void ensureStavesAddedToExposureMovement(Movement pExposureMovement)
  {
    final MachineConfiguration lCurrentMachineConfiguration =
                                                            MachineConfiguration.getCurrentMachineConfiguration();

    // Analog outputs at exposure:

    mExposureXStave =
                    pExposureMovement.ensureSetStave(lCurrentMachineConfiguration.getIntegerProperty("device.lsm.lightsheet."
                                                                                                     + getLightSheet().getName()
                                                                                                                      .toLowerCase()
                                                                                                     + ".x.index",
                                                                                                     2),
                                                     mExposureXStave);

    mExposureYStave =
                    pExposureMovement.ensureSetStave(lCurrentMachineConfiguration.getIntegerProperty("device.lsm.lightsheet."
                                                                                                     + getLightSheet().getName()
                                                                                                                      .toLowerCase()
                                                                                                     + ".y.index",
                                                                                                     3),
                                                     mExposureYStave);

    mExposureZStave =
                    pExposureMovement.ensureSetStave(lCurrentMachineConfiguration.getIntegerProperty("device.lsm.lightsheet."
                                                                                                     + getLightSheet().getName()
                                                                                                                      .toLowerCase()
                                                                                                     + ".z.index",
                                                                                                     4),
                                                     mExposureZStave);

    mExposureBStave =
                    pExposureMovement.ensureSetStave(lCurrentMachineConfiguration.getIntegerProperty("device.lsm.lightsheet."
                                                                                                     + getLightSheet().getName()
                                                                                                                      .toLowerCase()
                                                                                                     + ".b.index",
                                                                                                     5),
                                                     mExposureBStave);

    mExposureWStave =
                    pExposureMovement.ensureSetStave(lCurrentMachineConfiguration.getIntegerProperty("device.lsm.lightsheet."
                                                                                                     + getLightSheet().getName()
                                                                                                                      .toLowerCase()
                                                                                                     + ".w.index",
                                                                                                     6),
                                                     mExposureWStave);

    mExposureLAStave =
                     pExposureMovement.ensureSetStave(lCurrentMachineConfiguration.getIntegerProperty("device.lsm.lightsheet."
                                                                                                      + getLightSheet().getName()
                                                                                                                       .toLowerCase()
                                                                                                      + ".la.index",
                                                                                                      7),
                                                      mExposureLAStave);

    mExposureTStave =
                    pExposureMovement.ensureSetStave(lCurrentMachineConfiguration.getIntegerProperty("device.lsm.lightsheet."
                                                                                                     + getLightSheet().getName()
                                                                                                                      .toLowerCase()
                                                                                                     + ".t.index",
                                                                                                     8 + 7),
                                                     mExposureTStave);

    for (int i =
               0; i < mLightSheetQueue.getNumberOfLaserDigitalControls(); i++)
      mNonSIIluminationLaserTriggerStave =
                                         setLaserDigitalTriggerStave(pExposureMovement,
                                                                     i,
                                                                     mNonSIIluminationLaserTriggerStave);/**/

  }

  public void update(Movement mBeforeExposureMovement,
                     Movement mExposureMovement)
  {
    synchronized (this)
    {
      // info("Updating: " + getLightSheet().getName());

      final double lReadoutTimeInMicroseconds =
                                              getBeforeExposureMovementDuration(TimeUnit.MICROSECONDS);
      final double lExposureMovementTimeInMicroseconds =
                                                       getExposureMovementDuration(TimeUnit.MICROSECONDS);

      mBeforeExposureMovement.setDuration(round(lReadoutTimeInMicroseconds),
                                          TimeUnit.MICROSECONDS);

      mExposureMovement.setDuration(round(lExposureMovementTimeInMicroseconds),
                                    TimeUnit.MICROSECONDS);

      final double lLineExposureTimeInMicroseconds =
                                                   lReadoutTimeInMicroseconds
                                                     + lExposureMovementTimeInMicroseconds;
      mLineExposureInMicrosecondsVariable.set(lLineExposureTimeInMicroseconds);

      final double lGalvoYOffsetBeforeRotation =
                                               mLightSheetQueue.getYVariable()
                                                               .get()
                                                               .doubleValue();
      final double lGalvoZOffsetBeforeRotation =
                                               mLightSheetQueue.getZVariable()
                                                               .get()
                                                               .doubleValue();

      final double lGalvoYOffset =
                                 galvoRotateY(lGalvoYOffsetBeforeRotation,
                                              lGalvoZOffsetBeforeRotation);
      final double lGalvoZOffset =
                                 galvoRotateZ(lGalvoYOffsetBeforeRotation,
                                              lGalvoZOffsetBeforeRotation);

      final double lLightSheetHeight =
                                     getLightSheet().getHeightFunction()
                                                    .get()
                                                    .value(mLightSheetQueue.getHeightVariable()

                                                                           .get()
                                                                           .doubleValue())
                                       * mLightSheetQueue.getOverScanVariable()
                                                         .get()
                                                         .doubleValue();
      final double lGalvoAmplitudeY = galvoRotateY(lLightSheetHeight,
                                                   0);
      final double lGalvoAmplitudeZ = galvoRotateZ(lLightSheetHeight,
                                                   0);

      final double lGalvoYLowValue =
                                   getLightSheet().getYFunction()
                                                  .get()
                                                  .value(lGalvoYOffset
                                                         - lGalvoAmplitudeY);
      final double lGalvoYHighValue =
                                    getLightSheet().getYFunction()
                                                   .get()
                                                   .value(lGalvoYOffset
                                                          + lGalvoAmplitudeY);

      final double lGalvoZLowValue =
                                   getLightSheet().getZFunction()
                                                  .get()
                                                  .value(lGalvoZOffset
                                                         - lGalvoAmplitudeZ);
      final double lGalvoZHighValue =
                                    getLightSheet().getZFunction()
                                                   .get()
                                                   .value(lGalvoZOffset
                                                          + lGalvoAmplitudeZ);

      mBeforeExposureYStave.setSyncStart(0);
      mBeforeExposureYStave.setSyncStop(1);
      mBeforeExposureYStave.setStartValue((float) lGalvoYHighValue);
      mBeforeExposureYStave.setStopValue((float) lGalvoYLowValue);
      mBeforeExposureYStave.setExponent(0.2f);

      mBeforeExposureZStave.setSyncStart(0);
      mBeforeExposureZStave.setSyncStop(1);
      mBeforeExposureZStave.setStartValue((float) lGalvoZHighValue);
      mBeforeExposureZStave.setStopValue((float) lGalvoZLowValue);

      mExposureYStave.setSyncStart(0);
      mExposureYStave.setSyncStop(1);
      mExposureYStave.setStartValue((float) lGalvoYLowValue);
      mExposureYStave.setStopValue((float) lGalvoYHighValue);
      mExposureYStave.setOutsideValue((float) lGalvoYHighValue);
      mExposureYStave.setNoJump(true);

      mExposureZStave.setSyncStart(0);
      mExposureZStave.setSyncStop(1);
      mExposureZStave.setStartValue((float) lGalvoZLowValue);
      mExposureZStave.setStopValue((float) lGalvoZHighValue);
      mExposureZStave.setOutsideValue((float) lGalvoZHighValue);
      mExposureZStave.setNoJump(true);

      mBeforeExposureXStave.setValue((float) getLightSheet().getXFunction()
                                                            .get()
                                                            .value(mLightSheetQueue.getXVariable()
                                                                                   .get()
                                                                                   .doubleValue()));
      mExposureXStave.setValue((float) getLightSheet().getXFunction()
                                                      .get()
                                                      .value(mLightSheetQueue.getXVariable()
                                                                             .get()
                                                                             .doubleValue()));

      mBeforeExposureBStave.setValue((float) getLightSheet().getBetaFunction()
                                                            .get()
                                                            .value(mLightSheetQueue.getBetaInDegreesVariable()
                                                                                   .get()
                                                                                   .doubleValue()));
      mExposureBStave.setValue((float) getLightSheet().getBetaFunction()
                                                      .get()
                                                      .value(mLightSheetQueue.getBetaInDegreesVariable()
                                                                             .get()
                                                                             .doubleValue()));

      /*final double lFocalLength = mFocalLengthInMicronsVariable.get();
      final double lLambdaInMicrons = mLambdaInMicronsVariable.get();
      final double lLightSheetRangeInMicrons = mWidthVariable.getValue();
      
      final double lIrisDiameterInMm = GaussianBeamGeometry.getBeamIrisDiameter(lFocalLength,
                                                                                lLambdaInMicrons,
                                                                                lLightSheetRangeInMicrons);/**/
      double lWidthValue =
                         getLightSheet().getWidthFunction()
                                        .get()
                                        .value(mLightSheetQueue.getWidthVariable()
                                                               .get()
                                                               .doubleValue());

      mBeforeExposureWStave.setValue((float) lWidthValue);
      mExposureWStave.setValue((float) lWidthValue);

      final double lOverscan = mLightSheetQueue.getOverScanVariable()
                                               .get()
                                               .doubleValue();
      double lMarginTimeInMicroseconds = (lOverscan - 1)
                                         / (2 * lOverscan)
                                         * lExposureMovementTimeInMicroseconds;
      final double lMarginTimeRelativeUnits =
                                            microsecondsToRelative(lExposureMovementTimeInMicroseconds,
                                                                   lMarginTimeInMicroseconds);

      boolean lIsStepping = true;
      for (int i =
                 0; i < mLightSheetQueue.getNumberOfLaserDigitalControls(); i++)
        lIsStepping &= mLightSheetQueue.getSIPatternOnOffVariable(i)
                                       .get();

      mBeforeExposureYStave.setStepping(lIsStepping);
      mExposureYStave.setStepping(lIsStepping);
      mBeforeExposureZStave.setStepping(lIsStepping);
      mExposureZStave.setStepping(lIsStepping);

      for (int i =
                 0; i < mLightSheetQueue.getNumberOfLaserDigitalControls(); i++)
      {
        final Variable<Boolean> lLaserBooleanVariable =
                                                      mLightSheetQueue.getLaserOnOffArrayVariable(i);

        if (mLightSheetQueue.getSIPatternOnOffVariable(i).get())
        {

          final StructuredIlluminationPatternInterface lStructuredIlluminatioPatternInterface =
                                                                                              mLightSheetQueue.getSIPatternVariable(i)
                                                                                                              .get();
          final StaveInterface lSIIlluminationLaserTriggerStave =
                                                                lStructuredIlluminatioPatternInterface.getStave(lMarginTimeRelativeUnits);
          lSIIlluminationLaserTriggerStave.setEnabled(lLaserBooleanVariable.get());

          setLaserDigitalTriggerStave(mExposureMovement,
                                      i,
                                      lSIIlluminationLaserTriggerStave);
        }
        else
        {
          mNonSIIluminationLaserTriggerStave.setEnabled(lLaserBooleanVariable.get());
          mNonSIIluminationLaserTriggerStave.setStart((float) lMarginTimeRelativeUnits);
          mNonSIIluminationLaserTriggerStave.setStop((float) (1
                                                              - lMarginTimeRelativeUnits));
          setLaserDigitalTriggerStave(mExposureMovement,
                                      i,
                                      mNonSIIluminationLaserTriggerStave);
        }

      }

      double lPowerValue =
                         getLightSheet().getPowerFunction()
                                        .get()
                                        .value(mLightSheetQueue.getPowerVariable()
                                                               .get()
                                                               .doubleValue());

      if (mLightSheetQueue.getAdaptPowerToWidthHeightVariable().get())
      {
        double lWidthPowerFactor =
                                 getLightSheet().getWidthPowerFunction()
                                                .get()
                                                .value(lWidthValue);

        double lHeightPowerFactor =
                                  getLightSheet().getHeightPowerFunction()
                                                 .get()
                                                 .value(lLightSheetHeight
                                                        / lOverscan);/**/

        lPowerValue *= lWidthPowerFactor * lHeightPowerFactor;
      }

      mBeforeExposureLAStave.setValue(0f);
      mExposureLAStave.setValue((float) lPowerValue);

    }

  }

  private <O extends StaveInterface> O setLaserDigitalTriggerStave(Movement pExposureMovement,
                                                                   int i,
                                                                   O pStave)
  {

    final int lLaserDigitalLineIndex =
                                     MachineConfiguration.getCurrentMachineConfiguration()
                                                         .getIntegerProperty("device.lsm.lightsheet."
                                                                             + getLightSheet().getName()
                                                                                              .toLowerCase()
                                                                             + ".ld.index"
                                                                             + i,
                                                                             8 + i);
    return pExposureMovement.ensureSetStave(lLaserDigitalLineIndex,
                                            pStave);
  }

  public long getExposureMovementDuration(TimeUnit pTimeUnit)
  {
    return pTimeUnit.convert((long) (mLightSheetQueue.getEffectiveExposureInSecondsVariable()
                                                     .get()
                                                     .doubleValue()
                                     * 1e6),
                             TimeUnit.MICROSECONDS);
  }

  public long getBeforeExposureMovementDuration(TimeUnit pTimeUnit)
  {
    return pTimeUnit.convert((long) (mLightSheetQueue.getReadoutTimeInMicrosecondsPerLineVariable()
                                                     .get()
                                                     .doubleValue()
                                     * mLightSheetQueue.getImageHeightVariable()
                                                       .get()
                                                       .doubleValue()
                                     / 2),
                             TimeUnit.MICROSECONDS);
  }

  private double galvoRotateY(double pY, double pZ)
  {
    final double lAlpha =
                        Math.toRadians(getLightSheet().getAlphaFunction()
                                                      .get()
                                                      .value(mLightSheetQueue.getAlphaInDegreesVariable()
                                                                             .get()
                                                                             .doubleValue()));
    return pY * cos(lAlpha) - pZ * sin(lAlpha);
  }

  private double galvoRotateZ(double pY, double pZ)
  {
    final double lAlpha =
                        Math.toRadians(getLightSheet().getAlphaFunction()
                                                      .get()
                                                      .value(mLightSheetQueue.getAlphaInDegreesVariable()
                                                                             .get()
                                                                             .doubleValue()));
    return pY * sin(lAlpha) + pZ * cos(lAlpha);
  }

  private static double microsecondsToRelative(final double pTotalTime,
                                               final double pSubTime)
  {
    return pSubTime / pTotalTime;
  }

  public RampSteppingStave getGalvoScannerStaveBeforeExposureZ()
  {
    return mBeforeExposureZStave;
  }

  public RampSteppingStave getGalvoScannerStaveBeforeExposureY()
  {
    return mBeforeExposureYStave;
  }

  public ConstantStave getIllumPifocStaveBeforeExposureX()
  {
    return mBeforeExposureXStave;
  }

  public RampSteppingStave getGalvoScannerStaveExposureZ()
  {
    return mExposureZStave;
  }

  public RampSteppingStave getGalvoScannerStaveExposureY()
  {
    return mExposureYStave;
  }

  public ConstantStave getIllumPifocStaveExposureX()
  {
    return mExposureXStave;
  }

  public EdgeStave getTriggerOutStaveBeforeExposure()
  {
    return mBeforeExposureTStave;
  }

  public EdgeStave getTriggerOutStaveExposure()
  {
    return mExposureTStave;
  }

  public ConstantStave getLaserAnalogModulationBeforeExposure()
  {
    return mBeforeExposureLAStave;
  }

  public ConstantStave getLaserAnalogModulationExposure()
  {
    return mExposureLAStave;
  }

}

package clearcontrol.microscope.lightsheet.calibrator.modules;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import clearcontrol.core.math.argmax.ArgMaxFinder1DInterface;
import clearcontrol.core.math.argmax.Fitting1D;
import clearcontrol.core.math.argmax.methods.ModeArgMaxFinder;
import clearcontrol.core.math.functions.UnivariateAffineFunction;
import clearcontrol.core.math.regression.linear.TheilSenEstimator;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.plots.MultiPlot;
import clearcontrol.gui.plots.PlotTab;
import clearcontrol.ip.iqm.DCTS2D;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.calibrator.Calibrator;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArmInterface;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheetInterface;
import clearcontrol.scripting.engine.ScriptingEngine;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.StackInterface;
import gnu.trove.list.array.TDoubleArrayList;

import org.apache.commons.collections4.map.MultiKeyMap;

/**
 * Calibration module for the Z position of lightsheets and detection arms
 *
 * @author royer
 */
public class CalibrationZ
{

  private final Calibrator mCalibrator;
  private final LightSheetMicroscope mLightSheetMicroscope;
  private ArgMaxFinder1DInterface mArgMaxFinder;
  private MultiPlot mMultiPlotZFocusCurves, mMultiPlotZModels;
  private MultiKeyMap<Integer, UnivariateAffineFunction> mModels;
  private int mNumberOfDetectionArmDevices;
  private int mNumberOfLightSheetDevices;
  private int mIteration;
  private DCTS2D mDCTS2D;

  /**
   * Instanciates a Z calibrator module given calibrator
   * 
   * @param pCalibrator
   *          calibrator
   */
  public CalibrationZ(Calibrator pCalibrator)
  {
    super();
    mCalibrator = pCalibrator;
    mLightSheetMicroscope = pCalibrator.getLightSheetMicroscope();

    mMultiPlotZFocusCurves =
                           MultiPlot.getMultiPlot(this.getClass()
                                                      .getSimpleName()
                                                  + " calibration: focus curves");
    mMultiPlotZFocusCurves.setVisible(false);

    mMultiPlotZModels =
                      MultiPlot.getMultiPlot(this.getClass()
                                                 .getSimpleName()
                                             + " calibration: models");
    mMultiPlotZModels.setVisible(false);

    mNumberOfDetectionArmDevices =
                                 mLightSheetMicroscope.getDeviceLists()
                                                      .getNumberOfDevices(DetectionArmInterface.class);

    mNumberOfLightSheetDevices =
                               mLightSheetMicroscope.getDeviceLists()
                                                    .getNumberOfDevices(LightSheetInterface.class);

    mModels = new MultiKeyMap<>();
  }

  /**
   * Performs calibrationfor a given lightsheet index
   * 
   * @param pLightSheetIndex
   *          lightsheet index
   * @param pNumberOfDSamples
   *          number of detection Z samples
   * @param pNumberOfISamples
   *          number of illumination Z samples
   * @return true -> success
   */
  public boolean calibrate(int pLightSheetIndex,
                           int pNumberOfDSamples,
                           int pNumberOfISamples,
                           double pSearchAmplitude)
  {
    mArgMaxFinder = new ModeArgMaxFinder();

    mMultiPlotZFocusCurves.clear();
    mMultiPlotZFocusCurves.setVisible(true);

    mIteration++;
    if (pLightSheetIndex == 0 && mIteration == 0)
      mMultiPlotZModels.clear();
    if (!mMultiPlotZModels.isVisible())
      mMultiPlotZModels.setVisible(true);

    final TheilSenEstimator[] lTheilSenEstimators =
                                                  new TheilSenEstimator[mNumberOfDetectionArmDevices];
    final PlotTab[] lPlots =
                           new PlotTab[mNumberOfDetectionArmDevices];
    for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
    {
      lTheilSenEstimators[d] = new TheilSenEstimator();

      lPlots[d] =
                mMultiPlotZModels.getPlot(String.format("iter=%d, D=%d, I=%d",
                                                        mIteration,
                                                        d,
                                                        pLightSheetIndex));

      lPlots[d].setScatterPlot("D" + d);
      lPlots[d].setLinePlot("fit D" + d);
    }

    LightSheetInterface lLightSheetDevice =
                                          mLightSheetMicroscope.getDeviceLists()
                                                               .getDevice(LightSheetInterface.class,
                                                                          pLightSheetIndex);

    BoundedVariable<Number> lZVariable =
                                       lLightSheetDevice.getZVariable();
    double lMinIZ = lZVariable.getMin().doubleValue();
    double lMaxIZ = lZVariable.getMax().doubleValue();

    double lStepIZ = (lMaxIZ - lMinIZ) / (pNumberOfISamples - 1);

    double lMinZ = Double.NEGATIVE_INFINITY;
    double lMaxZ = Double.POSITIVE_INFINITY;

    double lAmplitudeZ = 0.5 * pSearchAmplitude * (lMaxIZ - lMinIZ);
    System.out.println("lAmplitudeZ=" + lAmplitudeZ);

    for (double iz = lMinIZ; iz <= lMaxIZ; iz += lStepIZ)
    {

      final double lPerturbedIZ = iz + 0.1 * lStepIZ
                                       * (2 * Math.random() - 1);

      lMinZ = lPerturbedIZ - lAmplitudeZ;
      lMaxZ = lPerturbedIZ + lAmplitudeZ;

      final double[] dz = focusZ(pLightSheetIndex,
                                 pNumberOfDSamples,
                                 lMinZ,
                                 lMaxZ,
                                 lPerturbedIZ);

      if (dz == null)
        return false;

      for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
        if (!Double.isNaN(dz[d]))
        {
          lTheilSenEstimators[d].enter(dz[d], lPerturbedIZ);
          lPlots[d].addPoint("D" + d, dz[d], lPerturbedIZ);
          lPlots[d].ensureUpToDate();
        }

      if (ScriptingEngine.isCancelRequestedStatic()
          || !mCalibrator.isRunning())
        return false;

    }

    for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
    {
      final UnivariateAffineFunction lModel =
                                            lTheilSenEstimators[d].getModel();

      System.out.println("lModel=" + lModel);

      mModels.put(pLightSheetIndex,
                  d,
                  lTheilSenEstimators[d].getModel());

      BoundedVariable<Number> lDetectionFocusZVariable =
                                                       mLightSheetMicroscope.getDeviceLists()
                                                                            .getDevice(DetectionArmInterface.class,
                                                                                       d)
                                                                            .getZVariable();

      double lMinDZ = lDetectionFocusZVariable.getMin().doubleValue();
      double lMaxDZ = lDetectionFocusZVariable.getMax().doubleValue();
      double lStepDZ = (lMaxDZ - lMinDZ) / 1000;

      for (double z = lMinDZ; z <= lMaxDZ; z += lStepDZ)
      {
        lPlots[d].addPoint("fit D" + d,
                           z,
                           mModels.get(pLightSheetIndex, d).value(z));
      }

      lPlots[d].ensureUpToDate();
    }

    return true;
  }

  private double[] focusZ(int pLightSheetIndex,
                          int pNumberOfDSamples,
                          double pMinZ,
                          double pMaxZ,
                          double pIZ)
  {

    try
    {

      double lMinDZ = pMinZ;
      double lMaxDZ = pMaxZ;

      for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
      {
        BoundedVariable<Number> lDetectionFocusZVariable =
                                                         mLightSheetMicroscope.getDeviceLists()
                                                                              .getDevice(DetectionArmInterface.class,
                                                                                         d)
                                                                              .getZVariable();

        lMinDZ = max(lMinDZ,
                     lDetectionFocusZVariable.getMin().doubleValue());
        lMaxDZ = min(lMaxDZ,
                     lDetectionFocusZVariable.getMax().doubleValue());
      }

      System.out.format("Focus: LightSheet=%d, Iz=%g, minDZ=%g, maxDZ=%g \n",
                        pLightSheetIndex,
                        pIZ,
                        lMinDZ,
                        lMaxDZ);

      double lStep = (lMaxDZ - lMinDZ) / (pNumberOfDSamples - 1);

      LightSheetMicroscopeQueue lQueue =
                                       mLightSheetMicroscope.requestQueue();
      lQueue.clearQueue();
      lQueue.zero();

      lQueue.setI(pLightSheetIndex);
      lQueue.setIX(pLightSheetIndex, 0);
      lQueue.setIY(pLightSheetIndex, 0);
      lQueue.setIZ(pLightSheetIndex, 0);

      final double[] dz = new double[mNumberOfDetectionArmDevices];

      final TDoubleArrayList lDZList = new TDoubleArrayList();

      for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
      {
        lQueue.setIZ(pLightSheetIndex, lMinDZ);
        lQueue.setDZ(d, lMinDZ);
        lQueue.setC(d, false);
      }
      lQueue.addCurrentStateToQueue();

      for (double z = lMinDZ; z <= lMaxDZ; z += lStep)
      {
        lDZList.add(z);

        for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
        {
          lQueue.setDZ(d, z);
          lQueue.setC(d, true);
        }

        lQueue.setIH(pLightSheetIndex, 0);
        lQueue.setIZ(pLightSheetIndex, pIZ);

        lQueue.addCurrentStateToQueue();
      }

      for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
      {
        lQueue.setDZ(d, lMinDZ);
        lQueue.setC(d, false);
      }
      lQueue.addCurrentStateToQueue();

      lQueue.finalizeQueue();

      /*ScoreVisualizerJFrame.visualize("queuedscore",
      																mLightSheetMicroscope.getDeviceLists()
      																											.getSignalGeneratorDevice(0)
      																											.getQueuedScore());/**/

      mLightSheetMicroscope.useRecycler("adaptation", 1, 4, 4);
      final Boolean lPlayQueueAndWait =
                                      mLightSheetMicroscope.playQueueAndWaitForStacks(lQueue,
                                                                                      lQueue.getQueueLength(),
                                                                                      TimeUnit.SECONDS);

      if (lPlayQueueAndWait)
        for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
        {
          final StackInterface lStackInterface =
                                               mLightSheetMicroscope.getCameraStackVariable(d)
                                                                    .get();

          if (lStackInterface == null)
            continue;

          if (mDCTS2D == null)
            mDCTS2D = new DCTS2D();
          final double[] lMetricArray =
                                      mDCTS2D.computeImageQualityMetric((OffHeapPlanarStack) lStackInterface);
          /*final double[] lMetricArray =
                                      ImageAnalysisUtils.computeAveragePowerIntensityPerPlane(lImage);/**/

          PlotTab lPlot =
                        mMultiPlotZFocusCurves.getPlot(String.format("D=%d, I=%d, Iz=%g",
                                                                     d,
                                                                     pLightSheetIndex,
                                                                     pIZ));
          lPlot.setScatterPlot("samples");

          // System.out.format("metric array: \n");
          for (int j = 0; j < lMetricArray.length; j++)
          {
            lPlot.addPoint("samples",
                           lDZList.get(j),
                           lMetricArray[j]);
            /*System.out.format(	"%d,%d\t%g\t%g\n",
            					i,
            					j,
            					lDZList.get(j),
            					lMetricArray[j]);/**/
          }
          lPlot.ensureUpToDate();

          final Double lArgMax =
                               mArgMaxFinder.argmax(lDZList.toArray(),
                                                    lMetricArray);

          if (lArgMax != null)
          {
            TDoubleArrayList lDCTSList =
                                       new TDoubleArrayList(lMetricArray);

            double lAmplitudeRatio =
                                   (lDCTSList.max() - lDCTSList.min())
                                     / lDCTSList.max();

            System.out.format("argmax=%s amplratio=%s \n",
                              lArgMax.toString(),
                              lAmplitudeRatio);

            lPlot.setScatterPlot("argmax");
            lPlot.addPoint("argmax", lArgMax, 0);

            if (lAmplitudeRatio > 0.1 && lArgMax > lDZList.get(0))
              dz[d] = lArgMax;
            else
              dz[d] = Double.NaN;

            if (mArgMaxFinder instanceof Fitting1D)
            {
              Fitting1D lFitting1D = (Fitting1D) mArgMaxFinder;

              double[] lFit =
                            lFitting1D.fit(lDZList.toArray(),
                                           new double[lDZList.size()]);

              for (int j = 0; j < lDZList.size(); j++)
              {
                lPlot.setScatterPlot("fit");
                lPlot.addPoint("fit", lDZList.get(j), lFit[j]);
              }
            }

            /*Double lLastFitProbability = mArgMaxFinder.getLastFitProbability();
            
            System.out.format("argmax=%s fitprob=%s \n",
            									lArgMax.toString(),
            									lLastFitProbability);
            
            lPlot.setScatterPlot("argmax");
            lPlot.addPoint("argmax", lArgMax, 0);
            
            if (lLastFitProbability != null && lLastFitProbability > 0.9)
            	dz[i] = lArgMax;
            else
            	dz[i] = Double.NaN;
            	/**/

          }
          else
          {
            dz[d] = Double.NaN;
            System.out.println("Argmax is NULL!");
          }
        }

      return dz;

    }
    catch (final InterruptedException e)
    {
      e.printStackTrace();
    }
    catch (final ExecutionException e)
    {
      e.printStackTrace();
    }
    catch (final TimeoutException e)
    {
      e.printStackTrace();
    }

    return null;

  }

  /**
   * Applies correction for a given lightsheet index.
   * 
   * @param pLightSheetIndex
   *          lightsheet index
   * @param pAdjustDetectionZ
   *          this flag determines whther the coreection should be applied to
   *          the detection arms too
   * @return calibration error
   */
  public double apply(int pLightSheetIndex, boolean pAdjustDetectionZ)
  {

    double lSlope = 0, lOffset = 0;

    for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
    {
      lSlope += mModels.get(pLightSheetIndex, 0).getSlope();
      lOffset += mModels.get(pLightSheetIndex, 0).getConstant();
    }

    lSlope /= mNumberOfDetectionArmDevices;
    lOffset /= mNumberOfDetectionArmDevices;

    final LightSheetInterface lLightSheetDevice =
                                                mLightSheetMicroscope.getDeviceLists()
                                                                     .getDevice(LightSheetInterface.class,
                                                                                pLightSheetIndex);

    System.out.println("before: getZFunction()="
                       + lLightSheetDevice.getZFunction());

    if (abs(lSlope) > 0.00001)
    {
      lLightSheetDevice.getZFunction()
                       .get()
                       .composeWith(new UnivariateAffineFunction(lSlope,
                                                                 lOffset));
      lLightSheetDevice.getZFunction().setCurrent();
    }
    else
      System.out.println("slope too low: " + abs(lSlope));
    System.out.println("after: getZFunction()="
                       + lLightSheetDevice.getZFunction());

    System.out.println("before: getYFunction()="
                       + lLightSheetDevice.getYFunction());

    adjustYFunctionScaleAndMinMax(lLightSheetDevice);

    if (mNumberOfDetectionArmDevices == 2 && pAdjustDetectionZ)
      applyDetectionZ(pLightSheetIndex);

    double lError = (abs(1 - lSlope) + abs(lOffset));

    System.out.println("lError=" + lError);

    return lError;

  }

  protected void adjustYFunctionScaleAndMinMax(final LightSheetInterface lLightSheetDevice)
  {
    lLightSheetDevice.getYFunction()
                     .set(UnivariateAffineFunction.axplusb(lLightSheetDevice.getZFunction()
                                                                            .get()
                                                                            .getSlope(),
                                                           0));

    BoundedVariable<Number> lZVariable =
                                       lLightSheetDevice.getZVariable();
    BoundedVariable<Number> lYVariable =
                                       lLightSheetDevice.getYVariable();

    lYVariable.setMinMax(lZVariable.getMin().doubleValue(),
                         lZVariable.getMax().doubleValue());

    System.out.println("after: getYFunction()="
                       + lLightSheetDevice.getYFunction());
  }

  protected void applyDetectionZ(int pLightSheetIndex)
  {
    double a0 = mModels.get(pLightSheetIndex, 0).getSlope();
    double b0 = mModels.get(pLightSheetIndex, 0).getConstant();
    double a1 = mModels.get(pLightSheetIndex, 1).getSlope();
    double b1 = mModels.get(pLightSheetIndex, 1).getConstant();

    double lDZIntercept0 = -b0 / a0;
    double lDZIntercept1 = -b1 / a1;

    System.out.println("lDZIntercept0=" + lDZIntercept0);
    System.out.println("lDZIntercept1=" + lDZIntercept1);

    double lDesiredIntercept = 0.5 * (lDZIntercept0 + lDZIntercept1);

    System.out.println("lDesiredIntercept=" + lDesiredIntercept);

    double lInterceptCorrection0 =
                                 -(lDesiredIntercept - lDZIntercept0);
    double lInterceptCorrection1 =
                                 -(lDesiredIntercept - lDZIntercept1);

    System.out.println("lInterceptCorrection0="
                       + lInterceptCorrection0);
    System.out.println("lInterceptCorrection1="
                       + lInterceptCorrection1);

    final DetectionArmInterface lDetectionArmDevice0 =
                                                     mLightSheetMicroscope.getDeviceLists()
                                                                          .getDevice(DetectionArmInterface.class,
                                                                                     0);
    final DetectionArmInterface lDetectionArmDevice1 =
                                                     mLightSheetMicroscope.getDeviceLists()
                                                                          .getDevice(DetectionArmInterface.class,
                                                                                     1);

    System.out.println("Before: lDetectionArmDevice0.getDetectionFocusZFunction()="
                       + lDetectionArmDevice0.getZFunction());
    System.out.println("Before: lDetectionArmDevice1.getDetectionFocusZFunction()="
                       + lDetectionArmDevice1.getZFunction());

    UnivariateAffineFunction lFunction0 =
                                        lDetectionArmDevice0.getZFunction()
                                                            .get();
    UnivariateAffineFunction lFunction1 =
                                        lDetectionArmDevice1.getZFunction()
                                                            .get();

    lFunction0.composeWith(UnivariateAffineFunction.axplusb(1,
                                                            lInterceptCorrection0));
    lFunction1.composeWith(UnivariateAffineFunction.axplusb(1,
                                                            lInterceptCorrection1));

    lDetectionArmDevice0.getZFunction().setCurrent();
    lDetectionArmDevice1.getZFunction().setCurrent();

    System.out.println("After: lDetectionArmDevice0.getDetectionFocusZFunction()="
                       + lDetectionArmDevice0.getZFunction());
    System.out.println("After: lDetectionArmDevice1.getDetectionFocusZFunction()="
                       + lDetectionArmDevice1.getZFunction());
  }

  /**
   * Resets this module
   */
  public void reset()
  {
    mMultiPlotZFocusCurves.clear();

    mIteration = 0;
    mMultiPlotZModels.clear();

  }

}

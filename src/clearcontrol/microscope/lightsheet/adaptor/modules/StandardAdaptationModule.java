package clearcontrol.microscope.lightsheet.adaptor.modules;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import clearcontrol.core.math.argmax.ArgMaxFinder1DInterface;
import clearcontrol.core.math.argmax.FitProbabilityInterface;
import clearcontrol.core.math.argmax.methods.ModeArgMaxFinder;
import clearcontrol.gui.plots.MultiPlot;
import clearcontrol.gui.plots.PlotTab;
import clearcontrol.ip.iqm.DCTS2D;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.acquisition.LightSheetAcquisitionStateInterface;
import clearcontrol.microscope.lightsheet.adaptor.utils.NDIterator;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArmInterface;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheetInterface;
import clearcontrol.stack.EmptyStack;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.StackInterface;
import gnu.trove.list.array.TDoubleArrayList;

/**
 * ND iterator adaptation module
 *
 * @author royer
 * @param <S>
 *          state type
 */
public abstract class StandardAdaptationModule<S extends LightSheetAcquisitionStateInterface<S>>
                                              extends
                                              NDIteratorAdaptationModule<S>
{

  private int mNumberOfSamples;
  private double mProbabilityThreshold;
  protected MultiPlot mMultiPlotZFocusCurves;

  /**
   * Instanciates a ND iterator adaptation module
   * 
   * @param pModuleName
   *          module name
   * @param pNumberOfSamples
   *          numbe rof samples
   * @param pProbabilityThreshold
   *          probability threshold
   */
  public StandardAdaptationModule(String pModuleName,
                                  int pNumberOfSamples,
                                  double pProbabilityThreshold)
  {
    super(pModuleName);
    setNumberOfSamples(pNumberOfSamples);
    setProbabilityThreshold(pProbabilityThreshold);

    mMultiPlotZFocusCurves =
                           MultiPlot.getMultiPlot(this.getClass()
                                                      .getSimpleName()
                                                  + " calibration: focus curves");
    mMultiPlotZFocusCurves.setVisible(true);

  }

  /**
   * Returns the probability threshold
   * 
   * @return probability threshold
   */
  public double getProbabilityThreshold()
  {
    return mProbabilityThreshold;
  }

  /**
   * Sets the probability threshold
   * 
   * @param pProbabilityThreshold
   *          probability threshold
   */
  public void setProbabilityThreshold(double pProbabilityThreshold)
  {
    mProbabilityThreshold = pProbabilityThreshold;
  }

  /**
   * Returns the number of samples
   * 
   * @return number of samples
   */
  public int getNumberOfSamples()
  {
    return mNumberOfSamples;
  }

  /**
   * Sets the number of samples
   * 
   * @param pNumberOfSamples
   *          number of samples
   */
  public void setNumberOfSamples(int pNumberOfSamples)
  {
    mNumberOfSamples = pNumberOfSamples;
  }

  @Override
  public void reset()
  {
    super.reset();

    LightSheetMicroscope lLightSheetMicroscope =
                                               getAdaptator().getLightSheetMicroscope();
    LightSheetAcquisitionStateInterface<S> lAcquisitionState =
                                                             getAdaptator().getCurrentAcquisitionStateVariable()
                                                                           .get();

    int lNumberOfControlPlanes =
                               lAcquisitionState.getInterpolationTables()
                                                .getNumberOfControlPlanes();

    int lNumberOfLighSheets =
                            lLightSheetMicroscope.getDeviceLists()
                                                 .getNumberOfDevices(LightSheetInterface.class);

    setNDIterator(new NDIterator(lNumberOfControlPlanes,
                                 lNumberOfLighSheets));

  }

  protected Future<?> findBestDOFValue(int pControlPlaneIndex,
                                       int pLightSheetIndex,
                                       LightSheetMicroscopeQueue pQueue,
                                       S lStackAcquisition,
                                       final TDoubleArrayList lDOFValueList)
  {

    try
    {
      LightSheetMicroscope lLightSheetMicroscope =
                                                 getAdaptator().getLightSheetMicroscope();

      lLightSheetMicroscope.useRecycler("adaptation", 1, 4, 4);
      final Boolean lPlayQueueAndWait =
                                      lLightSheetMicroscope.playQueueAndWaitForStacks(pQueue,
                                                                                      10 + pQueue.getQueueLength(),
                                                                                      TimeUnit.SECONDS);

      if (!lPlayQueueAndWait)
        return null;

      final int lNumberOfDetectionArmDevices =
                                             lLightSheetMicroscope.getDeviceLists()
                                                                  .getNumberOfDevices(DetectionArmInterface.class);

      ArrayList<StackInterface> lStacks = new ArrayList<>();
      for (int d = 0; d < lNumberOfDetectionArmDevices; d++)
        if (isRelevantDetectionArm(pControlPlaneIndex, d))
        {
          final StackInterface lStackInterface =
                                               lLightSheetMicroscope.getCameraStackVariable(d)
                                                                    .get();
          lStacks.add(lStackInterface.duplicate());

        }
        else
          lStacks.add(new EmptyStack());

      Runnable lRunnable = () -> {

        try
        {
          ArgMaxFinder1DInterface lSmartArgMaxFinder =
                                                     new ModeArgMaxFinder();

          ArrayList<Double> lArgMaxList = new ArrayList<Double>();

          for (int d = 0; d < lNumberOfDetectionArmDevices; d++)

          {
            if (!isRelevantDetectionArm(pControlPlaneIndex, d))
            {
              lArgMaxList.add(Double.NaN);
              continue;
            }

            final double[] lMetricArray =
                                        computeMetric(pControlPlaneIndex,
                                                      pLightSheetIndex,
                                                      d,
                                                      lDOFValueList,
                                                      lStacks.get(d));

            Double lArgmax =
                           lSmartArgMaxFinder.argmax(lDOFValueList.toArray(),
                                                     lMetricArray);

            System.out.println("lArgmax = " + lArgmax);

            if (lArgmax != null && !Double.isNaN(lArgmax))
            {
              if (lSmartArgMaxFinder instanceof FitProbabilityInterface)
              {
                double lFitProbability =
                                       ((FitProbabilityInterface) lSmartArgMaxFinder).getLastFitProbability();

                if (lFitProbability > getProbabilityThreshold())
                  lArgMaxList.add(lArgmax);
                else
                  lArgMaxList.add(Double.NaN);
              }
              else
              {
                lArgMaxList.add(lArgmax);
              }

            }
            else
              lArgMaxList.add(Double.NaN);

          }

          System.out.println("lArgMaxList=" + lArgMaxList.toString());

          for (StackInterface lStack : lStacks)
            lStack.free();

        }
        catch (Throwable e)
        {
          e.printStackTrace();
        }

      };

      Future<?> lFuture =
                        getAdaptator().executeAsynchronously(lRunnable);

      // FORCE SYNC:
      if (!getAdaptator().getConcurrentExecutionVariable().get())
      {
        try
        {
          lFuture.get();
        }
        catch (Throwable e)
        {
          e.printStackTrace();
        }
      }

      return lFuture;
    }
    catch (InterruptedException | ExecutionException
        | TimeoutException e)
    {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Returns true if the given detection arm index is relevant at this control
   * plane index
   * 
   * @param pControlPlaneIndex
   *          control plane idnex
   * @param pDetectionArmIndex
   *          detection arm index
   * @return true if relevant
   */
  public boolean isRelevantDetectionArm(int pControlPlaneIndex,
                                        int pDetectionArmIndex)
  {
    int lBestDetectionArm =
                          getAdaptator().getCurrentAcquisitionStateVariable()
                                        .get()
                                        .getBestDetectionArm(pControlPlaneIndex);
    return (lBestDetectionArm == pDetectionArmIndex);
  };

  protected double[] computeMetric(int pControlPlaneIndex,
                                   int pLightSheetIndex,
                                   int pDetectionArmIndex,
                                   final TDoubleArrayList lDOFValueList,
                                   StackInterface lDuplicatedStack)
  {

    DCTS2D lDCTS2D = new DCTS2D();

    System.out.format("computing DCTS on %s ...\n", lDuplicatedStack);
    final double[] lMetricArray =
                                lDCTS2D.computeImageQualityMetric((OffHeapPlanarStack) lDuplicatedStack);
    lDuplicatedStack.free();

    if (isRelevantDetectionArm(pControlPlaneIndex,
                               pDetectionArmIndex))
    {
      PlotTab lPlot =
                    mMultiPlotZFocusCurves.getPlot(String.format("LS=%d, D=%d CPI=%d",
                                                                 pLightSheetIndex,
                                                                 pDetectionArmIndex,
                                                                 pControlPlaneIndex));
      lPlot.clearPoints();
      lPlot.setScatterPlot("samples");

      for (int i = 0; i < lMetricArray.length; i++)
      {
        System.out.format("%g\t%g \n",
                          lDOFValueList.get(i),
                          lMetricArray[i]);
        lPlot.addPoint("samples",
                       lDOFValueList.get(i),
                       lMetricArray[i]);
      }
      lPlot.ensureUpToDate();
    }

    return lMetricArray;
  }

  @Override
  public boolean isReady()
  {
    return !getNDIterator().hasNext() && super.isReady();
  }

}

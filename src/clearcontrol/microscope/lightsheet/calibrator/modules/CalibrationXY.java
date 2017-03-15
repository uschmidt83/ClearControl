package clearcontrol.microscope.lightsheet.calibrator.modules;

import static java.lang.Math.abs;
import static java.lang.Math.min;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.basictypeaccess.offheap.ShortOffHeapAccess;
import net.imglib2.img.planar.OffHeapPlanarImg;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;
import clearcontrol.core.math.functions.UnivariateAffineFunction;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.calibrator.Calibrator;
import clearcontrol.microscope.lightsheet.calibrator.utils.ImageAnalysisUtils;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArmInterface;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheetInterface;
import clearcontrol.stack.StackInterface;
import gnu.trove.list.array.TDoubleArrayList;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.stat.StatUtils;
import org.ejml.simple.SimpleMatrix;

public class CalibrationXY
{

  private final LightSheetMicroscope mLightSheetMicroscope;

  private int mNumberOfDetectionArmDevices;
  private int mNumberOfLightSheetDevices;

  private MultiKeyMap<Integer, Vector2D> mOriginFromX,
      mUnitVectorFromX, mOriginFromY, mUnitVectorFromY;

  private MultiKeyMap<Integer, SimpleMatrix> mTransformMatrices;

  @SuppressWarnings("unchecked")
  public CalibrationXY(Calibrator pCalibrator)
  {
    super();
    mLightSheetMicroscope = pCalibrator.getLightSheetMicroscope();

    mNumberOfDetectionArmDevices =
                                 mLightSheetMicroscope.getDeviceLists()
                                                      .getNumberOfDevices(DetectionArmInterface.class);

    mNumberOfLightSheetDevices =
                               mLightSheetMicroscope.getDeviceLists()
                                                    .getNumberOfDevices(LightSheetInterface.class);

    mOriginFromX = new MultiKeyMap<>();
    mUnitVectorFromX = new MultiKeyMap<>();
    mOriginFromY = new MultiKeyMap<>();
    mUnitVectorFromY = new MultiKeyMap<>();

    mTransformMatrices = new MultiKeyMap<>();
  }

  public boolean calibrate(int pLightSheetIndex,
                           int pDetectionArmIndex,
                           int pNumberOfPoints)
  {
    return calibrate(pLightSheetIndex,
                     pDetectionArmIndex,
                     pNumberOfPoints,
                     true)
           && calibrate(pLightSheetIndex,
                        pDetectionArmIndex,
                        pNumberOfPoints,
                        false);
  }

  private boolean calibrate(int pLightSheetIndex,
                            int pDetectionArmIndex,
                            int pNumberOfPoints,
                            boolean pDoAxisX)
  {
    LightSheetInterface lLightSheet =
                                    mLightSheetMicroscope.getDeviceLists()
                                                         .getDevice(LightSheetInterface.class,
                                                                    pLightSheetIndex);

    double lMin, lMax;

    if (pDoAxisX)
    {
      BoundedVariable<Double> lLightSheetXFunction =
                                                   lLightSheet.getXVariable();
      lMin = lLightSheetXFunction.getMin();
      lMax = lLightSheetXFunction.getMax();
    }
    else
    {
      BoundedVariable<Double> lLightSheetYFunction =
                                                   lLightSheet.getYVariable();
      lMin = lLightSheetYFunction.getMin();
      lMax = lLightSheetYFunction.getMax();
    }

    try
    {
      TDoubleArrayList lOriginXList = new TDoubleArrayList();
      TDoubleArrayList lOriginYList = new TDoubleArrayList();

      TDoubleArrayList lUnitVectorXList = new TDoubleArrayList();
      TDoubleArrayList lUnitVectorYList = new TDoubleArrayList();

      double lMaxAbsY = min(abs(lMin), abs(lMax));
      for (double f =
                    0.5 * lMaxAbsY; f <= 0.7
                                         * lMaxAbsY; f +=
                                                       (0.2 * lMaxAbsY
                                                        / (pNumberOfPoints
                                                           - 1)))
      {
        Vector2D lCenterP, lCenter0, lCenterN;

        int lNumberOfPreImages = 6;

        if (pDoAxisX)
        {
          lCenterP =
                   lightSheetImageCenterWhenAt(pLightSheetIndex,
                                               pDetectionArmIndex,
                                               f,
                                               0,
                                               lNumberOfPreImages);

          lCenter0 =
                   lightSheetImageCenterWhenAt(pLightSheetIndex,
                                               pDetectionArmIndex,
                                               0,
                                               0,
                                               lNumberOfPreImages);

          lCenterN =
                   lightSheetImageCenterWhenAt(pLightSheetIndex,
                                               pDetectionArmIndex,
                                               -f,
                                               0,
                                               lNumberOfPreImages);
        }
        else
        {
          lCenterP =
                   lightSheetImageCenterWhenAt(pLightSheetIndex,
                                               pDetectionArmIndex,
                                               0,
                                               f,
                                               lNumberOfPreImages);

          lCenter0 =
                   lightSheetImageCenterWhenAt(pLightSheetIndex,
                                               pDetectionArmIndex,
                                               0,
                                               0,
                                               lNumberOfPreImages);

          lCenterN = lightSheetImageCenterWhenAt(pLightSheetIndex,
                                                 pDetectionArmIndex,
                                                 0,
                                                 -f,
                                                 lNumberOfPreImages);
        }

        System.out.format("center at %g: %s \n", f, lCenterP);
        System.out.format("center at %g: %s \n", -f, lCenterN);

        if (lCenterP == null && lCenterN == null)
          continue;

        lOriginXList.add(lCenter0.getX());
        lOriginYList.add(lCenter0.getY());

        if (f != 0)
        {
          double ux = (lCenterP.getX() - lCenterN.getX()) / 2f;
          double uy = (lCenterP.getY() - lCenterN.getY()) / 2f;

          System.out.format("Unit vector: (%g,%g) \n", ux, uy);

          lUnitVectorXList.add(ux);
          lUnitVectorYList.add(uy);
        }
      }

      double lOriginX = StatUtils.percentile(lOriginXList.toArray(),
                                             50);
      double lOriginY = StatUtils.percentile(lOriginYList.toArray(),
                                             50);

      double lUnitVectorX =
                          StatUtils.percentile(lUnitVectorXList.toArray(),
                                               50);
      double lUnitVectorY =
                          StatUtils.percentile(lUnitVectorYList.toArray(),
                                               50);

      if (pDoAxisX)
      {
        mOriginFromX.put(pLightSheetIndex,
                         pDetectionArmIndex,
                         new Vector2D(lOriginX, lOriginY));
        mUnitVectorFromX.put(pLightSheetIndex,
                             pDetectionArmIndex,
                             new Vector2D(lUnitVectorX,
                                          lUnitVectorY));

        System.out.format("From X axis: \n");
        System.out.format("Origin : %s \n", mOriginFromX);
        System.out.format("Unit Vector : %s \n", mUnitVectorFromX);
      }
      else
      {
        mOriginFromY.put(pLightSheetIndex,
                         pDetectionArmIndex,
                         new Vector2D(lOriginX, lOriginY));
        mUnitVectorFromY.put(pLightSheetIndex,
                             pDetectionArmIndex,
                             new Vector2D(lUnitVectorX,
                                          lUnitVectorY));

        System.out.format("From X axis: \n");
        System.out.format("Origin : %s \n", mOriginFromY);
        System.out.format("Unit Vector : %s \n", mUnitVectorFromY);
      }

    }
    catch (InterruptedException | ExecutionException
        | TimeoutException e)
    {
      e.printStackTrace();
      return false;
    }
    finally
    {
    }

    return true;
  }

  private Vector2D lightSheetImageCenterWhenAt(int pLightSheetIndex,
                                               int pDetectionArmIndex,
                                               double pX,
                                               double pY,
                                               int pN) throws InterruptedException,
                                                       ExecutionException,
                                                       TimeoutException
  {
    // Building queue start:
    mLightSheetMicroscope.clearQueue();
    mLightSheetMicroscope.zero();

    mLightSheetMicroscope.setI(pLightSheetIndex);
    mLightSheetMicroscope.setIZ(pLightSheetIndex, 0);
    mLightSheetMicroscope.setIH(pLightSheetIndex, 0);
    mLightSheetMicroscope.setIZ(pLightSheetIndex, 0);

    for (int i = 0; i < mNumberOfDetectionArmDevices; i++)
      mLightSheetMicroscope.setDZ(i, 0);

    for (int i = 1; i <= pN; i++)
    {
      mLightSheetMicroscope.setIX(pLightSheetIndex, pX);
      mLightSheetMicroscope.setIY(pLightSheetIndex, pY);
      for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
        mLightSheetMicroscope.setC(d, i == pN);
      mLightSheetMicroscope.addCurrentStateToQueue();
    }
    mLightSheetMicroscope.finalizeQueue();
    // Building queue end.

    mLightSheetMicroscope.useRecycler("adaptation", 1, 4, 4);
    final Boolean lPlayQueueAndWait =
                                    mLightSheetMicroscope.playQueueAndWaitForStacks(mLightSheetMicroscope.getQueueLength(),
                                                                                    TimeUnit.SECONDS);

    if (!lPlayQueueAndWait)
      return null;

    final StackInterface lStackInterface =
                                         mLightSheetMicroscope.getStackVariable(pDetectionArmIndex)
                                                              .get();

    OffHeapPlanarImg<UnsignedShortType, ShortOffHeapAccess> lImage =
                                                                   (OffHeapPlanarImg<UnsignedShortType, ShortOffHeapAccess>) lStackInterface.getImage();

    System.out.println("lImage=" + lImage);

    long lWidth = lImage.dimension(0);
    long lHeight = lImage.dimension(1);

    System.out.format("image: width=%d, height=%d \n",
                      lWidth,
                      lHeight);

    // ImagePlus lShow = ImageJFunctions.show(lImage);
    // lShow.setDisplayRange(0, 150);
    try
    {
      final RandomAccessible<UnsignedShortType> infiniteImg =
                                                            Views.extendValue(lImage,
                                                                              new UnsignedShortType());
      Gauss3.gauss(1, infiniteImg, lImage);
      // ImagePlus lShow = ImageJFunctions.show(lImage);
      // lShow.setDisplayRange(-100, 100);
      // lShow.setDisplayRange(-100, 100);
    }
    catch (IncompatibleTypeException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    ImageAnalysisUtils.cleanWithMin(lImage);
    Vector2D lPoint =
                    ImageAnalysisUtils.findCOMOfBrightestPointsForEachPlane(lImage)[0];

    lPoint =
           lPoint.subtract(new Vector2D(0.5 * lWidth, 0.5 * lHeight));

    System.out.format("image: lightsheet center at: %s \n", lPoint);

    Vector2D lNormalizedPoint =
                              new Vector2D(2 * lPoint.getX()
                                           / lWidth,
                                           2 * lPoint.getY()
                                                     / lHeight);

    System.out.format("image: lightsheet center at normalized coord: %s \n",
                      lNormalizedPoint);

    return lNormalizedPoint;
  }

  public double apply(int pLightSheetIndex, int pDetectionArmIndex)
  {
    System.out.format("Light sheet index: %d, detection arm index: %d \n",
                      pLightSheetIndex,
                      pDetectionArmIndex);

    Vector2D lOriginFromX = mOriginFromX.get(pLightSheetIndex,
                                             pDetectionArmIndex);
    Vector2D lOriginFromY = mOriginFromY.get(pLightSheetIndex,
                                             pDetectionArmIndex);

    System.out.format("lOriginFromX: %s \n", lOriginFromX);
    System.out.format("lOriginFromY: %s \n", lOriginFromY);

    Vector2D lOrigin = new Vector2D(0, 0);
    lOrigin = lOrigin.add(lOriginFromX);
    lOrigin = lOrigin.add(lOriginFromY);
    lOrigin = lOrigin.scalarMultiply(0.5);

    System.out.format("lOrigin: %s \n", lOrigin);

    Vector2D lUnitVectorU = mUnitVectorFromX.get(pLightSheetIndex,
                                                 pDetectionArmIndex);
    Vector2D lUnitVectorV = mUnitVectorFromY.get(pLightSheetIndex,
                                                 pDetectionArmIndex);

    System.out.format("lUnitVectorU: %s \n", lUnitVectorU);
    System.out.format("lUnitVectorV: %s \n", lUnitVectorV);

    SimpleMatrix lMatrix = new SimpleMatrix(2, 2);
    lMatrix.set(0, 0, lUnitVectorU.getX());
    lMatrix.set(1, 0, lUnitVectorU.getY());
    lMatrix.set(0, 1, lUnitVectorV.getX());
    lMatrix.set(1, 1, lUnitVectorV.getY());

    System.out.format("lMatrix: \n");
    lMatrix.print(4, 3);

    mTransformMatrices.put(pLightSheetIndex,
                           pDetectionArmIndex,
                           lMatrix);

    SimpleMatrix lInverseMatrix = lMatrix.invert();

    System.out.format("lInverseMatrix: \n");
    lInverseMatrix.print(4, 6);

    SimpleMatrix lOriginAsMatrix = new SimpleMatrix(2, 1);
    lOriginAsMatrix.set(0, 0, lOrigin.getX());
    lOriginAsMatrix.set(1, 0, lOrigin.getY());

    System.out.format("lOriginAsMatrix: \n");
    lOriginAsMatrix.print(4, 3);

    SimpleMatrix lNewOffsets = lInverseMatrix.mult(lOriginAsMatrix);

    System.out.format("lNewOffsets:\n");
    lNewOffsets.print(4, 3);

    double lXOffset = lNewOffsets.get(0, 0);
    double lYOffset = lNewOffsets.get(1, 0);

    System.out.format("lXOffset: %s \n", lXOffset);
    System.out.format("lYOffset: %s \n", lYOffset);

    LightSheetInterface lLightSheetDevice =
                                          mLightSheetMicroscope.getDeviceLists()
                                                               .getDevice(LightSheetInterface.class,
                                                                          pLightSheetIndex);

    System.out.format("lLightSheetDevice: %s \n", lLightSheetDevice);

    Variable<UnivariateAffineFunction> lFunctionXVariable =
                                                          lLightSheetDevice.getXFunction();
    Variable<UnivariateAffineFunction> lFunctionYVariable =
                                                          lLightSheetDevice.getYFunction();

    System.out.format("lFunctionXVariable: %s \n",
                      lFunctionXVariable);
    System.out.format("lFunctionYVariable: %s \n",
                      lFunctionYVariable);

    // TODO: use pixel calibration here...
    lFunctionXVariable.get()
                      .composeWith(UnivariateAffineFunction.axplusb(1,
                                                                    -lXOffset));
    lFunctionYVariable.get()
                      .composeWith(UnivariateAffineFunction.axplusb(1,
                                                                    -lYOffset));

    lFunctionXVariable.setCurrent();
    lFunctionYVariable.setCurrent();

    System.out.format("Updated-> lFunctionXVariable: %s \n",
                      lFunctionXVariable);
    System.out.format("Updated-> lFunctionYVariable: %s \n",
                      lFunctionYVariable);

    // TODO: use pixel calibration here...
    BoundedVariable<Double> lHeightVariable =
                                            lLightSheetDevice.getHeightVariable();
    Variable<UnivariateAffineFunction> lHeightFunctionVariable =
                                                               lLightSheetDevice.getHeightFunction();
    System.out.format("lHeightFunctionVariable: %s \n",
                      lHeightFunctionVariable);
    UnivariateAffineFunction lHeightFunction =
                                             UnivariateAffineFunction.axplusb(1,
                                                                              0);
    lHeightVariable.setMinMax(-1, 1);

    lHeightFunctionVariable.set(lHeightFunction);
    lHeightFunctionVariable.setCurrent();

    System.out.format("Updated-> lHeightFunctionVariable: %s \n",
                      lHeightFunctionVariable);

    double lError = abs(lXOffset) + abs(lYOffset);

    System.out.format("lError: %s \n", lError);

    return lError;
  }

  public void reset()
  {

  }

  public SimpleMatrix getTransformMatrix(int pLightSheetIndex,
                                         int pDetectionArmIndex)
  {
    return mTransformMatrices.get(pLightSheetIndex,
                                  pDetectionArmIndex);
  }

}

package clearcontrol.core.math.regression.ewr;

import clearcontrol.core.math.kdtree.KdTree;

import org.apache.commons.math3.analysis.MultivariateFunction;

public class CopyOfEnsembleLinearRegressors implements
                                            MultivariateFunction
{
  private static final int cBucketCapacity = 10;

  private int mDimension;
  private KdTree<Double> lKDTree;

  private boolean mIsUpToDate = false;

  public CopyOfEnsembleLinearRegressors(int pDimension)
  {
    mDimension = pDimension;
    lKDTree = new KdTree<Double>(mDimension, cBucketCapacity);
  }

  public void addPoint(double[] pPoint, double pValue)
  {
    lKDTree.addPoint(pPoint, pValue);
    mIsUpToDate = false;
  }

  public void update()
  {
    mIsUpToDate = true;
  }

  @Override
  public double value(double[] pPoint)
  {
    return 0;
  }

}

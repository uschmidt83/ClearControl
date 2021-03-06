package clearcontrol.microscope.lightsheet.acquisition.tables;

import java.util.ArrayList;

import clearcontrol.core.device.change.ChangeListeningBase;
import clearcontrol.core.math.interpolation.SplineInterpolationTable;
import clearcontrol.microscope.lightsheet.LightSheetDOF;

/**
 * Interpolation tables
 *
 * @author royer
 */
public class InterpolationTables extends
                                 ChangeListeningBase<InterpolationTables>
                                 implements Cloneable
{
  private final int mNumberOfLightSheetDevices;
  private final int mNumberOfDetectionArmDevices;
  private double mTransitionPlaneZ = 0;
  private ArrayList<SplineInterpolationTable> mInterpolationTableList =
                                                                      new ArrayList<SplineInterpolationTable>();

  /**
   * Instanciates an interpolation table given a number of detection arms and
   * lightsheets
   * 
   * @param pNumberOfDetectionArmDevices
   *          number of detection arms
   * @param pNumberOfLightSheetDevices
   *          number of lightsheets
   */
  public InterpolationTables(int pNumberOfDetectionArmDevices,
                             int pNumberOfLightSheetDevices)
  {
    super();

    mNumberOfDetectionArmDevices = pNumberOfDetectionArmDevices;
    mNumberOfLightSheetDevices = pNumberOfLightSheetDevices;

    SplineInterpolationTable lInterpolationTableDZ =
                                                   new SplineInterpolationTable(mNumberOfDetectionArmDevices);

    SplineInterpolationTable lInterpolationTableIX =
                                                   new SplineInterpolationTable(mNumberOfLightSheetDevices);
    SplineInterpolationTable lInterpolationTableIY =
                                                   new SplineInterpolationTable(mNumberOfLightSheetDevices);
    SplineInterpolationTable lInterpolationTableIZ =
                                                   new SplineInterpolationTable(mNumberOfLightSheetDevices);

    SplineInterpolationTable lInterpolationTableIA =
                                                   new SplineInterpolationTable(mNumberOfLightSheetDevices);
    SplineInterpolationTable lInterpolationTableIB =
                                                   new SplineInterpolationTable(mNumberOfLightSheetDevices);
    SplineInterpolationTable lInterpolationTableIW =
                                                   new SplineInterpolationTable(mNumberOfLightSheetDevices);
    SplineInterpolationTable lInterpolationTableIH =
                                                   new SplineInterpolationTable(mNumberOfLightSheetDevices);
    SplineInterpolationTable lInterpolationTableIP =
                                                   new SplineInterpolationTable(mNumberOfLightSheetDevices);

    mInterpolationTableList.add(lInterpolationTableDZ);
    mInterpolationTableList.add(lInterpolationTableIX);
    mInterpolationTableList.add(lInterpolationTableIY);
    mInterpolationTableList.add(lInterpolationTableIZ);

    mInterpolationTableList.add(lInterpolationTableIA);
    mInterpolationTableList.add(lInterpolationTableIB);
    mInterpolationTableList.add(lInterpolationTableIW);
    mInterpolationTableList.add(lInterpolationTableIH);
    mInterpolationTableList.add(lInterpolationTableIP);
  }

  /**
   * Instanciate an interpolation table that is a copy of an existing
   * interpolation table.
   * 
   * @param pInterpolationTable
   *          existing inerpolatio table
   */
  public InterpolationTables(InterpolationTables pInterpolationTable)
  {
    mNumberOfDetectionArmDevices =
                                 pInterpolationTable.mNumberOfDetectionArmDevices;
    mNumberOfLightSheetDevices =
                               pInterpolationTable.mNumberOfLightSheetDevices;

    mTransitionPlaneZ = pInterpolationTable.mTransitionPlaneZ;

    mInterpolationTableList = new ArrayList<>();

    for (SplineInterpolationTable lSplineInterpolationTable : pInterpolationTable.mInterpolationTableList)
    {
      mInterpolationTableList.add(lSplineInterpolationTable.clone());
    }
  }

  @Override
  public InterpolationTables clone()
  {
    return new InterpolationTables(this);
  }

  /**
   * Adds a control plane at a given z position
   * 
   * @param pZ
   *          z position
   */
  public void addControlPlane(double pZ)
  {
    for (SplineInterpolationTable lSplineInterpolationTable : mInterpolationTableList)
      lSplineInterpolationTable.addRow(pZ);
    notifyListeners(this);
  }

  /**
   * Returns the number of control planes
   * 
   * @return number of contol planes
   */
  public int getNumberOfControlPlanes()
  {
    return mInterpolationTableList.get(0).getNumberOfRows();
  }

  /**
   * Returns the number of devices for a given lightsheet DOF
   * 
   * @param pLightSheetDOF
   *          lightsheet DOF
   * @return number of devices for a given lightsheet DOF
   */
  public int getNumberOfDevices(LightSheetDOF pLightSheetDOF)
  {
    return getTable(pLightSheetDOF).getNumberOfColumns();
  }

  /**
   * Returns the z value for a given control plane index
   * 
   * @param pControlPlaneIndex
   *          control plane index
   * @return z value
   */
  public double getZ(int pControlPlaneIndex)
  {
    // we are interested in getting the Z position (X in table) _not_ the DZ
    // value!
    double lZ = getTable(LightSheetDOF.DZ).getRow(pControlPlaneIndex)
                                          .getX();
    return lZ;
  }

  /**
   * Returns min z value
   * 
   * @return min z value
   */
  public double getMinZ()
  {
    return getTable(LightSheetDOF.DZ).getMinX();
  }

  /**
   * Returns max z value
   * 
   * @return max z value
   */
  public double getMaxZ()
  {
    return getTable(LightSheetDOF.DZ).getMaxX();
  }

  /**
   * Returns interpolated value at a given position Z
   * 
   * @param pLightSheetDOF
   *          DOF
   * @param pDeviceIndex
   *          device index
   * @param pZ
   *          position at which to sample
   * @return interpolated value
   */
  public double getInterpolated(LightSheetDOF pLightSheetDOF,
                                int pDeviceIndex,
                                double pZ)
  {
    return getTable(pLightSheetDOF).getInterpolatedValue(pDeviceIndex,
                                                         pZ);
  }

  /**
   * Sets the value of a DOF for a given control plane index.
   * 
   * @param pLightSheetDOF
   *          DOF
   * @param pControlPlaneIndex
   *          control plane index
   * @param pDeviceIndex
   *          device index
   * @param pValue
   *          value to set
   */
  public void set(LightSheetDOF pLightSheetDOF,
                  int pControlPlaneIndex,
                  int pDeviceIndex,
                  double pValue)
  {
    getTable(pLightSheetDOF).setY(pControlPlaneIndex,
                                  pDeviceIndex,
                                  pValue);
    notifyListeners(this);
  }

  /**
   * Adds a delta value for a given DOF, control plane index, and device index.
   * 
   * @param pLightSheetDOF
   *          DOF
   * @param pControlPlaneIndex
   *          control plane index
   * @param pDeviceIndex
   *          device index
   * @param pDeltaValue
   *          delta value
   */
  public void add(LightSheetDOF pLightSheetDOF,
                  int pControlPlaneIndex,
                  int pDeviceIndex,
                  double pDeltaValue)
  {
    getTable(pLightSheetDOF).addY(pControlPlaneIndex,
                                  pDeviceIndex,
                                  pDeltaValue);
    notifyListeners(this);
  }

  /**
   * Sets the value of a DOF for a given control plane index.
   * 
   * @param pLightSheetDOF
   *          DOF
   * @param pControlPlaneIndex
   *          control plane index
   * @param pValue
   *          value to set
   */
  public void set(LightSheetDOF pLightSheetDOF,
                  int pControlPlaneIndex,
                  double pValue)
  {
    getTable(pLightSheetDOF).setY(pControlPlaneIndex, pValue);
    notifyListeners(this);
  }

  /**
   * Sets the value for a given DOF uniformely
   * 
   * @param pLightSheetDOF
   *          DOF
   * @param pValue
   *          value
   */
  public void set(LightSheetDOF pLightSheetDOF, double pValue)
  {
    getTable(pLightSheetDOF).setY(pValue);
    notifyListeners(this);
  }

  /**
   * Sets z position of transition plane
   * 
   * @param pZ
   *          z position
   */
  public void setTransitionPlaneZPosition(double pZ)
  {
    mTransitionPlaneZ = pZ;
    notifyListeners(this);
  }

  /**
   * Returns z position of transition plane
   * 
   * @return z position of transition plane
   */
  public double getTransitionPlaneZPosition()
  {
    return mTransitionPlaneZ;
  }

  private SplineInterpolationTable getTable(LightSheetDOF pLightSheetDOF)
  {
    return mInterpolationTableList.get(pLightSheetDOF.ordinal());
  }

}

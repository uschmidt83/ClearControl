package clearcontrol.microscope.lightsheet.adaptor.modules;

import java.util.function.Function;

import clearcontrol.core.concurrent.executors.AsynchronousExecutorServiceAccess;
import clearcontrol.core.device.name.NameableInterface;
import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.acquisition.LightSheetAcquisitionStateInterface;
import clearcontrol.microscope.lightsheet.adaptor.Adaptator;

/**
 * Interface implemented by all adaptation modules
 *
 * @author royer
 * @param <S>
 *          state type
 */
public interface AdaptationModuleInterface<S extends LightSheetAcquisitionStateInterface<S>>
                                          extends
                                          Function<Void, Boolean>,
                                          AsynchronousExecutorServiceAccess,
                                          NameableInterface
{

  /**
   * Sets the parent adaptor
   * 
   * @param pAdaptator
   *          parent adaptor
   */
  void setAdaptator(Adaptator<S> pAdaptator);

  /**
   * Returns the parent adaptor
   * 
   * @return parent adaptor
   */
  Adaptator<S> getAdaptator();

  /**
   * Sets the priority of this module
   * 
   * @param pPriority
   *          module priority
   */
  void setPriority(int pPriority);

  /**
   * Returns this modules's priority
   * 
   * @return module's priority
   */
  int getPriority();

  @Override
  Boolean apply(Void pVoid);

  /**
   * Resets this module
   */
  void reset();

  /**
   * Updates new state given the modules last results
   * 
   */
  void updateNewState();

  /**
   * Returns true if all tasks have been completed
   * 
   * @return true if all tasks completed, false otherwise
   */
  boolean areAllTasksCompleted();

  /**
   * Returns true if all steps have been completed
   * 
   * @return true if all steps completed, false otherwise
   */
  boolean areAllStepsCompleted();

  /**
   * Returns true if this module is ready - all measurements have been taken.
   * 
   * @return all measurements have been taken.
   */
  boolean isReady();

  /**
   * Returns the number of steps nescessary for completing this modules
   * adjustment
   * 
   * @return number of steps
   */
  int getNumberOfSteps();

  /**
   * Returns the remaining number of steps until this module is ready.
   * 
   * @return remaining number of steps until this module is ready
   */
  int getRemainingNumberOfSteps();

  /**
   * Returns the variable that controls whether this module is active
   * 
   * @return is-active variable
   */
  Variable<Boolean> getIsActiveVariable();

  /**
   * Returns status string variable
   * 
   * @return status string variable
   */
  Variable<String> getStatusStringVariable();

  /**
   * Convenience method for obtaining the is-active flag.
   * 
   * @return true -> module active, false otherwise
   */
  default boolean isActive()
  {
    return getIsActiveVariable().get();
  }

}

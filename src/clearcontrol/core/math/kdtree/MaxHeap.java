package clearcontrol.core.math.kdtree;

/**
 * Max Heap
 *
 *
 * @param <T>
 *          type stored in heap
 */
public interface MaxHeap<T>
{
  public int size();

  public void offer(double key, T value);

  public void replaceMax(double key, T value);

  public void removeMax();

  public T getMax();

  public double getMaxKey();
}
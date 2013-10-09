package variable.doublev;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import variable.DoubleVariableInterface;
import variable.EventPropagator;
import variable.NamedVariable;

public class DoubleVariable extends NamedVariable<Double>	implements
																													DoubleVariableInterface

{
	protected volatile double mValue;
	private final CopyOnWriteArrayList<DoubleVariable> mVariablesToSendUpdatesTo = new CopyOnWriteArrayList<DoubleVariable>();

	public DoubleVariable(final String pVariableName)
	{
		this(pVariableName, 0);
	}

	public DoubleVariable(final String pVariableName,
												final double pDoubleValue)
	{
		super(pVariableName);
		mValue = pDoubleValue;
	}

	@Override
	public void setCurrent()
	{
		setValue(mValue);
	}

	@Override
	public void setValue(final double pNewValue)
	{
		EventPropagator.clear();
		setValueInternal(pNewValue);
	}

	public void setLongValue(final long pNewValue)
	{
		setValue(Double.longBitsToDouble(pNewValue));
	}

	@Override
	public void set(final Double pNewValue)
	{
		setValue(pNewValue);
	}

	public void markAsTraversed()
	{
		EventPropagator.add(this);
	}

	public boolean setValueInternal(final double pNewValue)
	{
		if (EventPropagator.hasBeenTraversed(this))
		{
			return false;
		}
		markAsTraversed();

		final double lOldValueBeforeHook = mValue;

		// We protect ourselves from called code that might clear the Thread
		// traversal list:
		final ArrayList<Object> lCopyOfListOfTraversedObjects = EventPropagator.getCopyOfListOfTraversedObjects();
		mValue = setEventHook(lOldValueBeforeHook, pNewValue);
		notifyListenersOfSetEvent(lOldValueBeforeHook, pNewValue);
		EventPropagator.setListOfTraversedObjects(lCopyOfListOfTraversedObjects);

		sync(pNewValue, false);

		return true;
	}

	public void sync(	final double pNewValue,
										final boolean pClearEventQueue)
	{
		if (pClearEventQueue)
		{
			EventPropagator.clear();
		}

		// We protect ourselves from called code that might clear the Thread
		// traversal list:
		final ArrayList<Object> lCopyOfListOfTraversedObjects = EventPropagator.getCopyOfListOfTraversedObjects();

		if (mVariablesToSendUpdatesTo != null)
		{
			for (final DoubleVariable lDoubleVariable : mVariablesToSendUpdatesTo)
			{
				EventPropagator.setListOfTraversedObjects(lCopyOfListOfTraversedObjects);
				if (EventPropagator.hasNotBeenTraversed(lDoubleVariable))
				{
					lDoubleVariable.setValueInternal(pNewValue);
				}
			}
		}
		EventPropagator.setListOfTraversedObjects(lCopyOfListOfTraversedObjects);
		EventPropagator.addAllToListOfTraversedObjects(mVariablesToSendUpdatesTo);

	}

	public double setEventHook(	final double pOldValue,
															final double pNewValue)
	{
		return pNewValue;
	}

	public double getEventHook(final double pCurrentValue)
	{
		notifyListenersOfGetEvent(pCurrentValue);
		return pCurrentValue;
	}

	@Override
	public Double get()
	{
		return getEventHook(getValue());
	}

	@Override
	public double getValue()
	{
		return getEventHook(mValue);
	}

	public long getLongValue()
	{
		return Double.doubleToRawLongBits(mValue);
	}

	@Override
	public final void sendUpdatesTo(final DoubleVariable pDoubleVariable)
	{
		mVariablesToSendUpdatesTo.add(pDoubleVariable);
	}

	@Override
	public final void doNotSendUpdatesTo(final DoubleVariable pDoubleVariable)
	{
		mVariablesToSendUpdatesTo.remove(pDoubleVariable);
	}

	@Override
	public final void doNotSendAnyUpdates()
	{
		mVariablesToSendUpdatesTo.clear();
	}

	@Override
	public final void syncWith(final DoubleVariable pDoubleVariable)
	{
		this.sendUpdatesTo(pDoubleVariable);
		pDoubleVariable.sendUpdatesTo(this);
	}

	@Override
	public void doNotSyncWith(final DoubleVariable pDoubleVariable)
	{
		this.doNotSendUpdatesTo(pDoubleVariable);
		pDoubleVariable.doNotSendUpdatesTo(this);
	}

}

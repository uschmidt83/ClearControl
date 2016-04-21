package rtlib.hardware.stages.devices.ecc100.variables;

import ecc100.ECC100Axis;
import rtlib.core.variable.Variable;

public class ResetVariable extends Variable<Boolean>
{

	private final ECC100Axis mECC100Axis;

	public ResetVariable(String pVariableName, ECC100Axis pECC100Axis)
	{
		super(pVariableName, false);
		mECC100Axis = pECC100Axis;
	}

	@Override
	public Boolean setEventHook(Boolean pOldValue, Boolean pNewValue)
	{
		final Boolean lValue = super.setEventHook(pOldValue, pNewValue);
		// TODO: What is this supposed to be?
		return lValue;
	}

	@Override
	public Boolean getEventHook(Boolean pCurrentValue)
	{
		return super.getEventHook(pCurrentValue);
	}
}
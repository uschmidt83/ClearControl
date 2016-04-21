package rtlib.hardware.lasers.devices.cobolt.adapters;

import rtlib.com.serial.adapters.SerialTextDeviceAdapter;
import rtlib.hardware.lasers.devices.cobolt.adapters.protocol.ProtocolCobolt;

public class SetPowerOnOffAdapter extends CoboltAdapter<Boolean> implements
																																SerialTextDeviceAdapter<Boolean>
{

	@Override
	public byte[] getGetValueCommandMessage()
	{
		return null;
	}

	@Override
	public Boolean parseValue(final byte[] pMessage)
	{
		return null;
	}

	@Override
	public byte[] getSetValueCommandMessage(final Boolean pOldValue,
																					final Boolean pNewValue)
	{
		return pNewValue ? ProtocolCobolt.cSetLaserOnCommand.getBytes()
										: ProtocolCobolt.cSetLaserOffCommand.getBytes();
	}

}
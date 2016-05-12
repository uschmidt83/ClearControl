package clearcontrol.microscope.lightsheet.component.detection.gui.jfx;

import clearcontrol.gui.jfx.customvarpanel.CustomVariablePane;
import clearcontrol.gui.jfx.onoff.OnOffArrayPane;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArmInterface;

public class DetectionArmPanel extends CustomVariablePane
{

	public DetectionArmPanel(DetectionArmInterface pDetectionArmInterface)
	{
		super();

		addTab("DOFs");
		addSliderForVariable(	"Z :",
													pDetectionArmInterface.getZVariable(),
													5).setUpdateIfChanging(true);/**/

		addTab("Functions");

		addFunctionPane("Z: ", pDetectionArmInterface.getZFunction());/**/
		

		
		
	}

}
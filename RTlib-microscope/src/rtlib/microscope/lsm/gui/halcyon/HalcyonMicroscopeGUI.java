package rtlib.microscope.lsm.gui.halcyon;

import javafx.stage.Stage;
import model.node.HalcyonNode;
import model.node.HalcyonNodeType;
import rtlib.lasers.LaserDeviceInterface;
import rtlib.lasers.gui.LaserDeviceGUI;
import rtlib.microscope.lsm.LightSheetMicroscopeDeviceLists;
import rtlib.microscope.lsm.LightSheetMicroscopeInterface;
import rtlib.stages.StageDeviceInterface;
import rtlib.stages.gui.StageDeviceGUI;
import view.FxFrame;
import window.console.StdOutputCaptureConsole;
import window.demo.DemoToolbarWindow;
import window.toolbar.MicroscopeStartStopToolbar;

public class HalcyonMicroscopeGUI
{
	public HalcyonMicroscopeGUI( Stage primaryStage, LightSheetMicroscopeInterface lightSheetMicroscopeInterface )
			throws Exception
	{
		final FxFrame lHalcyonFrame = new FxFrame();
		lHalcyonFrame.start( primaryStage );

		LightSheetMicroscopeDeviceLists deviceLists = lightSheetMicroscopeInterface.getDeviceLists();

		// Laser Device list
		for(int i = 0; i < deviceLists.getNumberOfLaserDevices(); i++)
		{
			LaserDeviceInterface laserDevice = deviceLists.getLaserDevice( i );

			LaserDeviceGUI laserDeviceGUI = new LaserDeviceGUI( laserDevice );
			laserDeviceGUI.init();

			HalcyonNode node = new HalcyonNode( "Laser-" + i, HalcyonNodeType.Laser, laserDeviceGUI.getPanel() );
			lHalcyonFrame.addNode( node );
		}

		// Stage Device List
		for(int i =0; i < deviceLists.getNumberOfStageDevices(); i++)
		{
			StageDeviceInterface stageDevice = deviceLists.getStageDevice( i );

			//Stage
			StageDeviceGUI stageDeviceGUI = new StageDeviceGUI( stageDevice );
			stageDeviceGUI.init();

			HalcyonNode node = new HalcyonNode( "Stage-" + i, HalcyonNodeType.Stage, stageDeviceGUI.getPanel() );
			lHalcyonFrame.addNode( node );
		}

		// Utility interfaces are added
		lHalcyonFrame.addToolbar( new DemoToolbarWindow( lHalcyonFrame.getViewManager() ) );
	}
}

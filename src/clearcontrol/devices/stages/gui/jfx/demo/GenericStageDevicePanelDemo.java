package clearcontrol.devices.stages.gui.jfx.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import clearcontrol.devices.stages.StageType;
import clearcontrol.devices.stages.devices.sim.StageDeviceSimulator;
import clearcontrol.devices.stages.gui.jfx.GenericStageDevicePanel;

public class GenericStageDevicePanelDemo extends Application
{

  @Override
  public void start(Stage pPrimaryStage) throws Exception
  {
    StageDeviceSimulator lStageDeviceSimulator =
                                               new StageDeviceSimulator("demostage",
                                                                        StageType.XYZR);

    lStageDeviceSimulator.setSimLogging(true);

    lStageDeviceSimulator.addDOF("X", -100, 100);
    lStageDeviceSimulator.addDOF("Y", -100, 100);
    lStageDeviceSimulator.addDOF("Z", -100, 100);
    lStageDeviceSimulator.addDOF("R", 0, 360);

    GenericStageDevicePanel lGenericStageDevicePanel =
                                                     new GenericStageDevicePanel(lStageDeviceSimulator);

    Scene scene = new Scene(lGenericStageDevicePanel,
                            javafx.scene.paint.Color.WHITE);

    pPrimaryStage.setTitle(this.getClass().getSimpleName());
    pPrimaryStage.setScene(scene);
    pPrimaryStage.show();

  }

  public static void main(String[] args)
  {
    Application.launch(GenericStageDevicePanelDemo.class);
  }

}
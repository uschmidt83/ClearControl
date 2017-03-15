package clearcontrol.hardware.optomech.filterwheels.gui.jfx.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import clearcontrol.hardware.optomech.filterwheels.devices.sim.FilterWheelDeviceSimulator;
import clearcontrol.hardware.optomech.filterwheels.gui.jfx.FilterWheelDevicePanel;

public class FilterWheelDevicePanelDemo extends Application
{

  @Override
  public void start(Stage pPrimaryStage) throws Exception
  {
    FilterWheelDeviceSimulator lFilterWheelDeviceSimulator =
                                                           new FilterWheelDeviceSimulator("demofilterwheel",
                                                                                          0,
                                                                                          1,
                                                                                          2,
                                                                                          3);

    lFilterWheelDeviceSimulator.setSimLogging(true);
    lFilterWheelDeviceSimulator.setPosition(2);
    lFilterWheelDeviceSimulator.setSpeed(2);

    FilterWheelDevicePanel lFilterWheelDevicePanel =
                                                   new FilterWheelDevicePanel(lFilterWheelDeviceSimulator);

    Scene scene = new Scene(lFilterWheelDevicePanel,
                            javafx.scene.paint.Color.WHITE);

    pPrimaryStage.setTitle(this.getClass().getSimpleName());
    pPrimaryStage.setScene(scene);
    pPrimaryStage.show();

  }

  public static void main(String[] args)
  {
    Application.launch(FilterWheelDevicePanelDemo.class);
  }

}

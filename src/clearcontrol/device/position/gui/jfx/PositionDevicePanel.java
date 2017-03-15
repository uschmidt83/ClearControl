package clearcontrol.device.position.gui.jfx;

import java.util.ArrayList;
import javafx.application.Platform;

import clearcontrol.core.variable.Variable;
import clearcontrol.device.position.PositionDeviceInterface;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.togglebutton.CustomToggleButton;

public class PositionDevicePanel extends CustomGridPane
{

  public PositionDevicePanel(PositionDeviceInterface pPositionDeviceInterface)
  {
    super();

    int[] lValidPositions =
                          pPositionDeviceInterface.getValidPositions();

    Variable<Integer> lPositionVariable =
                                        pPositionDeviceInterface.getPositionVariable();

    ArrayList<CustomToggleButton> lToggleButtonList =
                                                    new ArrayList<>();

    for (int i = 0; i < lValidPositions.length; i++)
    {
      String lPositionName =
                           pPositionDeviceInterface.getPositionName(i);
      CustomToggleButton lToggleButton =
                                       new CustomToggleButton(lPositionName);
      add(lToggleButton, 0, i);
      lToggleButtonList.add(lToggleButton);

      final int fi = i;
      lToggleButton.setOnAction((e) -> {
        lPositionVariable.setAsync(fi);
      });

    }

    lPositionVariable.addSetListener((o, n) -> {

      if (n != o)
        Platform.runLater(() -> {
          int i = 0;
          for (CustomToggleButton lCustomToggleButton : lToggleButtonList)
          {
            lCustomToggleButton.setSelected(i == n);
            i++;
          }
        });
    });

  }
}

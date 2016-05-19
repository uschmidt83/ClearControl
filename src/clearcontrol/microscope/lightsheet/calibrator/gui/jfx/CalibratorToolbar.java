package clearcontrol.microscope.lightsheet.calibrator.gui.jfx;

import javafx.beans.property.BooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import org.dockfx.DockNode;

import clearcontrol.core.variable.Variable;
import clearcontrol.gui.jfx.gridpane.StandardGridPane;
import clearcontrol.gui.jfx.onoff.OnOffArrayPane;
import clearcontrol.gui.jfx.slider.VariableSlider;
import clearcontrol.gui.variable.JFXPropertyVariable;
import clearcontrol.microscope.lightsheet.calibrator.Calibrator;

public class CalibratorToolbar extends DockNode
{
	private GridPane mGridPane;

	public CalibratorToolbar(Calibrator pCalibrator)
	{
		super(new StandardGridPane());
		mGridPane = (GridPane) getContents();

		Button lStartCalibration = new Button("Calibrate");
		lStartCalibration.setAlignment(Pos.CENTER);
		lStartCalibration.setMaxWidth(Double.MAX_VALUE);
		lStartCalibration.setOnAction((e) -> {
			pCalibrator.calibrate();
		});
		GridPane.setColumnSpan(lStartCalibration, 3);
		mGridPane.add(lStartCalibration, 0, 0);

		Button lStart3D = new Button("Cancel");
		lStart3D.setAlignment(Pos.CENTER);
		lStart3D.setMaxWidth(Double.MAX_VALUE);
		lStart3D.setOnAction((e) -> {
			pCalibrator.setCancelCalibrate(true);
		});
		GridPane.setColumnSpan(lStart3D, 3);
		mGridPane.add(lStart3D, 0, 1);

		Button lStop = new Button("Stop");
		lStop.setAlignment(Pos.CENTER);
		lStop.setMaxWidth(Double.MAX_VALUE);
		lStop.setOnAction((e) -> {
			pInteractiveAcquisition.stopAcquisition();
		});
		GridPane.setColumnSpan(lStop, 3);
		mGridPane.add(lStop, 0, 2);

		VariableSlider<Double> lIntervalSlider = new VariableSlider<Double>("Period (s)",
																																				pInteractiveAcquisition.getLoopPeriodVariable(),
																																				0.0,
																																				1000.0,
																																				0.001,
																																				100.0);
		lIntervalSlider.setAlignment(Pos.BASELINE_CENTER);
		GridPane.setHgrow(lIntervalSlider.getSlider(), Priority.ALWAYS);
		mGridPane.add(lIntervalSlider.getLabel(), 0, 3);
		mGridPane.add(lIntervalSlider.getSlider(), 1, 3);
		mGridPane.add(lIntervalSlider.getTextField(), 2, 3);

		VariableSlider<Double> lExposureSlider = new VariableSlider<Double>("Exp (s)",
																																				pInteractiveAcquisition.getExposureVariable(),
																																				0.0,
																																				1.0,
																																				0.001,
																																				0.1);
		lExposureSlider.setAlignment(Pos.BASELINE_CENTER);
		GridPane.setHgrow(lExposureSlider.getSlider(), Priority.ALWAYS);
		mGridPane.add(lExposureSlider.getLabel(), 0, 4);
		mGridPane.add(lExposureSlider.getSlider(), 1, 4);
		mGridPane.add(lExposureSlider.getTextField(), 2, 4);

		Label lTriggerOnChangeLabel = new Label("Trigger-on-change");
		CheckBox lTriggerOnChangeLabelCheckBox = new CheckBox();
		GridPane.setColumnSpan(lTriggerOnChangeLabel, 2);
		mGridPane.add(lTriggerOnChangeLabel, 0, 5);
		mGridPane.add(lTriggerOnChangeLabelCheckBox, 2, 5);

		BooleanProperty lSelectedProperty = lTriggerOnChangeLabelCheckBox.selectedProperty();
		JFXPropertyVariable<Boolean> lJFXPropertyVariable = new JFXPropertyVariable<Boolean>(	lSelectedProperty,
																																													"TriggerOnChange",
																																													false);

		Variable<Boolean> lTriggerOnChangeVariable = pInteractiveAcquisition.getTriggerOnChangeVariable();
		lJFXPropertyVariable.syncWith(lTriggerOnChangeVariable);
		lSelectedProperty.set(lTriggerOnChangeVariable.get());

		Label lActiveCamerasLabel = new Label("Active Cameras");
		mGridPane.add(lActiveCamerasLabel, 0, 6);

		OnOffArrayPane lAddOnOffArray = new OnOffArrayPane();

		for (int c = 0; c < pInteractiveAcquisition.getNumberOfCameras(); c++)
		{
			lAddOnOffArray.addSwitch(	"" + c,
																pInteractiveAcquisition.getActiveCameraVariable(c));
		}

		GridPane.setColumnSpan(lAddOnOffArray, 2);
		mGridPane.add(lAddOnOffArray, 1, 6);

	}
}

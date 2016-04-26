package rtlib.gui.jfx.sliderpanel.demo;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import rtlib.core.variable.Variable;
import rtlib.core.variable.bounded.BoundedVariable;
import rtlib.gui.jfx.onoff.OnOffArrayPane;
import rtlib.gui.jfx.slider.VariableSlider;
import rtlib.gui.jfx.sliderpanel.CustomVariablePane;

public class CustomVariablePaneDemo extends Application
{

	@Override
	public void start(Stage stage)
	{
		Group root = new Group();
		Scene scene = new Scene(root, 800, 600);
		stage.setScene(scene);
		stage.setTitle("Slider Sample");
		// scene.setFill(Color.BLACK);

		CustomVariablePane lCustomVariablePane = new CustomVariablePane();

		Variable<Number> lDoubleVariable = new Variable<Number>("DoubleVar",
																														0.0);
		lDoubleVariable.addSetListener((o, n) -> {
			System.out.println("double: " + n);
		});
		lCustomVariablePane.addSliderForVariable(lDoubleVariable,
																			-1.0,
																			1.0,
																			0.1,
																			0.1);

		Variable<Number> lIntegerVariable1 = new Variable<Number>("IntegerVar",
																															2);
		lIntegerVariable1.addSetListener((o, n) -> {
			System.out.println("int: " + n);
		});
		lCustomVariablePane.addSliderForVariable(lIntegerVariable1,
																			-10,
																			30,
																			1,
																			5);

		Variable<Number> lIntegerVariable2 = new Variable<Number>("IntegerChng",
																															2);
		lIntegerVariable2.addSetListener((o, n) -> {
			System.out.println("int2: " + n);
		});
		VariableSlider<Number> lAddSliderForVariable = lCustomVariablePane.addSliderForVariable(	lIntegerVariable2,
																																											-10,
																																											30,
																																											1,
																																											5);
		lAddSliderForVariable.setUpdateIfChanging(true);

		BoundedVariable<Number> lBoundedVariable = new BoundedVariable<Number>(	"Bounded",
																																						2.0,
																																						-10.0,
																																						10.0,
																																						0.1);
		lBoundedVariable.addSetListener((o, n) -> {
			System.out.println("boundeddouble: " + n);
		});
		VariableSlider<Number> lBoundedVariableSlider = lCustomVariablePane.addSliderForVariable(lBoundedVariable,
																																											5.0);

		
		
		
		OnOffArrayPane lAddOnOffArray = lCustomVariablePane.addOnOffArray("onoff");
		
		for (int i = 0; i < 5; i++)
		{
			final int fi = i;

			Variable<Boolean> lBoolVariable = new Variable<>(	"b"+i,
																												i%2==0);
			lBoolVariable.addSetListener((o, n) -> {
				System.out.println("bool "+fi+": " + n);
			});

			lAddOnOffArray.addSwitch("S"+i, lBoolVariable);
		}
		
		
		
		root.getChildren().add(lCustomVariablePane);
		
		
		

		stage.show();
	}

	public static void main(String[] args)
	{
		launch(args);
	}
}
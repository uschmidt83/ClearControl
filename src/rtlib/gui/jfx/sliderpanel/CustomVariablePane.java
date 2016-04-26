package rtlib.gui.jfx.sliderpanel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import rtlib.core.variable.Variable;
import rtlib.core.variable.bounded.BoundedVariable;
import rtlib.gui.jfx.onoff.OnOffArrayPane;
import rtlib.gui.jfx.slider.VariableSlider;

public class CustomVariablePane extends GridPane
{

	private static final double cDefaultWidth = 300;

	private int mCursor = 0;
	private double mSliderWidth;

	public CustomVariablePane()
	{
		this(cDefaultWidth);
	}

	public CustomVariablePane(double pSliderWidth)
	{
		super();
		mSliderWidth = pSliderWidth;

		setAlignment(Pos.CENTER);
		setHgap(10);
		setVgap(10);
		setPadding(new Insets(25, 25, 25, 25));
	}

	public <T extends Number> VariableSlider<T> addSliderForVariable(	Variable<T> pVariable,
																																		T pMin,
																																		T pMax,
																																		T pGranularity,
																																		T pTicks)
	{
		return addSliderForVariable(pVariable.getName(),
																pVariable,
																pMin,
																pMax,
																pGranularity,
																pTicks);
	}

	public <T extends Number> VariableSlider<T> addSliderForVariable(	String pSliderName,
																																		Variable<T> pVariable,
																																		T pMin,
																																		T pMax,
																																		T pGranularity,
																																		T pTicks)
	{
		final VariableSlider<T> lSlider = new VariableSlider<T>(pSliderName,
																														pVariable,
																														pMin,
																														pMax,
																														pGranularity,
																														pTicks);
		lSlider.getSlider().setPrefWidth(mSliderWidth);
		lSlider.getSlider().setMinWidth(mSliderWidth/4);
		lSlider.getSlider().setMaxWidth(Double.MAX_VALUE);

		GridPane.setHgrow(lSlider.getSlider(), Priority.ALWAYS);

		int lCursor = mCursor++;
		add(lSlider.getLabel(), 0, lCursor);
		add(lSlider.getSlider(), 1, lCursor);
		add(lSlider.getTextField(), 2, lCursor);

		return lSlider;
	}

	public <T extends Number> VariableSlider<T> addSliderForVariable(	BoundedVariable<T> pVariable,
																																		T pTicks)
	{
		return addSliderForVariable(pVariable.getName(),
																pVariable,
																pTicks);
	}

	public <T extends Number> VariableSlider<T> addSliderForVariable(	String pSliderName,
																																		BoundedVariable<T> pVariable,
																																		T pTicks)
	{
		final VariableSlider<T> lSlider = new VariableSlider<T>(pSliderName,
																														pVariable,
																														pTicks);
		lSlider.setPrefWidth(mSliderWidth);
		lSlider.setMinWidth(mSliderWidth/4);
		lSlider.setMaxWidth(Double.MAX_VALUE);

		GridPane.setHgrow(lSlider.getSlider(), Priority.ALWAYS);

		int lCursor = mCursor++;
		add(lSlider.getLabel(), 0, lCursor);
		add(lSlider.getSlider(), 1, lCursor);
		add(lSlider.getTextField(), 2, lCursor);

		return lSlider;
	}
	
	public <T extends Number> OnOffArrayPane addOnOffArray(String pOnOffArrayPaneName)
	{
		
		final OnOffArrayPane lOnOffArrayPane = new OnOffArrayPane();
		lOnOffArrayPane.setVertical(false);
		
		lOnOffArrayPane.setPrefWidth(mSliderWidth);
		lOnOffArrayPane.setMinWidth(mSliderWidth/4);
		lOnOffArrayPane.setMaxWidth(Double.MAX_VALUE);

		GridPane.setHgrow(lOnOffArrayPane, Priority.ALWAYS);

		Label lLabel = new Label(pOnOffArrayPaneName); 
		
		int lCursor = mCursor++;
		add(lLabel, 0, lCursor);
		add(lOnOffArrayPane, 1, lCursor);
		GridPane.setColumnSpan(lOnOffArrayPane, 2);

		return lOnOffArrayPane;
	}
	
}
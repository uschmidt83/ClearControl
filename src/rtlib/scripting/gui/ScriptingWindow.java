package rtlib.scripting.gui;

import java.awt.HeadlessException;

import javax.swing.SwingUtilities;
import javafx.embed.swing.SwingNode;
import javafx.scene.layout.BorderPane;
import rtlib.scripting.engine.ScriptingEngine;

public class ScriptingWindow extends BorderPane
{

	private static final long serialVersionUID = 1L;
	private ScriptingPanel mScriptingPanel;
	private boolean mChanged = false;

	public ScriptingWindow() throws HeadlessException
	{
		this("Scripting Window", null, 60, 80);
	}

	public ScriptingWindow(	String pTitle,
													ScriptingEngine pScriptingEngine,
													int pNumberOfRows,
													int pNumberOfCols) throws HeadlessException
	{
		mScriptingPanel = new ScriptingPanel(	pTitle,
																					pScriptingEngine,

																							pNumberOfRows,
																					pNumberOfCols);
		final SwingNode node = new SwingNode();

		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				node.setContent( mScriptingPanel );
			}
		} );

		this.setOnMouseClicked( event -> {
			if(mChanged)
			{
				node.setContent( mScriptingPanel );
				mChanged = false;
			}
		} );

		this.widthProperty().addListener(
				( observable, oldValue, newValue ) -> mChanged = true );

		this.heightProperty().addListener(
				( observable, oldValue, newValue ) -> mChanged = true );

		setCenter( node );
	}

	public void loadLastLoadedScriptFile()
	{
		mScriptingPanel.loadLastLoadedScriptFile();
	}

}
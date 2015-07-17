package rtlib.microscope.lightsheet.demo;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import net.imglib2.img.basictypeaccess.offheap.ShortOffHeapAccess;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import org.junit.Test;
import org.python.google.common.collect.Lists;

import rtlib.cameras.StackCameraDeviceInterface;
import rtlib.cameras.devices.orcaflash4.OrcaFlash4StackCamera;
import rtlib.cameras.devices.sim.StackCameraDeviceSimulator;
import rtlib.core.concurrent.future.FutureBooleanList;
import rtlib.core.concurrent.thread.ThreadUtils;
import rtlib.microscope.lightsheet.LightSheetMicroscope;
import rtlib.microscope.lightsheet.detection.DetectionPath;
import rtlib.microscope.lightsheet.gui.LightSheetMicroscopeGUI;
import rtlib.microscope.lightsheet.illumination.LightSheet;
import rtlib.stack.processor.StackIdentityPipeline;
import rtlib.symphony.devices.SignalGeneratorInterface;
import rtlib.symphony.devices.nirio.NIRIOSignalGenerator;
import rtlib.symphony.devices.sim.SignalGeneratorSimulatorDevice;
import rtlib.symphony.gui.ScoreVisualizerJFrame;
import rtlib.symphony.movement.Movement;
import rtlib.symphony.score.ScoreInterface;

public class LightSheetMicroscopeDemo
{

	private static final double cImageResolution = 1024;

	@Test
	public void demoOnSimulators() throws InterruptedException,
																ExecutionException
	{
		final SignalGeneratorInterface lSignalGeneratorDevice = new SignalGeneratorSimulatorDevice();
		final StackCameraDeviceInterface<UnsignedShortType, ShortOffHeapAccess> lCamera = new StackCameraDeviceSimulator<>(	null,
																																																												new UnsignedShortType(),
																																																												lSignalGeneratorDevice.getTriggerVariable());

		demoWith(	true,
							Lists.newArrayList(lCamera),
							lSignalGeneratorDevice);

	}

	@Test
	public void demoOnRealHardwareSingleCamera() throws InterruptedException,
																							ExecutionException
	{
		final SignalGeneratorInterface lSignalGeneratorDevice = new NIRIOSignalGenerator();
		final StackCameraDeviceInterface<UnsignedShortType, ShortOffHeapAccess> lCamera = OrcaFlash4StackCamera.buildWithExternalTriggering(0);

		demoWith(	true,
							Lists.newArrayList(lCamera),
							lSignalGeneratorDevice);

	}

	@Test
	public void demoOnRealHardwareTwoCameras() throws InterruptedException,
																						ExecutionException
	{
		final SignalGeneratorInterface lSignalGeneratorDevice = new NIRIOSignalGenerator();
		final StackCameraDeviceInterface<UnsignedShortType, ShortOffHeapAccess> lCamera1 = OrcaFlash4StackCamera.buildWithExternalTriggering(0);
		final StackCameraDeviceInterface<UnsignedShortType, ShortOffHeapAccess> lCamera2 = OrcaFlash4StackCamera.buildWithExternalTriggering(1);

		demoWith(	true,
							Lists.newArrayList(lCamera1, lCamera2),
							lSignalGeneratorDevice);

	}

	@Test
	public void demoScriptingOnRealHardwareTwoCameras()	throws InterruptedException,
																											ExecutionException
	{
		final SignalGeneratorInterface lSignalGeneratorDevice = new NIRIOSignalGenerator();
		final StackCameraDeviceInterface<UnsignedShortType, ShortOffHeapAccess> lCamera1 = OrcaFlash4StackCamera.buildWithExternalTriggering(0);
		final StackCameraDeviceInterface<UnsignedShortType, ShortOffHeapAccess> lCamera2 = OrcaFlash4StackCamera.buildWithExternalTriggering(1);

		demoWith(	false,
							Lists.newArrayList(lCamera1, lCamera2),
							lSignalGeneratorDevice);

	}

	public void demoWith(	boolean pAutoStart,
												ArrayList<StackCameraDeviceInterface<UnsignedShortType, ShortOffHeapAccess>> pCameras,
												SignalGeneratorInterface pSignalGeneratorDevice) throws InterruptedException,
																																				ExecutionException
	{


		final LightSheetMicroscope lLightSheetMicroscope = new LightSheetMicroscope("demoscope");


		for (final StackCameraDeviceInterface<UnsignedShortType, ShortOffHeapAccess> lCamera : pCameras)
		{
			final StackIdentityPipeline<UnsignedShortType, ShortOffHeapAccess> lStackIdentityPipeline = new StackIdentityPipeline<UnsignedShortType, ShortOffHeapAccess>();

			lStackIdentityPipeline.getOutputVariable()
														.addSetListener((pCurrentValue, pNewValue) -> {
															System.out.println("StackIdentityPipeline" + lCamera.getName()
																									+ "->"
																									+ pNewValue);

														});

			lCamera.getStackWidthVariable().setValue(cImageResolution);
			lCamera.getStackHeightVariable().setValue(cImageResolution);
			lCamera.getExposureInMicrosecondsVariable().setValue(5000);

			lLightSheetMicroscope.getDeviceLists()
														.addStackCameraDevice(lCamera,
																									lStackIdentityPipeline);
		}

		lLightSheetMicroscope.getDeviceLists()
													.addSignalGeneratorDevice(pSignalGeneratorDevice);

		// Setting up staging movements:

		final Movement lBeforeExposureMovement = new Movement("BeforeExposure");
		final Movement lExposureMovement = new Movement("Exposure");




		final ScoreInterface lStagingScore = pSignalGeneratorDevice.getStagingScore();

		lStagingScore.addMovement(lBeforeExposureMovement);
		lStagingScore.addMovement(lExposureMovement);

		// setting up staging score visualization:

		final ScoreVisualizerJFrame lVisualizer = ScoreVisualizerJFrame.visualize("LightSheetDemo",
																																							lStagingScore);

		// Setting up detection path:

		final DetectionPath lDetectionPath = new DetectionPath("demodetpath");

		lLightSheetMicroscope.getDeviceLists()
													.addDetectionPathDevice(lDetectionPath);

		lDetectionPath.addStavesToBeforeExposureMovement(lBeforeExposureMovement);
		lDetectionPath.addStavesToExposureMovement(lExposureMovement);

		// Setting up lightsheet:

		final LightSheet lLightSheet = new LightSheet("demolightsheet",
																									9.4,
																									512,
																									2);
		lLightSheetMicroscope.getDeviceLists()
													.addLightSheetDevice(lLightSheet);

		lBeforeExposureMovement.setDuration(lLightSheet.getBeforeExposureMovementDuration(TimeUnit.NANOSECONDS),
																				TimeUnit.NANOSECONDS);
		lExposureMovement.setDuration(lLightSheet.getExposureMovementDuration(TimeUnit.NANOSECONDS),
																	TimeUnit.NANOSECONDS);

		lLightSheet.setBeforeExposureMovement(lBeforeExposureMovement);
		lLightSheet.setExposureMovement(lExposureMovement);

		lLightSheet.getLightSheetLengthInMicronsVariable().setValue(100);
		lLightSheet.getEffectiveExposureInMicrosecondsVariable()
								.setValue(5000);

		lLightSheet.getImageHeightVariable()
								.setValue(pCameras.get(0)
																	.getStackHeightVariable()
																	.getValue());



		// setting up scope GUI:

		final LightSheetMicroscopeGUI lGUI = new LightSheetMicroscopeGUI(lLightSheetMicroscope);

		if (lGUI != null)
			assertTrue(lGUI.open());
		else
			lLightSheetMicroscope.sendStacksToNull();

		assertTrue(lLightSheetMicroscope.open());
		Thread.sleep(1000);

		if (lGUI != null)
			lGUI.connectGUI();

		if (pAutoStart)
		{
			System.out.println("Start building queue");

			for (int i = 0; i < 128; i++)
				lLightSheetMicroscope.addCurrentStateToQueue();
			lLightSheetMicroscope.addCurrentStateToQueueNotCounting();
			System.out.println("finished building queue");

			while (lVisualizer.isVisible())
			{
				System.out.println("playQueue!");
				final FutureBooleanList lPlayQueue = lLightSheetMicroscope.playQueue();

				System.out.print("waiting...");
				final Boolean lBoolean = lPlayQueue.get();
				System.out.print(" ...done!");
				// System.out.println(lBoolean);
				// Thread.sleep(4000);
			}
		}
		else
		{
			while (lVisualizer.isVisible())
			{
				ThreadUtils.sleep(100, TimeUnit.MILLISECONDS);
			}
		}

		assertTrue(lLightSheetMicroscope.close());
		if (lGUI != null)
			assertTrue(lGUI.close());

	}

}
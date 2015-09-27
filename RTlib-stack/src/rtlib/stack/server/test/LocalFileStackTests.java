package rtlib.stack.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import coremem.recycling.BasicRecycler;
import net.imglib2.Cursor;
import net.imglib2.img.basictypeaccess.offheap.ShortOffHeapAccess;
import net.imglib2.img.planar.PlanarCursor;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import rtlib.core.variable.VariableInterface;
import rtlib.core.variable.bundle.VariableBundle;
import rtlib.core.variable.types.doublev.DoubleVariable;
import rtlib.core.variable.types.objectv.ObjectVariable;
import rtlib.stack.ContiguousOffHeapPlanarStackFactory;
import rtlib.stack.OffHeapPlanarStack;
import rtlib.stack.StackInterface;
import rtlib.stack.StackRequest;
import rtlib.stack.server.LocalFileStackSink;
import rtlib.stack.server.LocalFileStackSource;

public class LocalFileStackTests
{

	private static final int cBytesPerVoxel = 2;
	private static final long cSizeZ = 2048;
	private static final long cSizeY = 2048;
	private static final long cSizeX = 400;
	private static final int cNumberOfStacks = 2;
	private static final int cMaximalNumberOfAvailableStacks = 20;

	@Test
	public void testWriteSpeed() throws IOException
	{

		final File lRootFolder = new File(	File.createTempFile("test",
																"test")
												.getParentFile(),
											"LocalFileStackTests" + Math.random());/**/

		// final File lRootFolder = new File("/Volumes/External/Temp");

		lRootFolder.mkdirs();
		System.out.println(lRootFolder);

		final LocalFileStackSink<UnsignedShortType, ShortOffHeapAccess> lLocalFileStackSink = new LocalFileStackSink<UnsignedShortType, ShortOffHeapAccess>(new UnsignedShortType(),
																																							lRootFolder,
																																							"testSink");

		@SuppressWarnings("unchecked")
		final OffHeapPlanarStack<UnsignedShortType, ShortOffHeapAccess> lStack = (OffHeapPlanarStack<UnsignedShortType, ShortOffHeapAccess>) OffHeapPlanarStack.createStack(new UnsignedShortType(),
																																											cSizeX,
																																											cSizeY,
																																											cSizeZ);

		assertEquals(	cSizeX * cSizeY * cSizeZ,
						lStack.getNumberOfVoxels());

		assertEquals(	cSizeX * cSizeY * cSizeZ * cBytesPerVoxel,
						lStack.getSizeInBytes());

		for (int i = 0; i < cNumberOfStacks; i++)
		{

			final PlanarCursor<UnsignedShortType> lCursor = lStack.getPlanarImage()
																	.cursor();

			while (lCursor.hasNext())
			{
				final UnsignedShortType lUnsignedShortType = lCursor.next();
				lUnsignedShortType.set(i);
			}

			lCursor.reset();

			while (lCursor.hasNext())
			{
				final UnsignedShortType lUnsignedShortType = lCursor.next();
				assertEquals(i & 0xFFFF, lUnsignedShortType.get());
			}

		}

		long lStart = System.nanoTime();
		assertTrue(lLocalFileStackSink.appendStack(lStack));
		long lStop = System.nanoTime();

		double lElapsedTimeInSeconds = (lStop - lStart) * 1e-9;

		double lSpeed = (lStack.getSizeInBytes() * 1e-6) / lElapsedTimeInSeconds;

		System.out.format("speed: %g \n",lSpeed);

		lLocalFileStackSink.close();
		
		
		FileUtils.deleteDirectory(lRootFolder);

	}

	@Test
	public void testSinkAndSource() throws IOException
	{

		final File lRootFolder = new File(	File.createTempFile("test",
																"test")
												.getParentFile(),
											"LocalFileStackTests" + Math.random());/**/

		// final File lRootFolder = new File("/Volumes/External/Temp");

		lRootFolder.mkdirs();
		System.out.println(lRootFolder);

		{
			final LocalFileStackSink<UnsignedShortType, ShortOffHeapAccess> lLocalFileStackSink = new LocalFileStackSink<UnsignedShortType, ShortOffHeapAccess>(new UnsignedShortType(),
																																								lRootFolder,
																																								"testSink");

			final VariableBundle lVariableBundle = lLocalFileStackSink.getMetaDataVariableBundle();

			lVariableBundle.addVariable(new DoubleVariable(	"doublevar1",
															312));
			lVariableBundle.addVariable(new ObjectVariable<String>(	"stringvar1",
																	"123"));

			@SuppressWarnings("unchecked")
			final OffHeapPlanarStack<UnsignedShortType, ShortOffHeapAccess> lStack = (OffHeapPlanarStack<UnsignedShortType, ShortOffHeapAccess>) OffHeapPlanarStack.createStack(new UnsignedShortType(),
																																												cSizeX,
																																												cSizeY,
																																												cSizeZ);

			assertEquals(	cSizeX * cSizeY * cSizeZ,
							lStack.getNumberOfVoxels());
			// System.out.println(lStack.mNDimensionalArray.getLengthInElements()
			// *
			// 2);

			assertEquals(	cSizeX * cSizeY * cSizeZ * cBytesPerVoxel,
							lStack.getSizeInBytes());

			for (int i = 0; i < cNumberOfStacks; i++)
			{

				final PlanarCursor<UnsignedShortType> lCursor = lStack.getPlanarImage()
																		.cursor();

				while (lCursor.hasNext())
				{
					final UnsignedShortType lUnsignedShortType = lCursor.next();
					lUnsignedShortType.set(i);
				}

				lCursor.reset();

				while (lCursor.hasNext())
				{
					final UnsignedShortType lUnsignedShortType = lCursor.next();
					assertEquals(i & 0xFFFF, lUnsignedShortType.get());
				}

				assertTrue(lLocalFileStackSink.appendStack(lStack));
			}

			assertEquals(	cNumberOfStacks,
							lLocalFileStackSink.getNumberOfStacks());

			lLocalFileStackSink.close();
		}

		{
			final ContiguousOffHeapPlanarStackFactory<UnsignedShortType, ShortOffHeapAccess> lOffHeapPlanarStackFactory = new ContiguousOffHeapPlanarStackFactory<UnsignedShortType, ShortOffHeapAccess>();

			final BasicRecycler<StackInterface<UnsignedShortType, ShortOffHeapAccess>, StackRequest<UnsignedShortType>> lStackRecycler = new BasicRecycler<StackInterface<UnsignedShortType, ShortOffHeapAccess>, StackRequest<UnsignedShortType>>(	lOffHeapPlanarStackFactory,
																																																													cMaximalNumberOfAvailableStacks);

			final LocalFileStackSource<UnsignedShortType, ShortOffHeapAccess> lLocalFileStackSource = new LocalFileStackSource<UnsignedShortType, ShortOffHeapAccess>(	new UnsignedShortType(),
																																										lStackRecycler,
																																										lRootFolder,
																																										"testSink");

			final VariableBundle lVariableBundle = lLocalFileStackSource.getMetaDataVariableBundle();
			lVariableBundle.addVariable(new DoubleVariable(	"doublevar1",
															312));
			lVariableBundle.addVariable(new ObjectVariable<String>(	"stringvar1",
																	"123"));
			final VariableInterface<Double> lVariable1 = lVariableBundle.getVariable("doublevar1");
			// System.out.println(lVariable1.get());
			assertEquals(312, lVariable1.get(), 0.5);

			final VariableInterface<String> lVariable2 = lVariableBundle.getVariable("stringvar1");
			// System.out.println(lVariable2.get());
			assertEquals("123", lVariable2.get());

			StackInterface<UnsignedShortType, ShortOffHeapAccess> lStack;

			lLocalFileStackSource.update();

			assertEquals(	cNumberOfStacks,
							lLocalFileStackSource.getNumberOfStacks());

			for (int i = 0; i < cNumberOfStacks; i++)
			{
				lStack = lLocalFileStackSource.getStack(i);
				final Cursor<UnsignedShortType> lCursor = lStack.getImage()
																.cursor();

				while (lCursor.hasNext())
				{
					final UnsignedShortType lValue = lCursor.next();
					// System.out.println("size=" + lValue);
					assertEquals(i, lValue.get());
				}
			}

			lLocalFileStackSource.close();
		}

		FileUtils.deleteDirectory(lRootFolder);
		
	}
}

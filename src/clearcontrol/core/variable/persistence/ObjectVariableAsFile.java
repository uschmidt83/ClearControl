package clearcontrol.core.variable.persistence;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.variable.Variable;

public class ObjectVariableAsFile<O> extends Variable<O>
                                 implements Closeable

{
  private volatile long mCachedReferenceFileSignature =
                                                      Long.MIN_VALUE;

  private final File mFile;

  private final Object mLock = new Object();

  public ObjectVariableAsFile(final String pVariableName)
  {

    this(pVariableName,
         MachineConfiguration.getCurrentMachineConfiguration()
                             .getPersistentVariableFile(pVariableName),
         null);
  }

  public ObjectVariableAsFile(final String pVariableName,
                              final File pFile,
                              final O pReference)
  {
    super(pVariableName, pReference);
    mFile = pFile;
    mFile.getParentFile().mkdirs();
  }

  @Override
  public O get()
  {
    if (mReference != null && mFile != null
        && mFile.exists()
        && mFile.lastModified() < mCachedReferenceFileSignature)
    {
      return mReference;
    }

    try
    {
      mReference = readFromFile();
      return super.get();
    }
    catch (final Throwable e)
    {
      e.printStackTrace();
      return super.get();
    }
  }

  @Override
  public void set(final O pNewReference)
  {
    saveToFile(pNewReference);
    super.set(pNewReference);
  }

  private O readFromFile() throws FileNotFoundException,
                           IOException,
                           ClassNotFoundException
  {
    O lReference = null;
    synchronized (mLock)
    {
      ObjectInputStream lObjectInputStream = null;
      try
      {
        if (!(mFile.exists() && mFile.isFile()))
        {
          return super.get();
        }

        final FileInputStream lFileInputStream =
                                               new FileInputStream(mFile);
        final BufferedInputStream lBufferedInputStream =
                                                       new BufferedInputStream(lFileInputStream);
        lObjectInputStream =
                           new ObjectInputStream(lBufferedInputStream);

        lReference = (O) lObjectInputStream.readObject();
        mCachedReferenceFileSignature = mFile.lastModified();

        return lReference;
      }
      catch (final Throwable e)
      {
        e.printStackTrace();
      }
      finally
      {
        try
        {
          if (lObjectInputStream != null)
            lObjectInputStream.close();
        }
        catch (final IOException e)
        {
          e.printStackTrace();
        }
      }
    }
    return lReference;
  }

  private void saveToFile(O pReference)
  {
    synchronized (mLock)
    {
      ObjectOutputStream lObjectOutputStream = null;
      try
      {
        mFile.getParentFile().mkdirs();
        final FileOutputStream lFileOutputStream =
                                                 new FileOutputStream(mFile);
        final BufferedOutputStream lBufferedOutputStream =
                                                         new BufferedOutputStream(lFileOutputStream);
        lObjectOutputStream =
                            new ObjectOutputStream(lBufferedOutputStream);

        lObjectOutputStream.writeObject(pReference);

      }
      catch (final FileNotFoundException e)
      {
        e.printStackTrace();
      }
      catch (final IOException e)
      {
        e.printStackTrace();
      }
      finally
      {
        try
        {
          if (lObjectOutputStream != null)
            lObjectOutputStream.close();
        }
        catch (final IOException e)
        {
          e.printStackTrace();
        }
      }
    }

  };

  @Override
  public void close() throws IOException
  {

  }

}

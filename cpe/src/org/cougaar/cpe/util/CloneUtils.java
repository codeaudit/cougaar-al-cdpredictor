package org.cougaar.cpe.util;

import java.io.*;

public abstract class CloneUtils
{
    public static Object deepClone( Serializable s ) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1000) ;
        try
        {
            ObjectOutputStream oos = new ObjectOutputStream( bos ) ;
            oos.writeObject( s );
            oos.close();
            bos.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        ByteArrayInputStream bis = new ByteArrayInputStream( bos.toByteArray() ) ;
        try
        {
            ObjectInputStream ois = new ObjectInputStream( bis ) ;
            Object o = ois.readObject() ;
            return o ;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        throw new RuntimeException( "Error cloning " + s ) ;
    }
}

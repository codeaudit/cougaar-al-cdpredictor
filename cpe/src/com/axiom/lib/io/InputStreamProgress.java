package com.axiom.lib.io ;
import java.io.* ;
import com.axiom.lib.util.Progressable;

public class InputStreamProgress extends Thread implements Progressable {
    public InputStreamProgress( InputStream is ) {
        this.is = is;
    }

    public void run() {
        while ( true ) {
        }
    }

    InputStream is ;
}
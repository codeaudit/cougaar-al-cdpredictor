package org.cougaar.tools.castellan.ldm;

public class PlanLogConfig
{
    public String getLogCluster()
    {
        return logCluster;
    }

    public void setLogCluster( String logCluster )
    {
        this.logCluster = logCluster;
    }

    public int getARLogLevel()
    {
        return arLogLevel;
    }

    public void setARLogLevel( int arLogLevel )
    {
        this.arLogLevel = arLogLevel;
    }

    public boolean isLogEpochs()
    {
        return logEpochs;
    }

    public void setLogEpochs( boolean logEpochs )
    {
        this.logEpochs = logEpochs;
    }

    public boolean isLogPluginTriggers()
    {
        return logPluginTriggers;
    }

    public void setLogPluginTriggers( boolean logPluginTriggers )
    {
        this.logPluginTriggers = logPluginTriggers;
    }

    public boolean isServer()
    {
        return isServer;
    }

    public void setServer( boolean server )
    {
        isServer = server;
    }

    String logCluster ;
    int arLogLevel ;
    boolean logEpochs ;
    boolean logPluginTriggers ;
    boolean isServer = false ;
}

/*
 *  This sensor is a falling behind sensor based on the imbalance 
 *  between the performances of task arrival and task allocation.
 *  This sensor deals with ProjectSupply/Supply tasks and allocation to inventory assets.
 */


package org.cougaar.tools.alf.sensor;

import org.cougaar.tools.castellan.pdu.PDU;
import org.cougaar.tools.castellan.pdu.EventPDU;
import org.cougaar.tools.castellan.pdu.TaskPDU;
import org.cougaar.tools.castellan.pdu.AllocationPDU;
import org.cougaar.tools.castellan.pdu.UIDStringPDU;
import org.cougaar.tools.castellan.pdu.SymStringPDU;
import java.util.Iterator;
import java.util.Collection;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Arrays;
import java.util.Enumeration;
//import java.awt.Checkbox;
//import javax.swing.JFrame;
//import java.awt.Container;
//import java.awt.GridLayout;

public class FallingBehindSensor
{
    
    /**
     * Initialize the variables including agents to be monitored.
     */

    public FallingBehindSensor() {
        
        int i, j;
        clusters=128;
        time_series=new Hashtable[clusters][3];
        for (i=0; i<=clusters-1; i++)
            for(j=0; j<=2; j++)
                time_series[i][j]=new Hashtable();
        cluster=new String[clusters];
        
        // REAR-A NODE
        cluster[0]="NATO";
        cluster[1]="JSRCMDSE";
        cluster[2]="USEUCOM";
        cluster[3]="USAEUR";
        cluster[4]="5-CORPS";
        cluster[5]="HNS";
        cluster[6]="5-CORPS-REAR";
        cluster[7]="11-AVN-RGT";
        cluster[8]="12-AVNBDE";
        cluster[9]="130-ENGBDE";
        cluster[10]="244-ENGBN-CBTHVY";
        cluster[11]="52-ENGBN-CBTHVY";
        cluster[12]="18-MPBDE";
        cluster[13]="205-MIBDE";
        cluster[14]="22-SIGBDE";
        cluster[15]="30-MEDBDE";
        cluster[16]="69-ADABDE";
        cluster[17]="286-ADA-SCCO";
        cluster[18]="5-CORPS-ARTY";
        cluster[19]="41-FABDE";
        cluster[20]="1-27-FABN";
        cluster[21]="2-4-FABN-MLRS";
        cluster[22]="3-13-FABN-155";
        // REAR-B NODE
        cluster[23]="3-SUPCOM-HQ";
        cluster[24]="19-MMC";
        cluster[25]="208-SCCO";
        cluster[26]="27-TCBN-MVTCTRL";
        cluster[27]="7-CSG";
        cluster[28]="316-POL-SUPPLYBN";
        cluster[29]="515-POL-TRKCO";
        cluster[30]="900-POL-SUPPLYCO";
        cluster[31]="71-MAINTBN";
        cluster[32]="240-SSCO";
        cluster[33]="317-MAINTCO";
        cluster[34]="565-RPRPTCO";
        cluster[35]="597-MAINTCO";
        cluster[36]="71-ORDCO";
        // REAR-C NODE
        cluster[37]="125-ORDBN";
        cluster[38]="452-ORDCO";
        cluster[39]="529-ORDCO";
        cluster[40]="181-TCBN";
        cluster[41]="377-HVY-TRKCO";
        cluster[42]="41-POL-TRKCO";
        cluster[43]="51-MDM-TRKCO";
        cluster[44]="561-SSBN";
        cluster[45]="541-POL-TRKCO";
        cluster[46]="584-MAINTCO";
        // REAR-D NODE
        cluster[47]="21-TSC-HQ";
        cluster[48]="200-MMC";
        cluster[49]="7-TCGP-TPTDD";
        cluster[50]="AWR-2";
        cluster[51]="RSA";
        cluster[52]="37-TRANSGP";
        cluster[53]="28-TCBN";
        cluster[54]="109-MDM-TRKCO";
        cluster[55]="66-MDM-TRKCO";
        cluster[56]="68-MDM-TRKCO";
        cluster[57]="6-TCBN";
        cluster[58]="110-POL-SUPPLYCO";
        cluster[59]="416-POL-TRKCO";
        cluster[60]="632-MAINTCO";
        // REAR E NODE
        cluster[61]="29-SPTGP";
        cluster[62]="191-ORDBN";
        cluster[63]="23-ORDCO";
        cluster[64]="24-ORDCO";
        cluster[65]="702-EODDET";
        cluster[66]="720-EODDET";
        cluster[67]="51-MAINTBN";
        cluster[68]="18-PERISH-SUBPLT";
        cluster[69]="343-SUPPLYCO";
        cluster[70]="5-MAINTCO";
        cluster[71]="512-MAINTCO";
        cluster[72]="574-SSCO";
        // FWD-A NODE
        cluster[73]="1-AD";
        cluster[74]="1-AD-DIV";
        cluster[75]="1-4-ADABN";
        cluster[76]="141-SIGBN";
        cluster[77]="501-MIBN-CEWI";
        cluster[78]="501-MPCO";
        cluster[79]="69-CHEMCO";
        cluster[80]="DIVARTY-1-AD";
        cluster[81]="1-94-FABN";
        cluster[82]="25-FABTRY-TGTACQ";
        cluster[83]="DISCOM-1-AD";
        cluster[84]="123-MSB";
        // FWD-B NODE
        cluster[85]="1-BDE-1-AD";
        cluster[86]="1-36-INFBN";
        cluster[87]="1-37-ARBN";
        cluster[88]="16-ENGBN";
        cluster[89]="2-3-FABN";
        cluster[90]="2-37-ARBN";
        cluster[91]="501-FSB";
        // FWD-C NODE
        cluster[92]="2-BDE-1-AD";
        cluster[93]="1-35-ARBN";
        cluster[94]="1-6-INFBN";
        cluster[95]="2-6-INFBN";
        cluster[96]="4-27-FABN";
        cluster[97]="40-ENGBN";
        cluster[98]="47-FSB";
        //  FWD-D NODE
        cluster[99]="3-BDE-1-AD";
        cluster[100]="1-13-ARBN";
        cluster[101]="1-41-INFBN";
        cluster[102]="125-FSB";
        cluster[103]="2-70-ARBN";
        cluster[104]="4-1-FABN";
        cluster[105]="70-ENGBN";
        // FWD-E NODE
        cluster[106]="AVNBDE-1-AD";
        cluster[107]="1-1-CAVSQDN";
        cluster[108]="1-501-AVNBN";
        cluster[109]="127-DASB";
        cluster[110]="2-501-AVNBN";
        // FWD-F NODE
        cluster[111]="16-CSG";
        cluster[112]="485-CSB";
        cluster[113]="102-POL-SUPPLYCO";
        cluster[114]="26-SSCO";
        cluster[115]="588-MAINTCO";
        cluster[116]="592-ORDCO";
        cluster[117]="596-MAINTCO";
        cluster[118]="18-MAINTBN";
        cluster[119]="226-MAINTCO";
        cluster[120]="227-SUPPLYCO";
        cluster[121]="263-FLDSVC-CO";
        cluster[122]="77-MAINTCO";
        cluster[123]="106-TCBN";
        cluster[124]="15-PLS-TRKCO";
        cluster[125]="238-POL-TRKCO";
        cluster[126]="372-CGO-TRANSCO";
        cluster[127]="594-MDM-TRKCO";
   
  //      check= new Checkbox[clusters];
  //      fbframe=new JFrame("Falling Behind Sensor");
  //      fbcontainer=fbframe.getContentPane();
  //      fbcontainer.setLayout(new GridLayout(32,4)); 
        change=new int[clusters];
        state=new int[clusters];
 //       for (i=0; i<=clusters-1; i++) {
  //          check[i]=new Checkbox(cluster[i], false);
  //          fbcontainer.add(check[i]);
  //      }
 //       fbframe.setVisible(true);
    } 
    
    
    /**
     * Extract time series from PDU.
     */
          
    public void add (PDU pdu) {

        TaskPDU tpdu;
        AllocationPDU apdu;
        int i, clusterno=0;
        String source;
        if (pdu instanceof TaskPDU || pdu instanceof AllocationPDU) {

            source=pdu.getSource();
            for (i=0; i<=clusters-1; i++) {
                 if (source.equalsIgnoreCase(cluster[i])) {clusterno=i; break;}
            }
            if (i==clusters) return;

            if (pdu instanceof TaskPDU) {
                tpdu=(TaskPDU)pdu;
                // Gather task arrival time series for ProjectSupply and Supply Tasks.
                if (tpdu.getAction()==0 && (((UIDStringPDU)(tpdu.getUID())).getOwner().equalsIgnoreCase(source)==false) && (((SymStringPDU)(tpdu.getTaskVerb())).toString().equalsIgnoreCase("ProjectSupply") || ((SymStringPDU)(tpdu.getTaskVerb())).toString().equalsIgnoreCase("Supply"))) {
                    time_series[clusterno][0].put(((UIDStringPDU)(tpdu.getUID())).toString(), new Long(tpdu.getTime()));
                    change[clusterno]=1;
                    return;
                }
                // Gather task time series for tasks that is not interesting but allocated.                
                if (tpdu.getAction()==0 && (((SymStringPDU)(tpdu.getTaskVerb())).toString().equalsIgnoreCase("ProjectWithdraw") || ((SymStringPDU)(tpdu.getTaskVerb())).toString().equalsIgnoreCase("Withdraw")))
					if (((UIDStringPDU)(tpdu.getParentTask())).getOwner().equalsIgnoreCase(source)) {
                          time_series[clusterno][1].put(((UIDStringPDU)(tpdu.getUID())).toString(), new Long(tpdu.getTime()));
                          change[clusterno]=1;
                          return;
                    }
					if (tpdu.getAction()==0 && (((SymStringPDU)(tpdu.getTaskVerb())).toString().equalsIgnoreCase("ProjectSupply")==false) && (((SymStringPDU)(tpdu.getTaskVerb())).toString().equalsIgnoreCase("Supply")==false)&& (((SymStringPDU)(tpdu.getTaskVerb())).toString().equalsIgnoreCase("ProjectWithdraw")==false) && (((SymStringPDU)(tpdu.getTaskVerb())).toString().equalsIgnoreCase("Withdraw")==false)) {
                          time_series[clusterno][1].put(((UIDStringPDU)(tpdu.getUID())).toString(), new Long(tpdu.getTime()));
                          change[clusterno]=1; 
                          return;
                    }
            }
            // Gather allocation(not to organizational asset) time series.

            if (pdu instanceof AllocationPDU) {
                apdu=(AllocationPDU)pdu;
                if (apdu.getAction()==0 && ((UIDStringPDU)(apdu.getUID())).getOwner().equalsIgnoreCase(source) && ((UIDStringPDU)(apdu.getTask())).getOwner().equalsIgnoreCase(source) && ((UIDStringPDU)(apdu.getAsset())).getOwner().equalsIgnoreCase(source)) { 
                    time_series[clusterno][2].put(((UIDStringPDU)(apdu.getTask())).toString(), new Long(apdu.getTime()));                            
                    change[clusterno]=1;
                    return;
                }
            }
         }
    }
    
    
    /**
     * Update falling behind status.
     */
    
    public void update() {
        int i, n1, n2;
        double x=0, y=0;
        Object[] a;  
        String id, source;
        for (i=0; i<=clusters-1; i++) {
             if (change[i]==1) {
                 // Remove allocations that is not interesting.                 
                 if (time_series[i][1].size()>0) {
                     for (Enumeration e = time_series[i][1].keys(); e.hasMoreElements() ;) {
                          if(time_series[i][2].containsKey(id=(String)e.nextElement())) {
                             time_series[i][2].remove(id);
                             time_series[i][1].remove(id);
                          }
                     }
                 }
                 n1=time_series[i][2].size();
                 n2=time_series[i][0].size();
                 // Calculate falling behind and update status.                  
                 if (n1>2 && n2>2) {
                     if (n1>n2) {
				//check[i].setState(false);
				 state[i]=0; continue;}
                     a=time_series[i][2].values().toArray();
                     Arrays.sort(a);
                     y=(((Long)a[n1-1]).longValue()-((Long)a[0]).longValue())/1000.0;
                     a=time_series[i][0].values().toArray();                      
                     Arrays.sort(a);
                     x=(((Long)a[n1-1]).longValue()-((Long)a[0]).longValue())/1000.0;
                     if (y>x*0.8815+65) {
			//	check[i].setState(true);
				state[i]=1;}
                     else {
			//	check[i].setState(false);
				state[i]=0;}
                     System.out.print(cluster[i]+" "+n2+"(G) "+n1+"(A) "+x+"sec. "+y+"sec. \n");
			   if (state[i]==1) System.out.print("Falling Behind\n");
			   else System.out.print("\n");

                  }
                  change[i]=0;
              }
         }
    }

    Hashtable[][] time_series;
    int clusters;
    String[] cluster;
    int[] change;
    int[] state;
 //   JFrame fbframe;
 //   Container fbcontainer ;
 //   Checkbox[] check;
}

package org.cougaar.tools.castellan.plugin;

import org.cougaar.core.servlet.*;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.planning.ldm.plan.Task;

import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.GregorianCalendar;
import java.text.DateFormat;

public class PlanStatsViewerServlet extends HttpServlet  {

    BlackboardServletSupport bss ;

    /**
     * Method that recieves a BlackboardServletComponent
     **/
    public void setSimpleServletSupport( SimpleServletSupport support ){
       if( support instanceof BlackboardServletSupport ){
          bss = (BlackboardServletSupport) support;
       }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
       throws IOException {

        // Get the PlanLogStats
        bss.getBlackboardService().openTransaction();
        Collection c = bss.getBlackboardService().query( new UnaryPredicate() {
            public boolean execute ( Object o ) {
               if ( o instanceof PlanLogStats ) {
                return true ;
               }
                return false ;
            }
        }) ;

        // Count up the tasks.
        Collection c2 = bss.queryBlackboard( new UnaryPredicate() {
            public boolean execute(Object o) {
                if ( o instanceof Task ) {
                    return true ;
                }
                return false ;
            }
        } ) ;
        int taskCount = 0 ;
        for ( Iterator i2 = c2.iterator();i2.hasNext();) {
            i2.next() ;
            taskCount++ ;
        }

        bss.getBlackboardService().closeTransaction();

        PlanLogStats stats = null ;
        for ( Iterator iter = c.iterator() ;iter.hasNext(); ) {
            stats = ( PlanLogStats ) iter.next() ;
            break ;
        }

        if ( stats == null ) {
            return ;
        }

        Date d = new Date( stats.getFirstEventTime() ) ;
        DateFormat df = DateFormat.getDateInstance() ;
        String startTime = df.format( d ) ;
        String endTime = df.format( new Date( stats.getLastEventTime() ) ) ;

        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        out.println( "<head>" ) ;
        out.println( "<title>Plan Log Stats</title>" ) ;
        out.println( "</head>" ) ;
        out.println( "<body>" ) ;

        out.println( "<h1 align=\"center\">Plan Log Stats for " + bss.getAgentIdentifier().cleanToString() + " </h1>" ) ;
        out.println( "<br>" ) ;

        out.println( "<table cellpadding=\"2\" cellspacing=\"1\" border=\"1\" width=\"80%\"" ) ;
        out.println( " align=\"center\"> " ) ;
        out.println( "  <tbody>" ) ;
        out.println( "     <tr>" ) ;
        out.println( "       <td>First Event Time</td>" ) ;
        out.println( "       <td>" + startTime + "<br>" ) ;
        out.println( "      </td>" ) ;
        out.println( "    </tr>" ) ;
        out.println( "    <tr>" ) ;
        out.println( "       <td >Last Event Time <br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "       <td>" + endTime + "<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "     </tr>" ) ;
        out.println( "     <tr>" ) ;
        out.println( "       <td ># Messages Sent<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "       <td >" + stats.getNumMsgsSent() + "<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "     </tr>" ) ;
        out.println( "     <tr>" ) ;
        out.println( "       <td ># PDUs Sent<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "       <td >" + stats.getNumPdusSent() + "<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "     </tr>" ) ;
        out.println( "     <tr>" ) ;
        out.println( "       <td ># Bytes Sent<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "       <td >" + stats.getNumBytesSent() + "<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "     </tr>" ) ;
        out.println( "     <tr>" ) ;
        out.println( "       <td ># Unique Task UIDs<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "       <td >" + stats.getNumUniqueTaskUIDs() + "<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "     </tr>" ) ;
        out.println( "     <tr>" ) ;
        out.println( "       <td ># Task Adds<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "       <td >" + stats.getNumTaskAdds() + "<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "     </tr>" ) ;
        out.println( "     <tr>" ) ;
        out.println( "       <td ># Task Changes<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "       <td >" + stats.getNumTaskChanges() + "<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "     </tr>" ) ;
        out.println( "     <tr>" ) ;
        out.println( "       <td ># Task Removes<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "       <td >" + stats.getNumTaskRemoves() + "<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "     </tr>" ) ;
        out.println( "     <tr>" ) ;
        out.println( "       <td ># Tasks seen at execute method<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "       <td >" + stats.getNumTasksSeenDebug() + "<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "     </tr>" ) ;
        out.println( "     <tr>" ) ;
        out.println( "       <td ># Tasks seen through query<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "       <td >" + taskCount + "<br>" ) ;
        out.println( "       </td>" ) ;
        out.println( "     </tr>" ) ;
        out.println( "" ) ;
        out.println( "  </tbody>" ) ;
        out.println( "</table>" ) ;
        out.println( " <br>" ) ;

        out.println( "</body>" ) ;
        out.println( "</html>" ) ;

    }
}

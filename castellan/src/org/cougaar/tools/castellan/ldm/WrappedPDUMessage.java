package org.cougaar.tools.castellan.ldm;

import org.cougaar.tools.castellan.pdu.*;

public class WrappedPDUMessage extends LogMessage
{
    public WrappedPDUMessage(PDU pdu)
    {
        this.pdu = pdu;
    }

    public PDU getPDU()
    {
        return pdu;
    }

    public void setPDU(PDU pdu)
    {
        this.pdu = pdu;
    }

    protected PDU pdu ;
}

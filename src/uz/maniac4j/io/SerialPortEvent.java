
package uz.maniac4j.io;

import java.util.EventObject;


public class SerialPortEvent extends EventObject {

    /**
     * The DATA_AVAILABLE port event notifies that new data was received on the
     * port. When this event type is received, the user will typically schedule
     * a read on the input stream.
     */
    public static final int DATA_AVAILABLE = 1;
    /**
     * The OUTPUT_BUFFER_EMPTY port event notifies that all data in the ports
     * output buffer was processed. The user might use this event to continue
     * writing data to the ports output stream.
     */
    public static final int OUTPUT_BUFFER_EMPTY = 2;
    /**
     * The CTS port event is triggered when the Clear To Send line on the port
     * changes its logic level.
     */
    public static final int CTS = 3;
    /**
     * The DSR port event is triggered when the Data Set Ready line on the port
     * changes its logic level.
     */
    public static final int DSR = 4;
    /**
     * The RI port event is triggered when the Ring Indicator line on the port
     * changes its logic level.
     */
    public static final int RI = 5;
    /**
     * The CD port event is triggered when the Data Carrier Detect line on the
     * port changes its logic level.
     */
    public static final int CD = 6;
    /**
     * The OE port event signals an overrun error. This event is triggered, when
     * the port hardware receives data and the application does not read it or
     * does not read it fast enough. As a result a buffer located in hardware
     * and/or the driver overflows and data is lost.
     */
    public static final int OE = 7;
    /**
     * The PE port event signals a parity error. This event is triggered when
     * the port is configured to use parity bits and a datum with wrong parity
     * value is received. This means it is very likely that a datum with wrong
     * value has been received.
     */
    public static final int PE = 8;
    /**
     * The FE port event signals a framing error.
     */
    public static final int FE = 9;
    /**
     * The BI port event signals a break interrupt.
     */
    public static final int BI = 10;

    private final boolean oldValue;
    private final boolean newValue;
    private final int eventType;
    /*public int eventType           =0; depricated */

    /**
     * Creates a new <code>SerialPortEvent</code> of specified event type.
     *
     * @param srcport the port which is associated with the event
     * @param eventType the type of the event
     * @param oldValue the value of the signal before the event
     * @param newValue the value of the signal after the event
     */
    public SerialPortEvent(SerialPort srcport, int eventType, boolean oldValue, boolean newValue) {
        super(srcport);
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.eventType = eventType;
    }

    /**
     * Returns the type of the event which occurred.
     *
     * @return the event type
     */
    public int getEventType() {
        return eventType;
    }

    /**
     * Returns the value of the event context object after the event occurred.
     * For CTS, DSR this is the new logic level of the CTS or DSR line
     * respectively.
     *
     * @return the new value
     */
    //TODO (by Alexander Graf): check wich voltage level corresponds to which boolean value
    public boolean getNewValue() {
        return newValue;
    }

    /**
     * Returns the value of the event context object before the event occurred.
     * For CTS, DSR this is the old logic level of the CTS or DSR line
     * respectively.
     *
     * @return the old value
     */
    public boolean getOldValue() {
        return oldValue;
    }
}

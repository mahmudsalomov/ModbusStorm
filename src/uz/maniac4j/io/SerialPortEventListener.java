
package uz.maniac4j.io;

import java.util.EventListener;

/**
 * Observer interface which is informed about events happening on a
 * <code>SerialPort</code>.
 *
 * First, instances of <code>SerialPortEventListener</code> are registered on a
 * port using the <code>SerialPort.addEventListener()</code> method. Then the
 * desired events must be enabled on the port by calling
 * <code>SerialPort.notifyOn*(true)</code>.
 *
 * If one of the enabled events occurred on a port, the <code>serialEvent</code>
 * method of the listener is called with an event object describing the event.
 *
 * @author Trent Jarvi
 * @author Bartłomiej P. Prokop
 * @version 2.3
 */
public interface SerialPortEventListener extends EventListener {

    /**
     * Receives enabled event notifications from the port the event listener is
     * registered to.
     *
     * @param event an event object describing the event
     */
    public abstract void serialEvent(SerialPortEvent event);
}

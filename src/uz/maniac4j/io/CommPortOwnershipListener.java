
package uz.maniac4j.io;

import java.util.EventListener;


public interface CommPortOwnershipListener extends EventListener {

    /**
     * The port is owned by an application.
     */
    public static final int PORT_OWNED = 1;
    /**
     * The pot is not owned by an application and is ready to be opened.
     */
    public static final int PORT_UNOWNED = 2;
    /**
     * Another application requests to open the port. If this application
     * receives this event while holding the port and wants to give ownership to
     * the requesting application, then <code>CommPort.close()</code> should be
     * called as fast as possible.
     */
    public static final int PORT_OWNERSHIP_REQUESTED = 3;

    /**
     * Receives change notifications about the port ownership.
     *
     * @param type one of the <code>PORT_*</code> constants indicating the
     * notification type.
     */

    public abstract void ownershipChange(int type);
}

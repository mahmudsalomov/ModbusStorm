
package uz.maniac4j.io;

public final class PortInUseException extends Exception {

    /**
     * the owner of the port requested.
     */
    public String currentOwner;

    /**
     * create a instance of the Exception and store the current owner
     *
     * @param str detailed information about the current owner
     */
    PortInUseException(String str) {
        super(str);
        currentOwner = str;
    }

    public PortInUseException() {
        super();
    }
}

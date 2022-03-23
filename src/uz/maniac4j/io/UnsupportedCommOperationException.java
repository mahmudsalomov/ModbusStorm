
package uz.maniac4j.io;

public final class UnsupportedCommOperationException extends Exception {


    public UnsupportedCommOperationException() {
        super();
    }

    /**
     * create an instance with a message about why the Exception was thrown.
     *
     * @param str	A detailed message explaining the reason for the Exception.
     * @since JDK1.0
     */
    public UnsupportedCommOperationException(String str) {
        super(str);
    }
}

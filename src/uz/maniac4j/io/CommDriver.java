
package uz.maniac4j.io;


public interface CommDriver {

    default CommPort getCommPort(String portName) {
        return getCommPort(portName, CommPortIdentifier.PORT_SERIAL);
    }

    @Deprecated
    CommPort getCommPort(String portName, int portType);

    void initialize();
}

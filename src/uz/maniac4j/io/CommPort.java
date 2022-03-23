
package uz.maniac4j.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class CommPort {

    private static final Logger LOGGER = Logger.getLogger(CommPort.class.getName());

    private final String name;

    public abstract void enableReceiveFraming(int f) throws UnsupportedCommOperationException;

    public abstract void disableReceiveFraming();

    public abstract boolean isReceiveFramingEnabled();

    public abstract int getReceiveFramingByte();

    public abstract void disableReceiveTimeout();

    public abstract void enableReceiveTimeout(int time) throws UnsupportedCommOperationException;

    public abstract boolean isReceiveTimeoutEnabled();

    public abstract int getReceiveTimeout();

    public abstract void enableReceiveThreshold(int thresh) throws UnsupportedCommOperationException;

    public abstract void disableReceiveThreshold();

    public abstract int getReceiveThreshold();

    public abstract boolean isReceiveThresholdEnabled();

    public abstract void setInputBufferSize(int size);

    public abstract int getInputBufferSize();

    public abstract void setOutputBufferSize(int size);

    public abstract int getOutputBufferSize();

    public CommPort(String name) {
        this.name = name;
    }

    public void close() {
        LOGGER.log(Level.FINE, "Closing port {0}", name);
        try {
            CommPortIdentifier cp = CommPortIdentifier.getPortIdentifier(this);
            CommPortIdentifier.getPortIdentifier(this).internalClosePort();
        } catch (NoSuchPortException ignored) {
        }
    }

    public abstract InputStream getInputStream() throws IOException;

    public abstract OutputStream getOutputStream() throws IOException;

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}

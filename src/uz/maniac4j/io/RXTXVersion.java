
package uz.maniac4j.io;

import java.util.logging.Logger;


public class RXTXVersion {

    private static final Logger LOGGER = Logger.getLogger(RXTXVersion.class.getName());
    private static final String RXTX_VERSION = "2.2.2";
    private static final String EXPECTED_NATIVE_VERSION_RPI = "RXTX-2.2pre2";
    private static final String EXPECTED_NATIVE_VERSION_WIN = "RXTX-2.2-20081207 Cloudhopper Build rxtx.cloudhopper.net";

    static void ensureNativeCodeLoaded() {
        try {
            Class.forName("io.RXTXInitializer");
        } catch (ClassNotFoundException cnfe) {
            throw new IllegalStateException("RXTX not initialized", cnfe);
        }
        if (!getExpectedNativeVersion().equals(nativeGetVersion())) {
            LOGGER.warning("Native libraries mismatch");
        }
    }

    public static String getOsName() {
        return System.getProperty("os.name");
    }

    public static String getOsArch() {
        return System.getProperty("os.arch");
    }

    /**
     * static method to return the current version of RXTX unique to RXTX.
     *
     * @return a string representing the version "RXTX-1.4-9"
     */
    public static String getVersion() {
        return RXTX_VERSION;
    }

    /**
     * static method to return the expected version of native RXTX dll/so.
     *
     * @return a string representing the version "RXTX-1.4-9"
     */
    public static String getExpectedNativeVersion() {
        if ("arm".equals(getOsArch())) {
            return EXPECTED_NATIVE_VERSION_RPI;
        }
        return EXPECTED_NATIVE_VERSION_WIN;
    }

    public static native String nativeGetVersion();
}


package uz.maniac4j.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * This is the JavaComm for Linux driver.
 */
public class RXTXCommDriver implements CommDriver {

    private final static Logger logger = Logger.getLogger(RXTXCommDriver.class.getName());

    static {
        RXTXVersion.ensureNativeCodeLoaded();
    }

    /**
     * Get the Serial port prefixes for the running OS
     */
    private String deviceDirectory;

    private native boolean registerKnownPorts(int PortType);

    private native boolean isPortPrefixValid(String dev);

    private native boolean testRead(String dev, int type);

    private native String getDeviceDirectory();

    // FIXME: This method is never called.
    private String[] getValidPortPrefixes(String[] CandidatePortPrefixes) {
        /*
         256 is the number of prefixes ( COM, cua, ttyS, ...) not
         the number of devices (ttyS0, ttyS1, ttyS2, ...)

         On a Linux system there are about 400 prefixes in
         deviceDirectory.
         registerScannedPorts() assigns CandidatePortPrefixes to
         something less than 50 prefixes.

         Trent
         */

        String[] ValidPortPrefixes = new String[256];
        logger.fine("\nRXTXCommDriver:getValidPortPrefixes()");
        if (CandidatePortPrefixes == null) {
            logger.fine("\nRXTXCommDriver:getValidPortPrefixes() No ports prefixes known for this System.\nPlease check the port prefixes listed for " + RXTXVersion.getOsName() + " in RXTXCommDriver:registerScannedPorts()\n");
        }
        int i = 0;
        for (int j = 0; j < (CandidatePortPrefixes != null ? CandidatePortPrefixes.length : 0); j++) {
            if (isPortPrefixValid(CandidatePortPrefixes[j])) {
                ValidPortPrefixes[i++]
                        = CandidatePortPrefixes[j];
            }
        }
        String[] returnArray = new String[i];
        System.arraycopy(ValidPortPrefixes, 0, returnArray, 0, i);
        if (ValidPortPrefixes[0] == null) {

            logger.fine("\nRXTXCommDriver:getValidPortPrefixes() No ports matched the list assumed for this\nSystem in the directory " + deviceDirectory + ".  Please check the ports listed for \"" + RXTXVersion.getOsName() + "\" in\nRXTXCommDriver:registerScannedPorts()\nTried:");
            for (int j = 0; j < (CandidatePortPrefixes != null ? CandidatePortPrefixes.length : 0); j++) {
                logger.fine("\t"
                        + CandidatePortPrefixes[i]);
            }

        } else {
            logger.fine("\nRXTXCommDriver:getValidPortPrefixes()\nThe following port prefixes have been identified as valid on " + RXTXVersion.getOsName() + ":\n");
            /*
             for(int j=0;j<returnArray.length;j++)
             {
             if (debug)
             System.out.println("\t" + j + " " +
             returnArray[j]);
             }
             */
        }
        return returnArray;
    }

    /**
     * handle solaris/sunos /dev/cua/a convention
     */
    private void checkSolaris(String PortName, int PortType) {
        char p[] = {91};
        for (p[0] = 97; p[0] < 123; p[0]++) {
            if (testRead(PortName.concat(new String(p)), PortType)) {
                CommPortIdentifier.addPortName(PortName.concat(new String(p)), PortType, this);
            }
        }
        /**
         * check for 0-9 in case we have them (Solaris USB)
         */
        for (p[0] = 48; p[0] <= 57; p[0]++) {
            if (testRead(PortName.concat(new String(p)), PortType)) {
                CommPortIdentifier.addPortName(PortName.concat(new String(p)), PortType, this);
            }
        }
    }

    private void registerValidPorts(
            String[] CandidateDeviceNames,
            String[] ValidPortPrefixes,
            int PortType
    ) {
        int i = 0;
        int p = 0;
        /* FIXME quick fix to get COM1-8 on windows working.  The
         Read test is not working properly and its crunch time...
         if(osName.toLowerCase().indexOf("windows") != -1 )
         {
         for( i=0;i < CandidateDeviceNames.length;i++ )
         {
         CommPortIdentifier.addPortName( CandidateDeviceNames[i],
         PortType, this );
         }
         return;

         }
         */

        logger.fine("Entering registerValidPorts()");
        /* */
        logger.fine(" Candidate devices:");
        for (String candidateDeviceName : CandidateDeviceNames) {
            logger.fine("  "
                    + candidateDeviceName);
        }
        logger.fine(" valid port prefixes:");
        for (String validPortPrefix : ValidPortPrefixes) {
            logger.fine("  " + validPortPrefix);
        }
        /* */

        for (i = 0; i < CandidateDeviceNames.length; i++) {
            for (p = 0; p < ValidPortPrefixes.length; p++) {
                /* this determines:
                 * device file         Valid ports
                 * /dev/ttyR[0-9]*  != /dev/ttyS[0-9]*
                 * /dev/ttySI[0-9]* != /dev/ttyS[0-9]*
                 * /dev/ttyS[0-9]*  == /dev/ttyS[0-9]*

                 * Otherwise we check some ports
                 * multiple times.  Perl would rock
                 * here.
                 *
                 * If the above passes, we try to read
                 * from the port.  If there is no err
                 * the port is added.
                 * Trent
                 */
                String V = ValidPortPrefixes[ p];
                int VL = V.length();
                String C = CandidateDeviceNames[ i];
                if (C.length() < VL) {
                    continue;
                }
                String CU
                        = C.substring(VL).toUpperCase();
                String Cl
                        = C.substring(VL).toLowerCase();
                if (!(C.regionMatches(0, V, 0, VL)
                        && CU.equals(Cl))) {
                    continue;
                }
                String PortName;
                if (!RXTXVersion.getOsName().toLowerCase().contains("windows")) {
                    PortName = deviceDirectory + C;
                } else {
                    PortName = C;
                }

                logger.fine(C + " " + V);
                logger.fine(CU + " " + Cl);

                if (RXTXVersion.getOsName().equals("Solaris") || RXTXVersion.getOsName().equals("SunOS")) {
                    checkSolaris(PortName, PortType);
                } else if (testRead(PortName, PortType)) {
                    CommPortIdentifier.addPortName(PortName, PortType, this);
                }
            }
        }

        logger.fine("Leaving registerValidPorts()");

    }



    /**
     * Determine the OS and where the OS has the devices located
     */
    @Override
    public void initialize() {
        deviceDirectory = getDeviceDirectory();

        /*
         First try to register ports specified in the properties
         file.  If that doesn't exist, then scan for ports.
         */
        for (int PortType = CommPortIdentifier.PORT_SERIAL; PortType <= CommPortIdentifier.PORT_PARALLEL; PortType++) {
            if (!registerSpecifiedPorts(PortType)) {
                if (!registerKnownPorts(PortType)) {
                    registerScannedPorts(PortType);
                }
            }
        }
    }

    private void addSpecifiedPorts(String names, int PortType) {
        final String pathSep = System.getProperty("path.separator", ":");
        final StringTokenizer tok = new StringTokenizer(names, pathSep);

        logger.fine("\nRXTXCommDriver:addSpecifiedPorts()");

        while (tok.hasMoreElements()) {
            String PortName = tok.nextToken();

            if (testRead(PortName, PortType)) {
                CommPortIdentifier.addPortName(PortName, PortType, this);
            }
        }
    }

    /*
     * Register ports specified in the file "io.rxtx.properties"
     * Key system properties:
     *                   io.rxtx.SerialPorts
     * 			io.rxtx.ParallelPorts
     *
     * Tested only with sun jdk1.3
     * The file io.rxtx.properties may reside in the java extension dir,
     * or it can be anywhere in the classpath.
     *
     * Example: /usr/local/java/jre/lib/ext/io.rxtx.properties
     *
     * The file contains the following key properties:
     *
     *  io.rxtx.SerialPorts=/dev/ttyS0:/dev/ttyS1:
     *  io.rxtx.ParallelPorts=/dev/lp0:
     *
     */
    private boolean registerSpecifiedPorts(int PortType) {
        String val = null;
        Properties props = null;
        String file_loc = null;
        // Old style: properties file must be in JRE folder
        String ext_dirs = System.getProperty("java.ext.dirs");
        String[] dirArray = ext_dirs.split(System.getProperty("path.separator"));
        for (String s : dirArray) {
            String file_name = s + System.getProperty("file.separator") + "io.rxtx.properties";
            File file = new File(file_name);
            if (file.exists()) {
                file_loc = file_name;
                break;
            }
        }
        if (file_loc != null) {
            try (FileInputStream in = new FileInputStream(file_loc)) {
                props = new Properties();
                props.load(in);
            } catch (Exception e) {

                logger.fine("Error encountered while reading " + file_loc + ": " + e);

            }
        }
        if (props == null) {
            // New style: properties file anywhere in classpath
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                Enumeration resources = loader.getResources("io.rxtx.properties");
                while (resources.hasMoreElements()) {
                    URL propertyURL = (URL) resources.nextElement();
                    props = new Properties();
                    props.load(propertyURL.openStream());
                    break;
                }
            } catch (IOException e) {

                logger.fine("Error encountered while getting io.rxtx.properties from the classpath: " + e);

            }
        }

        logger.fine("checking for system-known ports of type " + PortType);

        logger.fine("checking registry for ports of type " + PortType);
        if (props != null) {
            switch (PortType) {
                case CommPortIdentifier.PORT_SERIAL:
                    if ((val = props.getProperty("io.rxtx.SerialPorts")) == null) {
                        val = props.getProperty("io.SerialPorts");
                    }
                    break;

                case CommPortIdentifier.PORT_PARALLEL:
                    if ((val = props.getProperty("io.rxtx.ParallelPorts")) == null) {
                        val = props.getProperty("io.ParallelPorts");
                    }
                    break;
                default:

                    logger.fine("unknown port type " + PortType + " passed to RXTXCommDriver.registerSpecifiedPorts()");

            }
        } else {

            logger.fine("The file: io.rxtx.properties doesn't exist.");

        }
        if (val != null) {
            addSpecifiedPorts(val, PortType);
            return true;
        } else {
            return false;
        }
    }

    /*
     * Look for all entries in deviceDirectory, and if they look like they should
     * be serial ports on this OS and they can be opened then register
     * them.
     *
     */
    private void registerScannedPorts(int PortType) {
        String[] CandidateDeviceNames;

        logger.fine("scanning device directory " + deviceDirectory + " for ports of type " + PortType);

        if (RXTXVersion.getOsName().equals("Windows CE")) {
            CandidateDeviceNames = new String[]{"COM1:", "COM2:", "COM3:", "COM4:",
                "COM5:", "COM6:", "COM7:", "COM8:"};
        } else if (RXTXVersion.getOsName().toLowerCase().contains("windows")) {
            String[] temp = new String[259];
            for (int i = 1; i <= 256; i++) {
                temp[i - 1] = "COM" + i;
            }
            for (int i = 1; i <= 3; i++) {
                temp[i + 255] = "LPT" + i;
            }
            CandidateDeviceNames = temp;
        } else if (RXTXVersion.getOsName().equals("Solaris") || RXTXVersion.getOsName().equals("SunOS")) {
            /* Solaris uses a few different ways to identify ports.
             They could be /dev/term/a /dev/term0 /dev/cua/a /dev/cuaa
             the /dev/???/a appears to be on more systems.

             The uucp lock files should not cause problems.
             */
            /*
             File dev = new File( "/dev/term" );
             String deva[] = dev.list();
             dev = new File( "/dev/cua" );
             String devb[] = dev.list();
             String[] temp = new String[ deva.length + devb.length ];
             for(int j =0;j<deva.length;j++)
             deva[j] = "term/" + deva[j];
             for(int j =0;j<devb.length;j++)
             devb[j] = "cua/" + devb[j];
             System.arraycopy( deva, 0, temp, 0, deva.length );
             System.arraycopy( devb, 0, temp,
             deva.length, devb.length );
             if( debug ) {
             for( int j = 0; j< temp.length;j++)
             System.out.println( temp[j] );
             }
             CandidateDeviceNames=temp;
             */

            /*

             ok..  Look the the dirctories representing the port
             kernel driver interface.

             If there are entries there are possibly ports we can
             use and need to enumerate.
             */
            String[] term = new String[2];
            int l = 0;
            File dev = null;

            dev = new File("/dev/term");
            if (Objects.requireNonNull(dev.list()).length > 0) {
                term[l++] = "term/";
            }
            /*
             dev = new File( "/dev/cua0" );
             if( dev.list().length > 0 )
             term[l++] = "cua/";
             */
            String[] temp = new String[l];
            for (l--; l >= 0; l--) {
                temp[l] = term[l];
            }
            CandidateDeviceNames = temp;
        } else {
            File dev = new File(deviceDirectory);
            CandidateDeviceNames = dev.list();
        }
        if (CandidateDeviceNames == null) {

            logger.fine("RXTXCommDriver:registerScannedPorts() no Device files to check ");

            return;
        }

        String[] CandidatePortPrefixes = {};
        switch (PortType) {
            case CommPortIdentifier.PORT_SERIAL:

                logger.fine("scanning for serial ports for os " + RXTXVersion.getOsName());

                /*  There are _many_ possible ports that can be used
                 on Linux.  See below in the fake Linux-all-ports
                 case for a list.  You may add additional ports
                 here but be warned that too many will significantly
                 slow down port enumeration.  Linux 2.6 has udev
                 support which should be faster as only ports the
                 kernel finds should be exposed in /dev

                 See also how to override port enumeration and
                 specifying port in INSTALL.

                 taj
                 */
                if (RXTXVersion.getOsName().equals("Linux")) {
                    String[] Temp = {
                        "ttyS", // linux Serial Ports
                        "ttyAMA", // for Raspberry Pi 2
                        "ttySA", // for the IPAQs
                        "ttyUSB", // for USB frobs
                        "rfcomm", // bluetooth serial device
                        "ttyircomm", // linux IrCommdevices (IrDA serial emu)
                    };
                    CandidatePortPrefixes = Temp;
                } else if (RXTXVersion.getOsName().equals("Linux-all-ports")) {
                    /* if you want to enumerate all ports ~5000
                     possible, then replace the above with this
                     */
                    String[] Temp = {
                        "comx", // linux COMMX synchronous serial card
                        "holter", // custom card for heart monitoring
                        "modem", // linux symbolic link to modem.
                        "rfcomm", // bluetooth serial device
                        "ttyircomm", // linux IrCommdevices (IrDA serial emu)
                        "ttycosa0c", // linux COSA/SRP synchronous serial card
                        "ttycosa1c", // linux COSA/SRP synchronous serial card
                        "ttyACM",// linux CDC ACM devices
                        "ttyC", // linux cyclades cards
                        "ttyCH",// linux Chase Research AT/PCI-Fast serial card
                        "ttyD", // linux Digiboard serial card
                        "ttyE", // linux Stallion serial card
                        "ttyF", // linux Computone IntelliPort serial card
                        "ttyH", // linux Chase serial card
                        "ttyI", // linux virtual modems
                        "ttyL", // linux SDL RISCom serial card
                        "ttyM", // linux PAM Software's multimodem boards
                        // linux ISI serial card
                        "ttyMX",// linux Moxa Smart IO cards
                        "ttyP", // linux Hayes ESP serial card
                        "ttyR", // linux comtrol cards
                        // linux Specialix RIO serial card
                        "ttyS", // linux Serial Ports
                        "ttySI",// linux SmartIO serial card
                        "ttySR",// linux Specialix RIO serial card 257+
                        "ttyT", // linux Technology Concepts serial card
                        "ttyUSB",//linux USB serial converters
                        "ttyV", // linux Comtrol VS-1000 serial controller
                        "ttyW", // linux specialix cards
                        "ttyX" // linux SpecialX serial card
                    };
                    CandidatePortPrefixes = Temp;
                } else if (RXTXVersion.getOsName().toLowerCase().contains("qnx")) {
                    CandidatePortPrefixes = new String[]{
                        "ser"
                    };
                } else if (RXTXVersion.getOsName().equals("Irix")) {
                    CandidatePortPrefixes = new String[]{
                        "ttyc", // irix raw character devices
                        "ttyd", // irix basic serial ports
                        "ttyf", // irix serial ports with hardware flow
                        "ttym", // irix modems
                        "ttyq", // irix pseudo ttys
                        "tty4d",// irix RS422
                        "tty4f",// irix RS422 with HSKo/HSki
                        "midi", // irix serial midi
                        "us" // irix mapped interface
                    };
                } else if (RXTXVersion.getOsName().equals("FreeBSD")) //FIXME this is probably wrong
                {
                    CandidatePortPrefixes = new String[]{
                        "ttyd", //general purpose serial ports
                        "cuaa", //dialout serial ports
                        "ttyA", //Specialix SI/XIO dialin ports
                        "cuaA", //Specialix SI/XIO dialout ports
                        "ttyD", //Digiboard - 16 dialin ports
                        "cuaD", //Digiboard - 16 dialout ports
                        "ttyE", //Stallion EasyIO (stl) dialin ports
                        "cuaE", //Stallion EasyIO (stl) dialout ports
                        "ttyF", //Stallion Brumby (stli) dialin ports
                        "cuaF", //Stallion Brumby (stli) dialout ports
                        "ttyR", //Rocketport dialin ports
                        "cuaR", //Rocketport dialout ports
                        "stl" //Stallion EasyIO board or Brumby N
                    };
                } else if (RXTXVersion.getOsName().equals("NetBSD")) // FIXME this is probably wrong
                {
                    CandidatePortPrefixes = new String[]{
                        "tty0" // netbsd serial ports
                    };
                } else if (RXTXVersion.getOsName().equals("Solaris") || RXTXVersion.getOsName().equals("SunOS")) {
                    CandidatePortPrefixes = new String[]{
                        "term/",
                        "cua/"
                    };
                } else if (RXTXVersion.getOsName().equals("HP-UX")) {
                    CandidatePortPrefixes = new String[]{
                        "tty0p",// HP-UX serial ports
                        "tty1p" // HP-UX serial ports
                    };
                } else if (RXTXVersion.getOsName().equals("UnixWare")                        || RXTXVersion.getOsName().equals("OpenUNIX")) {
                    CandidatePortPrefixes = new String[]{
                        "tty00s", // UW7/OU8 serial ports
                        "tty01s",
                        "tty02s",
                        "tty03s"
                    };
                } else if (RXTXVersion.getOsName().equals("OpenServer")) {
                    CandidatePortPrefixes = new String[]{
                        "tty1A", // OSR5 serial ports
                        "tty2A",
                        "tty3A",
                        "tty4A",
                        "tty5A",
                        "tty6A",
                        "tty7A",
                        "tty8A",
                        "tty9A",
                        "tty10A",
                        "tty11A",
                        "tty12A",
                        "tty13A",
                        "tty14A",
                        "tty15A",
                        "tty16A",
                        "ttyu1A", // OSR5 USB-serial ports
                        "ttyu2A",
                        "ttyu3A",
                        "ttyu4A",
                        "ttyu5A",
                        "ttyu6A",
                        "ttyu7A",
                        "ttyu8A",
                        "ttyu9A",
                        "ttyu10A",
                        "ttyu11A",
                        "ttyu12A",
                        "ttyu13A",
                        "ttyu14A",
                        "ttyu15A",
                        "ttyu16A"
                    };
                } else if (RXTXVersion.getOsName().equals("Compaq's Digital UNIX") || RXTXVersion.getOsName().equals("OSF1")) {
                    CandidatePortPrefixes = new String[]{
                        "tty0" //  Digital Unix serial ports
                    };
                } else if (RXTXVersion.getOsName().equals("BeOS")) {
                    CandidatePortPrefixes = new String[]{
                        "serial" // BeOS serial ports
                    };
                } else if (RXTXVersion.getOsName().equals("Mac OS X")) {
                    CandidatePortPrefixes = new String[]{
                        // Keyspan USA-28X adapter, USB port 1
                        "cu.KeyUSA28X191.",
                        // Keyspan USA-28X adapter, USB port 1
                        "tty.KeyUSA28X191.",
                        // Keyspan USA-28X adapter, USB port 2
                        "cu.KeyUSA28X181.",
                        // Keyspan USA-28X adapter, USB port 2
                        "tty.KeyUSA28X181.",
                        // Keyspan USA-19 adapter
                        "cu.KeyUSA19181.",
                        // Keyspan USA-19 adapter
                        "tty.KeyUSA19181."
                    };
                } else if (RXTXVersion.getOsName().toLowerCase().contains("windows")) {
                    CandidatePortPrefixes = new String[]{
                        "COM" // win32 serial ports
                    //"//./COM"    // win32 serial ports
                    };
                } else {
                    logger.fine("No valid prefixes for serial ports have been entered for " + RXTXVersion.getOsName() + " in RXTXCommDriver.java.  This may just be a typo in the method registerScanPorts().");
                }
                break;

            case CommPortIdentifier.PORT_PARALLEL:
                logger.fine("scanning for parallel ports for os " + RXTXVersion.getOsName());
                /**
                 * Get the Parallel port prefixes for the running os
                 */
                if (RXTXVersion.getOsName().equals("Linux") /*
                         || osName.equals("NetBSD") FIXME
                         || osName.equals("HP-UX")  FIXME
                         || osName.equals("Irix")   FIXME
                         || osName.equals("BeOS")   FIXME
                         || osName.equals("Compaq's Digital UNIX")   FIXME
                         */) {
                    CandidatePortPrefixes = new String[]{
                        "lp" // linux printer port
                    };
                } else if (RXTXVersion.getOsName().equals("FreeBSD")) {
                    CandidatePortPrefixes = new String[]{
                        "lpt"
                    };
                } else if (RXTXVersion.getOsName().toLowerCase().contains("windows")) {
                    CandidatePortPrefixes = new String[]{
                        "LPT"
                    };
                } else /* printer support is green */ {
                    CandidatePortPrefixes = new String[]{};
                }
                break;
            default:
                logger.fine("Unknown PortType " + PortType + " passed to RXTXCommDriver.registerScannedPorts()");
        }
        registerValidPorts(CandidateDeviceNames, CandidatePortPrefixes, PortType);
    }


    /*
     * <p>From the NullDriver.java CommAPI sample.
     */
    /**
     * @param PortName The name of the port the OS recognizes
     * @param PortType CommPortIdentifier.PORT_SERIAL or PORT_PARALLEL
     * @return CommPort getCommPort() will be called by CommPortIdentifier from
     * its openPort() method. PortName is a string that was registered earlier
     * using the CommPortIdentifier.addPortName() method. getCommPort() returns
     * an object that extends either SerialPort or ParallelPort.
     */
    @Override
    public CommPort getCommPort(String PortName, int PortType) {
        logger.fine("RXTXCommDriver:getCommPort(" + PortName + "," + PortType + ")");
        try {
            if (PortType == CommPortIdentifier.PORT_SERIAL) {
                if (!RXTXVersion.getOsName().toLowerCase().contains("windows")) {
                    return new RXTXPort(PortName);
                } else {
                    return new RXTXPort(deviceDirectory + PortName);
                }
//                case CommPortIdentifier.PORT_PARALLEL:
//                    return new LPRPort(PortName);
            } else {
                logger.fine("unknown PortType  " + PortType + " passed to RXTXCommDriver.getCommPort()");
            }
        } catch (PortInUseException e) {
            logger.fine("Port " + PortName + " in use by another application");
        }
        return null;
    }

    /*  Yikes.  Trying to call println from C for odd reasons */
    public void Report(String arg) {
        System.out.println(arg);
    }
}

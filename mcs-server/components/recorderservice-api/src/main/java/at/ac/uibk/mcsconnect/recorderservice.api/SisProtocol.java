package at.ac.uibk.mcsconnect.recorderservice.api;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import static at.ac.uibk.mcsconnect.common.api.ControlCharacters.*;
import static at.ac.uibk.mcsconnect.common.api.StrUtils.shortenStringToLength;

/**
 * {@link SisProtocol.Gettable#correspondencePattern} is used as an EOF.
 *
 * For {@link SisProtocol.Gettable#correspondencePattern} and friends,
 * skip ESC, because SMP does not echo an ESC char, rather the representation.
 *
 *
 */
public interface SisProtocol { // MUST BE FINAL FOR EMBEDDED ENUMS TO WORK PROPERLY (E.G. TOSTRING)

    //private static final Logger LOGGER = LoggerFactory.getLogger(SisProtocol.class);
    // NFA-based matching
    static final String UNICODE_ALPHANUMERIC = "[\\p{IsL}\\w\\W]"; // empty word not supported, see Unicode Technical Standard #18 IsL=Unicode category letters, see Character class
    static final String UNICODE_ALPHANUMERIC_1_127 = "[\\p{IsL}\\w\\W]{1,127}"; // empty word not supported, see Unicode Technical Standard #18 IsL=Unicode category letters, see Character class
    static final String UNICODE_ALPHANUMERIC_HYPHEN_UNDERSCORE_PERIOD_SPACE = "[-\\p{Lu}\\p{Ll}_. ]";
    static final String COVERAGE_REG = "M1";
    static final String PRESENTER_REG = "M2";
    static final String RELATION_REG = "M9";
    static final String SOURCE_REG = "M11";
    static final String SUBJECT_REG = "M12";
    static final String TITLE_REG = "M13";
    static final String WRITE = "*";
    static final String RCDR = "RCDR";
    static final String Rcdr = "Rcdr";

    public static enum Gettable {

        FIRMWARE("Q", "Q[0-9.]+" + CR, "Q([0-9.]+)" + CR), // Example version: 2.11
        RUNNING_STATE(ESC + "Y" + RCDR + CR, "Y" + RCDR + CR + LF + "[0-2]{1}" + CR + CR, "Y" + RCDR + CR + LF + "([0-2]{1})" + CR + CR),
        UNIT_NAME(ESC + "CN" + CR, "CN" + CR + LF + "[\\x00-\\x7F]+" + CR + CR, LF + "([\\x00-\\x7F]+)" + CR + CR), // Device name (63 characters, max) Must comply with internet host name standards.
        TELNET_PORT(ESC + "MT" + CR, "MT" + CR + LF + "[0-9]+" + CR + CR, LF + "([0-9]+)" + CR + CR),
        SSH_PORT(ESC + "BPMAP" + CR, "BPMAP" + CR + LF + "[0-9]+" + CR + CR, LF + "([0-9]+)" + CR + CR),
        HTTP_PORT(ESC + "MH" + CR, "MH" + CR + LF + "[0-9]+" + CR + CR, LF + "([0-9]+)" + CR + CR),
        SNMP_PORT(ESC + "APMAP" + CR, "APMAP" + CR + LF + "[0-9]+" + CR + CR, LF + "([0-9]+)" + CR + CR),
        HTTPS_PORT(ESC + "SPMAP" + CR, "SPMAP" + CR + LF + "[0-9]+" + CR + CR, LF + "([0-9]+)" + CR + CR),
        SNMP_UNIT_LOCATION(ESC + "LSNMP" + CR , "LSNMP" + CR + LF + UNICODE_ALPHANUMERIC_HYPHEN_UNDERSCORE_PERIOD_SPACE + "{1,64}" + CR + CR, LF + "(" + UNICODE_ALPHANUMERIC_HYPHEN_UNDERSCORE_PERIOD_SPACE + "{1,64})" + CR + CR), // SNMP location, up to 64 characters (default="Not Specified")
        SNMP_UNIT_CONTACT(ESC + "CSNMP" + CR, "CSNMP" + CR + LF + UNICODE_ALPHANUMERIC_HYPHEN_UNDERSCORE_PERIOD_SPACE + "{1,64}" + CR + CR, LF + "(" + UNICODE_ALPHANUMERIC_HYPHEN_UNDERSCORE_PERIOD_SPACE + "{1,64})" + CR + CR), // X621: SNMP contact name text, up to 64 characters (default="Not Specified")
        SNMP_PRIVATE_COMMUNITY_STRING(ESC + "XSNMP" + CR, "XSNMP" + CR + LF + UNICODE_ALPHANUMERIC_HYPHEN_UNDERSCORE_PERIOD_SPACE + "{1,64}" + CR + CR, LF + "(" + UNICODE_ALPHANUMERIC_HYPHEN_UNDERSCORE_PERIOD_SPACE + "{1,64})" + CR + CR), // X624: SNMP private community StrUtil, up to 64 characters (default="private")
        SNMP_PUBLIC_COMMUNITY_STRING(ESC + "PSNMP" + CR, "PSNMP" + CR + LF + UNICODE_ALPHANUMERIC_HYPHEN_UNDERSCORE_PERIOD_SPACE + "{1,64}" + CR + CR, LF + "(" + UNICODE_ALPHANUMERIC_HYPHEN_UNDERSCORE_PERIOD_SPACE + "{1,64})" + CR + CR), // X623: SNMP public community StrUtil, up to 64 characters (default="public")
        SNMP_STATE(ESC + "ESNMP" + CR, "ESNMP" + CR + LF + "[0-1]{1}" + CR + CR, LF + "([0-1]{1})" + CR + CR),
        // TODO Fix TIMEZONE (causing toString issues); suspect issue with \\X+
        TIMEZONE(ESC + "TZON" + CR, "TZON" + CR + LF + "[\\p{Lower}\\p{Upper}]+\\*" + UNICODE_ALPHANUMERIC + "+" + CR + CR, LF + "([\\p{Lower}\\p{Upper}]+\\*" + UNICODE_ALPHANUMERIC + "+" + ")" + CR + CR), // X14*  Time zone acronym (2 to 6 letters)*Greenwich Mean Time (GMT) offset value: -12:00 to 14:00. Represents hours and minutes (HH:MM) offset from GMT including the time zone name.
        DHCP_MODE(ESC + "DH" + CR, "DH" + CR + LF + "[0-1]{1}" + CR + CR, LF + "([0-1]{1})" + CR + CR), // X9:
        MAC_ADDRESS(ESC + "CH" + CR, "CH" + CR + LF + "[a-zA-Z0-9]{2}-[a-zA-Z0-9]{2}-[a-zA-Z0-9]{2}-[a-zA-Z0-9]{2}-[a-zA-Z0-9]{2}-[a-zA-Z0-9]{2}" + CR + CR, LF + "([a-zA-Z0-9]{2}-[a-zA-Z0-9]{2}-[a-zA-Z0-9]{2}-[a-zA-Z0-9]{2}-[a-zA-Z0-9]{2}-[a-zA-Z0-9]{2})" + CR + CR), // X18: Hardware MAC address (00-05-A6-NN-NN-NN)
        PORT_TIMEOUT(ESC + "0TC" + CR, "0TC" + CR + LF + "[0-9]+" + CR + CR, LF + "([0-9]+)" + CR + CR), // X69: Port timeout in tens of seconds (zero padded. Default: 00030 = 300 seconds)
        GLOBAL_PORT_TIMEOUT(ESC + "1TC" + CR, "1TC" + CR + LF + "[0-9]+" + CR + CR, LF + "([0-9]+)" + CR + CR),
        MODEL_NAME("1I", "1I" + UNICODE_ALPHANUMERIC + "+" + CR + CR, "1I(" + UNICODE_ALPHANUMERIC + "+" + ")" + CR + CR),
        MODEL_DESCRIPTION("2I", "2I" + UNICODE_ALPHANUMERIC + "+" + CR + CR, "2I(" + UNICODE_ALPHANUMERIC + "+" + ")" + CR + CR),
        ACTIVE_ALARMS("39I", "39I" + UNICODE_ALPHANUMERIC + "+" + CR + CR, "39I(" + UNICODE_ALPHANUMERIC + "+" + ")" + CR + CR),
        PART_NUMBER("N", "N[0-9-]+" + CR + CR, "N([0-9-]+)" + CR + CR),
        COVERAGE(ESC + COVERAGE_REG + RCDR + CR, COVERAGE_REG + RCDR + CR + LF + UNICODE_ALPHANUMERIC + "*" + CR + CR, COVERAGE_REG + RCDR + CR + LF + "(" + UNICODE_ALPHANUMERIC + "*" + ")" + CR + CR),
        PRESENTER(ESC + PRESENTER_REG + RCDR + CR, PRESENTER_REG + RCDR + CR + LF + UNICODE_ALPHANUMERIC + "*" + CR + CR, PRESENTER_REG + RCDR + CR + LF + "(" + UNICODE_ALPHANUMERIC + "*" + ")" + CR + CR),
        RELATION(ESC + RELATION_REG + RCDR + CR, RELATION_REG + RCDR + CR + LF + UNICODE_ALPHANUMERIC + "*" + CR + CR, RELATION_REG + RCDR + CR + LF + "(" + UNICODE_ALPHANUMERIC + "*" + ")" + CR + CR),
        SOURCE(ESC + SOURCE_REG + RCDR + CR, SOURCE_REG + RCDR + CR + LF + UNICODE_ALPHANUMERIC + "*" + CR + CR, SOURCE_REG + RCDR + CR + LF + "(" + UNICODE_ALPHANUMERIC + "*" + ")" + CR + CR),
        SUBJECT(ESC + SUBJECT_REG + RCDR + CR, SUBJECT_REG + RCDR + CR + LF + UNICODE_ALPHANUMERIC + "*" + CR + CR, SUBJECT_REG + RCDR + CR + LF + "(" + UNICODE_ALPHANUMERIC + "*" + ")" + CR + CR),
        TITLE(ESC + TITLE_REG + RCDR + CR, TITLE_REG + RCDR + CR + LF + UNICODE_ALPHANUMERIC + "*" + CR + CR, TITLE_REG + RCDR + CR + LF + "(" + UNICODE_ALPHANUMERIC + "*" + ")" + CR + CR);

        public final String payload;
        public final Pattern correspondencePattern;
        public final Pattern meaningfulResponsePatternAsGroup;

        Gettable(String payload, String correspondencePattern, String meaningfulResponsePatternAsGroup) {
            this.payload = payload;
            this.correspondencePattern = Pattern.compile(correspondencePattern, Pattern.DOTALL);
            this.meaningfulResponsePatternAsGroup = Pattern.compile(meaningfulResponsePatternAsGroup, Pattern.DOTALL);
        }
    }

    /**
     * Settable represents registers on the SMP device. Registers are mapped to Dublin Core properties. The caller should only
     * be aware of Dublin Core Standard Properties.
     */
    public static enum Settable {

        // NOTE: SMP Adds LF after CR of message, which must be parsed in the correspondencePattern immediately after sent CR
        COVERAGE(DublinCore.COVERAGE, ESC + COVERAGE_REG + "*" + "%s" + RCDR + CR, COVERAGE_REG + "\\*" + UNICODE_ALPHANUMERIC_1_127 + RCDR + CR + LF + Rcdr + COVERAGE_REG + "\\*" + UNICODE_ALPHANUMERIC_1_127 + CR + CR, "Rcdr" + COVERAGE_REG + "\\*(" + UNICODE_ALPHANUMERIC_1_127 + ")" + CR),
        PRESENTER(DublinCore.CREATOR,ESC + PRESENTER_REG + "*" + "%s" + RCDR + CR, PRESENTER_REG + "\\*" + UNICODE_ALPHANUMERIC_1_127 + RCDR + CR + LF + Rcdr + PRESENTER_REG + "\\*" + UNICODE_ALPHANUMERIC_1_127 + CR + CR, "Rcdr" + PRESENTER_REG + "\\*(" + UNICODE_ALPHANUMERIC_1_127 + ")" + CR),
        RELATION(DublinCore.RELATION, ESC + RELATION_REG + "*" + "%s" + RCDR + CR, RELATION_REG + "\\*" + UNICODE_ALPHANUMERIC_1_127 + RCDR + CR + LF + Rcdr + RELATION_REG + "\\*" + UNICODE_ALPHANUMERIC_1_127 + CR + CR, "Rcdr" + RELATION_REG + "\\*(" + UNICODE_ALPHANUMERIC_1_127 + ")" + CR),
        SOURCE(DublinCore.SOURCE, ESC + SOURCE_REG + "*" + "%s" + RCDR + CR, SOURCE_REG + "\\*" + UNICODE_ALPHANUMERIC_1_127 + RCDR + CR + LF + Rcdr + SOURCE_REG + "\\*" + UNICODE_ALPHANUMERIC_1_127 + CR + CR, "Rcdr" + SOURCE_REG + "\\*(" + UNICODE_ALPHANUMERIC_1_127 + ")" + CR),
        SUBJECT(DublinCore.SUBJECT,ESC + SUBJECT_REG + "*" +"%s" + RCDR + CR, SUBJECT_REG + "\\*" + UNICODE_ALPHANUMERIC_1_127 + RCDR + CR + LF + Rcdr + SUBJECT_REG + "\\*" + UNICODE_ALPHANUMERIC_1_127 + CR + CR, "Rcdr" + SUBJECT_REG + "\\*(" + UNICODE_ALPHANUMERIC_1_127 + ")" + CR),
        TITLE(DublinCore.TITLE, ESC + TITLE_REG + "*" + "%s" + RCDR + CR, TITLE_REG + "\\*" + UNICODE_ALPHANUMERIC_1_127 + RCDR + CR + LF + Rcdr + TITLE_REG + "\\*" + UNICODE_ALPHANUMERIC_1_127 + CR + CR, "Rcdr" + TITLE_REG + "\\*(" + UNICODE_ALPHANUMERIC_1_127 + ")" + CR);

        public final DublinCore dcProperty;
        public final String template;
        public final Pattern correspondencePattern;
        public final Pattern meaningfulResponsePatternAsGroup;

        Settable(DublinCore dcProperty, String template, String correspondencePattern, String meaningfulResponsePatternAsGroup) {
            this.dcProperty = dcProperty;
            this.template = template;
            this.correspondencePattern = Pattern.compile(correspondencePattern, Pattern.DOTALL);
            this.meaningfulResponsePatternAsGroup = Pattern.compile(meaningfulResponsePatternAsGroup, Pattern.DOTALL);
        }

        // TODO: Implement 1-127 chars and alphanumeric restrictions on domain
        //Function<String, String> buildPayload = s -> String.format(template, s);
        public String buildPayload(String s) {
            return String.format(template, preprocessMetadataValue(s));
        }

        /**
         * This function might better be implemented in the the Recorder implementations if the {@link SisProtocol}
         * may be used for multiple SMP models. At the the time implementing this, there were only model SMP351s.
         *
         * @param str
         * @return
         */
        private static String preprocessMetadataValue(String str) {
            return shortenTo127.apply(str);
        }

        public static Function<String, String> shortenTo127 = shortenStringToLength.apply(127);

        Function<DublinCore, Optional<Settable>> dcNameToSettable = p -> Arrays.stream(values()).filter(e -> p.equals(e.dcProperty)).findFirst();
    }

    public static enum Command {
        STOPRECORDING(ESC + "Y0" + RCDR + CR, "Y0" + RCDR + CR + LF + Rcdr + "Y0" + CR + CR, "(" + Rcdr + "Y0)" + CR),
        STARTRECORDING(ESC + "Y1" + RCDR + CR, "Y1" + RCDR + CR + LF + Rcdr + "Y1" + CR + CR, "(" + Rcdr + "Y1)" + CR),
        PAUSERECORDING(ESC + "Y2" + RCDR + CR, "Y2" + RCDR + CR + LF + Rcdr + "Y2" + CR + CR, "(" + Rcdr + "Y2)" + CR);

        public final String command;
        public final Pattern correspondencePattern;
        public final Pattern meaningfulResponsePatternAsGroup;

        Command(String command, String correspondencePattern, String meaningfulResponsePatternAsGroup) {
            this.command = command;
            this.correspondencePattern = Pattern.compile(correspondencePattern, Pattern.DOTALL);
            this.meaningfulResponsePatternAsGroup = Pattern.compile(meaningfulResponsePatternAsGroup, Pattern.DOTALL);
        }
    }

    public static class Patterns {
        public final static Pattern patGreeter = Pattern.compile("[0-9]{2}:[0-9]{2}:[0-9]{2}\r", Pattern.DOTALL); // looking for time at end of greeter 13:11:07
    }

    //@Override
    //public String toString() {
    //    return this.getClass().getSimpleName();
    //}
}
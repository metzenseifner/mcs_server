package at.ac.uibk.mcsconnect.common.api;

public enum ControlCharacters {

    CR(0x000d,String.valueOf(0x0D)),
    ESC(0x001b, String.valueOf(0x1B)),
    LF(0x000a, String.valueOf(0x0A));

    private char hex;
    private String str;

    ControlCharacters(int hex, String str) {
        this.hex = (char) hex;
        this.str = str;
    }

    public String toString() {
        return "" + hex;
    }
}
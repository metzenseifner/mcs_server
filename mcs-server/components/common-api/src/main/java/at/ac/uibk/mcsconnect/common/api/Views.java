package at.ac.uibk.mcsconnect.common.api;

/**
 * by default – all properties not explicitly marked as being part of a view, are serialized. We are disabling that
 * behavior with the handy DEFAULT_VIEW_INCLUSION feature.
 */
public class Views {

    public static class Public {}
    public static class Admin extends Public {}

}
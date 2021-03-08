package at.ac.uibk.mcsconnect.functional.common;

/**
 * An effect. A program.
 *
 * Does not return a value, but mutates the outside world.
 */
public interface Executable {
    void exec();
}

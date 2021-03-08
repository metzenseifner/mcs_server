package at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden;

/**
 * Describes concurrency capability of actions.
 * All actions are handled atomically.
 *
 * If an smpAction changes state of target, is dependent on other states
 * or it must be handled in a specific order, then use
 * SIMPLEX
 *
 * If two smpAction can be handled concurrently independent
 * of each other, like setting metadata fields, then use
 * MULTIPLEX
 *
 * If an smpAction does not change state of the target (it is a passive smpAction),
 * and it can be carried out independently, like fetching recording running state,
 * then use
 * SIMPLEXAUX.
 */
public enum SshCacheType {

    SERIAL,
    QUEUE,
    AUX;

    SshCacheType() {}

}

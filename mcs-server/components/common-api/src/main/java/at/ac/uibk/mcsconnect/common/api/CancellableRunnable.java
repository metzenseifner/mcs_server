package at.ac.uibk.mcsconnect.common.api;

/**
 * With the motto: Tasks should have a cancellation policy (threads have an interruption policy)
 *
 * Workflow Idea for Cancellable Runnables:
 * Extend Runnable
 * Make the “cancellable” resources (e.g. the input stream) an instance field, which
 * provide a cancel method to your extended runnable, where you get the “cancellable” resource and cancel it (e.g. call inputStream.close())
 * Implement a custom ThreadFactory that in turn creates custom Thread instances that override the interrupt() method and invoke the cancel() method on your extended Runnable
 * Instantiate the executor with the custom thread factory (static factory methods take it as an argument)
 * Handle abrupt closing/stopping/disconnecting of your blocking resources, in the run() method
 */
public interface CancellableRunnable extends Runnable {
    void cancel();
}

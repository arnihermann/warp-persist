package com.wideplay.warp.persist;

/**
 * Created with IntelliJ IDEA.
 * User: dhanji
 * Date: Oct 7, 2007
 * Time: 8:48:33 PM
 *
 * This interface is used to gain manual control over the unit of work.
 *
 * The Unit of Work referred to by WorkManager will always be local to the
 * calling thread. Be careful to endWork() in a finally block. Neither JPA,
 * nor Hibernate supports threadsafe sessions (reasoning behind thread-locality
 * of Unit of Work semantics).
 *
 * Using WorkManager with the SPR Filter inside a request is not recommended.
 *
 * Using WorkManager with session-per-txn strategy is not terribly clever either.
 *
 * Using WorkManager with session-per-request strategy but *outside* a request
 * (i.e. in a background or bootstrap thread) is probably a good use case.
 *
 * @author Dhanji R. Prasanna (dhanji gmail com)
 */
public interface WorkManager {

    /**
     * Starts a Unit Of Work. Underneath, causes a session to
     * the data layer to be opened. If there is already one open,
     * the invocation will do nothing. In this way, you can define
     * arbitrary units-of-work that nest within one another safely.
     */
    void beginWork();

    /**
     * Declares an end to the current Unit of Work. Underneath, causes any
     * open session to the data layer to close. If there is no Unit of work
     * open, then the call returns silently. You can safely invoke endWork()
     * repeatedly.
     *
     */
    void endWork();
}

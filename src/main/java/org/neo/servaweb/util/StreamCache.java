package org.neo.servaweb.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.neo.servaaiagent.ifc.NotifyCallbackIFC;

public class StreamCache {
    private static StreamCache instance = new StreamCache();
    private Map<String, NotifyCallbackIFC> callbackMap = new ConcurrentHashMap<>();
    private Map<String, Future<?>> taskMap = new ConcurrentHashMap<>();

    public static StreamCache getInstance() {
        return instance;
    }

    public void put(String session, NotifyCallbackIFC callback, Future<?> future) {
        if (callback != null) callbackMap.put(session, callback);
        if (future != null) taskMap.put(session, future);
    }

    public NotifyCallbackIFC get(String session) {
        return callbackMap.get(session);
    }

    public boolean isTaskRunning(String session) {
        Future<?> future = taskMap.get(session);
        return future != null && !future.isDone();
    }

    public void remove(String session) {
        if (callbackMap.containsKey(session)) {
            NotifyCallbackIFC callback = callbackMap.get(session);
            callback.removeWorkingThread();
            callback.clearHistory();
            callback.closeOutputStream();
            callbackMap.remove(session);
        }
        taskMap.remove(session);
    }
}

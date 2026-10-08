package com.example.objectverse.observer;

public interface ProjectModelObserver {

    String getObserverName();

    void onModelChanged(Long projectId, String eventType);
}

package com.example.objectverse.observer;

import org.springframework.stereotype.Component;

@Component
public class ReviewUpdateObserver implements ProjectModelObserver {

    @Override
    public String getObserverName() {
        return "ReviewUpdateObserver";
    }

    @Override
    public void onModelChanged(Long projectId, String eventType) {
        System.out.println("[ObjectVerse Observer] Review result marked stale, projectId=" + projectId + ", eventType=" + eventType);
    }
}

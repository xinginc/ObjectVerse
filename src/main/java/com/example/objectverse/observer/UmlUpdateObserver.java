package com.example.objectverse.observer;

import org.springframework.stereotype.Component;

@Component
public class UmlUpdateObserver implements ProjectModelObserver {

    @Override
    public String getObserverName() {
        return "UmlUpdateObserver";
    }

    @Override
    public void onModelChanged(Long projectId, String eventType) {
        System.out.println("[ObjectVerse Observer] UML refresh marked, projectId=" + projectId + ", eventType=" + eventType);
    }
}

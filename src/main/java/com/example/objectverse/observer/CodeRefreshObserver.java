package com.example.objectverse.observer;

import org.springframework.stereotype.Component;

@Component
public class CodeRefreshObserver implements ProjectModelObserver {

    @Override
    public String getObserverName() {
        return "CodeRefreshObserver";
    }

    @Override
    public void onModelChanged(Long projectId, String eventType) {
        System.out.println("[ObjectVerse Observer] Generated code marked stale, projectId=" + projectId + ", eventType=" + eventType);
    }
}

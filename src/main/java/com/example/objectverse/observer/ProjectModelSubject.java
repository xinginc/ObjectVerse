package com.example.objectverse.observer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProjectModelSubject {

    private final List<ProjectModelObserver> observers = new ArrayList<>();

    public ProjectModelSubject(List<ProjectModelObserver> observers) {
        this.observers.addAll(observers);
    }

    public void registerObserver(ProjectModelObserver observer) {
        observers.add(observer);
    }

    public void notifyObservers(Long projectId, String eventType) {
        for (ProjectModelObserver observer : observers) {
            observer.onModelChanged(projectId, eventType);
        }
    }
}

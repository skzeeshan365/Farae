package com.reiserx.farae.Classes;

public class DatabaseListener {
    private DatabaseObserver observer;

    public DatabaseListener(DatabaseObserver observer) {
        this.observer = observer;
    }
    public void update() {
        observer.onSuccess();
    }
}

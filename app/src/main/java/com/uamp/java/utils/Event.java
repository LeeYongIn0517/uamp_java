package com.uamp.java.utils;

import androidx.annotation.Nullable;

public class Event<T> {
    private final T content;
    private Boolean hasBeenHandled = false;

    public Event(T content) {
        this.content = content;
    }

    public Boolean hasBeenHandled() {
        return hasBeenHandled;
    }

    @Nullable
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    public T peekContent() {
        return content;
    }
}

package com.reiserx.farae.Classes;

public interface DatabaseObserver {
        void onSuccess();
        //And The second message, when the process will fail.
        void onFailure(String error);
}

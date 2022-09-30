package com.reiserx.farae.Classes;


import android.util.Log;

import com.reiserx.farae.Models.exceptionUpload;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class ExceptionHandler {
    Exception e;
    String UserID;

    public ExceptionHandler(Exception e, String UserID) {
        this.e = e;
        this.UserID = UserID;
    }
    public void upload() {
        StackTraceElement exception = e.getStackTrace()[0];
        String className = exception.getClassName();
        String methodName = exception.getMethodName();
        String filename = exception.getFileName();
        Log.e("vhfnhnfen", String.valueOf(e));
        int LineNumber = exception.getLineNumber();
        String currentTime = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        FirebaseDatabase mdb = FirebaseDatabase.getInstance();
        String clase = className.replace(".", "");
        DatabaseReference ref = mdb.getReference("Administration").child("Error logs").child(UserID).child((clase+LineNumber));
        exceptionUpload exceptionUpload = new exceptionUpload(className, methodName, e.toString(), LineNumber, filename, currentTime);
        ref.setValue(exceptionUpload);
    }
}

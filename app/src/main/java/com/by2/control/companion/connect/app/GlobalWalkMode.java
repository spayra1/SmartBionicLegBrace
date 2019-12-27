package com.by2.control.companion.connect.app;

import android.app.Application;

/**
 * Created by Syamantak on 12/21/2016.
 */

public class GlobalWalkMode extends Application {
    private int walkChoice = 6;

    public int getWalkChoice() {
        return walkChoice;
    }

    public void setWalkChoice(int walkChoiceInput) {
        walkChoice = walkChoiceInput;
    }
}

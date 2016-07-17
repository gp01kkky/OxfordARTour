package com.ar.oxford.oxfordtourar.MapHelper;

import android.support.design.widget.BottomSheetDialog;
import android.view.View;

/**
 * Created by Kelvin Khoo on 29/06/2016.
 */


    public interface OnTaskCompleted {
        void onTaskCompleted(Boolean response, BottomSheetDialog bottomSheetDialog, View v);
    }


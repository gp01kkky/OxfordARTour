package com.ar.oxford.oxfordtourar.MapHelper;

import com.ar.oxford.oxfordtourar.model.Place;

import java.util.List;

/**
 * Created by Kelvin Khoo on 29/06/2016.
 */


    public interface OnTaskCompleted {
        void onTaskCompleted(Boolean response, List<Place> placeList);
    }


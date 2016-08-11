package com.ar.oxford.oxfordtourar.Util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ar.oxford.oxfordtourar.R;

/**
 * Created by Kelvin Khoo on 04/08/2016.
 */
public class GetIconFromType {

    public GetIconFromType()
    {

    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public int getIconIDFromType(String type)
    {
        int resource_id;
        switch (type)
        {
            case "restaurant":
                resource_id = R.drawable.icn_restaurant;
                break;
            case "food":
                resource_id = R.drawable.icn_restaurant;
                break;
            case "bar":
                resource_id = R.drawable.icn_bar;
                break;
            case "cafe":
                resource_id = R.drawable.icn_cafe;
                break;
            case "bakery":
                resource_id = R.drawable.icn_cafe;
                break;
            case "bus_station":
                resource_id = R.drawable.icn_bus_station;
                break;
            case "city_hall":
                resource_id = R.drawable.icn_civic_building;
                break;
            case "shopping_mall":
                resource_id = R.drawable.icn_shopping;
                break;
            case "store":
                resource_id = R.drawable.icn_shopping;
                break;
            case "train_station":
                resource_id = R.drawable.icn_train_station;
                break;
            case "transit_station":
                resource_id = R.drawable.icn_train_station;
                break;
            case "amusement_park":
                resource_id = R.drawable.icn_attraction;
                break;
            case "point_of_interest":
                resource_id = R.drawable.icn_other;
                break;
            case "car_rental":
                resource_id = R.drawable.icn_car_rental;
                break;
            case "night_club":
                resource_id = R.drawable.icn_bar;
                break;
            case "museum":
                resource_id = R.drawable.icn_musuem;
                break;
            case "place_of_worship":
                resource_id = R.drawable.icn_place_of_worship;
                break;
            case "church":
                resource_id = R.drawable.icn_place_of_worship;
                break;
            case "lodging":
                resource_id = R.drawable.icn_lodging;
                break;
            case "post_office":
                resource_id = R.drawable.icn_post_office;
                break;
            case "police":
                resource_id = R.drawable.icn_police;
                break;
            case "art_gallery":
                resource_id = R.drawable.icn_gallery;
                break;
            case "gas_station":
                resource_id = R.drawable.icn_gas_station;
                break;
            case "university":
                resource_id = R.drawable.icn_university;
                break;
            case "school":
                resource_id = R.drawable.icn_university;
                break;
            case "pharmacy":
                resource_id = R.drawable.icn_shopping;
                break;
            case "clothing_store":
                resource_id = R.drawable.icn_shopping;
                break;
            case "meal_takeaway":
                resource_id = R.drawable.icn_restaurant;
                break;
            case "beauty_salon":
                resource_id = R.drawable.icn_shopping;
                break;
            case "jewelry_store":
                resource_id = R.drawable.icn_shopping;
                break;
            case "bank":
                resource_id = R.drawable.icn_bank;
                break;
            case "finance":
                resource_id = R.drawable.icn_bank;
                break;
            case "atm":
                resource_id = R.drawable.icn_bank;
                break;
            case "local_government_office":
                resource_id = R.drawable.icn_civic_building;
                break;
            case "electronic_store":
                resource_id = R.drawable.icn_shopping;
                break;
            case "department_store":
                resource_id = R.drawable.icn_shopping;
                break;
            case "movie_theatre":
                resource_id = R.drawable.icn_cinema;
                break;
            case "library":
                resource_id = R.drawable.icn_university;
                break;
            case "hospital":
                resource_id = R.drawable.icn_hospital;
                break;
            case "health":
                resource_id = R.drawable.icn_hospital;
                break;
            case "doctor":
                resource_id = R.drawable.icn_hospital;
                break;
            case "grocery_or_supermarket":
                resource_id = R.drawable.icn_supermarket;
                break;
            case "convenience_store":
                resource_id = R.drawable.icn_shopping;
                break;
            case "shoe_store":
                resource_id = R.drawable.icn_shopping;
                break;
            default:
                resource_id = R.drawable.places_default;
                break;
        }
        return resource_id;
    }

    public String getIconAssetFromType(String type)
    {
        String resource_id;
        switch (type)
        {
            case "restaurant":
                resource_id = "assets://icn_restaurant.png";
                break;
            case "food":
                resource_id = "assets://icn_restaurant.png";
                break;
            case "bar":
                resource_id = "assets://icn_bar.png";
                break;
            case "cafe":
                resource_id = "assets://icn_cafe.png";
                break;
            case "bakery":
                resource_id = "assets://icn_cafe.png";
                break;
            case "bus_station":
                resource_id = "assets://icn_bus_station.png";
                break;
            case "city_hall":
                resource_id = "assets://icn_civic_building.png";
                break;
            case "shopping_mall":
                resource_id = "assets://icn_shopping.png";
                break;
            case "store":
                resource_id = "assets://icn_shopping.png";
                break;
            case "train_station":
                resource_id = "assets://icn_train_station.png";
                break;
            case "transit_station":
                resource_id = "assets://icn_train_station.png";
                break;
            case "amusement_park":
                resource_id = "assets://icn_attraction.png";
                break;
            case "point_of_interest":
                resource_id = "assets://icn_other.png";
                break;
            case "car_rental":
                resource_id = "assets://icn_car_rental.png";
                break;
            case "night_club":
                resource_id = "assets://icn_bar.png";
                break;
            case "museum":
                resource_id = "assets://icn_musuem.png";
                break;
            case "place_of_worship":
                resource_id = "assets://icn_place_of_worship.png";
                break;
            case "church":
                resource_id = "assets://icn_place_of_worship.png";
                break;
            case "lodging":
                resource_id = "assets://icn_lodging.png";
                break;
            case "post_office":
                resource_id = "assets://icn_post_office.png";
                break;
            case "police":
                resource_id = "assets://icn_police.png";
                break;
            case "art_gallery":
                resource_id = "assets://icn_gallery.png";
                break;
            case "gas_station":
                resource_id = "assets://icn_gas_station.png";
                break;
            case "university":
                resource_id = "assets://icn_university.png";
                break;
            case "school":
                resource_id = "assets://icn_university.png";
                break;
            case "pharmacy":
                resource_id = "assets://icn_shopping.png";
                break;
            case "clothing_store":
                resource_id = "assets://icn_shopping.png";
                break;
            case "meal_takeaway":
                resource_id = "assets://icn_restaurant.png";
                break;
            case "beauty_salon":
                resource_id = "assets://icn_shopping.png";
                break;
            case "jewelry_store":
                resource_id = "assets://icn_shopping.png";
                break;
            case "bank":
                resource_id = "assets://icn_bank.png";
                break;
            case "finance":
                resource_id = "assets://icn_bank.png";
                break;
            case "atm":
                resource_id = "assets://icn_bank.png";
                break;
            case "local_government_office":
                resource_id = "assets://icn_civic_building.png";
                break;
            case "electronic_store":
                resource_id = "assets://icn_shopping.png";
                break;
            case "department_store":
                resource_id = "assets://icn_shopping.png";
                break;
            case "movie_theatre":
                resource_id = "assets://icn_cinema.png";
                break;
            case "library":
                resource_id = "assets://icn_university.png";
                break;
            case "hospital":
                resource_id = "assets://icn_hospital.png";
                break;
            case "health":
                resource_id = "assets://icn_hospital.png";
                break;
            case "doctor":
                resource_id = "assets://icn_hospital.png";
                break;
            case "grocery_or_supermarket":
                resource_id = "assets://icn_supermarket.png";
                break;
            case "convenience_store":
                resource_id = "assets://icn_shopping.png";
                break;
            case "shoe_store":
                resource_id = "assets://icn_shopping.png";
                break;
            default:
                resource_id = "assets://place_default.png";
                break;
        }
        return resource_id;
    }

}

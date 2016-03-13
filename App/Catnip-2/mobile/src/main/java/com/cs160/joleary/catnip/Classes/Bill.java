package com.cs160.joleary.catnip.Classes;

import com.google.gson.JsonObject;

/**
 * Created by namhyun on 3/11/16.
 */
public class Bill {
    public String chamber;
    public String billId;
    public String dateIntroduced;
    public String title;

    public Bill(JsonObject jsonObject) {
        chamber = jsonObject.get("chamber").getAsString();
        billId = jsonObject.get("bill_id").getAsString();
        dateIntroduced = jsonObject.get("introduced_on").getAsString();

        if (jsonObject.get("short_title").isJsonNull()) {
            title = jsonObject.get("official_title").getAsString();
        } else {
            title = jsonObject.get("short_title").getAsString();
        }
    }


}

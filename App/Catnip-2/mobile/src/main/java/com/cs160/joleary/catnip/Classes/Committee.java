package com.cs160.joleary.catnip.Classes;

import com.google.gson.JsonObject;

/**
 * Created by namhyun on 3/11/16.
 */
public class Committee {
    public String chamber;
    public String id;
    public String name;

    public Committee(JsonObject jsonObject) {
        chamber = jsonObject.get("chamber").getAsString();
        id = jsonObject.get("committee_id").getAsString();
        name = jsonObject.get("name").getAsString();
    }
}

package com.elementary.tasks.core.network.places

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PlacesResponse {

    @SerializedName("results")
    @Expose
    var results: List<Place> = listOf()
    @SerializedName("status")
    @Expose
    var status: String = ""
}

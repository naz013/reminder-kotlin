package com.elementary.tasks.core.network.places

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Place {

    @SerializedName("geometry")
    @Expose
    var geometry: Geometry? = null
    @SerializedName("icon")
    @Expose
    var icon: String? = ""
    @SerializedName("id")
    @Expose
    var id: String = ""
    @SerializedName("name")
    @Expose
    var name: String? = ""
    @SerializedName("types")
    @Expose
    var types: List<String> = listOf()
    @SerializedName("formatted_address")
    @Expose
    var formattedAddress: String? = ""
}

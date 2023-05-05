package com.management.capstone.server

import com.google.gson.annotations.SerializedName

data class RequestScore (
    @SerializedName("nickname")
    val nickName:String,
    val score:Int)
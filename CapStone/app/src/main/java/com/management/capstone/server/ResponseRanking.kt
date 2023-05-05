package com.management.capstone.server

import com.google.gson.annotations.SerializedName

data class ResponseRanking (
    @SerializedName("nickname")
    val nickName:String,
    val score:Int,
    val ranking:Int)
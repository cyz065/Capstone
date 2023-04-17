package com.management.capstone.server

data class ResponsePicture(
    var id:String,
    var image_file:String,
    var predicted_class:String
)
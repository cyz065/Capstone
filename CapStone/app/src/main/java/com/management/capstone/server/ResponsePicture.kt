package com.management.capstone.server

data class ResponsePicture(
    var id:String,
    var image_file:String,
    var predicted_class1:String,
    var predicted_class2:String,
    var predicted_class3:String,
    var predicted_class4:String,
    var predicted_class5:String
)

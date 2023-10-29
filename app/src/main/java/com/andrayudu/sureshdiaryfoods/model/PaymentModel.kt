package com.andrayudu.sureshdiaryfoods.model


data class PaymentModel(
    var amount:Int=0,
    var date:String?=null,
    var via:String?=null
)
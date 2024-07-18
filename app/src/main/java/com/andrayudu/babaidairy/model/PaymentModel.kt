package com.andrayudu.babaidairy.model


data class PaymentModel(
    var amount:Int=0,
    var date:String?=null,
    var via:String?=null
)
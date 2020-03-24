@file:JsModule("date-fns/differenceInSeconds")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")
package date_fns

import kotlin.js.Date

@JsName("default")
external fun differenceInSeconds(dateLeft: Date, dateRight: Date): Number

@JsName("default")
external fun differenceInSeconds(dateLeft: Date, dateRight: Number): Number

@JsName("default")
external fun differenceInSeconds(dateLeft: Number, dateRight: Date): Number

@JsName("default")
external fun differenceInSeconds(dateLeft: Number, dateRight: Number): Number


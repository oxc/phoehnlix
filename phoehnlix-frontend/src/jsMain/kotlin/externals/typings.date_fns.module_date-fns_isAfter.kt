@file:JsModule("date-fns/isAfter")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")
package date_fns

import kotlin.js.Date

@JsName("default")
external fun isAfter(date: Date, dateToCompare: Date): Boolean

@JsName("default")
external fun isAfter(date: Date, dateToCompare: Number): Boolean

@JsName("default")
external fun isAfter(date: Number, dateToCompare: Date): Boolean

@JsName("default")
external fun isAfter(date: Number, dateToCompare: Number): Boolean

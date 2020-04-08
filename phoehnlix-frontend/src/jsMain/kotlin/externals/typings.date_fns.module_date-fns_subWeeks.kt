@file:JsModule("date-fns/subWeeks")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")
package date_fns

import kotlin.js.Date

@JsName("default")
external fun subWeeks(date: Date, amount: Number): Date

@JsName("default")
external fun subWeeks(date: Number, amount: Number): Date

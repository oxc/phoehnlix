@file:JsModule("date-fns/formatISO")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")
package date_fns

import kotlin.js.Date

@JsName("default")
external fun formatISO(date: Date, options: `T$10` = definedExternally): String

@JsName("default")
external fun formatISO(date: Number, options: `T$10` = definedExternally): String


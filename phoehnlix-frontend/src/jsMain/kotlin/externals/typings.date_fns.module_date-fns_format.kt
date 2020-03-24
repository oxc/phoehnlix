@file:JsModule("date-fns/format")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")
package date_fns

import kotlin.js.Date

external interface FormatOptions {
  var locale: Locale?
    get() = definedExternally
    set(value) = definedExternally
  var weekStartsOn: String /* 0 | 1 | 2 | 3 | 4 | 5 | 6 */
  var firstWeekContainsDate: Number?
    get() = definedExternally
    set(value) = definedExternally
  var useAdditionalWeekYearTokens: Boolean?
    get() = definedExternally
    set(value) = definedExternally
  var useAdditionalDayOfYearTokens: Boolean?
    get() = definedExternally
    set(value) = definedExternally
}

@JsName("default")
external fun format(date: Date, format: String, options: FormatOptions = definedExternally): String

@JsName("default")
external fun format(date: Number, format: String, options: FormatOptions = definedExternally): String


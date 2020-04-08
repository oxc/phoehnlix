package de.esotechnik.phoehnlix.frontend.util

import date_fns.addSeconds
import date_fns.isAfter
import date_fns.isBefore
import date_fns.subDays
import date_fns.subMonths
import date_fns.subWeeks
import date_fns.subYears
import kotlin.js.Date

/**
 * @author Bernhard Frauendienst
 */
inline fun Date.isAfter(other: Date) = isAfter(this, other)
inline fun Date.isBefore(other: Date) = isBefore(this, other)

inline fun Date.addSeconds(amount: Number) = addSeconds(this, amount)
inline fun Date.subDays(amount: Number) = subDays(this, amount)
inline fun Date.subMonths(amount: Number) = subMonths(this, amount)
inline fun Date.subWeeks(amount: Number) = subWeeks(this, amount)
inline fun Date.subYears(amount: Number) = subYears(this, amount)

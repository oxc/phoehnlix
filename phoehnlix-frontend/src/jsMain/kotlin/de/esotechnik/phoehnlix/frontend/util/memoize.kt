package de.esotechnik.phoehnlix.frontend.util

import kotlin.reflect.KProperty

/**
 * @author Bernhard Frauendienst
 */
fun <T, R> memoizeOne(memoized: (T) -> R): (T) -> R {
  var initialized = false
  var lastValue1: T? = null
  var lastResult: R? = null

  return initializer@{ p1 ->
    if (initialized) {
      if (p1 == lastValue1) {
        return@initializer lastResult.unsafeCast<R>()
      }
    }
    val result = memoized(p1)
    initialized = true
    lastValue1 = p1
    lastResult = result
    return@initializer result
  }
}

fun <I1, I2, R> memoizeOne(memoized: (I1, I2) -> R): (I1, I2) -> R {
  var initialized = false
  var lastValue1: I1? = null
  var lastValue2: I2? = null
  var lastResult: R? = null

  return initializer@{ p1, p2 ->
    if (initialized) {
      if (p1 == lastValue1 && p2 == lastValue2) {
        return@initializer lastResult.unsafeCast<R>()
      }
    }
    val result = memoized(p1, p2)
    initialized = true
    lastValue1 = p1
    lastValue2 = p2
    lastResult = result
    return@initializer result
  }
}

fun <T, R> memoizeOne(f: (T) -> R, p1: () -> T) = memoizeOne(f)(p1)
fun <I1, I2, R> memoizeOne(f: (I1, I2) -> R, p1: () -> I1, p2: () -> I2) = memoizeOne(f)(p1, p2)

operator fun <T, R> ((T) -> R).invoke(p1: () -> T) = CurryingValueDelegate1(this, p1)
operator fun <I1, I2, R> ((I1, I2) -> R).invoke(p1: () -> I1, p2: () -> I2) = CurryingValueDelegate2(this, p1, p2)

class CurryingValueDelegate1<T, R>(private val f: (T) -> R, private val p1: () -> T) {
  operator fun getValue(thisRef: Any?, property: KProperty<*>): R {
    return f(p1())
  }
}
class CurryingValueDelegate2<I1, I2, R>(private val f: (I1, I2) -> R, private val p1: () -> I1, private val p2: () -> I2) {
  operator fun getValue(thisRef: Any?, property: KProperty<*>): R {
    return f(p1(), p2())
  }
}

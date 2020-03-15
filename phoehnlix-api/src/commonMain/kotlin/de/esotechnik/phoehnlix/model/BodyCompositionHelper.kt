import de.esotechnik.phoehnlix.model.ActivityLevel
import de.esotechnik.phoehnlix.model.ActivityLevel.*

/*
object BodyCompositionHelper {
  private const val CLASSIFICATION_STEPS = 9
  private const val LOW_CLASSIFICATION_THRESHOLD = 3
  private const val NORMAL_CLASSIFICATION_THRESHOLD = 6
  const val MAX_IMPEDANCE = 1200
  private val BMI_CLASSIFICATIONS = doubleArrayOf(17.5, 18.0, 18.5, 20.5, 23.0, 25.1, 27.5, 30.0)
  private val FAT_CLASSIFICATIONS = doubleArrayOf(-10.0, -5.0, 0.0, 3.0, 6.0, 10.0, 10.5, 20.0)
  private val METABOLIC_RATE_CLASSIFICATIONS = doubleArrayOf(0.85, 0.9, 0.95, 1.0, 1.05, 1.1, 1.15, 1.2)
  private val MUSCLE_CLASSIFICATIONS = doubleArrayOf(-6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0)
  private val WATER_CLASSIFICATIONS = doubleArrayOf(-6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0)


  private fun getBmiClassification(var0: Double): Int {
    return getClassification(
      var0,
      ClassificationCalculator { ix: Int -> BMI_CLASSIFICATIONS[ix - 1] }
    )
  }

  private fun getBodyFatClassification(var0: Double, var2: Int, var3: Boolean): Int {
    var var0 = var0
    var var4: Double
    if (var3) {
      var4 = var0
      if (var0 < 4.0) {
        var4 = 4.0
      }
      var0 = var4 - (var2 * var2).toDouble() * 0.0019 - var2.toDouble() * 0.028
      var4 = 7.114
    } else {
      var4 = var0
      if (var0 < 10.0) {
        var4 = 10.0
      }
      var0 = var4 + (var2 * var2).toDouble() * 0.0032 - var2.toDouble() * 0.5006
      var4 = 8.078
    }
    return getClassification(var0 - var4, OxunX6YtjgZUw4nMwbibeu1vQCY.INSTANCE)
  }

  private fun getBodyFatForClassification(var0: Double, var2: Int, var3: Boolean): Double {
    val var4: Double
    val var6: Double
    if (var3) {
      var4 = (var2 * var2).toDouble() * 0.0019 + var2.toDouble() * 0.028
      var6 = 7.114
    } else {
      var4 = (var2 * var2).toDouble() * -0.0032 + var2.toDouble() * 0.5006
      var6 = 8.078
    }
    return var4 + var6 + var0
  }

  private fun getBodyWaterForClassification(var0: Double, var2: Int, var3: Boolean): Double {
    val var4: Double
    val var6: Double
    if (var3) {
      var4 = (var2 * var2).toDouble() * 8.0E-4 - var2.toDouble() * 0.1715
      var6 = 63.484
    } else {
      var4 = (var2 * var2).toDouble() * 0.004 - var2.toDouble() * 0.4621
      var6 = 61.614
    }
    return var4 + var6 + var0
  }

  private fun getClassification(var0: Double, calculator: ClassificationCalculator): Int {
    for (classificationIndex in 1..8) {
      if (var0 < calculator.getNextClassifier(classificationIndex)) {
        return classificationIndex
      }
    }
    return 9
  }

  fun getClassificationForType(var0: MeasureType?, var1: Double, var3: User): Int {
    return when (var0) {
      WEIGHT -> getWeightClassification(var1, var3.getHeightInCm())
      BMI -> getBmiClassification(var1)
      BODYFAT -> getBodyFatClassification(var1, var3.getAge(), var3.isMale())
      WATER -> getWaterClassification(var1, var3.getAge(), var3.isMale())
      MUSCLE -> getMuscleClassification(var1, var3.isMale())
      METABOLICRATE -> getMetabolicRateClassification(
        var1,
        var3.getAthleticState(),
        var3.getHeightInCm(),
        var3.getAge(),
        var3.isMale()
      )
      else -> 0
    }
  }

  private fun getMetabolicRateClassification(var0: Double, var2: Int, var3: Int, var4: Int, var5: Boolean): Int {
    val var6: Double
    var6 = if (var2 != 1) {
      if (var2 != 2) {
        if (var2 != 3) {
          1.75
        } else {
          1.5
        }
      } else {
        1.3
      }
    } else {
      1.2
    }
    val var8: Int
    val var9: Short
    if (var5) {
      var8 = var4 + 200
      var9 = 5
    } else {
      var8 = var4 + 190
      var9 = -161
    }
    return getClassification(
      var0 / (var6 * ((var3 * var3 / 10000 * var8).toDouble() + var3.toDouble() * 6.25 - (var4 * 5).toDouble() + var9.toDouble())),
      ClassificationCalculator { ix: Int -> METABOLIC_RATE_CLASSIFICATIONS[ix - 1] }
    )
  }

  private fun getMuscleClassification(var0: Double, var2: Boolean): Int {
    val var3: Byte
    var3 = if (var2) {
      40
    } else {
      30
    }
    return getClassification(var0 - var3.toDouble(), Q_4MB8O_DnH7l7bOEiJxWxUCGoY.INSTANCE)
  }

  private fun getMuscleForClassification(var0: Double, var2: Boolean): Double {
    val var3: Byte
    var3 = if (var2) {
      40
    } else {
      30
    }
    return var3.toDouble() + var0
  }

  private val optimalBmiValues: Pair<Double, Double>
    private get() = Pair(18.5, 25.0)

  fun getOptimalBodyFatValue(var0: Int, var1: Boolean): Pair<Double, Double> {
    return Pair(
      getBodyFatForClassification(0.0, var0, var1),
      getBodyFatForClassification(10.0, var0, var1)
    )
  }

  fun getOptimalBodyWaterValue(var0: Int, var1: Boolean): Pair<Double, Double> {
    return Pair(
      getBodyWaterForClassification(-2.0, var0, var1),
      getBodyWaterForClassification(4.0, var0, var1)
    )
  }

  fun getOptimalMuscleValue(var0: Boolean): Pair<Double, Double> {
    return Pair(
      getMuscleForClassification(-2.0, var0),
      getMuscleForClassification(4.0, var0)
    )
  }

  fun getOptimalValuesForType(
    var0: MeasureType,
    var1: Int,
    var2: Int,
    var3: Boolean
  ): Pair<Double, Double> {
    val var4: Int = SyntheticClass_1.`$SwitchMap$de$soehnle$connect$logic$models$enums$MeasureType`.get(var0.ordinal())
    return if (var4 != 1) {
      if (var4 != 2) {
        if (var4 != 3) {
          if (var4 != 4) {
            if (var4 != 5) Pair(0.0, 0.0) else getOptimalMuscleValue(var3)
          } else {
            getOptimalBodyWaterValue(var1, var3)
          }
        } else {
          getOptimalBodyFatValue(var1, var3)
        }
      } else {
        optimalBmiValues
      }
    } else {
      getOptimalWeightValues(var2)
    }
  }

  fun getOptimalWeightValues(var0: Int): Pair<Double, Double> {
    return Pair(
      getWeightForClassification(18.5, var0),
      getWeightForClassification(25.1, var0)
    )
  }

  private fun getWaterClassification(var0: Double, var2: Int, var3: Boolean): Int {
    var var0 = var0
    val var4: Double
    if (var3) {
      var0 = var0 - (var2 * var2).toDouble() * 8.0E-4 + var2.toDouble() * 0.1715
      var4 = 63.484
    } else {
      var0 = var0 - (var2 * var2).toDouble() * 0.004 + var2.toDouble() * 0.4621
      var4 = 61.614
    }
    return getClassification(var0 - var4, { WATER_CLASSIFICATIONS[it - 1] })
  }

  private fun getWeightClassification(var0: Double, var2: Int): Int {
    return getBmiClassification(computeBodyMassIndex(var0, var2))
  }

  private fun getWeightForClassification(var0: Double, var2: Int): Double {
    return var0 * (var2 * var2).toDouble() / 10000.0
  }

  private interface ClassificationCalculator {
    fun getNextClassifier(ix: Int): Double
  }
}

 */
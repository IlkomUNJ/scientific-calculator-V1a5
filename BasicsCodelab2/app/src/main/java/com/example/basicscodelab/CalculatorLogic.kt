package com.example.basicscodelab

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.*

data class CalculatorState(
    val expression: String = "0",
    val result: String? = null,
    val isResultDisplayed: Boolean = false
)

class CalculatorViewModel {
    var state by mutableStateOf(CalculatorState())
        private set

    fun onButtonClick(buttonText: String) {
        when (buttonText) {
            in "0".."9", "." -> handleNumberInput(buttonText)
            in listOf("+", "-", "x", "/", "xʸ") -> handleOperator(buttonText)
            "=" -> performCalculation()
            "AC" -> clearAll()
            "DEL" -> deleteLast()
            "x!" -> handleFactorialInput()
            "log", "ln" -> handleLogFunctionInput(buttonText)

            "sin", "cos", "tan", "sin⁻¹", "cos⁻¹", "tan⁻¹" ->
                handleTrigonometricFunctionInput(buttonText)

            "√x", "e", "¹/x" -> handleScientificFunction(buttonText)
        }
    }

    private fun handleNumberInput(input: String) {
        if (state.isResultDisplayed) {
            state = state.copy(
                expression = input,
                result = null,
                isResultDisplayed = false
            )
            return
        }

        val currentExp = state.expression
        val lastPart = currentExp.split('+', '-', 'x', '/', '^').last()

        if (lastPart.endsWith("!")) {
            state = state.copy(expression = currentExp + "x" + input)
            return
        }

        val isStartingZero = currentExp == "0"

        if (input == ".") {
            if (!lastPart.contains(".")) {
                val newExp = if (lastPart.isEmpty() || currentExp.endsWith('+') || currentExp.endsWith('-') || currentExp.endsWith('x') || currentExp.endsWith('/') || currentExp.endsWith('^')) {
                    currentExp + "0."
                } else {
                    currentExp + "."
                }
                state = state.copy(expression = newExp)
            }
        } else {
            val newExp = if (isStartingZero && input != "0") {
                input
            } else if (isStartingZero && input == "0") {
                "0"
            } else {
                currentExp + input
            }
            state = state.copy(expression = newExp)
        }
    }

    private fun handleOperator(operator: String) {
        val actualOperator = if (operator == "xʸ") "^" else operator

        if (state.isResultDisplayed) {
            state = state.copy(
                expression = state.result!! + actualOperator,
                result = null,
                isResultDisplayed = false
            )
            return
        }

        val currentExp = state.expression
        if (currentExp.isNotEmpty() && (currentExp.last().toString() in listOf("+", "-", "x", "/", "^"))) {
            state = state.copy(expression = currentExp.dropLast(1) + actualOperator)
        } else {
            state = state.copy(expression = currentExp + actualOperator)
        }
    }

    private fun performCalculation() {
        if (state.isResultDisplayed) return

        var calculationExpression = state.expression

        while (calculationExpression.isNotEmpty() &&
            (calculationExpression.last().toString() in listOf("+", "-", "x", "/", "^", "(", "."))) {
            calculationExpression = calculationExpression.dropLast(1)
        }

        val openParentheses = calculationExpression.count { it == '(' }
        val closeParentheses = calculationExpression.count { it == ')' }

        repeat(openParentheses - closeParentheses) {
            calculationExpression += ")"
        }

        if (calculationExpression != state.expression) {
            state = state.copy(expression = calculationExpression)
        }

        calculationExpression = processTrigonometry(calculationExpression)
        if (calculationExpression.contains("Error")) { state = state.copy(result = "Error", isResultDisplayed = true); return }

        calculationExpression = processLogarithms(calculationExpression)
        if (calculationExpression.contains("Error")) { state = state.copy(result = "Error", isResultDisplayed = true); return }

        calculationExpression = processFactorials(calculationExpression)
        if (calculationExpression.contains("Error")) { state = state.copy(result = "Error", isResultDisplayed = true); return }

        calculationExpression = evaluatePower(calculationExpression)
        if (calculationExpression.contains("Error")) { state = state.copy(result = "Error", isResultDisplayed = true); return }

        calculationExpression = calculationExpression.replace('x', '*')

        try {
            val tokens = splitExpression(calculationExpression)

            if (tokens.isEmpty()) {
                state = state.copy(result = formatNumber(0.0), isResultDisplayed = true)
                return
            }

            if (tokens.size == 1) {
                val finalResult = tokens.first().toDouble()
                state = state.copy(result = formatNumber(finalResult), isResultDisplayed = true)
                return
            }

            var result = tokens.first().toDouble()
            var i = 1
            while (i < tokens.size - 1) {
                val operator = tokens[i]
                val nextValue = tokens[i+1].toDouble()
                result = calculate(result, operator, nextValue)
                i += 2
            }

            state = state.copy(result = formatNumber(result), isResultDisplayed = true)
        } catch (e: Exception) {
            state = state.copy(result = "Error", isResultDisplayed = true)
        }
    }

    private fun evaluatePower(expression: String): String {
        var tempExp = expression
        val powerRegex = "([+\\-]?[0-9]*\\.?[0-9]+)\\^([+\\-]?[0-9]*\\.?[0-9]+)".toRegex()

        while (true) {
            val match = powerRegex.find(tempExp)
            if (match == null) break

            val baseStr = match.groupValues[1]
            val exponentStr = match.groupValues[2]

            val base = baseStr.toDoubleOrNull()
            val exponent = exponentStr.toDoubleOrNull()

            if (base == null || exponent == null) return "Error"

            val result = base.pow(exponent)

            if (result.isNaN() || result.isInfinite()) return "Error"

            tempExp = tempExp.replaceRange(match.range, formatNumber(result))
        }

        return tempExp
    }

    private fun processFactorials(expression: String): String {
        val factorialRegex = "(\\d+)!".toRegex()
        return expression.replace(factorialRegex) { matchResult ->
            val numberStr = matchResult.groupValues[1]
            val number = numberStr.toIntOrNull()
            if (number != null && number >= 0) {
                formatNumber(factorial(number))
            } else {
                matchResult.value
            }
        }
    }

    private fun processLogarithms(expression: String): String {
        var tempExp = expression
        var changed = true

        while (changed && !tempExp.contains("Error")) {
            changed = false
            val originalExp = tempExp

            val lnRegex = "ln\\(([+\\-]?[0-9]*\\.?[0-9]+)\\)".toRegex()
            tempExp = tempExp.replace(lnRegex) { matchResult ->
                changed = true
                val numberStr = matchResult.groupValues[1]
                val number = numberStr.toDoubleOrNull()
                if (number != null && number > 0) formatNumber(ln(number)) else "Error"
            }

            val log10Regex = "log\\(([+\\-]?[0-9]*\\.?[0-9]+)\\)".toRegex()
            tempExp = tempExp.replace(log10Regex) { matchResult ->
                changed = true
                val numberStr = matchResult.groupValues[1]
                val number = numberStr.toDoubleOrNull()
                if (number != null && number > 0) formatNumber(log10(number)) else "Error"
            }

            if (tempExp == originalExp) changed = false
        }

        return tempExp
    }

    private fun processTrigonometry(expression: String): String {
        var tempExp = expression
        var changed = true

        while (changed && !tempExp.contains("Error")) {
            changed = false
            val originalExp = tempExp

            val trigRegex = "(sin|cos|tan|sin⁻¹|cos⁻¹|tan⁻¹)\\(([+\\-]?[0-9]*\\.?[0-9]+)\\)".toRegex()

            tempExp = tempExp.replace(trigRegex) { matchResult ->
                changed = true
                val function = matchResult.groupValues[1]
                val numberStr = matchResult.groupValues[2]
                val number = numberStr.toDoubleOrNull()

                if (number == null) return@replace "Error"

                val result = when (function) {
                    "sin" -> sin(number.toRadians())
                    "cos" -> cos(number.toRadians())
                    "tan" -> tan(number.toRadians())
                    "sin⁻¹" -> if (number in -1.0..1.0) asin(number).toDegrees() else Double.NaN
                    "cos⁻¹" -> if (number in -1.0..1.0) acos(number).toDegrees() else Double.NaN
                    "tan⁻¹" -> atan(number).toDegrees()
                    else -> number
                }

                if (result.isNaN() || result.isInfinite()) "Error" else formatNumber(result)
            }

            if (tempExp == originalExp) changed = false
        }

        return tempExp
    }

    private fun handleScientificFunction(function: String) {
        val currentExp = state.expression

        if (function == "e") {
            val eValueStr = formatNumber(kotlin.math.E)
            val newExpression = if (state.isResultDisplayed || currentExp == "0") {
                eValueStr
            } else {
                val lastChar = currentExp.lastOrNull()
                if (lastChar?.isDigit() == true || lastChar == '.' || lastChar == ')') {
                    currentExp + "x" + eValueStr
                } else {
                    currentExp + eValueStr
                }
            }
            state = state.copy(expression = newExpression, result = null, isResultDisplayed = false)
            return
        }

        // Logika untuk √x dan ¹/x
        val lastNumberStr = state.expression.split('+', '-', 'x', '/', '^').lastOrNull() ?: return
        val currentValue = lastNumberStr.toDoubleOrNull() ?: return

        val result = when (function) {
            "√x" -> sqrt(currentValue)
            "¹/x" -> if (currentValue != 0.0) 1.0 / currentValue else Double.NaN // <--- PERUBAHAN DI SINI
            else -> currentValue
        }

        if (result.isNaN() || result.isInfinite()) {
            state = state.copy(result = "Error", isResultDisplayed = true)
            return
        }

        val newExpression = state.expression.dropLast(lastNumberStr.length) + formatNumber(result)
        state = state.copy(expression = newExpression, result = null, isResultDisplayed = false)
    }

    private fun handleFactorialInput() {
        val currentExp = state.expression

        if (state.isResultDisplayed) {
            state = state.copy(expression = state.result + "!", result = null, isResultDisplayed = false)
            return
        }

        val lastPart = currentExp.split('+', '-', 'x', '/', '^').last()

        if (currentExp.isNotEmpty() && (currentExp.last().isDigit() || currentExp.last() == ')')) {
            if (!lastPart.contains("!")) {
                state = state.copy(expression = currentExp + "!", result = null, isResultDisplayed = false)
            }
        } else if (currentExp.isNotEmpty() && currentExp.last() == '!') {
            state = state.copy(expression = currentExp + "x" + lastPart, result = null, isResultDisplayed = false)
        }
    }

    private fun handleLogFunctionInput(function: String) {
        val currentExp = state.expression
        val functionString = "$function("

        if (state.isResultDisplayed || currentExp == "0") {
            state = state.copy(expression = functionString, result = null, isResultDisplayed = false)
            return
        }

        val lastChar = currentExp.lastOrNull()

        if (lastChar?.isDigit() == true || lastChar == '!' || lastChar == '.' || lastChar == ')') {
            state = state.copy(expression = currentExp + "x" + functionString)
        } else {
            state = state.copy(expression = currentExp + functionString)
        }
    }

    private fun handleTrigonometricFunctionInput(function: String) {
        val currentExp = state.expression
        val functionString = "$function("

        if (state.isResultDisplayed || currentExp == "0") {
            state = state.copy(expression = functionString, result = null, isResultDisplayed = false)
            return
        }

        val lastChar = currentExp.lastOrNull()

        if (lastChar?.isDigit() == true || lastChar == '!' || lastChar == '.' || lastChar == ')') {
            state = state.copy(expression = currentExp + "x" + functionString)
        } else {
            state = state.copy(expression = currentExp + functionString)
        }
    }

    private fun splitExpression(expression: String): List<String> {
        val regex = "([+\\-*/])".toRegex()
        val parts = regex.findAll(expression).toList()

        val tokens = mutableListOf<String>()
        var lastIndex = 0

        for (match in parts) {
            val op = match.value
            val number = expression.substring(lastIndex, match.range.first)

            if (number.isNotEmpty()) tokens.add(number)
            tokens.add(op)

            lastIndex = match.range.last + 1
        }

        val lastNumber = expression.substring(lastIndex)
        if (lastNumber.isNotEmpty()) tokens.add(lastNumber)

        return tokens
    }

    private fun calculate(operand1: Double, operator: String, operand2: Double): Double {
        return when (operator) {
            "+" -> operand1 + operand2
            "-" -> operand1 - operand2
            "*" -> operand1 * operand2
            "/" -> if (operand2 != 0.0) operand1 / operand2 else Double.NaN
            else -> operand2
        }
    }

    private fun clearAll() {
        state = CalculatorState()
    }

    private fun deleteLast() {
        if (state.isResultDisplayed) {
            state = CalculatorState()
            return
        }

        if (state.expression.length > 1) {
            state = state.copy(expression = state.expression.dropLast(1))
        } else {
            state = state.copy(expression = "0")
        }
    }

    fun getClearButtonText(): String {
        return if (state.expression != "0" && !state.isResultDisplayed) {
            "C"
        } else {
            "AC"
        }
    }

    private fun formatNumber(number: Double): String {
        return if (number.isNaN() || number.isInfinite()) "Error"
        else if (number % 1.0 == 0.0) {
            number.toLong().toString()
        } else {
            String.format("%.10f", number).trimEnd('0').trimEnd('.')
        }
    }

    private fun factorial(n: Int): Double {
        return if (n < 0) Double.NaN
        else if (n == 0) 1.0
        else (1..n).fold(1.0) { acc, i -> acc * i }
    }
}

fun Double.toRadians(): Double = this * PI / 180
fun Double.toDegrees(): Double = this * 180 / PI
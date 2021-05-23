import kotlin.math.round
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType
import kotlin.collections.List as List1

class Matrx() {
    init {
    }

    fun matr(data: String): String {
        val typeMessage = data.split(",", limit = 2)
        val cmd = typeMessage[1].split(",", limit = 2)
        val col = cmd[1].split(",", limit = 2)
        val row = col[1].split(",", limit = 2)
        val volume = row[1].split("|", limit = 2)
        val matrx = volume[1].split("/", limit = 2)
        val matrx1 = matrx[0].split(";")
        val matrx2 = matrx[1].split(";")
        var result = ""
        when (cmd[0]) {
            "SUM" -> {
                result = sum(matrx1, matrx2)
            }
            "PRD" -> {
                result = prd(matrx1, matrx2, col[0].toInt())
            }
            "NUM" -> {
                result = num(matrx1, volume[0].toDouble())
            }
        }
        result = data + "=" + result
        return result
    }

    // сложение матриц
    fun sum(matrx1: kotlin.collections.List<String>, matrx2: kotlin.collections.List<String>): String {
        var matrxOut = ""
        var i = 0
        matrx1.forEach {
            matrxOut += (round((it.toDouble() + matrx2[i].toDouble())) * 100.0 / 100.0).toString() + ";"
            i++
        }
        return matrxOut.substring(0, matrxOut.length - 1)
    }

    // умножение матриц
    fun prd(matrx1: kotlin.collections.List<String>, matrx2: kotlin.collections.List<String>, cols: Int): String {
        var matrxOut: String = ""
        val tmp: Array<Double> = Array(matrx1.size) { 0.0 }
        var rowsN = 0
        var k = 0
        for (j in 0..matrx1.size - 1) {
            for (i in 0..cols - 1) {
                tmp[j] = tmp[j] + matrx1[i + rowsN].toDouble() * matrx2[cols * i + k].toDouble()
            }
            k++
            if (k == cols) {
                rowsN = rowsN + cols
                k = 0
            }
        }
        tmp.forEach {
            matrxOut += (round(it * 100.0) / 100.0).toString() + ";"
        }
        return matrxOut.substring(0, matrxOut.length - 1)
    }

    // умножение матрицы на число
    fun num(matrx1: kotlin.collections.List<String>, volue: Double): String {
        var matrxOut = ""
        matrx1.forEach {
            matrxOut += (round((it.toDouble() * volue) * 100.0) / 100.0).toString() + ";"
        }
        return matrxOut.substring(0, matrxOut.length - 1)
    }
}
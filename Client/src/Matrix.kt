import kotlin.math.round

class Matrix {

    fun matrix(data: String): String {
        val typeMessage = data.split(",", limit = 2)
        val cmd = typeMessage[1].split(",", limit = 2)
        val col = cmd[1].split(",", limit = 2)
        val row = col[1].split(",", limit = 2)
        val volume = row[1].split("|", limit = 2)
        val matrix = volume[1].split("/", limit = 2)
        val matrix1 = matrix[0].split(";")
        val matrix2 = matrix[1].split(";")
        var result = ""
        when (cmd[0]) {
            "SUM" -> {
                result = sum(matrix1, matrix2)
            }
            "PRD" -> {
                result = prd(matrix1, matrix2, col[0].toInt())
            }
            "NUM" -> {
                result = num(matrix1, volume[0].toDouble())
            }
        }
        result = "$data=$result"
        return result
    }

    // сложение матриц
    private fun sum(matrix1: List<String>, matrix2: List<String>): String {
        var matrixOut = ""
        var i = 0
        matrix1.forEach {
            matrixOut += (round((it.toDouble() + matrix2[i].toDouble())) * 100.0 / 100.0).toString() + ";"
            i++
        }
        return matrixOut.substring(0, matrixOut.length - 1)
    }

    // умножение матриц
    private fun prd(matrix1: List<String>, matrix2: List<String>, cols: Int): String {
        var matrixOut = ""
        val tmp: Array<Double> = Array(matrix1.size) { 0.0 }
        var rowsN = 0
        var k = 0
        for (j in matrix1.indices) {
            for (i in 0 until cols) {
                tmp[j] = tmp[j] + matrix1[i + rowsN].toDouble() * matrix2[cols * i + k].toDouble()
            }
            k++
            if (k == cols) {
                rowsN += cols
                k = 0
            }
        }
        tmp.forEach {
            matrixOut += (round(it * 100.0) / 100.0).toString() + ";"
        }
        return matrixOut.substring(0, matrixOut.length - 1)
    }

    // умножение матрицы на число
    private fun num(matrix1: List<String>, volume: Double): String {
        var matrixOut = ""
        matrix1.forEach {
            matrixOut += (round((it.toDouble() * volume) * 100.0) / 100.0).toString() + ";"
        }
        return matrixOut.substring(0, matrixOut.length - 1)
    }
}
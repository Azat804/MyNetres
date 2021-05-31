import java.util.*
import kotlinx.coroutines.*

suspend fun main() {
    val dbh = DBHelper(dbName = "Matrix")
    dbh.connect()
    dbh.createDatabase()
    var cols: Int
    var rows: Int
    var numMatrix: Int
    var multiplier: Double
    var id: Int
    var idMatrix: Int
    var portion1: String
    var portion2: String
    val s = Server()
    s.addMessageListener { data -> dbh.insertTablesResult(runBlocking {s.dataProcessing(data)})}
    s.start()
    var cmd: String
    var data = ""
    val sc = Scanner(System.`in`)
    do {
        var sourceMatrix = ""
        println("Команды операций над матрицами (SUM  - суммирование, PRD  - произведение, NUM - умножение на число), STOP - остановка сервера.\nВведите команду:")
        cmd = sc.nextLine()
        do {
            println("Количество подключенных клиентов=" + s.clients.size)
            println("Введите номер клиента 1 - inf или 0 для всех:")
            id = sc.nextLine().toInt()
            if (id > s.clients.size)
                println("Превышение значения подкюченных клентов, введите значение меньше " + s.clients.size)
        } while (id > s.clients.size)
        if (cmd == "SUM" || cmd == "PRD" || cmd == "NUM") {
            dbh.connect()
            dbh.dropTablesResult()
            multiplier = 0.0
            println("Введите количество столбцов:")
            cols = sc.nextLine().toInt()
            println("Введите количество строк:")
            rows = sc.nextLine().toInt()
            if (cmd == "NUM") {
                numMatrix = 1
                println("Введите множитель:")
                multiplier = sc.nextLine().toDouble()
            } else {
                numMatrix = 2
            }
            dbh.createTablesResult(cols)
            for (i in 1..numMatrix) {
                println("Введите номер $i матрицы:")
                idMatrix = sc.nextLine().toInt()
                sourceMatrix += if (i > 1) {
                    "/" + dbh.readingData(rows, cols, idMatrix)
                } else {
                    dbh.readingData(rows, cols, idMatrix)
                }
            }
            if (numMatrix == 1) sourceMatrix += "/"
            println("Исходная матрица: $sourceMatrix")
            val matrix = sourceMatrix.split("/", limit = 2)
            val matrix1Portion = matrix[0].split(";")
            val matrix2Portion = matrix[1].split(";")
            val rws = 1 //задает кол-во строк в порции
            for (i in (0 until rows)) {
                portion1 = ""
                portion2 = ""
                for (j in i * cols until (i * cols + cols)) {
                    portion1 += matrix1Portion[j] + ";"
                    if (cmd != "NUM" && cmd != "PRD") {
                        portion2 += matrix2Portion[j] + ";"
                    }
                }
                if (cmd == "SUM") {
                    data = ("matrices,$cmd,$cols,$rws,$multiplier|" + portion1.substring(
                        0, portion1.length - 1
                    ) + "/" + portion2.substring(0, portion2.length - 1))
                }
                if (cmd == "NUM") {
                    data = ("matrices,$cmd,$cols,$rws,$multiplier|" + portion1.substring(
                        0,
                        portion1.length - 1
                    )) + "/"
                }
                if (cmd == "PRD") {
                    data = ("matrices,$cmd,$cols,$rws,$multiplier|" + portion1.substring(
                        0, portion1.length - 1
                    ) + "/" + matrix[1])
                }
                println("Отправлено на расчет порция клиенту: [$id] $data")
                do {
                    if (s.startPortion && data.trim().isNotEmpty()) {
                        s.clearList()
                        if (id == 0) {
                            s.senData(data) //отправка всем клиентам
                        } else {
                            s.send(id, data) //адресная отправка
                        }
                        data = ""
                    }
                    delay(1000) //задержка, чтобы поймать момент переключения флажка startportion
                } while (!s.startPortion)
            }
        } else { // для простого сообщения
            if (id == 0) s.senData(cmd)
            else s.send(id, cmd)
        }
        dbh.disconnect()
    } while (cmd != "STOP")
    s.stop()
}
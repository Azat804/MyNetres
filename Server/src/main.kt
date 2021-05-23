import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import kotlin.concurrent.thread

fun main() {
    val dbh = DBHelper(dbName = "Matrix")
    dbh.createDatabase()
    var cols = 0
    var rows = 0
    var rowStart = 0
    var colStart = 0
    var numMatr: Int // кол-во матриц
    var mult: Double // множитель
    var id = 0
    var id_matr = 1
    var portion1: String
    var portion2: String
    val s = Server()
    s.addMessageListener { data -> dbh.output(s.dataProcessing(data)) }
    s.start()
    var cmd: String
    var data = ""
    val sc = Scanner(System.`in`)
    do {
        var matr = ""
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
            mult = 0.0
            println("Введите размеры матриц:")
            println("Введите количество столбцов:")
            cols = sc.nextLine().toInt()
            println("Введите количество строк:")
            rows = sc.nextLine().toInt()
            if (cmd == "NUM") {
                numMatr = 1
                println("Введите множитель:")
                mult = sc.nextLine().toDouble()
            } else {
                numMatr = 2
            }
            dbh.createTablesOutput(cols)
            for (i in 1..numMatr) {
                println("Введите номер $i матрицы:")
                id_matr = sc.nextLine().toInt()
                println("Введите начальные точки матрицы:")
                println("Номер строки $i матрицы:")
                rowStart = sc.nextLine().toInt()
                println("Номер столбца $i матрицы:")
                colStart = sc.nextLine().toInt()
                if (i > 1)
                    matr = matr + "/" + dbh.result(rows, cols, rowStart, colStart, id_matr)
                else
                    matr = matr + dbh.result(rows, cols, rowStart, colStart, id_matr)
            }
            if (numMatr == 1) matr = matr + "/"
            println("Исходная матрица: " + matr)
            val matrx = matr.split("/", limit = 2)
            val matrx1Portion = matrx[0].split(";")
            val matrx2Portion = matrx[1].split(";")
            var k = 0
            val rws = 1 //задает кол-во строк в порции
            for (i in (1..rows)) {
                portion1 = ""
                portion2 = ""
                for (j in k * cols..(k * cols + cols) - 1) {
                    portion1 += matrx1Portion[j] + ";"
                    if (cmd != "NUM" && cmd != "PRD") {
                        portion2 += matrx2Portion[j] + ";"
                    }
                }
                k++
                if (cmd == "SUM") {
                    data = ("matrices," + cmd + "," + cols + "," + rws + "," + mult + "|" + portion1.substring(
                        0, portion1.length - 1
                    ) + "/" + portion2.substring(0, portion2.length - 1))
                }
                if (cmd == "NUM") {
                    data = ("matrices," + cmd + "," + cols + "," + rws + "," + mult + "|" + portion1.substring(
                        0,
                        portion1.length - 1
                    )) + "/"
                }
                if (cmd == "PRD") {
                    data = ("matrices," + cmd + "," + cols + "," + rws + "," + mult + "|" + portion1.substring(
                        0, portion1.length - 1
                    ) + "/" + matrx[1])
                }
                println("Отправлено на расчет порция клиенту: [$id] " + data)

                do {
                    if (s.poritionStart()) {
                        s.clearList()
                        if (id == 0) {
                            s.senData(data)
                        } else {
                            s.send(id, data)
                        }
                        Thread.sleep(3000)
                    }
                } while (!s.poritionStart())
            }
        } else { // для простого сообщения
            if (id == 0) s.senData(cmd)
            else s.send(id, cmd)
        }
        dbh.disconnect()
    } while (cmd != "STOP")
    s.stop()
}
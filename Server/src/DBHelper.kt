import java.math.BigDecimal
import java.sql.*
import java.math.RoundingMode
import java.util.*
import kotlin.math.round

class DBHelper(
    val dbName: String,
    val host: String = "localhost", val port: Int = 3306, val user: String = "root", val password: String = "root"
) {
    private var stmt: Statement? = null

    fun connect() {
        stmt?.run {
            if (!isClosed) close()
        }
        var rep = 0
        do {
            try {
                stmt =
                    DriverManager.getConnection("jdbc:mysql://$host:$port/$dbName?serverTimezone=UTC", user, password)
                        .createStatement()
            } catch (e: SQLSyntaxErrorException) {
                val tstmt =
                    DriverManager.getConnection("jdbc:mysql://$host:$port/?serverTimezone=UTC", user, password)
                        .createStatement()
                tstmt.execute("CREATE SCHEMA `$dbName`")
                tstmt.closeOnCompletion()
                rep++
            }
        } while (stmt == null && rep < 2)
    }

    fun disconnect() {
        stmt?.close()
    }

    fun createDatabase() {
        dropTables()
        //       createTables()
        createinput2table("2", "2")
        //       fillTables()
    }

    private fun dropTables() {
        stmt?.run {
            //         addBatch("DROP TABLE IF EXISTS `Input`")
            addBatch("DROP TABLE IF EXISTS `Input2`")
            addBatch("DROP TABLE IF EXISTS `Result`")
            executeBatch()
        }
    }

    fun dropTablesResult() {
        stmt?.run {
            addBatch("DROP TABLE IF EXISTS `Result`")
            executeBatch()
        }
    }

    /*   private fun createTables() {
           stmt?.run {
               addBatch("START TRANSACTION;")
               var j = 0
               addBatch("CREATE TABLE IF NOT EXISTS `matrix`.`Input` ( `ID` INT(40) NOT NULL AUTO_INCREMENT , `1` VARCHAR(40)  NULL , PRIMARY KEY (`ID`)) ENGINE = InnoDB;")
               for (i in 2..100) {
                   j = i - 1
                   addBatch("ALTER TABLE `Input` ADD `$i` VARCHAR(40)   NULL  AFTER `$j`;")
               }
               executeBatch()
           }
       }

       private fun fillTables() {
           var tmp = ""
           var polya = ""
           val random = Random()
           var rand: BigDecimal
           stmt?.run {
               addBatch("delete from `Input`")
               for (i in 1..100) {
                   polya += "`$i`" + ","
               }
               polya = polya.substring(0, polya.length - 1)
               for (k in (1..100)) {
                   tmp = ""
                   for (i in 1..100) {
                       rand = BigDecimal(random.nextDouble() * 2000 - 1000).setScale(2, RoundingMode.HALF_EVEN)
                       tmp += "'" + "$rand" + "'" + ","
                   }
                   tmp = tmp.substring(0, tmp.length - 1)
                   addBatch("INSERT INTO `Input` ($polya) VALUES ($tmp)")
               }
               executeBatch()
           }
       }
   */
    fun createTablesOutput(cols: Int) {
        var polya = "`1`"
        var j = 0
        stmt?.run {
            addBatch("START TRANSACTION;")
            addBatch("CREATE TABLE IF NOT EXISTS `matrix`.`Result` ( `ID` INT(40) NOT NULL AUTO_INCREMENT , `1` VARCHAR(40) NOT NULL , PRIMARY KEY (`ID`)) ENGINE = InnoDB;")
            for (i in (2..cols)) {
                polya = polya + ",`$i`"
                j = i - 1
                addBatch("ALTER TABLE `Result` ADD `$i` VARCHAR(40)  NOT NULL  AFTER `$j`;")
            }
            executeBatch()
        }
    }

    fun result(lines: Int, columns: Int, lineStart: Int, columnStart: Int, id_matr: Int): String {
        var coldbmatr = "1"
        var rowdbmatr = "1"
        var maxiddbmatr = ""
        var tmp = id_matr
        val sqlmaxiddbmatr = "SELECT MAX(`ID_Matrix`) FROM `input2`"
        val rsmaxidmatr = stmt?.executeQuery(sqlmaxiddbmatr)
        while (rsmaxidmatr?.next() == true) {
            maxiddbmatr = rsmaxidmatr.getString(1)
        }
        if (id_matr <= maxiddbmatr.toInt()) {
            val sqlcoldbmatr = "SELECT MAX(`col`) FROM `input2` WHERE `ID_Matrix`=$id_matr"
            val rscol = stmt?.executeQuery(sqlcoldbmatr)
            while (rscol?.next() == true) {
                coldbmatr = rscol.getString(1)
            }
            val sqlrowdbmatr = "SELECT MAX(`row`) FROM `input2` WHERE `ID_Matrix`=$id_matr"
            val rsrow = stmt?.executeQuery(sqlrowdbmatr)
            while (rsrow?.next() == true) {
                rowdbmatr = rsrow.getString(1)
            }
        }
        if (coldbmatr.toInt() < columns || rowdbmatr.toInt() < lines || id_matr > maxiddbmatr.toInt()) {
            tmp = maxiddbmatr.toInt() + 1
            fillinput2table(lines, columns, tmp)
        }
        var matr = ""
        val sql = "SELECT * FROM `Input2` WHERE ID_Matrix=$tmp"
        val rs = stmt?.executeQuery(sql)
        while (rs?.next() == true) {
            matr = matr + rs.getString(4) + ";"
        }
        return matr.substring(0, matr.length - 1)
    }

    fun output(data: String) {
        val record = data.split(":", limit = 2)
        if (record[0] == "YES") {
            val cols = record[1].split(",", limit = 2)
            val rows = cols[1].split(",", limit = 2)
            val stroka = rows[1].split(";")
            var polya = "`1`"
            var tmp = ""
            var j = 0
            stmt?.run {
                for (i in (2..cols[0].toInt())) {
                    polya = polya + ",`$i`"
                    j = i - 1
                }
                var k = 0
                for (i in (1..rows[0].toInt())) {
                    tmp = ""
                    for (j in k * cols[0].toInt()..(k * cols[0].toInt() + cols[0].toInt()) - 1) {
                        tmp += "'" + stroka[j] + "'" + ","
                    }
                    k++
                    tmp = tmp.substring(0, tmp.length - 1)
                    addBatch("INSERT INTO `Result` ($polya) VALUES ($tmp)")
                }
                executeBatch()
            }
        }
    }

    fun createinput2table(n: String, m: String) {
        val random = Random()
        var rand: BigDecimal
        stmt?.run {
            addBatch("START TRANSACTION;")
            addBatch("CREATE TABLE IF NOT EXISTS `matrix`.`Input2` ( `ID_Matrix` INT(40) NOT NULL , `row` INT(40)  NOT NULL , `col` INT(40) NOT NULL, `element` DOUBLE NOT NULL, PRIMARY KEY (`ID_Matrix`,`row`,`col`)) ENGINE = InnoDB;")
            for (id in 1..3) {
                for (i in 1..n.toInt()) {
                    for (j in 1..m.toInt()) {
                        rand = BigDecimal(random.nextDouble() * 2000 - 1000).setScale(2, RoundingMode.HALF_EVEN)
                        addBatch("INSERT INTO `Input2` (`ID_Matrix`, `row`,`col`,`element`) VALUES ('$id','$i','$j','$rand')")
                    }
                }
            }
            executeBatch()
        }
    }

    fun fillinput2table(lines: Int, columns: Int, maxiddbmatr: Int) {
        val random = Random()
        var rand: BigDecimal
        stmt?.run {
            addBatch("START TRANSACTION;")
            for (i in 1..lines) {
                for (j in 1..columns) {
                    rand = BigDecimal(random.nextDouble() * 2000 - 1000).setScale(2, RoundingMode.HALF_EVEN)
                    addBatch("INSERT INTO `Input2` (`ID_Matrix`, `row`,`col`,`element`) VALUES ('$maxiddbmatr','$i','$j','$rand')")
                    executeBatch()
                }
            }
        }

    }
}
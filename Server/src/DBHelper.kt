import java.math.BigDecimal
import java.sql.*
import java.math.RoundingMode
import java.util.*

class DBHelper(
    val dbName: String,
    private val host: String = "localhost",
    private val port: Int = 3306,
    private val user: String = "root",
    private val password: String = "root"
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
                val tStmt =
                    DriverManager.getConnection("jdbc:mysql://$host:$port/?serverTimezone=UTC", user, password)
                        .createStatement()
                tStmt.execute("CREATE SCHEMA `$dbName`")
                tStmt.closeOnCompletion()
                rep++
            }
        } while (stmt == null && rep < 2)
    }

    fun disconnect() {
        stmt?.close()
    }

    fun createDatabase() {
        dropTables()
        //    createTables()
        createTableInput2()
        //     fillTables()
    }

    private fun dropTables() {
        stmt?.run {
            //   addBatch("DROP TABLE IF EXISTS `Input`")
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

    /*  private fun createTables() {
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

    fun readingData(lines: Int, columns: Int, idMatrix: Int): String { // считывание данных из таблицы input2
        var colDbMatrix = "1"
        var rowDbMatrix = "1"
        var maxIdDbMatrix = ""
        var tmp = idMatrix
        val rsMaxIdMatrix = stmt?.executeQuery("SELECT MAX(`ID_Matrix`) FROM `input2`")
        while (rsMaxIdMatrix?.next() == true) {
            maxIdDbMatrix = rsMaxIdMatrix.getString(1)
        }
        if (idMatrix <= maxIdDbMatrix.toInt()) {
            val rsCol = stmt?.executeQuery("SELECT MAX(`col`) FROM `input2` WHERE `ID_Matrix`=$idMatrix")
            while (rsCol?.next() == true) {
                colDbMatrix = rsCol.getString(1)
            }
            val rsRow = stmt?.executeQuery("SELECT MAX(`row`) FROM `input2` WHERE `ID_Matrix`=$idMatrix")
            while (rsRow?.next() == true) {
                rowDbMatrix = rsRow.getString(1)
            }
        }
        if (colDbMatrix.toInt() < columns || rowDbMatrix.toInt() < lines || idMatrix > maxIdDbMatrix.toInt()) {
            tmp = maxIdDbMatrix.toInt() + 1
            fillTablesInput2(lines, columns, tmp)
        }
        var matrix = ""
        val sql="SELECT * FROM `Input2` WHERE `ID_Matrix`=$tmp AND `col`<=$columns AND `row`<=$lines"
        val rs = stmt?.executeQuery(sql)
        while (rs?.next() == true) {
            matrix = matrix + rs.getString(4) + ";"
        }
        return matrix.substring(0, matrix.length - 1)
    }

    fun createTablesResult(cols: Int) {
        var polya = "`1`"
        var j: Int
        stmt?.run {
            addBatch("START TRANSACTION;")
            addBatch("CREATE TABLE IF NOT EXISTS `matrix`.`Result` ( `ID` INT(40) NOT NULL AUTO_INCREMENT , `1` VARCHAR(40) NOT NULL , PRIMARY KEY (`ID`)) ENGINE = InnoDB;")
            for (i in (2..cols)) {
                polya += ",`$i`"
                j = i - 1
                addBatch("ALTER TABLE `Result` ADD `$i` VARCHAR(40)  NOT NULL  AFTER `$j`;")
            }
            executeBatch()
        }
    }

   fun insertTablesResult(data: String) { //
        val record = data.split(":", limit = 2)
        if (record[0] == "YES") {//чтобы не пропустить простое сообщение на запись в бд
            val cols = record[1].split(",", limit = 2)
            val rows = cols[1].split(",", limit = 2)
            val line = rows[1].split(";")
            var polya = "`1`"
            var tmp: String
            stmt?.run {
                for (i in (2..cols[0].toInt())) {
                    polya += ",`$i`"
                }
                for (i in (0..rows[0].toInt()) - 1) {
                    tmp = ""
                    for (j in i * cols[0].toInt() until (i * cols[0].toInt() + cols[0].toInt())) {
                        tmp += "'" + line[j] + "'" + ","
                    }
                    tmp = tmp.substring(0, tmp.length - 1)
                    addBatch("INSERT INTO `Result` ($polya) VALUES ($tmp)")
                }
                executeBatch()
            }
        }
    }

    private fun createTableInput2() {
        val n = "1"
        val m = "1"
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

    private fun fillTablesInput2(lines: Int, columns: Int, maxIdDbMatrix: Int) {
        val random = Random()
        var rand: BigDecimal
        stmt?.run {
            for (i in 1..lines) {
                for (j in 1..columns) {
                    rand = BigDecimal(random.nextDouble() * 2000 - 1000).setScale(2, RoundingMode.HALF_EVEN)
                    addBatch("INSERT INTO `Input2` (`ID_Matrix`, `row`,`col`,`element`) VALUES ('$maxIdDbMatrix','$i','$j','$rand')")
                    executeBatch()
                }
            }
        }
    }
}
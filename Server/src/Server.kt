import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread
import kotlinx.coroutines.*

class Server(port: Int = 5804) {
    private val sSocket: ServerSocket
    val clients = mutableListOf<Client>()
    private var stop = false
    private var stp = true
    private var results = ""
    private var recalculate = false
    private var rec = false
    private val sendList = mutableListOf<String>()
    private val sendListReceived = mutableListOf<String>()
    private var recalculatedClients = 1
    var startPortion = true
    private val messageListeners = mutableListOf<(String) -> Unit>()
    fun addMessageListener(l: (String) -> Unit) {
        messageListeners.add(l)
    }

    private val communicator: SocketIO

    inner class Client(private val socket: Socket) {
        private var sio: SocketIO? = null
        private val id: Int = clients.size + 1
        fun startDialog() {
            sio = SocketIO(socket).apply {
                addSocketClosedListener {
                    clients.remove(this@Client)
                }
                addMessageListener { data ->
                    messageListeners.forEach { l -> l("[$id] $data") }
                    clients.forEach { client ->
                        if (client != this@Client) client.send("[$id] $data")
                    }
                }
                startDataReceiving()
            }
        }

        fun stop() {
            sio?.stop()
        }

        fun send(data: String) {
            sio?.sendData(data)
        }

        fun sendData(cmd: String) {
            sio?.sendData(cmd)
        }
    }

    init {
        sSocket = ServerSocket(port)
        communicator = SocketIO(Socket())
    }

    fun stop() {
        sSocket.close()
        stop = true
    }

    fun start() = CoroutineScope(Dispatchers.Default).launch {
        stop = false
        try {
            while (!stop) {
                clients.add(
                    Client(
                        sSocket.accept()
                    ).also { client -> client.startDialog() })
            }
        } catch (e: Exception) {
            println("${e.message}")
        } finally {
            stopAllClients()
            sSocket.close()
            println("Сервер остановлен.")
        }
    }

    /*  fun start() {
          stop = false
          thread {
              try {
                  while (!stop) {
                      clients.add(
                          Client(
                              sSocket.accept()
                          ).also { client -> client.startDialog() })
                  }
              } catch (e: Exception) {
                  println("${e.message}")
              } finally {
                  stopAllClients()
                  sSocket.close()
                  println("Сервер остановлен.")
              }
          }
      }
  */

    private fun stopAllClients() {
        clients.forEach { client -> client.stop() }
    }

    fun send(i: Int, data: String) {
        sendList.add(i.toString())
        clients[i - 1].send(data)
    }

    suspend fun senData(cmd: String) {
        var i = 1
        clients.forEach { client ->
            delay(500)
            client.sendData(cmd)
            sendList.add(i.toString())
            i++
        }
    }

    suspend fun dataProcessing(data: String): String {
        println("Получено: $data")
        val v = data.split("[", limit = 2)
        val idClient = v[1].split("]", limit = 2)
        val typeMessage = idClient[1].split(",", limit = 2)
        if (typeMessage[0].trim() == "matrices") {
            val dataForRecord: String
            val dataResult = idClient[1].split("=", limit = 2)
            delay(1000)
            sendListReceived.add(idClient[0])
            rec = false
            stp = true //доп.защита для того, чтобы не произошла одновременная запись в базу
            if (results.trim().isEmpty()) {
               // if (idClient[0].toInt() == 1) { //имитация ошибки, присваиваем первому клиенту ошибку
               //     results = dataResult[1] + "1"
             //   } else {
                    results = dataResult[1]
            //    }
            } else {
                if (results.trim() != dataResult[1]) {
                    recalculate = true
                }
            }
            recalculation(dataResult[0])
            if (sendList == sendListReceived && stp) {
                stp = false
                if (recalculate) {
                    results = ""
                    rec = true
                    recalculate = false
                    if (recalculatedClients != clients.size) {
                        recalculation(dataResult[0])
                    } else {
                        println("Ошибка расчета")
                        startPortion = true
                        return ":"
                    }
                } else {
                    val cmd = typeMessage[1].split(",", limit = 2)
                    val cols = cmd[1].split(",", limit = 2)
                    val rows = cols[1].split(",", limit = 2)
                    dataForRecord = "YES:" + cols[0] + "," + rows[0] + "," + dataResult[1]
                    println("В БД записан результат = $results")
                    startPortion = true
                    return dataForRecord // для записи в БД
                }
            }
        }
        return ":" // для простого сообщения
    }

    fun clearList() {
        sendList.clear()
        sendListReceived.clear()
        results = ""
        recalculatedClients = 1
        startPortion = false
    }

    private suspend fun recalculation(data: String) {
        var j = 1
        val clientsRecalculators: Int
        if (rec) {
            clientsRecalculators = 3 // кол-во пересчитывающих клиентов, если есть ошибка
            rec = false
            recalculatedClients = 1 // счетчик пересчитавших клиентов
        } else {
            clientsRecalculators = 2 // кол-во пересчитывающих клиентов в первом проходе без ошибки
        }
        clients.forEach { _ ->
            if (recalculatedClients <= clientsRecalculators) {
                delay(500)
                if (!sendList.contains(j.toString())) {
                    sendList.add(j.toString())
                    clients[j - 1].send(data.trim())
                    recalculatedClients++
                    println("Отправлено на перерасчет клиенту: [$j]$data")
                }
            }
            j++
        }
    }
}
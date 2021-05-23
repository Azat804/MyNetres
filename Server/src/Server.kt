import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class Server(port: Int = 5804) {
    private val sSocket: ServerSocket
    val clients = mutableListOf<Client>()
    private var stop = false
    private var stp = true
    private var results = ""
    private var recalculate = false
    private var rec = false
    private val sendList = mutableListOf<String>()
    private val sendListRecive = mutableListOf<String>()
    private var recalculatingClients = 1
    var startPortion = true
    private val messageListeners = mutableListOf<(String) -> Unit>()
    fun addMessageListener(l: (String) -> Unit) {
        messageListeners.add(l)
    }

    val communicator: SocketIO

    inner class Client(val socket: Socket) {
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

    fun start() {
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

    private fun stopAllClients() {
        clients.forEach { client -> client.stop() }
    }

    fun send(i: Int, data: String) {
        sendList.add(i.toString())
        clients[i - 1].send(data)
    }

    fun senData(cmd: String) {
        var i = 1
        clients.forEach { client ->
            Thread.sleep(500)
            client.sendData(cmd)
            sendList.add(i.toString())
            i++
        }
    }

    fun dataProcessing(data: String): String {
        println("Получено: " + data)
        val v = data.split("[", limit = 2)
        val idClient = v[1].split("]", limit = 2)
        val typeMessage = idClient[1].split(",", limit = 2)
        if (typeMessage[0].trim() == "matrices") {
            var dataForrecord = ":"
            val dataResult = idClient[1].split("=", limit = 2)
            sendListRecive.add(idClient[0])
            rec = false
            stp = true
            if (results.trim().length == 0) {
                if (idClient[0].toInt() == 1) {
                    results = dataResult[1] + "1"
                } else {
                    results = dataResult[1]
                }
            } else {
                if (results.trim() != dataResult[1]) {
                    recalculate = true
                }
            }
            recalculation(dataResult[0])
            if (sendListRecive.size % 3 == 0 && stp) {
                stp = false
                if (recalculate) {
                    results = ""
                    rec = true
                    recalculate = false
                    recalculation(dataResult[0])
                } else {
                    val cmd = typeMessage[1].split(",", limit = 2)
                    val cols = cmd[1].split(",", limit = 2)
                    val rows = cols[1].split(",", limit = 2)
                    dataForrecord = "YES:" + cols[0] + "," + rows[0] + "," + dataResult[1]
                    println("В БД записан результат = " + results)
                    startPortion = true
                    return dataForrecord
                }
            }
        }
        return ":"
    }

    fun clearList() {
        sendList.clear()
        sendListRecive.clear()
        results = ""
        recalculatingClients = 1
        startPortion = false
    }

    fun recalculation(data: String) {
        var j = 1
        val clnt: Int // задает кол-во клиентов, которые считают, включая 1-го
        if (rec) {
            clnt = 4
            rec = false
            recalculatingClients = 1
        } else {
            clnt = 3
        }
        clients.forEach { client ->
            if (recalculatingClients < clnt) {
                Thread.sleep(500)
                if (!sendList.contains(j.toString())) {
                    sendList.add(j.toString())
                    clients[j - 1].send(data.trim())
                    recalculatingClients++
                    println("Отправлено на перерасчет клиенту: [$j]" + data)
                }
            }
            j++
        }
    }

    fun poritionStart(): Boolean {
        Thread.sleep(2000)
        return startPortion
    }

}
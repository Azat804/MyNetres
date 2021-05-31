import java.net.Socket

class Client(
    host: String,
    port: Int
) {
    private val socket: Socket
    private val communicator: SocketIO
    private val messageListeners = mutableListOf<(String) -> Unit>()

    init {
        socket = Socket(host, port)
        communicator = SocketIO(socket)
    }

    fun stop() {
        communicator.stop()
    }

    fun start() {
        communicator.addMessageListener {
            messageListeners.forEach { l -> l(it) }
        }
        communicator.startDataReceiving()
    }

    fun send(data: String) {
        communicator.sendData(data)
    }

    fun addMessageListener(l: (String) -> Unit) {
        messageListeners.add(l)
    }

    fun addSessionFinishedListener(l: () -> Unit) {
        communicator.addSocketClosedListener(l)
    }
}
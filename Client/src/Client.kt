import java.net.Socket
////
import java.util.ArrayList

class Client(
    val host: String,
    val port: Int
) {
    private val socket: Socket
     val communicator: SocketIO
    val messageListeners = mutableListOf<(String)-> Unit>()
    init{
        socket = Socket(host, port)
        communicator = SocketIO(socket)
    }

    fun stop(){
        communicator.stop()
    }

    fun start(){
        communicator.addMessageListener {
            messageListeners.forEach { l -> l(it) }
        }
        communicator.startDataReceiving()
    }

    fun send(data: String) {
        communicator.sendData(data)
    }

    fun addMessageListener(l:(String)-> Unit){
        messageListeners.add(l)
    }
    fun addSessionFinishedListener(l: ()->Unit){
        communicator.addSocketClosedListener(l)
    }

    fun removeSessionFinishedListener(l: ()->Unit){
        communicator.removeSocketClosedListener(l)
    }
}
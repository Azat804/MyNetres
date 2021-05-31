import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.net.Socket
import kotlin.concurrent.thread
import kotlinx.coroutines.*

class SocketIO(val socket: Socket) {

    private var stop = false
    private val socketClosedListener = mutableListOf<() -> Unit>()

    fun addSocketClosedListener(l: () -> Unit) {
        socketClosedListener.add(l)
    }

    private val messageListeners = mutableListOf<(String) -> Unit>()
    fun addMessageListener(l: (String) -> Unit) {
        messageListeners.add(l)
    }

    fun stop() {
        stop = true
        socket.close()
    }

    fun startDataReceiving() = CoroutineScope(Dispatchers.Default).launch {
        stop = false
        try {
            val br = BufferedReader(InputStreamReader(socket.getInputStream()))
            while (!stop) {
                val data = br.readLine()
                if (data != null) {
                    messageListeners.forEach { l -> l(data) }
                } else {
                    throw IOException("Связь прервалась")
                }
            }
        } catch (ex: Exception) {
            messageListeners.forEach { l -> ex.message?.let { l(it) } }
            //   println(ex.message)
        } finally {
            socket.close()
            socketClosedListener.forEach { it() }
        }
    }

    /* fun startDataReceiving() {
         stop = false
         thread{
             try {
                 val br = BufferedReader(InputStreamReader(socket.getInputStream()))
                 while (!stop) {
                     val data = br.readLine()
                     if (data!=null){
                         messageListeners.forEach { l -> l(data) }
                     }
                     else {
                         throw IOException("Связь прервалась")
                     }
                 }
             } catch (ex: Exception){
                 messageListeners.forEach { l -> ex.message?.let { l(it) } }
              //   println(ex.message)
             }
             finally {
                 socket.close()
                 socketClosedListener.forEach{it()}
             }
         }
     }
 */
    fun sendData(data: String): Boolean {
        try {
            val pw = PrintWriter(socket.getOutputStream())
            pw.println(data)
            pw.flush()
            return true
        } catch (ex: Exception) {
            return false
        }
    }
}
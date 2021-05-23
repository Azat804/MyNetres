import java.awt.Dimension
import java.awt.TextArea
import java.awt.TextField
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.*
import kotlin.system.exitProcess

class MainWindow : JFrame() {
    init {
        val client = Client("localhost", 5804)
        client.start()

        defaultCloseOperation = EXIT_ON_CLOSE
        title = "Чат"
        minimumSize = Dimension(100, 350)
        val comment="Введите сообщение:"
        val textField = TextField(comment)
        val textArea = TextArea()
        textArea.setEditable(false);
        textField.addFocusListener(object : FocusListener {
            override fun focusGained(e: FocusEvent) {
                textField.text = ""
            }
            override fun focusLost(e: FocusEvent?) {
            }
        })
        val button = JButton("Отправить")
        button.addActionListener {
            if (textField.text != comment && textField.text!=""){
            textArea.append("Вы: ${textField.text}\n")
            client.send(textField.text)
            textField.text = comment}
        }
        setResizable(false);
        val mainPanel = JPanel()

        val layout = GroupLayout(mainPanel)
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGap(10)
                .addComponent(
                    textArea,
                    GroupLayout.PREFERRED_SIZE,
                    220,
                    GroupLayout.PREFERRED_SIZE
                )
                .addGap(10)
                .addGroup(
                    layout.createParallelGroup()
                        .addComponent(
                            textField,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.PREFERRED_SIZE
                        )
                )
                .addGap(10)
                .addGroup(
                    layout.createParallelGroup()
                        .addComponent(
                            button,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.PREFERRED_SIZE
                        )
                )
        )
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGap(20)
                .addGroup(
                    layout.createParallelGroup()
                        .addComponent(textArea)
                        .addComponent(textField)
                        .addComponent(button)
                )
                .addGap(20)
        )
        mainPanel.add(textArea)
        mainPanel.add(textField)
        mainPanel.add(button)
        mainPanel.layout = layout
        add(mainPanel)
        pack()

        client.addSessionFinishedListener {
            textArea.append("Работа с сервером завершена. Нажмите Enter для выхода...")
            exitProcess(0)
        }

        client.addMessageListener { data ->
            val typeMessage = data.split(",", limit = 2)
            if (typeMessage[0] == "matrices") client.send(
                Matrx().matr(data)
            ) else textArea.append(data + "\n")
        }
    }
}
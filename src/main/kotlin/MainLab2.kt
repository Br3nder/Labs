import lab2.Parser
import java.io.File

fun main(){
    var filePath: String?
    val parser = Parser()
    while(true){
        print("[INFO] For exit enter: [Exit/exit/e]\nEnter filePath: ")
        filePath = readLine()
        if(filePath == "Exit" || filePath == "exit" || filePath == "e")
            return
        if(filePath != null && File(filePath).exists()) {
            println("[INFO] File found!")
            parser.setFileName(filePath)
            if (filePath.endsWith("csv")){
                parser.readCSV()
                parser.showData()
            }
            else if (filePath.endsWith("xml")){
                parser.readXML()
                parser.showData()
            }
            else
                println("[ERROR] Unknown format!")
        }
        else
            println("[ERROR] File not found!")
    }
}
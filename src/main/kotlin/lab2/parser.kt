package lab2

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.w3c.dom.Document
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.xml.parsers.DocumentBuilderFactory

class Parser {
    private var file: String = ""
    private var townBook: HashMap<Address, Int> = HashMap()
    private var uniqueCities: MutableList<String> = mutableListOf() // unique cities in file
    private var dublicates: HashMap<Address, Int>? = null
    private var deltaTime: Long = 0

    private data class Address(
        val city: String,
        val street: String,
        val house: Int,
        val floor: Int
    )
    /** Add new addresss in uniqueCity and townBook or calculate it count */
    private fun putNewAddress(newAddress: Address){
        if(!uniqueCities.contains(newAddress.city))
            uniqueCities.add(newAddress.city)

        if(townBook.containsKey(newAddress)){
            val i: Int? = townBook[newAddress]
            if (i != null) {
                townBook[newAddress] = i + 1
            }
        }
        else {
            townBook[newAddress] = 1
        }
    }

    private fun resetData(){
        townBook = HashMap()
        uniqueCities = mutableListOf()
        dublicates = null
    }

    fun setFileName(file: String){
        this.file = file
    }

    fun readCSV(){
        resetData()
        println("start parsing...")
        val startTime = System.currentTimeMillis()
        val bufferedReader = BufferedReader(FileReader(file))
        val parser = CSVParser(bufferedReader, CSVFormat.DEFAULT
            .withFirstRecordAsHeader()
            .withQuote('"')
            .withDelimiter(';')
            .withRecordSeparator("\r\n")
        )

        for (line in parser){
            val newAddress = Address(
                line.get("city"),
                line.get("street"),
                line.get("house").toInt(),
                line.get("floor").toInt())

            putNewAddress(newAddress)
        }
        dublicates = townBook.filterValues { it > 1 } as HashMap<Address, Int>?
        deltaTime = System.currentTimeMillis() - startTime
    }

    fun readXML(){
        resetData()
        println("start parsing...")
        val startTime = System.currentTimeMillis()
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val document: Document = builder.parse(File(file))
        val addressElements = document.documentElement.getElementsByTagName("item")

        for (indexAddress in 0 .. addressElements.length - 1) {
            val address = addressElements.item(indexAddress)
            val attributes = address.attributes
            val newAddress = Address(
                attributes.getNamedItem("city").nodeValue,
                attributes.getNamedItem("street").nodeValue,
                attributes.getNamedItem("house").nodeValue.toInt(),
                attributes.getNamedItem("floor").nodeValue.toInt()
            )
            putNewAddress(newAddress)
        }

        dublicates = townBook.filterValues { it > 1 } as HashMap<Address, Int>?
        deltaTime = System.currentTimeMillis() - startTime
    }

    fun showData(){
        for(dublicate in dublicates.orEmpty())
            println("City: ${dublicate.key.city} Dublicates: ${dublicate.value}")

        for(city in uniqueCities){
            val count = townBook.filterKeys { city == it.city }
            for(i in 1..5)
                println("In $city ${count.filterKeys { i == it.floor }.count()} houses with $i floors")
        }

        println("End parsing in ${deltaTime} mls")
    }

}
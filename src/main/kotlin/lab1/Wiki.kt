package lab1

import java.net.URI
import java.net.URLEncoder
import com.google.gson.*
import java.awt.Desktop
import com.google.gson.JsonObject
import java.net.URL

class WikiRequest(
    private val request: String
) {
    private val BASE_LINK = "https://ru.wikipedia.org/w/api.php?action=query&list=search&utf8=&format=json&srsearch="
    private val RESULT_LINK = "https://ru.wikipedia.org/w/index.php?curid="
    private val results: List<WikiResult>

    init {
        val requestString = getResponse()
        results = getResults(requestString)
    }

    class WikiResult(
        val id: String,
        private val title: String
    ) {
        companion object {
            fun fromJsonObject(o: JsonObject): WikiResult {
                return WikiResult(
                    o.getAsJsonPrimitive("pageid").asString,
                    o.getAsJsonPrimitive("title").asString
                )
            }
        }
        override fun toString(): String {
            return "Заголовок: $title | id = $id"
        }
    }

    fun getResultsList() = results

    private fun getResponse(): String = URL(BASE_LINK + URLEncoder.encode("\"$request\"")).readText()

    private fun getResults(jsonString: String): List<WikiResult> {
        val jsonArray = Gson()
            .fromJson(jsonString, JsonObject::class.java)
            .getAsJsonObject("query")
            .getAsJsonArray("search")
        val results = emptyList<WikiResult>().toMutableList()
        jsonArray.forEach {
            results.add(WikiResult.fromJsonObject(it.asJsonObject))
        }
        return results
    }

    fun openPage(id: Int) {
        Desktop.getDesktop().browse(URI(RESULT_LINK + results[id].id))
    }
}
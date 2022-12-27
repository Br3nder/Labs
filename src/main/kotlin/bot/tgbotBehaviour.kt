import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitLocationMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitText
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.location
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardMarkup
import dev.inmo.tgbotapi.types.buttons.SimpleKeyboardButton
import dev.inmo.tgbotapi.types.location.Location
import dev.inmo.tgbotapi.types.location.StaticLocation
import dev.inmo.tgbotapi.utils.RiskFeature
import dev.inmo.tgbotapi.utils.matrix
import kotlinx.coroutines.flow.first
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TgBot {
    private val botToken = "5819969795:AAG7o0eV-fVpsu85OmsrMU55mDQgRSTmCuU"
    private val cityLocationToken = "wMiEsIfzRf8TccsF2ZJ3rw==ThqP7DnfIixRikqH"
    private var location: Location? = null

    private data class CurrentWeather(
        val temperature: Float,
        val windspeed: Float,
        val winddirection: Float,
        val weathercode: Int,
        val time: String
    )

    private data class Weather(
        val latitude: Double,
        val longitude: Double,
        @SerializedName("generationtime_ms")
        val generationtimeMS: Float,
        @SerializedName("utc_offset_seconds")
        val utcOffsetSeconds: Int,
        val timezone: String,
        @SerializedName("timezone_abbreviature")
        val timezoneAbbreviature: String,
        val elevation: Float,
        @SerializedName("current_weather")
        val currentWeather: CurrentWeather
    )

    private data class City(
        @SerializedName("name")
        val name: String,
        @SerializedName("latitude")
        val latitude: Double,
        @SerializedName("longitude")
        val longitude: Double,
        @SerializedName("country")
        val country: String,
        @SerializedName("population")
        val population: Int,
        @SerializedName("is_capital")
        val isCapital: Boolean
    )

    private fun getWeather(location: Location? = null): Weather?{

        lateinit var apiString: String

        when {
            location != null -> {

                apiString = "https://api.open-meteo.com/v1/forecast?latitude=${location.latitude}&longitude=${location.longitude}&current_weather=true&timezone=Europe%2FMoscow"

            }
            else -> return null

        }

        val client = HttpClient.newBuilder().build()
        val apiRequest = HttpRequest.newBuilder()
            .uri(URI.create(apiString))
            .build()

        val response = client.send(apiRequest, HttpResponse.BodyHandlers.ofString())

        val gson = Gson()
        val weather: Weather = gson.fromJson(response.body(), Weather::class.java)

        println(response.body())
        return weather
    }

    private fun getLocationByCityName(cityName: String): Location?{
        val apiString = "https://api.api-ninjas.com/v1/city?name=${cityName}"
        val client = HttpClient.newBuilder().build()
        println("\nstart sending")
        val apiRequest = HttpRequest.newBuilder()
            .uri(URI.create(apiString))
            .header("X-Api-Key", cityLocationToken)
            .build()
        val response = client.send(apiRequest, HttpResponse.BodyHandlers.ofString())
        val gson = Gson()
        val cities = gson.fromJson(response.body(), Array<City>::class.java).toList()
        cities.forEach{println(it)}
        //todo{случай, если несколько городов? Дать выбор.}
        if(cities.isEmpty())
            return null
        return StaticLocation(latitude = cities[0].latitude, longitude = cities[0].longitude)
    }

    @OptIn(RiskFeature::class)
    suspend fun start() {

        val bot = telegramBot(botToken)

        bot.buildBehaviourWithLongPolling {
            println(getMe())
            onCommand("start"){
                sendTextMessage(
                    it.chat.id,
                    "Hi, I'm ETU's R2D2. I 'll help you find out the weather)\n *UiiPupzPip*"
                )
            }
            onCommand("weathersettings"){
                val optionReplyMarkup = ReplyKeyboardMarkup(
                    matrix {
                        +SimpleKeyboardButton("Location")
                    }
                )

                val locationReplyMarkup = ReplyKeyboardMarkup(
                    matrix {
                        +SimpleKeyboardButton("I send my coordinates")
                        +SimpleKeyboardButton("I send city name")
                    }
                )

                waitText(
                    SendTextMessage(
                        it.chat.id,
                        "Select option for change\n *PiriPiPuZzPiuIu*",
                        replyMarkup = optionReplyMarkup
                    )
                )

                val option = waitText().first().text
                when(option){
                    "Location" ->  {
                        val locationType = waitText(
                            SendTextMessage(
                                it.chat.id,
                                "How I can find you?",
                                replyMarkup = locationReplyMarkup
                            )
                        ).first().text

                        if(locationType == "I send my coordinates"){
                            location = waitLocationMessage(
                                SendTextMessage(
                                    it.chat.id,
                                    "Ok, i'm waiting..."
                                )
                            ).first().location
                            sendTextMessage(
                                it.chat.id,
                                "Get it!"
                            )
                        }
                        else if ( locationType == "I send city name"){
                            val cityName = waitText(
                                SendTextMessage(
                                    it.chat.id,
                                    "Send me city name on eng...",
                                    replyMarkup = null
                                )
                            ).first().text
                            sendTextMessage(
                                it.chat.id,
                                "Try to find your cordinates...\nPlease, wait..."
                            )
                            val loc = getLocationByCityName(cityName)
                            if(loc != null)
                                location = getLocationByCityName(cityName)

                        }
                        if(location != null)
                            sendTextMessage(
                                it.chat.id,
                                "I have found!"
                            )
                        else
                            sendTextMessage(
                                it.chat.id,
                                "I can't find this city..."
                            )
                        //todo{Сделать, чтобы скрывалось меню, после выбора опции}
                    }
                }
            }
            onCommand("getweather") {
                if(location != null){
                    val weather = getWeather(location = location)
                    if(weather != null){
                        sendTextMessage(
                            it.chat.id,
                            "temperature is ${weather.currentWeather.temperature}"
                        )
                    }
                } else {
                    sendTextMessage(
                        it.chat.id,
                        "Sorry, but I can't find you..."
                    )
                }
            }
            onCommand("db"){
                val sql = SQLBehaviour()
                val optionReplyMarkup = ReplyKeyboardMarkup(
                    matrix {
                        +SimpleKeyboardButton("show friends")
                        +SimpleKeyboardButton("write off the debt")
                    }
                )

                val answer = waitText(
                    SendTextMessage(
                        it.chat.id,
                        "Select command",
                        replyMarkup = optionReplyMarkup
                    )
                ).first().text

                when(answer){
                    "show friends" -> {
                        val friends = sql.showAllFriendData()
                        var text = ""
                        friends.forEach{
                            text += "${it.name} ${it.phone} ${it.money}\n"
                        }
                        sendTextMessage(
                            it.chat.id,
                            text
                        )
                    }
                    "write off the debt" -> {
                        var answer = waitText(
                            SendTextMessage(
                                it.chat.id,
                                "Send me phone and money amount for off the debt, separated by commas"
                            )
                        ).first().text
                        val parameters = answer.split(",")
                        answer = sql.writOffTheDebt(parameters[0], parameters[1].toFloat())
                        sendTextMessage(
                            it.chat.id,
                            answer
                        )
                    }
                }
            }
        }.join()

    }


}
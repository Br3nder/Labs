import java.sql.Connection
import java.sql.DriverManager

class SQLBehaviour() {

    val connection: Connection

    data class Friend(
        val name: String,
        val phone: String,
        val money: Float
    )

    init {
        val jdbcUrl = "jdbc:sqlserver://localhost:1433;database=Friends;trustServerCertificate=true;"
        connection = DriverManager.getConnection(jdbcUrl, "telegramBot", "tgbotpass")
        println(connection.isValid(0))
    }

    fun showAllFriendData(): MutableList<Friend> {
        val query = connection.prepareStatement("select * from FriendsData")
        val result = query.executeQuery()

        val friends: MutableList<Friend> = mutableListOf()
        while(result.next()){
            friends.add(
                Friend(
                    result.getString("name").filter { it != ' ' },
                    result.getString("phone").filter { it != ' ' },
                    result.getFloat("money")
                )
            )
        }
        friends.forEach{
            println(it)
        }

        return friends
    }

    fun writOffTheDebt(phone: String ,moneyAmount: Float): String{
        try {
            val query = connection.prepareStatement("update FriendsData set money = money - $moneyAmount where phone = '$phone'")
            query.executeUpdate()
            return "Done!"
        } catch(e:Exception) {
            return "Error: ${e.message}"
        }
    }

}
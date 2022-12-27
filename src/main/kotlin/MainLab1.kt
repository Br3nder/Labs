import lab1.WikiRequest

fun main() {

    println("Поиск:")
    val input = readln()
    val request = WikiRequest(input)

    println("По запросу \"$input\" найдено:")
    request.getResultsList().forEachIndexed { index, result ->
        println("$index. $result")
    }
    println("\nИндекс страницы:")
    try{
        val inputIndex = readln().toInt()
        request.openPage(inputIndex)
    } catch (e: Exception) {
        println("Некорректный ввод!\nError msg: ${e.message}")
    }
}

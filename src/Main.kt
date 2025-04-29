import kotlinx.coroutines.*
import kotlin.random.Random

class Student(
    name: String,
    age: Int,
    grades: List<Int>
) {
    var name: String = name.trim().capitalize()
        set(value) {
            field = value.trim().capitalize()
        }

    var age: Int = age.coerceAtLeast(0)
        set(value) {
            if (value >= 0) field = value
        }

    private var grades: MutableList<Int> = grades.toMutableList()

    val isAdult: Boolean
        get() = age >= 18

    val status: String by lazy {
        println(">>> Computing status for $name")
        if (isAdult) "Adult" else "Minor"
    }

    init {
        println("Init Student: name=$name, age=$age, grades=$grades")
    }

    constructor(name: String) : this(name, 0, emptyList())

    fun getAverage(): Double =
        if (grades.isNotEmpty()) grades.average() else 0.0

    fun processGrades(operation: (Int) -> Int) {
        grades = grades.map(operation).toMutableList()
        println("$name processed grades -> $grades")
    }

    fun updateGrades(newGrades: List<Int>) {
        grades = newGrades.toMutableList()
        println("$name updated grades -> $grades")
    }

    operator fun plus(other: Student): Student {
        val combinedName = "$name & ${other.name}"
        val combinedAge = maxOf(age, other.age)
        return Student(combinedName, combinedAge, grades + other.grades)
    }

    operator fun times(factor: Int): Student {
        return Student(name, age, grades.map { it * factor })
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Student) return false
        return name == other.name && getAverage() == other.getAverage()
    }

    override fun hashCode(): Int {
        return name.hashCode() * 31 + getAverage().toInt()
    }

    override fun toString(): String {
        return "Student(name=$name, age=$age, grades=$grades, avg=${"%.2f".format(getAverage())})"
    }
}

class Group(vararg studs: Student) {
    private val students = studs.toList()
    operator fun get(index: Int): Student = students[index]
    fun getTopStudent(): Student? = students.maxByOrNull { it.getAverage() }
}

suspend fun fetchGradesFromServer(): List<Int> {
    delay(2000)
    return List(5) { Random.nextInt(1, 6) }
}

fun main() = runBlocking {
    println("=== Створюємо студентів ===")
    val s1 = Student("Ivan petrov ", age = 19, grades = listOf(4, 5, 3))
    val s2 = Student(name = "maria")
    val s3 = Student("Olena", 17, listOf(5, 5, 4))

    println("\n=== Перевіряємо isAdult та lazy status ===")
    println("${s1.name} isAdult = ${s1.isAdult}")
    println("Status first access: ${s1.status}")
    println("Status second access: ${s1.status}")

    println("\n=== Оператор + та * ===")
    val combined = s1 + s3
    println("Combined: $combined")
    val boosted = s3 * 2
    println("Boosted grades: $boosted")

    println("\n=== processGrades (додаємо +1 до кожної) ===")
    s1.processGrades { it + 1 }

    println("\n=== Група та getTopStudent ===")
    val group = Group(s1, s2, s3, combined)
    println("Student at index 2: ${group[2]}")
    println("Top student: ${group.getTopStudent()}")

    println("\n=== Асинхронне оновлення оцінок для ${s2.name} ===")
    val deferred = async { fetchGradesFromServer() }
    println("Fetching grades from server...")
    val newGrades = deferred.await()
    s2.updateGrades(newGrades)

    println("\n=== Кінець демонстрації ===")
}

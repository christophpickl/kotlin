package foo

// NOTE THIS FILE IS AUTO-GENERATED by the generateTestDataForReservedWords.kt. DO NOT EDIT!

object TestObject {
    val implements: Int = 0

    fun test() {
        testNotRenamed("implements", { implements })
    }
}

fun box(): String {
    TestObject.test()

    return "OK"
}
// "Create property 'foo'" "true"
// ERROR: Property must be initialized or be abstract

class A {
    class B {
        val foo: Int

        fun test(): Int {
            return foo
        }
    }
}

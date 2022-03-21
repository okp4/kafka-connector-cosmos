import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MyTests : StringSpec({
    "hello world message should return an hello world string" {
        message shouldBe "Hello World!"
    }
})

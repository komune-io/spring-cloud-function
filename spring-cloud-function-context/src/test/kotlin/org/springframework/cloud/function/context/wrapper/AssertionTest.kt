package org.springframework.cloud.function.context.wrapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AssertionTest {

    @Test
    fun `test isNotNull with different types`() {
        // Test with String
        val string: String = "test"
        assertThat(string).isNotNull

        // Test with Int
        val int: Int = 42
        assertThat(int).isNotNull

        // Test with Object
        val obj: Any = Object()
        assertThat(obj).isNotNull

        // Test with Flow
        val flowObj: Flow<String> = flow {
            emit("test1")
            emit("test2")
        }
        assertThat(flowObj).isNotNull
        
        // Alternative assertion for Flow
        assertThat(flowObj).isNotEqualTo(null)
    }
}
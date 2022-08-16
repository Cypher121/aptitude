package coffee.cypher.aptitude.util

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.codecs.PrimitiveCodec

class ConstCodec(private val const: String) : PrimitiveCodec<String> {
    override fun <T : Any> read(ops: DynamicOps<T>, input: T): DataResult<String> {
        return ops.getStringValue(input).flatMap {
            if (it == const) {
                DataResult.success(it)
            } else {
                DataResult.error("Const codec only accepts $const")
            }
        }
    }

    override fun <T : Any> write(ops: DynamicOps<T>, value: String): T {
        require(value == const) { "Const codec only accepts $const" }
        return ops.createString(const)
    }

    fun <T : Any> typed(value: T): Codec<T> {
        return flatXmap({ DataResult.success(value) }, {
            if (value == it) {
                DataResult.success(const)
            } else {
                DataResult.error("Const codec only accepts $value, got $it")
            }
        })
    }

    override fun toString(): String {
        return "Const[$const]"
    }
}

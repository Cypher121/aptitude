package coffee.cypher.gradleutil.filters

import blue.endless.jankson.JsonGrammar
import java.io.FilterReader
import java.io.Reader

class FlatteningJsonFilter(input: Reader) : FilterReader(
    with(JsonUtil) {
        input.asJson().flatten().toJson(JsonGrammar.STRICT).reader()
    }
)

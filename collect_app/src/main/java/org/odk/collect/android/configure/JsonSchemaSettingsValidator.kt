package org.odk.collect.android.configure

import com.github.fge.jackson.JsonNodeReader
import com.github.fge.jsonschema.main.JsonSchemaFactory
import java.io.InputStream
import java.io.StringReader

class JsonSchemaSettingsValidator(private val schemaProvider: () -> InputStream) :
    SettingsValidator {

    override fun isValid(json: String): Boolean {
        return schemaProvider().use { schemaStream ->
            StringReader(json).use {
                JsonSchemaFactory
                    .byDefault()
                    .getJsonSchema(JsonNodeReader().fromInputStream(schemaStream))
                    .validate(JsonNodeReader().fromReader(it))
                    .isSuccess
            }
        }
    }
}

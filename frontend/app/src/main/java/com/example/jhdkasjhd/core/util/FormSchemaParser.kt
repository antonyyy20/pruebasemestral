package com.example.jhdkasjhd.core.util

data class FormField(
    val key: String,
    val label: String,
    val type: String,
    val required: Boolean
)

object FormSchemaParser {

    @Suppress("UNCHECKED_CAST")
    fun parseFields(schema: Map<String, Any?>): List<FormField> {
        val fieldsRaw = schema["fields"] as? List<*> ?: return emptyList()
        return fieldsRaw.mapNotNull { item ->
            val map = item as? Map<String, Any?> ?: return@mapNotNull null
            FormField(
                key = map["key"]?.toString().orEmpty(),
                label = map["label"]?.toString().orEmpty(),
                type = map["type"]?.toString() ?: "text",
                required = map["required"] as? Boolean ?: false
            )
        }.filter { it.key.isNotBlank() }
    }
}

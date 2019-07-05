package com.dsobko.gcp.data

import com.google.cloud.bigquery.FieldValueList


object GcpDataExtractor {

    fun extractString(result: FieldValueList, field: String) = checkForNull(result, field)

    fun extractInt(result: FieldValueList, field: String): Int? = checkForNull(result, field)?.toInt()

    fun extractFloat(result: FieldValueList, field: String): Float? = checkForNull(result, field)?.toFloat()

    private fun checkForNull(result: FieldValueList, field: String): String? {
        return if (result[field].value == null || result[field].isNull) null
        else result[field].value as String
    }
}

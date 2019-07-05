package com.dsobko.gcp.models

import com.google.cloud.bigquery.Field
import com.google.cloud.bigquery.LegacySQLTypeName

data class ComparableField(var tableName: String, var name: String, var type: LegacySQLTypeName, var mode: Field.Mode?){
    var value: Any? = null
}

fun ComparableField.splitFieldName() = this.name.split(".")





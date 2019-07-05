package com.dsobko.gcp.utils

import com.dsobko.gcp.models.ComparableField
import com.dsobko.gcp.models.splitFieldName
import com.google.cloud.bigquery.*
import com.google.cloud.bigquery.FieldValue.Attribute.REPEATED
import org.slf4j.LoggerFactory
import kotlin.test.fail

object TableFlattener {

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun flattenTableSchema(tableName: String, schema: Schema): Set<ComparableField> {
        val flattenedSchema: MutableSet<ComparableField> = mutableSetOf()
        schema.fields.forEach { parentField ->
            when {
                parentField.subFields != null -> parentField.subFields.forEach { subFieldFirstLevel ->
                    when {
                        subFieldFirstLevel.subFields != null -> subFieldFirstLevel.subFields.forEach { subFieldSecondLevel ->
                            when {
                                subFieldSecondLevel.subFields != null -> subFieldSecondLevel.subFields.forEach { subFieldThirdLevel ->
                                    when {
                                        subFieldThirdLevel.subFields != null -> subFieldThirdLevel.subFields.forEach { subFieldForthLevel ->
                                            flattenedSchema.add(ComparableField(tableName, "${parentField.name}.${subFieldFirstLevel.name}.${subFieldSecondLevel.name}.${subFieldThirdLevel.name}.${subFieldForthLevel.name}", subFieldForthLevel.type, subFieldForthLevel.mode))
                                        }
                                        else -> flattenedSchema.add(ComparableField(tableName, "${parentField.name}.${subFieldFirstLevel.name}.${subFieldSecondLevel.name}.${subFieldThirdLevel.name}", subFieldThirdLevel.type, subFieldThirdLevel.mode))
                                    }
                                }
                                else -> flattenedSchema.add(ComparableField(tableName, "${parentField.name}.${subFieldFirstLevel.name}.${subFieldSecondLevel.name}", subFieldSecondLevel.type, subFieldSecondLevel.mode))
                            }
                        }
                        else -> flattenedSchema.add(ComparableField(tableName, "${parentField.name}.${subFieldFirstLevel.name}", subFieldFirstLevel.type, subFieldFirstLevel.mode))
                    }
                }
                else -> flattenedSchema.add(ComparableField(tableName, parentField.name, parentField.type, parentField.mode))
            }
        }
        return flattenedSchema
    }



    fun flattenRandomDataRowToSchemaWithValues(randomRowTableResult: TableResult): List<Set<ComparableField>> {
        val nestedSchema = randomRowTableResult.schema.fields
        val dataRow = randomRowTableResult.values.first()!!

        val recordsCount = extractNestedFieldsOfDataRow(dataRow).maxBy { it.size }!!.size
        val recordsList = mutableListOf<Set<ComparableField>>()

        for (i in 0 until recordsCount) {

            var fullSchema = flattenTableSchema("aaaaaaa", randomRowTableResult.schema)

            val listForExtraPopulation = extractNestedFieldsOfDataRow(dataRow)
                    .filter { it.size > i }
                    .toList()
            val indexesForExtraPopulation = listForExtraPopulation
                    .asSequence()
                    .map { value -> dataRow.asSequence().map { it.value }.indexOf(value) }
            val namesForExtraPopulation = indexesForExtraPopulation
                    .map { randomRowTableResult.schema.fields[it].name }
                    .toList()

            var schemaWithFieldsToPopulate = fullSchema
                    .filter { it.splitFieldName().size > i || namesForExtraPopulation.contains(it.splitFieldName()[0]) }


            schemaWithFieldsToPopulate.forEach {
                val nestedLevel = it.splitFieldName()
                val nestedLevelsCount = nestedLevel.count()

                log.debug("dataRow = $dataRow \n nestedLevel = $nestedLevel  nestedLevelsCount = $nestedLevelsCount")
                log.debug("Field has (nestedLevelsCount - 1) = ${nestedLevelsCount - 1}")

                when (nestedLevelsCount - 1) {
                    0 -> {
                        val dataValue = dataRow[nestedLevel[0]].value
                        fullSchema.find { a -> a == it }!!.value = dataValue
                        log.debug("At nested level 0, value = \" $dataValue \" was added")
                    }
                    1 -> populateFirsNestedLevelFields(dataRow, nestedLevel, nestedSchema, i, fullSchema, it)
                    else -> fail("Not implemented level of nesting fields (>1)!!!")
                }
            }
            recordsList.add(fullSchema)
        }
        return recordsList
    }



    private fun populateFirsNestedLevelFields(dataRow: FieldValueList,
                                              nestedLevel: List<String>,
                                              nestedSchema: FieldList,
                                              i: Int,
                                              fullSchema: Set<ComparableField>,
                                              it: ComparableField) {

        val parentValue = dataRow[nestedLevel[0]].value

        val childFieldsFirstLevelNames = nestedSchema[nestedLevel[0]].subFields
        val childFieldsFirstLevelIndex = childFieldsFirstLevelNames.indexOf(childFieldsFirstLevelNames.findFieldInSequence(nestedLevel[1]))

        if (checkParentValueIsList(parentValue, i)) {
            val childFieldFirstLevelList = (parentValue as FieldValueList)[i].value
            if (checkChildValueIsList(childFieldFirstLevelList, childFieldsFirstLevelIndex)) {
                val childFieldFifthLevelData = (childFieldFirstLevelList as FieldValueList)[childFieldsFirstLevelIndex].value
                val valueToSet = if (checkChildValueIsList(childFieldFifthLevelData, i)) (childFieldFifthLevelData as FieldValueList)[i] else childFieldFifthLevelData
                fullSchema.find { a -> a == it }!!.value = valueToSet
                log.debug("At nested level ${1}, value = \" $valueToSet \" was added")
            }
        }
    }


    private fun extractNestedFieldsOfDataRow(dataRow: FieldValueList) =
            dataRow
                    .asSequence()
                    .filter { it.value is FieldValueList && it.attribute == REPEATED }
                    .map { it.value }
                    .filter { it is FieldValueList }
                    .map { it as FieldValueList }

    private fun FieldList.findFieldInSequence(fieldName: String) = this.asSequence().find { it.name == fieldName }

    private fun checkParentValueIsList(parentValue: Any?, i: Int) = parentValue is FieldValueList && parentValue.isNotEmpty() && parentValue.size > i

    private fun checkChildValueIsList(childValue: Any?, childFieldIndex: Int) = childValue is FieldValueList && childValue.isNotEmpty() && childValue.size > childFieldIndex


}
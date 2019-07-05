package com.dsobko.gcp.utils

import com.dsobko.gcp.models.ComparableField

object DistinctRowsQueryCreator {

    fun createQueryDistinctRows(tableSchema: Set<ComparableField>, repeatedFieldsSchema: Set<ComparableField>): String {

        /*

        Example of possible query with nested fields

          SELECT a.test_1, b.test_2, count(*)
          from `test.abc` a
          left join
          unnest(a.data.ress) sdba
          group by 1,2 having count(*)> 1

          */

        val parentRepeatedFields = repeatedFieldsSchema
                .asSequence()
                .map { it.name }
                .toList()

        val allRepeatedFields = tableSchema
                .asSequence()
                .filter { parentRepeatedFields.contains(it.name.substringBeforeLast(".")) } //Repeated fields with all childs, but without parent repeated field
                .map { it.name }
                .toList()

        val flattenFields = tableSchema
                .asSequence()
                .filter { !allRepeatedFields.contains(it.name) } //Filter out all repeated fields, only flattened remaining
                .map { it.name }
                .minus("time") //Could be different, remove from dupes check
                .toList()


        val selectClauseBuffer = StringBuffer()
        val joinClauseBuffer = StringBuffer()
        parentRepeatedFields
                .asSequence()
                .forEachIndexed { index, elem ->
                    allRepeatedFields
                            .asSequence()
                            .filter { it.substringBeforeLast(".") == elem }
                            .toList()
                            .forEach {
                                selectClauseBuffer.append(" a$index.${it.removePrefix("$elem.")} as a${index}_${it.removePrefix("$elem.")},")
                            }

                    joinClauseBuffer.append("left join unnest(a.$elem) a$index \n ")
                }

        val flattenFieldsSelectClause = StringBuffer()
        flattenFields
                .asSequence()
                .forEach { elem ->
            flattenFieldsSelectClause.append("a.$elem as a_${elem.replace(".", "_")} ,")
        }


        val groupClause = StringBuffer()
        for (i in 1 until tableSchema.size) {
            groupClause.append("$i ,")
        }

        val queryBuffer = StringBuffer()
        queryBuffer.apply {
            append("select ")
            append(flattenFieldsSelectClause)
            append(" \n")
            append(selectClauseBuffer.toString())
            append(" COUNT(*) \n")
            append("FROM {0} a\n")
            append(joinClauseBuffer.toString())
            append("GROUP BY ${groupClause.removeSuffix(",")} \n")
            append("HAVING COUNT(*) > 1")
        }
        return queryBuffer.toString()
    }
}
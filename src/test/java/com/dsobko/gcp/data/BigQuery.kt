package com.dsobko.gcp.data

import com.dsobko.gcp.data.GcpDataExtractor.extractInt
import com.google.cloud.bigquery.*
import org.assertj.core.api.Assertions
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.test.fail


@Service
class BigQuery {

    private val log = getLogger(this.javaClass)

    fun retrieveTableRowsCount(project: String, datasetName: String, tableName: String): Int {
        val query = "SELECT row_count FROM `$project.$datasetName.__TABLES__` WHERE table_id = '$tableName'"
        return extractInt(executeQuery(query).values.first(), "row_count")!!
    }

    fun executeQuery(query: String): TableResult {
        val bigQuery = BigQueryOptions.getDefaultInstance().service
        val jobId = JobId.of(UUID.randomUUID().toString())
        var queryJob = bigQuery.create(JobInfo.newBuilder(QueryJobConfiguration.newBuilder(query).build()).setJobId(jobId).build())

        queryJob = queryJob!!.waitFor()

        when {
            queryJob == null -> fail("Can't execute GCP Query!!!")
            queryJob.status.error != null -> throw RuntimeException(queryJob.status.error.toString())
        }

        return queryJob.getQueryResults()!!
    }

    fun executeQueryAndSaveResults(query: String, dataset: String, destinationTable: String) {
        val bigQuery = BigQueryOptions.getDefaultInstance().service
        val queryConfig =
                QueryJobConfiguration.newBuilder(query)
                        .setWriteDisposition(JobInfo.WriteDisposition.WRITE_TRUNCATE)
                        .setDestinationTable(TableId.of(dataset, destinationTable))
                        .build()

        bigQuery.query(queryConfig)
    }

    fun createExternalTableForFileFromBucket() {
        val bigQuery = BigQueryOptions.getDefaultInstance().service

        val bucketName = "test"
        val fileName = "test"

        val datasetName = "a_test"
        val tableName = "autotest_test_test_12345"

        val destinationTable = TableId.of(datasetName, tableName)
        val configuration = LoadJobConfiguration
                .newBuilder(destinationTable, "gs://$bucketName/$fileName")
                .setFormatOptions(FormatOptions.avro())
                .setAutodetect(true)
                .setIgnoreUnknownValues(true)
                .setCreateDisposition(JobInfo.CreateDisposition.CREATE_IF_NEEDED)
                .build()

        var remoteLoadJob = bigQuery.create(JobInfo.of(configuration))
        remoteLoadJob = remoteLoadJob.waitFor()

        Assertions.assertThat(remoteLoadJob.status.error).isNull()
    }

    fun retrieveTableModifiedDate(datasetName: String, tableName: String): LocalDateTime? {
        val bigQuery = BigQueryOptions.getDefaultInstance().service
        val modifiedTimestamp = bigQuery.getTable(datasetName, tableName).lastModifiedTime
        log.info("Retrieved modified date timestamp for table $datasetName.$tableName = $modifiedTimestamp")
        val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(modifiedTimestamp), ZoneOffset.UTC)
        log.info("Retrieved modified date for table $datasetName.$tableName = $dateTime")
        return dateTime
    }

    fun retrieveTableSchema(datasetName: String, tableName: String): Schema {
        val bigQuery = BigQueryOptions.getDefaultInstance().service
        val tableDefinition: TableDefinition  = bigQuery.getTable(datasetName, tableName).getDefinition()
        return tableDefinition.schema!!
    }



}
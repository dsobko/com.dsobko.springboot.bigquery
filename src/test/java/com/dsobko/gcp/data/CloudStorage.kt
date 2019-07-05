package com.dsobko.gcp.data

import com.google.cloud.storage.Blob
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.dsobko.gcp.props.GcpEnvConfigs
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service
import java.time.Instant.ofEpochMilli
import java.time.LocalDate
import java.time.LocalDateTime.ofInstant
import java.time.ZoneOffset.UTC
import kotlin.test.fail

@Service
class CloudStorage(private val gcpEnvConfigs: GcpEnvConfigs) {

    private val log = getLogger(this.javaClass)

    fun retrieveFileModifiedDate(bucketName: String, fileName: String): LocalDate {
        val modifiedTimestamp: Long
        try {
            modifiedTimestamp = storage.get(bucketName)[fileName].updateTime
        } catch (e: IllegalStateException) {
            fail("The file: $fileName is not present in bucket: $bucketName $")
        }
        log.info("Modified at date as timestamp of file \"$fileName\" in bucket \"$bucketName\" = $modifiedTimestamp")

        val modifiedDate = ofInstant(ofEpochMilli(modifiedTimestamp), UTC).toLocalDate()
        log.info("Modified at date of file \"$fileName\" in bucket \"$bucketName\" = $modifiedDate")

        return modifiedDate
    }

    fun retrieveFileSize(bucketName: String, fileName: String): Long = storage.get(bucketName)[fileName].size

    fun retrieveFile(bucketName: String, filePath: String): String {
        val blob = storage.get(bucketName)[filePath]
        return String(blob.getContent(Blob.BlobSourceOption.generationMatch()))
    }

    private val storage: Storage
        get() {
            val storageFactory = StorageOptions.DefaultStorageFactory()
            val optionsBuilder = StorageOptions.newBuilder().setProjectId(gcpEnvConfigs.project).build()
            return storageFactory.create(optionsBuilder)
        }

}
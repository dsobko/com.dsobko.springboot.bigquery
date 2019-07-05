package com.dsobko.gcp.data

import com.google.cloud.datastore.DatastoreOptions
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.Query
import com.google.cloud.datastore.StructuredQuery
import com.dsobko.gcp.props.GcpEnvConfigs
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC

@Service
class DataStore(private val gcpEnvConfigs: GcpEnvConfigs) {

    private val log = getLogger(this.javaClass)

    fun retrieveEntity(nameSpace: String, kind: String, orderByProperty: String): Entity {
        val datastoreFactory = DatastoreOptions.DefaultDatastoreFactory()
        val optionsBuilder = DatastoreOptions.newBuilder().setProjectId(gcpEnvConfigs.project).build()
        val datastore = datastoreFactory.create(optionsBuilder)

        val query = Query.newEntityQueryBuilder()
                .setNamespace(nameSpace)
                .setKind(kind)
                .setOrderBy(StructuredQuery.OrderBy.desc(orderByProperty))
                .setLimit(1)
                .build()

        return datastore.run(query).next()!!.apply {
            log.info("Data from Datastore = $this")
        }
    }

    fun retrieveMaxId(): LocalDateTime {
        val entity = retrieveEntity("ETL_LOG", "JOURNAL", "id")
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(entity.getString("id").toLong()), UTC).apply {
            log.info("Datetime from Datastore = $this")
        }
    }
}
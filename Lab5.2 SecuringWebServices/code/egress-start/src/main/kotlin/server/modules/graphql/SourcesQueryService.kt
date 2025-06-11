package be.ugent.idlab.predict.ocmt.egress.server.modules.graphql

import be.ugent.idlab.predict.ocmt.egress.server.modules.getSourcesFluxQuery
import be.ugent.idlab.predict.ocmt.egress.services.Influx
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query

class SourcesQueryService: Query {
    @GraphQLDescription("Returns an overview of all sources that ever submitted data")
    suspend fun sources() :List<String> {
        val records = Influx.query(getSourcesFluxQuery())
        val foundSources = records.mapNotNull { record ->
            record.getValueByKey("source")?.toString()
        }

        return foundSources
    }
}

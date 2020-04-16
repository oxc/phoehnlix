package de.esotechnik.phoehnlix.apiservice.util

import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.ApplicationResponse
import io.ktor.util.pipeline.PipelineContext
import java.lang.RuntimeException

/**
 * @author Bernhard Frauendienst
 */
internal class ParameterOverridingContext(
  pipelineContext: PipelineContext<Unit, ApplicationCall>,
  parameters: Parameters
) : PipelineContext<Unit, ApplicationCall> by pipelineContext {

  override val context = ParameterOverridingApplicationCall(pipelineContext.context, parameters)
}

internal class ParameterOverridingApplicationCall(
  applicationCall: ApplicationCall,
  override val parameters: Parameters
) : ApplicationCall by applicationCall {
  override val response: ApplicationResponse
    get() = throw InvalidResponseAccess()
}

internal class InvalidResponseAccess : RuntimeException()
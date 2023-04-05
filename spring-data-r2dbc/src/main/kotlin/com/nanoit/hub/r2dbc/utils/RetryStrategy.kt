package com.nanoit.hub.r2dbc.utils

import java.time.Duration
import org.slf4j.LoggerFactory
import reactor.util.retry.Retry
import reactor.util.retry.RetryBackoffSpec

object RetryStrategy {
    private val log = LoggerFactory.getLogger(RetryStrategy::class.java)

    fun defaults(): RetryBackoffSpec = Retry.backoff(5, Duration.ofMillis(200L))
            .jitter(0.75)
            .doBeforeRetry { log.debug("[@RETRY:DEFAULT@] before {} {}", it.totalRetries(), it.retryContextView()) }
            .doAfterRetry { log.debug("[@RETRY:DEFAULT@] before {} {}", it.totalRetries(), it.retryContextView()) }
//            .onRetryExhaustedThrow { spec, signal -> RetryExhaustedException("$spec, $signal") }
}

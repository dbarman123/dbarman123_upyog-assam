package org.egov.bpa.repository;

import java.util.Map;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.egov.bpa.config.BPAConfiguration;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class ServiceRequestRepository {

	private ObjectMapper mapper;

	private RestTemplate restTemplate;

	private RetryTemplate retryTemplate;

	@Autowired
	private BPAConfiguration config;

	@Autowired
	public ServiceRequestRepository(ObjectMapper mapper, RestTemplate restTemplate, RetryTemplate retryTemplate) {
		this.mapper = mapper;
		this.restTemplate = restTemplate;
		this.retryTemplate = retryTemplate;
	}

	/*public Object fetchResult(StringBuilder uri, Object request) {
		Object response = null;
		log.info("URI: " + uri.toString());
		try {
		//	log.info("Request: " + mapper.writeValueAsString(request));
			response = restTemplate.postForObject(uri.toString(), request, Map.class);
		//	log.info("Response : {}", mapper.writeValueAsString(response));
		} catch (HttpClientErrorException e) {
			log.error("External Service threw an Exception: ", e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (Exception e) {
			log.error("Exception while fetching from searcher: ", e);
			throw new ServiceCallException(e.getMessage());
		}

		return response;
	}*/

	/**
	 * Fetches result from an external service with retry mechanism for transient failures.
	 *
	 * @param uriStringBuilder The URI to call.
	 * @param request          The request payload.
	 * @return The response from the external service.
	 * @throws ServiceCallException if a non-retryable error occurs or all retries are exhausted.
	 */
	public Object fetchResult(StringBuilder uriStringBuilder, Object request) {
		String uri = uriStringBuilder.toString();
		return retryTemplate.execute(context -> {
			int retryCount = context.getRetryCount();

			if (retryCount > 0) {
				// exponential backoff calculation
				long nextBackoff = (long) (config.getBackoffMs() * Math.pow(2, retryCount - 1));

				// enforce max cap if needed
				nextBackoff = Math.min(nextBackoff, 30000);

				log.warn("Retry attempt #{} → exponential backoff applied: {} ms",
						retryCount, nextBackoff);
			}


			log.info("URI: {}", uri);
			Object response = null;

			try {
				response = restTemplate.postForObject(uri, request, Map.class);
			}
			catch (HttpClientErrorException e) {
				// Do NOT retry — 4xx
				log.error("External Service threw a 4xx error: ", e);
				throw new ServiceCallException(e.getResponseBodyAsString());
			}
			catch (HttpServerErrorException e) {
				// 5xx → retry allowed
				log.error("External Service threw a 5xx error, retrying: ", e);
				throw e;
			}
			catch (ResourceAccessException e) {
				// ConnectException / SocketTimeoutException wrapped
				log.error("Connection related exception, retrying: ", e);
				throw e;
			}
			catch (Exception e) {
				log.error("Unknown exception: ", e);
				throw new ServiceCallException(e.getMessage());
			}

			return response;

		});
	}


	public String getShorteningURL(StringBuilder uri, Object request) {
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		String response = null;
		StringBuilder str = new StringBuilder(this.getClass().getCanonicalName()).append(".fetchResult:")
				.append(System.lineSeparator());
		str.append("URI: ").append(uri.toString()).append(System.lineSeparator());
		try {
			log.debug(str.toString());
			response = restTemplate.postForObject(uri.toString(), request, String.class);
		} catch (HttpClientErrorException e) {
			log.error("External Service threw an Exception: ", e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (Exception e) {
			log.error("Exception while fetching from searcher: ", e);
		}
		return response;
	}

}

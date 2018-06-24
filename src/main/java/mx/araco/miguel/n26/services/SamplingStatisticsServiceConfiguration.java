package mx.araco.miguel.n26.services;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @author MiguelAraCo
 */
@Configuration
@ConfigurationProperties( "mx.araco.miguel.n26" )
public class SamplingStatisticsServiceConfiguration {
	private Duration samplePeriod;
	private Duration samplingPeriod;

	public Duration getSamplePeriod() { return samplePeriod; }

	public void setSamplePeriod( Duration samplePeriod ) { this.samplePeriod = samplePeriod; }

	public Duration getSamplingPeriod() { return samplingPeriod; }

	public void setSamplingPeriod( Duration samplingPeriod ) { this.samplingPeriod = samplingPeriod; }
}

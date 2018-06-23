package mx.araco.miguel.n26;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @author MiguelAraCo
 */
@SpringBootApplication
public class Application {
	public static void main( String[] args ) {
		SpringApplication.run( Application.class, args );
	}

	@Configuration
	@ConfigurationProperties( "mx.araco.miguel.n26" )
	public static class StatisticsServiceConfiguration {
		private Duration samplePeriod;
		private Duration samplingPeriod;

		public Duration getSamplePeriod() { return samplePeriod; }

		public void setSamplePeriod( Duration samplePeriod ) { this.samplePeriod = samplePeriod; }

		public Duration getSamplingPeriod() { return samplingPeriod; }

		public void setSamplingPeriod( Duration samplingPeriod ) { this.samplingPeriod = samplingPeriod; }
	}
}

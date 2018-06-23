package mx.araco.miguel.n26;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.Instant;
import java.util.TimeZone;

/**
 * @author MiguelAraCo
 */
@SpringBootApplication
public class Application {
	public static void main( String[] args ) {
		TimeZone.setDefault( TimeZone.getTimeZone( "UTC" ) );
		System.out.println( Instant.now().toEpochMilli() );
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

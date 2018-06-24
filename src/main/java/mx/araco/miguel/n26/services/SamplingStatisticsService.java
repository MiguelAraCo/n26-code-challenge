package mx.araco.miguel.n26.services;

import mx.araco.miguel.n26.models.Statistics;
import mx.araco.miguel.n26.models.Transaction;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Implementation of {@link StatisticsService}. The statistics returned (and maintained by it)
 * are based on a configurable "sampling period".
 * <p>
 * When calling {@link SamplingStatisticsService#register(Transaction)}, the service will only
 * register the transaction if it happened somewhere in between the moment the method was called
 * minus the sampling period, and the moment the method was called (e.g. between 60s in the past
 * and now).
 * <p>
 * The statistics returned by {@link SamplingStatisticsService#get()} are only about the
 * transactions registered during that time.
 * <p>
 * The service uses a sampling strategy to keep execution time and memory constant (O(1)).
 * <p>
 * The drawback of this approach is that, depending on the time the statistics are requested,
 * the service may return statistics that include more data than the one received strictly in the
 * configured sampling period.
 * <p>
 * The margin of error will be determined by the configured sample period (the fragment of time
 * used by this service to store a statistics sample), and the frequency of transactions.
 *
 * @author MiguelAraCo
 */
@Service
public class SamplingStatisticsService implements StatisticsService {
	private final Duration samplePeriod;
	private final Duration samplingPeriod;
	private final int sampleSize;

	private Map<Instant, Statistics> samples;

	public SamplingStatisticsService( SamplingStatisticsServiceConfiguration configuration ) {
		this.samplePeriod = configuration.getSamplePeriod();
		this.samplingPeriod = configuration.getSamplingPeriod();

		this.sampleSize = ( (int) ( this.samplingPeriod.toNanos() / this.samplePeriod.toNanos() ) ) + 1;
		this.samples = Collections.emptyMap();
	}

	public void reset() {
		synchronized ( this ) {
			this.samples = Collections.emptyMap();
		}
	}

	public Statistics get() {
		synchronized ( this ) {
			return _get();
		}
	}

	private Statistics _get() {
		if ( this.samples.size() == 0 ) return new Statistics();

		Instant now = Instant.now();
		checkSamples( now );

		return getSamplesStatistics();
	}

	private Statistics getSamplesStatistics() {
		Statistics aggregate = new Statistics();
		this.samples.forEach( ( key, sample ) -> aggregate.add( sample ) );
		return aggregate;
	}

	public RegisterResult register( Transaction transaction ) {
		synchronized ( this ) {
			return _register( transaction );
		}
	}

	private RegisterResult _register( Transaction transaction ) {
		Instant now = Instant.now();

		if ( transactionHappensInTheFuture( transaction, now ) ) throw new IllegalArgumentException( "Transactions can't have timestamps of the future" );

		checkSamples( now );

		if ( transactionIsOutsideSamplingPeriod( transaction, now ) ) return RegisterResult.DISCARDED;

		Instant sampleKey = getSampleKey( transaction.getTimestamp() ).orElseThrow( IllegalStateException::new );
		updateStatistics( sampleKey, transaction );

		return RegisterResult.REGISTERED;
	}

	private boolean transactionHappensInTheFuture( Transaction transaction, Instant now ) {
		return transaction.getTimestamp().isAfter( now );
	}

	private boolean transactionIsOutsideSamplingPeriod( Transaction transaction, Instant now ) {
		return now.minus( this.samplingPeriod ).isAfter( transaction.getTimestamp() );
	}

	private void checkSamples( Instant now ) {
		if ( this.samples.size() == 0 ) {
			initializeSamples( now );
		} else if ( outsideCurrentSamplingPeriod( now ) ) {
			if ( allSamplesAreInvalid( now ) ) initializeSamples( now );
			else renewSamples( now );
		}
	}

	private void initializeSamples( Instant now ) {
		this.samples = new LinkedHashMap<>( sampleSize, 0.75f, false );

		Instant sampleKey = now.minus( samplingPeriod );
		for ( int i = 0; i < this.sampleSize; i++ ) {
			this.samples.put( sampleKey, new Statistics() );
			sampleKey = sampleKey.plus( samplePeriod );
		}
	}

	private boolean outsideCurrentSamplingPeriod( Instant now ) {
		return getPenultimateSampleKey()
			.map( penultimateSampleKey ->
				now.minus( this.samplingPeriod ).isAfter( penultimateSampleKey )
			)
			.orElse( false );
	}

	private Optional<Instant> getOldestSampleKey() {
		//noinspection LoopStatementThatDoesntLoop
		for ( Instant sampleKey : this.samples.keySet() ) {
			return Optional.of( sampleKey );
		}
		return Optional.empty();
	}

	private Optional<Instant> getPenultimateSampleKey() {
		Iterator<Instant> keys = this.samples.keySet().iterator();
		if ( keys.hasNext() ) {
			// Discard last key
			keys.next();
			return Optional.of( keys.next() );
		} else {
			return Optional.empty();
		}
	}

	private boolean allSamplesAreInvalid( Instant now ) {
		return getNewestSampleKey()
			.map( newestSampleKey ->
				now.minus( this.samplingPeriod ).isAfter( newestSampleKey )
			)
			.orElseThrow( IllegalStateException::new );
	}

	private Optional<Instant> getNewestSampleKey() {
		Instant newestSampleKey = null;

		for ( Instant sampleKey : this.samples.keySet() ) newestSampleKey = sampleKey;

		return Optional.ofNullable( newestSampleKey );
	}

	private void renewSamples( Instant now ) {
		int removed = 0;

		Iterator<Instant> sampleStarts = this.samples.keySet().iterator();
		while ( sampleStarts.hasNext() ) {
			Instant sampleStart = sampleStarts.next();
			if ( ! now.minus( samplingPeriod ).isAfter( sampleStart.plus( samplePeriod ) ) ) break;

			sampleStarts.remove();
			removed++;
		}

		Instant newestSampleStart = getNewestSampleKey().orElseThrow( IllegalStateException::new );
		for ( int i = 0; i < removed; i++ ) {
			newestSampleStart = newestSampleStart.plus( samplePeriod );
			this.samples.put( newestSampleStart, new Statistics() );
		}
	}

	private Optional<Instant> getSampleKey( Instant timestamp ) {
		return getOldestSampleKey()
			.flatMap( oldestSampleKey -> {
				long delta = Duration.between( oldestSampleKey, timestamp ).toNanos();
				int sampleKeyIndex = delta != 0 ? (int) ( delta / this.samplePeriod.toNanos() ) : 0;

				return getSampleKey( sampleKeyIndex );
			} );
	}

	private Optional<Instant> getSampleKey( int index ) {
		// Is the index out of range?
		if ( index >= this.samples.size() ) return Optional.empty();

		Duration difference = Duration.ZERO;
		for ( int i = 0; i < index; i++ ) {
			difference = difference.plus( this.samplePeriod );
		}

		// Assign the difference to a different variable that is effectively final to use it inside lambda
		Duration finalDifference = difference;

		return this
			.getOldestSampleKey()
			.map( oldestSampleKey -> oldestSampleKey.plus( finalDifference ) );
	}

	private void updateStatistics( Instant sampleKey, Transaction transaction ) {
		Statistics statistics = Optional.ofNullable( this.samples.get( sampleKey ) ).orElseThrow( IllegalArgumentException::new );
		statistics.add( transaction.getAmount() );
	}
}

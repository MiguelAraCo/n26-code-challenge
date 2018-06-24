package mx.araco.miguel.services;

import mx.araco.miguel.n26.Application;
import mx.araco.miguel.n26.models.Statistics;
import mx.araco.miguel.n26.models.Transaction;
import mx.araco.miguel.n26.services.SamplingStatisticsService;
import mx.araco.miguel.n26.services.StatisticsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static org.junit.Assert.assertEquals;

/**
 * @author MiguelAraCo
 */
@RunWith( SpringRunner.class )
@SpringBootTest(
	properties = {
		"mx.araco.miguel.n26.sampling-period=PT1S",
		"mx.araco.miguel.n26.sample-period=PT0.1S"
	},
	classes = {
		Application.class
	}
)
public class SamplingStatisticsServiceTest {
	@Autowired
	private SamplingStatisticsService statisticsService;

	@Before
	public void resetStatisticsService() {
		this.statisticsService.reset();
	}

	@Test
	public void returnsREGISTEREDForTransactionsInsideSamplingPeriod() {
		Transaction transaction = new Transaction( new BigDecimal( "10.25" ), Instant.now() );

		assertEquals( "The service didn't return the expected result", StatisticsService.RegisterResult.REGISTERED, this.statisticsService.register( transaction ) );
	}

	@Test
	public void returnsDISCARDEDForTransactionsOutsideSamplingPeriod() {
		Transaction transaction = new Transaction( new BigDecimal( "10.25" ), Instant.now().minus( Duration.parse( "PT2S" ) ) );

		assertEquals( "The service didn't return the expected result", StatisticsService.RegisterResult.DISCARDED, this.statisticsService.register( transaction ) );
	}

	@Test( expected = IllegalArgumentException.class )
	public void throwsExceptionForTransactionsInTheFuture() {
		Transaction transaction = new Transaction( new BigDecimal( "10.25" ), Instant.now().plus( Duration.parse( "PT2S" ) ) );
		this.statisticsService.register( transaction );
	}

	@Test
	public void calculatesStatisticsCorrectly() throws Exception {
		List<Transaction> transactions = new ArrayList<>();
		transactions.add( new Transaction( new BigDecimal( "10.25" ), Instant.now().minus( Duration.parse( "PT0.1S" ) ) ) );
		transactions.add( new Transaction( new BigDecimal( "6.52" ), Instant.now().minus( Duration.parse( "PT0.2S" ) ) ) );
		transactions.add( new Transaction( new BigDecimal( "4.32" ), Instant.now().minus( Duration.parse( "PT0.2S" ) ) ) );
		transactions.add( new Transaction( new BigDecimal( "10" ), Instant.now().minus( Duration.parse( "PT0.3S" ) ) ) );
		transactions.add( new Transaction( new BigDecimal( "6.43" ), Instant.now().minus( Duration.parse( "PT0.15S" ) ) ) );
		transactions.add( new Transaction( new BigDecimal( "2.40" ), Instant.now().minus( Duration.parse( "PT0.13S" ) ) ) );
		transactions.add( new Transaction( new BigDecimal( "5.50" ), Instant.now().minus( Duration.parse( "PT0.2S" ) ) ) );
		transactions.add( new Transaction( new BigDecimal( "4.23" ), Instant.now().minus( Duration.parse( "PT0.8S" ) ) ) );

		ForkJoinPool myPool = new ForkJoinPool( 8 );
		myPool.submit( () ->
			transactions.parallelStream().forEach( this.statisticsService::register )
		).get();

		Statistics statistics = this.statisticsService.get();

		assertEquals( "The returned statistics didn't have the expected max", 0, statistics.getMax().compareTo( new BigDecimal( "10.25" ) ) );
		assertEquals( "The returned statistics didn't have the expected min", 0, statistics.getMin().compareTo( new BigDecimal( "2.4" ) ) );
		assertEquals( "The returned statistics didn't have the expected sum", 0, statistics.getSum().compareTo( new BigDecimal( "49.65" ) ) );
		assertEquals( "The returned statistics didn't have the expected count", 8, (long) statistics.getCount() );
		assertEquals( "The returned statistics didn't have the expected avg", 0, statistics.getAvg().compareTo( new BigDecimal( "6.21" ) ) ); // 6.20625 rounded up
	}

	@Test
	public void ignoresTransactionsOutsideSamplingPeriod() throws Exception {
		List<Transaction> transactions = new ArrayList<>();
		transactions.add( new Transaction( new BigDecimal( "10.25" ), Instant.now().minus( Duration.parse( "PT2S" ) ) ) ); // Outside
		transactions.add( new Transaction( new BigDecimal( "6.43" ), Instant.now().minus( Duration.parse( "PT0.1S" ) ) ) ); // Inside
		transactions.add( new Transaction( new BigDecimal( "6.52" ), Instant.now().minus( Duration.parse( "PT2S" ) ) ) ); // Outside
		transactions.add( new Transaction( new BigDecimal( "4" ), Instant.now().minus( Duration.parse( "PT0.1S" ) ) ) ); // Inside
		transactions.add( new Transaction( new BigDecimal( "2.40" ), Instant.now().minus( Duration.parse( "PT2S" ) ) ) ); // Outside
		transactions.add( new Transaction( new BigDecimal( "5.50" ), Instant.now().minus( Duration.parse( "PT0.1S" ) ) ) ); // Inside
		transactions.add( new Transaction( new BigDecimal( "10" ), Instant.now().minus( Duration.parse( "PT2S" ) ) ) ); // Outside
		transactions.add( new Transaction( new BigDecimal( "4.23" ), Instant.now().minus( Duration.parse( "PT0.1S" ) ) ) ); // Inside

		ForkJoinPool myPool = new ForkJoinPool( 8 );
		myPool.submit( () ->
			transactions.parallelStream().forEach( this.statisticsService::register )
		).get();

		Statistics statistics = this.statisticsService.get();

		assertEquals( "The returned statistics didn't have the expected max", 0, statistics.getMax().compareTo( new BigDecimal( "6.43" ) ) );
		assertEquals( "The returned statistics didn't have the expected min", 0, statistics.getMin().compareTo( new BigDecimal( "4" ) ) );
		assertEquals( "The returned statistics didn't have the expected sum", 0, statistics.getSum().compareTo( new BigDecimal( "20.16" ) ) );
		assertEquals( "The returned statistics didn't have the expected count", 4, (long) statistics.getCount() );
		assertEquals( "The returned statistics didn't have the expected avg", 0, statistics.getAvg().compareTo( new BigDecimal( "5.04" ) ) );
	}

	@Test
	public void discardsOldStatistics() throws Exception {
		List<Transaction> transactions = new ArrayList<>();
		// Transactions of which statistics are going to be discarded after sleeping
		transactions.add( new Transaction( new BigDecimal( "10.25" ), Instant.now().minus( Duration.parse( "PT0.8S" ) ) ) );
		transactions.add( new Transaction( new BigDecimal( "6.52" ), Instant.now().minus( Duration.parse( "PT0.8S" ) ) ) );
		transactions.add( new Transaction( new BigDecimal( "2.40" ), Instant.now().minus( Duration.parse( "PT0.8S" ) ) ) );
		transactions.add( new Transaction( new BigDecimal( "10" ), Instant.now().minus( Duration.parse( "PT0.8S" ) ) ) );

		// Transactions of which statistics will be left
		transactions.add( new Transaction( new BigDecimal( "6.43" ), Instant.now().minus( Duration.parse( "PT0.1S" ) ) ) );
		transactions.add( new Transaction( new BigDecimal( "4" ), Instant.now().minus( Duration.parse( "PT0.1S" ) ) ) );
		transactions.add( new Transaction( new BigDecimal( "5.50" ), Instant.now().minus( Duration.parse( "PT0.1S" ) ) ) );
		transactions.add( new Transaction( new BigDecimal( "4.23" ), Instant.now().minus( Duration.parse( "PT0.1S" ) ) ) );

		ForkJoinPool myPool = new ForkJoinPool( 8 );
		myPool.submit( () ->
			transactions.parallelStream().forEach( this.statisticsService::register )
		).get();

		Thread.sleep( 500 );

		Statistics statistics = this.statisticsService.get();

		assertEquals( "The returned statistics didn't have the expected max", 0, statistics.getMax().compareTo( new BigDecimal( "6.43" ) ) );
		assertEquals( "The returned statistics didn't have the expected min", 0, statistics.getMin().compareTo( new BigDecimal( "4" ) ) );
		assertEquals( "The returned statistics didn't have the expected sum", 0, statistics.getSum().compareTo( new BigDecimal( "20.16" ) ) );
		assertEquals( "The returned statistics didn't have the expected count", 4, (long) statistics.getCount() );
		assertEquals( "The returned statistics didn't have the expected avg", 0, statistics.getAvg().compareTo( new BigDecimal( "5.04" ) ) );
	}
}

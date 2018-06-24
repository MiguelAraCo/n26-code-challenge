package mx.araco.miguel.n26.services;

import mx.araco.miguel.n26.models.Statistics;
import mx.araco.miguel.n26.models.Transaction;

/**
 * Service in charge of calculating {@link Statistics} of the registered transactions.
 *
 * @author MiguelAraCo
 */
public interface StatisticsService {
	/**
	 * Returns a {@link Statistics} object with the statistics of the transactions received
	 * (the transactions included in the statistics are implementation-specific)
	 *
	 * @return statistics for the transactions registered by the service (potentially only
	 * for a certain time-period)
	 */
	Statistics get();

	/**
	 * Possible results of calling {@link StatisticsService#register(Transaction)}
	 */
	enum RegisterResult {
		REGISTERED,
		DISCARDED,
	}

	/**
	 * Register a transaction in the service so it is included in its statistics
	 *
	 * @param transaction
	 * 	the transaction to register, not null
	 * @return {@link RegisterResult#REGISTERED} if the transaction was registered
	 * {@link RegisterResult#DISCARDED} if it was discarded
	 */
	RegisterResult register( Transaction transaction );
}

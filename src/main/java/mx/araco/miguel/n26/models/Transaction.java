package mx.araco.miguel.n26.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * POJO representing a bank transaction
 *
 * @author MiguelAraCo
 */
public class Transaction {
	@NotNull
	private final BigDecimal amount;
	@NotNull
	private final Instant timestamp;

	@JsonCreator
	public Transaction(
		@JsonProperty( "amount" ) BigDecimal amount,
		@JsonProperty( "timestamp" ) Instant timestamp
	) {
		this.amount = amount;
		this.timestamp = timestamp;
	}

	public BigDecimal getAmount() { return amount; }

	public Instant getTimestamp() { return timestamp; }
}

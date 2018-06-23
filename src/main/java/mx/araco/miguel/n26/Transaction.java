package mx.araco.miguel.n26;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @author MiguelAraCo
 */
public class Transaction {
	private final BigDecimal amount;
	private final Instant timestamp;

	@JsonCreator
	public Transaction( @JsonProperty( "amount" ) BigDecimal amount, @JsonProperty( "timestamp" ) Instant timestamp ) {
		this.amount = amount;
		this.timestamp = timestamp;
	}

	public BigDecimal getAmount() { return amount; }

	public Instant getTimestamp() { return timestamp; }
}

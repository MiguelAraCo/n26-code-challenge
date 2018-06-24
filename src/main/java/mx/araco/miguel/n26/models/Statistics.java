package mx.araco.miguel.n26.models;

import java.math.BigDecimal;

/**
 * @author MiguelAraCo
 */
public class Statistics {
	private BigDecimal avg = new BigDecimal( "0" );
	private BigDecimal sum = new BigDecimal( "0" );
	private BigDecimal min = null;
	private BigDecimal max = null;
	private Long count = 0L;

	public void add( BigDecimal occurrence ) {
		if ( this.min == null || occurrence.compareTo( this.min ) < 0 ) this.min = occurrence;
		if ( this.max == null || occurrence.compareTo( this.max ) > 0 ) this.max = occurrence;
		this.count++;
		this.sum = this.sum.add( occurrence );
		this.avg = this.sum.divide( new BigDecimal( this.count ) );
	}

	public void add( Statistics sample ) {
		if ( sample.getMin() != null ) {
			if ( this.getMin() == null || sample.getMin().compareTo( this.getMin() ) < 0 ) this.setMin( sample.getMin() );
		}

		if ( sample.getMax() != null ) {
			if ( this.getMax() == null || sample.getMax().compareTo( this.getMax() ) > 0 ) this.setMax( sample.getMax() );
		}

		this.setSum( this.getSum().add( sample.getSum() ) );
		this.setCount( this.getCount() + sample.getCount() );
		if ( this.getCount() != 0 ) this.setAvg( this.getSum().divide( new BigDecimal( this.getCount() ) ) );

	}

	public BigDecimal getAvg() { return avg; }

	public void setAvg( BigDecimal avg ) { this.avg = avg; }

	public BigDecimal getSum() { return sum; }

	public void setSum( BigDecimal sum ) { this.sum = sum; }

	public BigDecimal getMin() { return min; }

	public void setMin( BigDecimal min ) { this.min = min; }

	public BigDecimal getMax() { return max; }

	public void setMax( BigDecimal max ) { this.max = max; }

	public Long getCount() { return count; }

	public void setCount( Long count ) { this.count = count; }
}

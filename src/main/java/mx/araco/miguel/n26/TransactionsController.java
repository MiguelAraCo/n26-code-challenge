package mx.araco.miguel.n26;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author MiguelAraCo
 */
@Controller
public class TransactionsController {
	private StatisticsService statisticsService;

	@RequestMapping( value = "transactions", method = RequestMethod.POST )
	public ResponseEntity<Void> addTransaction( @RequestBody Transaction transaction ) {
		StatisticsService.RegisterResult result = this.statisticsService.register( transaction );

		switch ( result ) {
			case REGISTERED:
				return new ResponseEntity<>( HttpStatus.CREATED );
			case DISCARDED:
				return new ResponseEntity<>( HttpStatus.NO_CONTENT );
			default:
				throw new IllegalStateException();
		}
	}

	@Autowired
	public void setStatisticsService( StatisticsService statisticsService ) { this.statisticsService = statisticsService; }
}

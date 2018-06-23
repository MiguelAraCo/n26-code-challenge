package mx.araco.miguel.n26;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author MiguelAraCo
 */
@RestController
public class StatisticsController {
	private StatisticsService statisticsService;

	@GetMapping( "statistics" )
	public ResponseEntity<Statistics> getLatestStatistics() {
		Statistics statistics = this.statisticsService.get();
		return new ResponseEntity<>( statistics, HttpStatus.OK );
	}

	@Autowired
	public void setStatisticsService( StatisticsService statisticsService ) { this.statisticsService = statisticsService; }
}

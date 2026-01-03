
package inventory.service;

import inventory.model.SalesRecord;
import java.util.List;

public class ForecastService {

    // Simple moving average forecast
    public static int forecastDemand(List<SalesRecord> history, int days) {

        if (history == null || history.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (SalesRecord record : history) {
            total += record.getQuantitySold();
        }

        int avgPerDay = total / history.size();
        return avgPerDay * days;
    }
}

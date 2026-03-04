package com.lld.bookmyshow.pricing;

import com.lld.bookmyshow.models.Show;
import com.lld.bookmyshow.models.ShowSeat;

/**
 * Pricing varies by show time: morning shows cheaper, prime-time expensive.
 * Weekend surcharge applied. Multiplied with seat base price.
 */
public class ShowTimePricingStrategy implements PricingStrategy {

    @Override
    public double calculatePrice(Show show, ShowSeat showSeat) {
        double basePrice = showSeat.getSeat().getSeatType().getBasePrice();
        double multiplier = getTimeMultiplier(show);
        double weekendSurcharge = isWeekend(show) ? 1.2 : 1.0;

        return basePrice * multiplier * weekendSurcharge;
    }

    private double getTimeMultiplier(Show show) {
        int hour = show.getStartTime().getHour();
        if (hour < 12) return 0.8;      // Morning: 20% discount
        if (hour < 17) return 1.0;      // Afternoon: base price
        return 1.3;                      // Evening/Night: 30% premium
    }

    private boolean isWeekend(Show show) {
        return show.getStartTime().getDayOfWeek().getValue() >= 6;
    }
}

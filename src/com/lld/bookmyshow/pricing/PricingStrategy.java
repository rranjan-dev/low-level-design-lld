package com.lld.bookmyshow.pricing;

import com.lld.bookmyshow.models.Show;
import com.lld.bookmyshow.models.ShowSeat;

public interface PricingStrategy {
    double calculatePrice(Show show, ShowSeat showSeat);
}

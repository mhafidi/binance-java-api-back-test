package com.binance.api.utils;

import com.binance.api.client.domain.market.Candlestick;

public class PivotPoints {

    public double r1;
    public  double r2;
    public double r3,s1,s2;
    public double pivot;
    public PivotPoints(Candlestick candlestick)
    {
        pivot=(Double.parseDouble(candlestick.getHigh())+Double.parseDouble(candlestick.getClose())+Double.parseDouble(candlestick.getLow()))/3;
        r1=2*pivot-Double.parseDouble(candlestick.getLow());
        r2=pivot+(Double.parseDouble(candlestick.getHigh())-Double.parseDouble(candlestick.getLow()));
        r3=Double.parseDouble(candlestick.getHigh())+2*(pivot-Double.parseDouble(candlestick.getLow()));
        s1=2*pivot-Double.parseDouble(candlestick.getHigh());
        s2=pivot-(Double.parseDouble(candlestick.getHigh())-Double.parseDouble(candlestick.getLow()));
    }

    public double getR1() {
        return r1;
    }

    public double getPivot() {
        return pivot;
    }
}

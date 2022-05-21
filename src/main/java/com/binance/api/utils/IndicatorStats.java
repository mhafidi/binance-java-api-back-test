package com.binance.api.utils;

import com.binance.api.client.domain.market.Candlestick;

import java.util.HashMap;
import java.util.List;

public class IndicatorStats {


    public static HashMap<String,Double> sma_period(List<Candlestick> candlesticks,int period)
    {
        HashMap<String, Double> hashMap= new HashMap<>();
        SizedStack<Double> sizedStack=new SizedStack<>(period);
        for(int i=0;i<candlesticks.size();i++)
        {
            sizedStack.push(Double.parseDouble(candlesticks.get(i).getClose()));
            if(sizedStack.size()==period)
                hashMap.put(candlesticks.get(i).getCloseTime().toString(),sizedStack.stream().mapToDouble(elt->elt).average().getAsDouble());

        }
        return hashMap;
    }
}

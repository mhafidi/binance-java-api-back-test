package com.binance.api.utils;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class IndicatorStats {
    static BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
    static BinanceApiRestClient client = factory.newRestClient();

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

    public static List<Candlestick> pullAllCandleStickHistory(String assetPair, CandlestickInterval candlestickInterval)
    {
        List<Candlestick> fullHistoryCandleSticks = new ArrayList<>();
        List<Candlestick> candlesticks_begin = client.getCandlestickBars(assetPair, candlestickInterval,1,0L,null);
        List<Candlestick> candlesticks_last = client.getCandlestickBars(assetPair, candlestickInterval,1,null,null);

        Long last=candlesticks_begin.get(0).getOpenTime();
        Long latestLast=candlesticks_last.get(0).getCloseTime();
        //Long last;
        List<Candlestick> candlesticks ;//= client.getCandlestickBars(assetPair, candlestickInterval,null,start,null);
        boolean finishedPull=false;

        while(!finishedPull)
        {
            candlesticks = client.getCandlestickBars(assetPair, candlestickInterval,null,last,null);
            fullHistoryCandleSticks.addAll(candlesticks);
            last=fullHistoryCandleSticks.get(fullHistoryCandleSticks.size()-1).getCloseTime();
            candlesticks_last = client.getCandlestickBars(assetPair, candlestickInterval,1,null,null);
            latestLast=candlesticks_last.get(0).getCloseTime();
            finishedPull=(last.equals(latestLast));


        }

      /*  DateFormat obj = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS Z");
        Date res = new Date(start);
        System.out.println(obj.format(res));

        res = new Date(latestLast);
        System.out.println(obj.format(res));*/
        return fullHistoryCandleSticks;
    }
}

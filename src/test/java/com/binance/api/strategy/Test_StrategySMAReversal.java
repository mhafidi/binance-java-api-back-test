package com.binance.api.strategy;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.utils.IndicatorStats;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

public class Test_StrategySMAReversal
{
    final double FEE_RATE=0.1/100;

    @Test
    public void strategy_SMA_reversal()
    {


        double capital=1000.0,startingCapitalValue=capital;
        double positionSize=0.1;//1% of the capital
        double positionValue,quantityOfBoughtAsset=0.0;

        Candlestick lastCandlestick=null;


        List<Candlestick> candlesticks = IndicatorStats.pullAllCandleStickHistory("BTCUSDT",CandlestickInterval.HOURLY);// client.getCandlestickBars("BTCUSDT", CandlestickInterval.HOURLY,1000,null,null);
        System.out.println("Number of CandleSticks of the sample: "+candlesticks.size());
        HashMap<String,Double> sma30 = IndicatorStats.sma_period(candlesticks,30);
        HashMap<String,Double> sma60 = IndicatorStats.sma_period(candlesticks,60);

        String closeTime,previousCloseTime;
        int i=2;
        while(i<candlesticks.size())
        {
            closeTime = candlesticks.get(i-1).getCloseTime().toString();
            previousCloseTime = candlesticks.get(i-2).getCloseTime().toString();
            lastCandlestick=candlesticks.get(i);
            if(sma30.containsKey(closeTime) && sma60.containsKey(closeTime) &&
                    sma30.containsKey(previousCloseTime) && sma60.containsKey(previousCloseTime))
            {
                if(sma30.get(closeTime)-sma60.get(closeTime)<0.1)//reversal
                {
                    if(sma30.get(previousCloseTime)-sma60.get(previousCloseTime)<0)//previously was in downtrend config
                    {
                        if(Double.parseDouble(candlesticks.get(i-1).getClose())>sma30.get(closeTime) && quantityOfBoughtAsset==0.0)
                        {
                            System.out.println("Open Position at price["+lastCandlestick.getClose()+"]");
                            positionValue=positionSize*capital;
                            capital=capital-positionValue;
                            positionValue=positionValue-positionValue*FEE_RATE;
                            quantityOfBoughtAsset=positionValue/Double.parseDouble(lastCandlestick.getClose());
                            System.out.println("Bought Asset Value["+quantityOfBoughtAsset+"]");
                        }

                    }
                    else if(quantityOfBoughtAsset!=0.0)
                    {
                        System.out.println("Position opened, proceed to close ");
                        System.out.println("close Position at price["+lastCandlestick.getClose()+"], " +
                                "Quantity["+quantityOfBoughtAsset+"]");
                        quantityOfBoughtAsset=quantityOfBoughtAsset-quantityOfBoughtAsset*FEE_RATE;
                        capital=capital+quantityOfBoughtAsset*Double.parseDouble(lastCandlestick.getClose());
                        quantityOfBoughtAsset=0.0;
                    }
                }
            }
            i++;
        }
        if(quantityOfBoughtAsset!=0.0)
        {
            System.out.println("Remaining position opened");
            System.out.println("Position opened, proceed to close ");
            System.out.println("close Position at price["+lastCandlestick.getClose()+"], " +
                    "Quantity["+quantityOfBoughtAsset+"]");
            quantityOfBoughtAsset=quantityOfBoughtAsset-quantityOfBoughtAsset*FEE_RATE;
            capital=capital+quantityOfBoughtAsset*Double.parseDouble(lastCandlestick.getClose());

        }

        double rate= (capital-startingCapitalValue);
        rate= rate/startingCapitalValue;
        rate = 100.0*rate;
        System.out.println("Last Capital Value ["+capital+"]\n" +
                "Starting Capital Value ["+startingCapitalValue+"]\n" +
                "P&L Value ["+rate+"%]");
    }

}

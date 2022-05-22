package com.binance.api.strategy;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.utils.IndicatorStats;
import com.binance.api.utils.PivotPoints;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.binance.api.utils.IndicatorStats.isADownTrendCandleStick;

public class Test_Strategy_BullishEngulfing
{
    final double FEE_RATE=0.1/100;

    @Test
    public void strategy_BullishEngulfing()
    {


        double capital=1000.0,startingCapitalValue=capital;
        double positionSize=0.1;//1% of the capital
        double positionValue,quantityOfBoughtAsset=0.0;



        List<Candlestick> candlesticks = IndicatorStats.pullAllCandleStickHistory("BTCUSDT", CandlestickInterval.HOURLY);// client.getCandlestickBars("BTCUSDT", CandlestickInterval.HOURLY,1000,null,null);
        System.out.println("Number of CandleSticks of the sample: "+candlesticks.size());
        HashMap<String,Double> sma30 = IndicatorStats.sma_period(candlesticks,6);
        int numberOfTotalPosition=0,numberOfProfitPosition=0,numberOfLosses=0;

        String closeTime;
        for(int i=2;i<candlesticks.size();i++)
        {
            closeTime = candlesticks.get(i-1).getCloseTime().toString();

            if(true)
            {
                if  (/*Double.parseDouble(candlesticks.get(i-1).getClose())>sma30.get(closeTime) &&*/
                        !isADownTrendCandleStick(candlesticks.get(i-1)) && isADownTrendCandleStick(candlesticks.get(i-2)) &&
                       // Double.parseDouble(candlesticks.get(i).getOpen())>sma30.get(closeTime) &&
                Double.parseDouble(candlesticks.get(i-1).getClose())>Double.parseDouble(candlesticks.get(i-2).getOpen()) &&
                        Double.parseDouble(candlesticks.get(i-1).getOpen())<Double.parseDouble(candlesticks.get(i-2).getClose()))

                {
                    numberOfTotalPosition++;
                    double sellPrice=-1;
                    double open = Double.parseDouble(candlesticks.get(i).getOpen());
                    double close = Double.parseDouble(candlesticks.get(i).getClose());
                    PivotPoints pivotPointsSell = new PivotPoints(candlesticks.get(i));
                    System.out.println("Open Position at price["+open+"]");
                    positionValue=positionSize*capital;
                    capital=capital-positionValue;
                    positionValue=positionValue-positionValue*FEE_RATE;
                    quantityOfBoughtAsset=positionValue/open;
                    System.out.println("Bought Asset Value["+quantityOfBoughtAsset+"]");
                    if(pivotPointsSell.s1>Double.parseDouble(candlesticks.get(i).getLow()))
                        sellPrice = pivotPointsSell.s1;

                    if (sellPrice == 0.0 || sellPrice==-1)
                    {


                        if(pivotPointsSell.r1<=Double.parseDouble(candlesticks.get(i).getHigh()))
                        {
                           sellPrice=pivotPointsSell.r1;

                        }
                        else
                        {
                           sellPrice=close;
                        }

                    }
                    else
                    {
                        sellPrice=close;

                    }
                    if(open<sellPrice)
                        numberOfProfitPosition++;
                    else
                        numberOfLosses++;
                    System.out.println("Position opened, proceed to close ");
                    System.out.println("close Position at price["+sellPrice+"], " +
                            "Quantity["+quantityOfBoughtAsset+"]");
                    quantityOfBoughtAsset=quantityOfBoughtAsset-quantityOfBoughtAsset*FEE_RATE;
                    capital=capital+quantityOfBoughtAsset*sellPrice;
                    sellPrice=0.0;
                }

            }

        }


        double rate= (capital-startingCapitalValue);
        rate= rate/startingCapitalValue;
        rate = 100.0*rate;
        System.out.println("Last Capital Value ["+capital+"]\n" +
                "Starting Capital Value ["+startingCapitalValue+"]\n" +
                "P&L Value ["+rate+"%]");
        System.out.println("Number Of Open Positions ["+numberOfTotalPosition+"]\n" +
                "Number Of Profitable Positions ["+numberOfProfitPosition+"]\n" +
                "Number Of Lost Positions ["+numberOfLosses+"]");
    }

}

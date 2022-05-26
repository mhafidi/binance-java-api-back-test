package com.binance.api.strategy;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.utils.IndicatorStats;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

public class Test_StrategySMAReversal_v1
{
    final double FEE_RATE=0.1/100;
    int globalOpenedPositions =0;
    int globalProfitPositions =0;
    int globalLostPositions =0;
    @Test
    public void strategy_SMA_reversal()
    {
        double capital=0.0,init=1000;

        capital+=sma_reversal("BTCUSDT",0.5,0.1,init);
        capital+=sma_reversal("ETHUSDT",0.5,0.1,init);
        capital+=sma_reversal("DOTUSDT",0.5,0.1,init);
        capital+=sma_reversal("XRPUSDT",0.5,0.1,init);
        capital+=sma_reversal("ADAUSDT",0.5,0.1,init);
        capital+=sma_reversal("TRXUSDT",0.5,0.1,init);
        capital+=sma_reversal("LTCUSDT",0.5,0.1,init);
        capital+=sma_reversal("NEARUSDT",0.5,0.1,init);
        capital+=sma_reversal("LINKUSDT",0.5,0.1,init);
        capital+=sma_reversal("XLMUSDT",0.5,0.1,init);
        System.out.println("======================================");
        System.out.println("Final capital: "+init*10);
        System.out.println("Final capital: "+capital);

        System.out.println("Global Opened positions: "+globalOpenedPositions);
        System.out.println("Global Lost positions: "+globalLostPositions);
        System.out.println("Global Profit position: "+globalProfitPositions);

    }

    private double sma_reversal(String assetPair,double agressive,double defensive, double initialCapital)
    {
        System.out.println("============="+assetPair+"==================");
        double capital=initialCapital,startingCapitalValue=capital;
        double positionSize=agressive,openPositionPrice=0.0,closePositionPrice=0.0;//1% of the capital
        double positionValue,quantityOfBoughtAsset=0.0;
        int numberOfTotalPosition=0,numberOfProfitPosition=0,numberOfLosses=0;
        Candlestick lastCandlestick=null;


        List<Candlestick> candlesticks = IndicatorStats.pullAllCandleStickHistoryStartingFrom(assetPair,CandlestickInterval.HOURLY,"21-November-2021");// client.getCandlestickBars("BTCUSDT", CandlestickInterval.HOURLY,1000,null,null);
        System.out.println("Number of CandleSticks of the sample: "+candlesticks.size());
        HashMap<String,Double> sma30 = IndicatorStats.sma_period(candlesticks,110);
        HashMap<String,Double> sma60 = IndicatorStats.sma_period(candlesticks,200);

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
                            numberOfTotalPosition++;
                            openPositionPrice=Double.parseDouble(lastCandlestick.getClose());
                            positionValue=positionSize*capital;
                            capital=capital-positionValue;
                            positionValue=positionValue-positionValue*FEE_RATE;
                            quantityOfBoughtAsset=positionValue/openPositionPrice;
                        }

                    }
                    else if(quantityOfBoughtAsset!=0.0)
                    {
                        closePositionPrice=Double.parseDouble(lastCandlestick.getClose());
                        if(closePositionPrice>openPositionPrice)
                            numberOfProfitPosition++;
                        else
                            numberOfLosses++;
                        quantityOfBoughtAsset=quantityOfBoughtAsset-quantityOfBoughtAsset*FEE_RATE;
                        capital=capital+quantityOfBoughtAsset*closePositionPrice;
                        quantityOfBoughtAsset=0.0;
                    }
                }
            }
            i++;
        }
        if(quantityOfBoughtAsset!=0.0)
        {
            closePositionPrice=Double.parseDouble(lastCandlestick.getClose());
            if(closePositionPrice>openPositionPrice)
                numberOfProfitPosition++;
            else
                numberOfLosses++;
            quantityOfBoughtAsset=quantityOfBoughtAsset-quantityOfBoughtAsset*FEE_RATE;
            capital=capital+quantityOfBoughtAsset*closePositionPrice;
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
        globalOpenedPositions+=numberOfTotalPosition;
        globalLostPositions+=numberOfLosses;
        globalProfitPositions+=numberOfProfitPosition;

        System.out.println("=====================================");

        return capital;
    }

}

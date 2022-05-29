package com.binance.api.strategy;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.utils.IndicatorStats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class StrategySMAReversal implements Runnable
{
    final double FEE_RATE=0.1/100;
    int globalOpenedPositions =0;
    int globalProfitPositions =0;
    int globalLostPositions =0;
    int smaFast;
    int smaSlow;
    double initialCapital;
    double ret;
    ConcurrentHashMap<String,List<Candlestick>> candleStickLists = new ConcurrentHashMap<>();
    ArrayList<String> assetPairs;
    boolean done=false;

    public StrategySMAReversal(int smaFast, int smaSlow, ConcurrentHashMap<String, List<Candlestick>> candleStickLists, ArrayList<String> assetPairs,double initialCapital) {
        this.initialCapital=initialCapital;
        this.smaFast = smaFast;
        this.smaSlow = smaSlow;
        this.candleStickLists = candleStickLists;
        this.assetPairs = assetPairs;
    }

    private double sma_reversal(String assetPair, double agressive, double defensive, double initialCapital, int smaFast, int smaSlow)
    {
        //System.out.println("============="+assetPair+"==================");
        double capital=initialCapital,startingCapitalValue=capital;
        double positionSize=agressive,openPositionPrice=0.0,closePositionPrice=0.0;//1% of the capital
        double positionValue,quantityOfBoughtAsset=0.0;
        int numberOfTotalPosition=0,numberOfProfitPosition=0,numberOfLosses=0;
        Candlestick lastCandlestick=null;


        List<Candlestick> candlesticks = candleStickLists.get(assetPair);
        //IndicatorStats.pullAllCandleStickHistoryStartingFrom(assetPair,CandlestickInterval.HOURLY,"21-November-2021");// client.getCandlestickBars("BTCUSDT", CandlestickInterval.HOURLY,1000,null,null);
        // System.out.println("Number of CandleSticks of the sample: "+candlesticks.size());
        HashMap<String,Double> sma30 = IndicatorStats.sma_period(candlesticks,smaFast);
        HashMap<String,Double> sma60 = IndicatorStats.sma_period(candlesticks,smaSlow);

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
//        System.out.println("Last Capital Value ["+capital+"]\n" +
//                "Starting Capital Value ["+startingCapitalValue+"]\n" +
//                "P&L Value ["+rate+"%]");
//        System.out.println("Number Of Open Positions ["+numberOfTotalPosition+"]\n" +
//                "Number Of Profitable Positions ["+numberOfProfitPosition+"]\n" +
//                "Number Of Lost Positions ["+numberOfLosses+"]");
        globalOpenedPositions+=numberOfTotalPosition;
        globalLostPositions+=numberOfLosses;
        globalProfitPositions+=numberOfProfitPosition;

//        System.out.println("=====================================");

        return capital;
    }
    private void strategy_SMA_reversal(int smaFast,int smaSlow)
    {


        ret=candleStickLists.keySet().parallelStream().mapToDouble(pairs->sma_reversal(pairs,0.5,0.1,initialCapital,smaFast,smaSlow)).sum();
//        System.out.println("======================================");
//        System.out.println("Final capital: "+init*10);
//        System.out.println("Final capital: "+capital);
//
//        System.out.println("Global Opened positions: "+globalOpenedPositions);
//        System.out.println("Global Lost positions: "+globalLostPositions);
//        System.out.println("Global Profit position: "+globalProfitPositions);
      done=true;

    }
    @Override
    public void run() {

        strategy_SMA_reversal(smaFast,smaSlow);
    }

    public double getRet() {
        return ret;
    }

    public boolean isDone() {
        return done;
    }

    public int getSmaFast() {
        return smaFast;
    }

    public int getSmaSlow() {
        return smaSlow;
    }
}

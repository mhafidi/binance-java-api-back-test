package com.binance.api.strategy;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.utils.IndicatorStats;
import com.binance.api.utils.SizedStack;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.IntStream;

public class Test_StrategySMAReversal_v1
{
    final double FEE_RATE=0.1/100;
    int globalOpenedPositions =0;
    int globalProfitPositions =0;
    int globalLostPositions =0;
     ExecutorService mainThreadService = Executors.newFixedThreadPool(1002);
     ArrayList<StrategySMAReversal> resStrategySMAReversals;


    ConcurrentHashMap<String,List<Candlestick>> candleStickLists = new ConcurrentHashMap<>();
    ArrayList<String> assetPairs = new ArrayList<String>() {
        {
            add("BTCUSDT");
            add("ETHUSDT");
            add("DOTUSDT");
            add("XRPUSDT");
            add("ADAUSDT");
            add("TRXUSDT");
            add("LTCUSDT");
            add("NEARUSDT");
            add("LINKUSDT");
            add("XLMUSDT");
        }
    };
    
    @Test
    public void statistical_inspection() throws InterruptedException {
        initialization();
        int smaFast=0,smaSlow=0;
        double pnl=0.0,pnlTmp=0.0;
//        Toto mm;
//        IntStream.range(10,200).parallel().
//                mapToObj(
//                        index->IntStream.range(index+1,200).
//                                mapToObj(index2->{
//                    pnlTmp=strategy_SMA_reversal(index,index2);
//                    if(pnlTmp>pnl)
//                    {
//                        pnl=pnlTmp;
//                        smaFast=index;
//                        smaSlow=index2;
//                        mm = new Toto(smaFast,smaSlow,pnl);
//                    }
//                    return mm;
//                    )).

        for(int i=10;i<300;i++)
        {
            for(int j=i+1;j<300;j++)
            {
                StrategySMAReversal strategySMAReversal= new StrategySMAReversal(i,j,candleStickLists,assetPairs,1000.0);
                resStrategySMAReversals.add(strategySMAReversal);
               // mainThreadService.submit(strategySMAReversal);

//
//                pnlTmp=strategy_SMA_reversal(i,j);
//                globalOpenedPositions =0;
//                globalProfitPositions =0;
//                globalLostPositions =0;
//                if(pnlTmp>pnl)
//                {
//                    pnl=pnlTmp;
//                    smaFast=i;
//                    smaSlow=j;
//                    System.out.println("*SMA Fast***["+smaFast+"] SMA Slow***["+smaSlow+"]*PNL     ***["+pnl+"]*");
               // }
            }
        }
        System.out.println("start threads");


        for(StrategySMAReversal strategySMAReversal:resStrategySMAReversals)
        {
            while(((ThreadPoolExecutor)mainThreadService).getActiveCount()>1000)
            {
                System.out.println("waiting 20 s to lower the pressure");
                System.out.println("the current running index is["+resStrategySMAReversals.indexOf(strategySMAReversal)+"]");
                System.out.println("remaining tasks to start["+(resStrategySMAReversals.size()-resStrategySMAReversals.indexOf(strategySMAReversal))+"]");
                Thread.sleep(20000);
            }
            mainThreadService.submit(strategySMAReversal);
        }
//        resStrategySMAReversals.parallelStream().
//                forEach(strategySMAReversal -> mainThreadService.submit(strategySMAReversal));
        boolean notAllDone=false;
        System.out.println("all tasks started");
        long numberOfDone;
        while(!notAllDone)
        {
            notAllDone=!resStrategySMAReversals.parallelStream().
                    anyMatch(strategySMAReversal -> !strategySMAReversal.isDone());

            //numberOfDone=resStrategySMAReversals.parallelStream().filter(StrategySMAReversal::isDone).count();

        }

        System.out.println("NumberOfDoneTasks:"+resStrategySMAReversals.parallelStream().filter(StrategySMAReversal::isDone).count());
        StrategySMAReversal res=resStrategySMAReversals.parallelStream().max(Comparator.comparingDouble(StrategySMAReversal::getRet)).get();

       // System.out.println();
        System.out.println("*****************************************");
        System.out.println("*SMA Fast***["+res.getSmaFast()+"]****************");
        System.out.println("*SMA Slow***["+res.getSmaSlow()+"]****************");
        System.out.println("*PNL     ***["+res.getRet()+"]********************");
    }



    private void initialization()
    {
        List<Candlestick> candlesticks;
        System.out.println("********Starting collecting historical market data***********");
        for(String assetPair:assetPairs)
        {
            System.out.println("====collecting data of the pair ["+assetPair+"]=========");
            candlesticks = IndicatorStats.pullAllCandleStickHistoryStartingFrom(assetPair, CandlestickInterval.HOURLY, "21-November-2021");
            candleStickLists.put(assetPair,candlesticks);
        }

        resStrategySMAReversals= new ArrayList<>();
        System.out.println("**************Market Data collection ended***************************");
    }
    private double strategy_SMA_reversal(int smaFast,int smaSlow)
    {
        double capital,init=1000;

        capital=candleStickLists.keySet().parallelStream().mapToDouble(pairs->sma_reversal(pairs,0.5,0.1,init,smaFast,smaSlow)).sum();
//        System.out.println("======================================");
//        System.out.println("Final capital: "+init*10);
//        System.out.println("Final capital: "+capital);
//
//        System.out.println("Global Opened positions: "+globalOpenedPositions);
//        System.out.println("Global Lost positions: "+globalLostPositions);
//        System.out.println("Global Profit position: "+globalProfitPositions);
        return capital;

    }

    private double sma_reversal(String assetPair,double agressive,double defensive, double initialCapital,int smaFast,int smaSlow)
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


}

package com.binance.api.strategy;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.utils.IndicatorStats;
import com.binance.api.utils.PivotPoints;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.binance.api.utils.IndicatorStats.isADownTrendCandleStick;

public class Test_Strategy_BullishEngulfing_enhanced_v1
{
    final double FEE_RATE=0.1/100;
    BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
    BinanceApiRestClient client = factory.newRestClient();

    int globalOpenedPositions=0;
    int globalLostPositions=0;
    int globalProfitPositions=0;
    @Test
    public void strategy_BullishEngulfing()
    {

        double capital=0.0,init=1000;

        capital+=enhancedBullishEngulfing("BTCUSDT",0.5,0.1,init);
        capital+=enhancedBullishEngulfing("ETHUSDT",0.5,0.1,init);
        capital+=enhancedBullishEngulfing("DOTUSDT",0.5,0.1,init);
        capital+=enhancedBullishEngulfing("XRPUSDT",0.5,0.1,init);
        capital+=enhancedBullishEngulfing("ADAUSDT",0.5,0.1,init);
        capital+=enhancedBullishEngulfing("TRXUSDT",0.5,0.1,init);
        capital+=enhancedBullishEngulfing("LTCUSDT",0.5,0.1,init);
        capital+=enhancedBullishEngulfing("NEARUSDT",0.5,0.1,init);
        capital+=enhancedBullishEngulfing("LINKUSDT",0.5,0.1,init);
        capital+=enhancedBullishEngulfing("XLMUSDT",0.5,0.1,init);
        System.out.println("======================================");
        System.out.println("Final capital: "+init*10);
        System.out.println("Final capital: "+capital);

        System.out.println("Global Opened positions: "+globalOpenedPositions);
        System.out.println("Global Lost positions: "+globalLostPositions);
        System.out.println("Global Profit position: "+globalProfitPositions);

    }

    private double enhancedBullishEngulfing(String assetPair,double agressive,double defensive, double initialCapital)
    {

        System.out.println("============="+assetPair+"==================");
        double capital=initialCapital,startingCapitalValue=capital;
        double positionSize;//1% of the capital
        double positionValue,quantityOfBoughtAsset=0.0;


        List<Candlestick> candlesticks = IndicatorStats.pullAllCandleStickHistoryStartingFrom(assetPair, CandlestickInterval.HOURLY,"8-November-2015");// client.getCandlestickBars("BTCUSDT", CandlestickInterval.HOURLY,1000,null,null);
        System.out.println("Number of CandleSticks of the sample: "+candlesticks.size());
        HashMap<String,Double> sma50 = IndicatorStats.sma_period(candlesticks,50);
        HashMap<String,Double> sma100 = IndicatorStats.sma_period(candlesticks,100);
        int numberOfTotalPosition=0,numberOfProfitPosition=0,numberOfLosses=0;
        double sellPrice;
        PivotPoints pivotPoints;
        String closeTime;
        double open,close;
        double openPrice=0.0,stopPrice=0.0,tPPrice=0.0;
        Candlestick currentCandleStick;
        int i=2,keep=0;
        while(i<candlesticks.size())
        {
            pivotPoints = new PivotPoints(candlesticks.get(i-2));
            closeTime = candlesticks.get(i-1).getCloseTime().toString();
            currentCandleStick= candlesticks.get(i);
            if(sma50.containsKey(closeTime) && sma100.containsKey(closeTime) && Double.parseDouble(candlesticks.get(i-1).getClose())>sma50.get(closeTime) && quantityOfBoughtAsset==0.0 &&
                    sma50.get(closeTime)>sma100.get(closeTime))
            {
                positionSize=agressive;
                if  (//isADownTrendCandleStick(candlesticks.get(i-3)) && Double.parseDouble(candlesticks.get(i-3).getClose())>Double.parseDouble(candlesticks.get(i-2).getOpen()) &&
                        !isADownTrendCandleStick(candlesticks.get(i-1)) && isADownTrendCandleStick(candlesticks.get(i-2)) &&
                Double.parseDouble(candlesticks.get(i-1).getClose())>Double.parseDouble(candlesticks.get(i-2).getOpen()) &&
                        Double.parseDouble(candlesticks.get(i-1).getOpen())<Double.parseDouble(candlesticks.get(i-2).getClose())&&
                        Double.parseDouble(candlesticks.get(i-1).getClose())>pivotPoints.r2 && Double.parseDouble(candlesticks.get(i-1).getLow())<sma50.get(closeTime))

                {
                    PivotPoints pivotPointsSell = new PivotPoints(candlesticks.get(i-1));
                    if(pivotPointsSell.s1>=Double.parseDouble(candlesticks.get(i).getLow()))
                    {
                        numberOfTotalPosition++;
                        positionValue = positionSize * capital;
                        capital = capital - positionValue;
                        positionValue = positionValue - positionValue * FEE_RATE;
                        openPrice = pivotPointsSell.s1;
                        quantityOfBoughtAsset = positionValue / openPrice;
                        stopPrice = (Double.parseDouble(candlesticks.get(i - 1).getLow())+Double.parseDouble(candlesticks.get(i - 1).getHigh()))/2;//TODO optimize the stop price to avoid a quick stop
                        tPPrice = new PivotPoints(candlesticks.get(i - 1)).r2;
                    }


                }

            }
            if(quantityOfBoughtAsset!=0.0)
            {
                if(stopPrice>=Double.parseDouble(currentCandleStick.getClose()))
                {
                    quantityOfBoughtAsset=quantityOfBoughtAsset-quantityOfBoughtAsset*FEE_RATE;
                    capital=capital+quantityOfBoughtAsset*Double.parseDouble(currentCandleStick.getClose());
                    if(Double.parseDouble(currentCandleStick.getClose())<openPrice)
                        numberOfLosses++;
                    else
                        numberOfProfitPosition++;

                    openPrice=0.0;
                    stopPrice=0.0;
                    quantityOfBoughtAsset=0.0;
                }
                else if(tPPrice<=Double.parseDouble(currentCandleStick.getClose()) )
                {
                    quantityOfBoughtAsset=quantityOfBoughtAsset-quantityOfBoughtAsset*FEE_RATE;
                    capital=capital+quantityOfBoughtAsset*Double.parseDouble(currentCandleStick.getClose());
                    if(Double.parseDouble(currentCandleStick.getClose())<openPrice)
                        numberOfLosses++;
                    else
                        numberOfProfitPosition++;

                    openPrice=0.0;
                    stopPrice=0.0;
                    quantityOfBoughtAsset=0.0;
                }
                else
                {
                    stopPrice = (Double.parseDouble(candlesticks.get(i).getLow())); //(Double.parseDouble(candlesticks.get(i).getLow())+Double.parseDouble(candlesticks.get(i).getHigh()))/2;
                    tPPrice = new PivotPoints(currentCandleStick).r1;

                }

            }

            i++;

        }
        System.out.println("======================================");
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



        return capital;
    }


}

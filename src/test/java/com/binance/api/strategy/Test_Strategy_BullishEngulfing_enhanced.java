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

public class Test_Strategy_BullishEngulfing_enhanced
{
    final double FEE_RATE=0.1/100;
    BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
    BinanceApiRestClient client = factory.newRestClient();
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

    }

    private double enhancedBullishEngulfing(String assetPair,double agressive,double defensive, double initialCapital)
    {

        System.out.println("============="+assetPair+"==================");
        double capital=initialCapital,startingCapitalValue=capital;
        double positionSize;//1% of the capital
        double positionValue,quantityOfBoughtAsset=0.0;


        List<Candlestick> candlesticks = IndicatorStats.pullAllCandleStickHistoryStartingFrom(assetPair, CandlestickInterval.HOURLY,"8-November-2015");// client.getCandlestickBars("BTCUSDT", CandlestickInterval.HOURLY,1000,null,null);
        System.out.println("Number of CandleSticks of the sample: "+candlesticks.size());
        HashMap<String,Double> sma30 = IndicatorStats.sma_period(candlesticks,30);
        int numberOfTotalPosition=0,numberOfProfitPosition=0,numberOfLosses=0;
        double sellPrice,buyPrice;
        PivotPoints pivotPoints;
        String closeTime;
        double open,close;
        for(int i=2;i<candlesticks.size();i++)
        {
            pivotPoints = new PivotPoints(candlesticks.get(i-2));
            closeTime = candlesticks.get(i-1).getCloseTime().toString();

            if(sma30.containsKey(closeTime) && Double.parseDouble(candlesticks.get(i-1).getClose())>sma30.get(closeTime))
            {
                positionSize=agressive;
                if  (
                        isADownTrendCandleStick(candlesticks.get(i-3)) && Double.parseDouble(candlesticks.get(i-3).getClose())>Double.parseDouble(candlesticks.get(i-2).getOpen()) &&
                        !isADownTrendCandleStick(candlesticks.get(i-1)) && isADownTrendCandleStick(candlesticks.get(i-2)) &&
                Double.parseDouble(candlesticks.get(i-1).getClose())>Double.parseDouble(candlesticks.get(i-2).getOpen()) &&
                        Double.parseDouble(candlesticks.get(i-1).getOpen())<Double.parseDouble(candlesticks.get(i-2).getClose())
                  && Double.parseDouble(candlesticks.get(i-1).getClose())>pivotPoints.r1)

                {
                    //oneMinCandleSticks= client.getCandlestickBars(assetPair,CandlestickInterval.ONE_MINUTE,null,candlesticks.get(i).getOpenTime(),candlesticks.get(i).getCloseTime());
                    PivotPoints pivotPointsSell = new PivotPoints(candlesticks.get(i-1));
                    if(pivotPointsSell.s1>=Double.parseDouble(candlesticks.get(i).getLow()))
                    {
                        numberOfTotalPosition++;
                        close = Double.parseDouble(candlesticks.get(i).getClose());


                        buyPrice = pivotPointsSell.s1;
                        positionValue = positionSize * capital;
                        capital = capital - positionValue;
                        positionValue = positionValue - positionValue * FEE_RATE;
                        quantityOfBoughtAsset = positionValue / buyPrice;
                        sellPrice = close;
                        if(pivotPointsSell.r1<=Double.parseDouble(candlesticks.get(i).getHigh()))
                            sellPrice=pivotPointsSell.r1;



                        if (buyPrice < sellPrice)
                            numberOfProfitPosition++;
                        else
                            numberOfLosses++;
                        quantityOfBoughtAsset = quantityOfBoughtAsset - quantityOfBoughtAsset * FEE_RATE;
                        capital = capital + quantityOfBoughtAsset * sellPrice;
                    }
                }

            }
//            else
//            {
//                positionSize=defensive;
//                if  (/*Double.parseDouble(candlesticks.get(i-1).getClose())>sma30.get(closeTime) &&*/
//                        !isADownTrendCandleStick(candlesticks.get(i-1)) && isADownTrendCandleStick(candlesticks.get(i-2)) &&
//                                // Double.parseDouble(candlesticks.get(i).getOpen())>sma30.get(closeTime) &&
//                                Double.parseDouble(candlesticks.get(i-1).getClose())>Double.parseDouble(candlesticks.get(i-2).getOpen()) &&
//                                Double.parseDouble(candlesticks.get(i-1).getOpen())<Double.parseDouble(candlesticks.get(i-2).getClose()))
//
//                {
//                    numberOfTotalPosition++;
//
//                    open = Double.parseDouble(candlesticks.get(i).getOpen());
//                    close = Double.parseDouble(candlesticks.get(i).getClose());
//                    PivotPoints pivotPointsSell = new PivotPoints(candlesticks.get(i));
//                    positionValue=positionSize*capital;
//                    capital=capital-positionValue;
//                    positionValue=positionValue-positionValue*FEE_RATE;
//                    quantityOfBoughtAsset=positionValue/open;
//
//
//                    if(pivotPointsSell.r1<=Double.parseDouble(candlesticks.get(i).getHigh()))
//                    {
//                        sellPrice=pivotPointsSell.r1;
//
//                    }
//                    else
//                    {
//                        sellPrice=close;
//                    }
//
//                    if(open<sellPrice)
//                        numberOfProfitPosition++;
//                    else
//                        numberOfLosses++;
//                    quantityOfBoughtAsset=quantityOfBoughtAsset-quantityOfBoughtAsset*FEE_RATE;
//                    capital=capital+quantityOfBoughtAsset*sellPrice;
//                }
//            }

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



        return capital;
    }


}

package com.binance.api.strategy;

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

    @Test
    public void strategy_BullishEngulfing()
    {

        double capital=0.0;

        //capital+=enhancedBullishEngulfing("LUNAUSDT",0.5,0.1);
        capital+=enhancedBullishEngulfing("BTCUSDT",0.5,0.1);
        capital+=enhancedBullishEngulfing("DOTUSDT",0.5,0.1);
        capital+=enhancedBullishEngulfing("XRPUSDT",0.5,0.1);
        capital+=enhancedBullishEngulfing("ADAUSDT",0.5,0.1);
        capital+=enhancedBullishEngulfing("TRXUSDT",0.5,0.1);
        capital+=enhancedBullishEngulfing("LTCUSDT",0.5,0.1);
        capital+=enhancedBullishEngulfing("NEARUSDT",0.5,0.1);
        capital+=enhancedBullishEngulfing("LINKUSDT",0.5,0.1);
        capital+=enhancedBullishEngulfing("XLMUSDT",0.5,0.1);
        System.out.println("======================================");
        System.out.println("Final capital: "+10000);
        System.out.println("Final capital: "+capital);

    }

    private double enhancedBullishEngulfing(String assetPair,double agressive,double defensive)
    {

        System.out.println("============="+assetPair+"==================");
        double capital=1000.0,startingCapitalValue=capital;
        double positionSize=agressive;//1% of the capital
        double positionValue,quantityOfBoughtAsset=0.0;


        List<Candlestick> candlesticks = IndicatorStats.pullAllCandleStickHistory(assetPair, CandlestickInterval.HOURLY);// client.getCandlestickBars("BTCUSDT", CandlestickInterval.HOURLY,1000,null,null);
        System.out.println("Number of CandleSticks of the sample: "+candlesticks.size());
        HashMap<String,Double> sma30 = IndicatorStats.sma_period(candlesticks,30);
        int numberOfTotalPosition=0,numberOfProfitPosition=0,numberOfLosses=0;
        double sellPrice=-1;
        String closeTime;
        double open,close;
        for(int i=2;i<candlesticks.size();i++)
        {
            closeTime = candlesticks.get(i-1).getCloseTime().toString();

            if(sma30.containsKey(closeTime) && Double.parseDouble(candlesticks.get(i-1).getClose())>sma30.get(closeTime))
            {
                positionSize=agressive;
                if  (/*Double.parseDouble(candlesticks.get(i-1).getClose())>sma30.get(closeTime) &&*/
                        !isADownTrendCandleStick(candlesticks.get(i-1)) && isADownTrendCandleStick(candlesticks.get(i-2)) &&
                       // Double.parseDouble(candlesticks.get(i).getOpen())>sma30.get(closeTime) &&
                Double.parseDouble(candlesticks.get(i-1).getClose())>Double.parseDouble(candlesticks.get(i-2).getOpen()) &&
                        Double.parseDouble(candlesticks.get(i-1).getOpen())<Double.parseDouble(candlesticks.get(i-2).getClose()))

                {
                    numberOfTotalPosition++;
                    open = Double.parseDouble(candlesticks.get(i).getOpen());
                    close = Double.parseDouble(candlesticks.get(i).getClose());
                    PivotPoints pivotPointsSell = new PivotPoints(candlesticks.get(i));
                    positionValue=positionSize*capital;
                    capital=capital-positionValue;
                    positionValue=positionValue-positionValue*FEE_RATE;
                    quantityOfBoughtAsset=positionValue/open;
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
                    quantityOfBoughtAsset=quantityOfBoughtAsset-quantityOfBoughtAsset*FEE_RATE;
                    capital=capital+quantityOfBoughtAsset*sellPrice;
                    sellPrice=0.0;
                }

            }
            else
            {
                positionSize=defensive;
                if  (/*Double.parseDouble(candlesticks.get(i-1).getClose())>sma30.get(closeTime) &&*/
                        !isADownTrendCandleStick(candlesticks.get(i-1)) && isADownTrendCandleStick(candlesticks.get(i-2)) &&
                                // Double.parseDouble(candlesticks.get(i).getOpen())>sma30.get(closeTime) &&
                                Double.parseDouble(candlesticks.get(i-1).getClose())>Double.parseDouble(candlesticks.get(i-2).getOpen()) &&
                                Double.parseDouble(candlesticks.get(i-1).getOpen())<Double.parseDouble(candlesticks.get(i-2).getClose()))

                {
                    numberOfTotalPosition++;

                    open = Double.parseDouble(candlesticks.get(i).getOpen());
                    close = Double.parseDouble(candlesticks.get(i).getClose());
                    PivotPoints pivotPointsSell = new PivotPoints(candlesticks.get(i));
                    positionValue=positionSize*capital;
                    capital=capital-positionValue;
                    positionValue=positionValue-positionValue*FEE_RATE;
                    quantityOfBoughtAsset=positionValue/open;
                    if(pivotPointsSell.r2<Double.parseDouble(candlesticks.get(i).getHigh()))
                        sellPrice = pivotPointsSell.r2;

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
        return capital;
    }


}

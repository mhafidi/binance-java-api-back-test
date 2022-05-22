package com.binance.api.draft;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.utils.IndicatorStats;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ToolsTest
{
    @Test
    public void test_values()
    {
        List<Candlestick> candlestickList=IndicatorStats.pullAllCandleStickHistory("BTCUSDT",CandlestickInterval.DAILY);
        DateFormat obj = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS Z");
        Date res = new Date(candlestickList.get(0).getCloseTime());
        System.out.println("Start Date: "+obj.format(res));

        res = new Date(candlestickList.get(candlestickList.size()-1).getCloseTime());
        System.out.println("End Date: "+obj.format(res));

        System.out.println("Number of daily Candles: "+candlestickList.size());
        List<Candlestick> candlestickList_min=IndicatorStats.pullAllCandleStickHistory("BTCUSDT",CandlestickInterval.HOURLY);
        System.out.println("Number of Hourly Candles: "+candlestickList_min.size());

    }
}

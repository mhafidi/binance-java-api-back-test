package com.binance.api.strategy;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.utils.FileCandleStickWriter;
import com.binance.api.utils.IndicatorStats;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DumpDataToFiles
{
    ConcurrentHashMap<String, List<Candlestick>> candleStickLists = new ConcurrentHashMap<>();
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
    public void dump_data_to_files()
    {
        List<Candlestick> candlesticks;
        System.out.println("********Starting collecting historical market data***********");
        for(String assetPair:assetPairs)
        {
            System.out.println("====collecting data of the pair ["+assetPair+"]=========");
            candlesticks = IndicatorStats.pullAllCandleStickHistoryStartingFrom(assetPair, CandlestickInterval.HOURLY, "21-November-2005");
            candleStickLists.put(assetPair,candlesticks);
        }
        System.out.println("**************Market Data collection ended***************************");

        writeDataToFiles();
    }

    private void writeDataToFiles() {

        FileCandleStickWriter fileCandleStickWriter;
        for(String assetPair:candleStickLists.keySet())
        {
            fileCandleStickWriter = new FileCandleStickWriter(assetPair+".json",candleStickLists.get(assetPair));
            fileCandleStickWriter.write();

        }
    }
}

package com.binance.api.utils;

import org.junit.Test;

import java.io.File;

public class Test_FileCandleSticksReader
{
    FileCandleSticksReader fileCandleSticksReader;



    @Test
    public  void testCandleSticksReader()
    {

        fileCandleSticksReader = new FileCandleSticksReader("src"+ File.separator+"main"+File.separator+"resources");
        fileCandleSticksReader.uploadMarketCandleSticks();
        System.out.println(fileCandleSticksReader.getPairsCandleSticks().values().size());
    }
}

package com.binance.api.utils;

import com.binance.api.client.domain.market.Candlestick;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class FileCandleStickWriter
{
    String fileName;
    List<Candlestick> candlesticks;


    public FileCandleStickWriter(String fileName, List<Candlestick> candlesticks) {
        this.fileName = fileName;
        this.candlesticks = candlesticks;
    }

    public void write()  {
        try {
            File file = new File("src" + File.separator + "main" + File.separator + "resources" + File.separator + fileName);
            FileWriter fileWriter = new FileWriter(file, true);
            for (Candlestick candlestick : candlesticks) {
                try {
                    fileWriter.write(candlestick.toString());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            fileWriter.flush();
            fileWriter.close();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());

        }
    }
}

package com.binance.api.utils;

import com.binance.api.client.domain.market.Candlestick;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class FileCandleSticksWriter
{
    String fileName;
    List<Candlestick> candlesticks;


    public FileCandleSticksWriter(String fileName, List<Candlestick> candlesticks) {
        this.fileName = fileName;
        this.candlesticks = candlesticks;
    }

    public void write()  {
        try {
            File file = new File("src" + File.separator + "main" + File.separator + "resources" + File.separator + fileName);
            FileWriter fileWriter = new FileWriter(file, true);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(candlesticks);
            try {
                fileWriter.write(json);
            } catch (Exception e) {
                System.out.println(e.getMessage());
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

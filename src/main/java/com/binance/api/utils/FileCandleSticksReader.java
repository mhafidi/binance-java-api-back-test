package com.binance.api.utils;

import com.binance.api.client.domain.market.Candlestick;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileCandleSticksReader
{
    ConcurrentHashMap<String,List<Candlestick>> pairsCandleSticks;

    String pathDir;

    public FileCandleSticksReader(String pathDir) {
        this.pathDir = pathDir;
        pairsCandleSticks = new ConcurrentHashMap<>();
    }

    public void uploadMarketCandleSticks()
    {
        File f = new File(pathDir);
        List<String> filesPath= Arrays.stream(Objects.requireNonNull(f.list())).map(fileName->pathDir+File.separator+fileName).collect(Collectors.toList());
        filesPath.forEach(this::parseFile);
    }

    private void parseFile(String filePath)
    {
        File f= new File(filePath);
        List<Candlestick> candlesticks= new ArrayList<>();
        String pairName=f.getName().replace(".json","");
        System.out.println("pulling pair ["+pairName+"]");
        ObjectMapper mapper = new ObjectMapper();
        try(Stream<String> lFileStream = Files.lines(f.toPath()))
        {

            lFileStream.forEach(line-> {
                try {
                    List l=(mapper.readValue(line, new TypeReference<List<Candlestick>>(){}));
                    candlesticks.addAll(l);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        pairsCandleSticks.put(pairName,candlesticks);
    }

    public ConcurrentHashMap<String, List<Candlestick>> getPairsCandleSticks() {
        return pairsCandleSticks;
    }
}

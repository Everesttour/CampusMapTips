package com.skhu.tips.model.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.skhu.tips.model.entity.Building;
import com.skhu.tips.model.entity.Facility;

/**
 * JSON 파일을 읽어서 Building과 Facility 객체로 변환하는 유틸리티 클래스
 * Gson 라이브러리 사용
 */
public class JsonDataLoader {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * JSON 파일을 읽어서 문자열로 반환합니다.
     * 클래스패스와 파일 시스템 경로를 모두 시도합니다.
     * 
     * @param resourcePath 리소스 경로 (예: "data/buildings.json")
     * @return JSON 파일 내용
     * @throws Exception 파일을 읽을 수 없을 때
     */
    private static String readJsonFile(String resourcePath) throws Exception {
        // 1. 클래스패스에서 리소스 로드 시도
        InputStream inputStream = JsonDataLoader.class.getClassLoader()
            .getResourceAsStream(resourcePath);
        
        if (inputStream == null) {
            // 2. 파일 시스템에서 로드 시도
            java.io.File file = new java.io.File("src/resources/" + resourcePath);
            if (file.exists()) {
                inputStream = new java.io.FileInputStream(file);
            } else {
                throw new Exception("리소스를 찾을 수 없습니다: " + resourcePath);
            }
        }
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line).append("\n");
            }
            return jsonContent.toString();
        }
    }
    
    /**
     * buildings.json 파일을 읽어서 Building 리스트로 변환합니다.
     * 
     * @return Building 객체 리스트
     * @throws Exception JSON 파일을 읽을 수 없을 때
     */
    public static List<Building> loadBuildings() throws Exception {
        String json = readJsonFile("data/buildings.json");
        Building[] buildings = gson.fromJson(json, Building[].class);
        return Arrays.asList(buildings);
    }
    
    /**
     * facilities.json 파일을 읽어서 Facility 리스트로 변환합니다.
     * 
     * @return Facility 객체 리스트
     * @throws Exception JSON 파일을 읽을 수 없을 때
     */
    public static List<Facility> loadFacilities() throws Exception {
        String json = readJsonFile("data/facilities.json");
        Facility[] facilities = gson.fromJson(json, Facility[].class);
        return Arrays.asList(facilities);
    }
}

package com.skhu.tips.model.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.skhu.tips.model.entity.Building;
import com.skhu.tips.model.entity.Facility;

/**
 * JSON 파일을 읽어서 Building과 Facility 객체로 변환하는 유틸리티 클래스
 * Gson 라이브러리 사용 (JAR 배포 호환 수정됨)
 */
public class JsonDataLoader {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * JSON 파일을 읽어서 문자열로 반환합니다.
     * 수정됨: File I/O를 제거하고 오직 getResourceAsStream만 사용
     * * @param resourcePath 리소스 경로 (예: "/resources/data/buildings.json")
     * @return JSON 파일 내용
     */
    private static String readJsonFile(String resourcePath) throws Exception {
        // [중요] 경로가 '/'로 시작하지 않으면 붙여줌 (절대 경로 통일)
        if (!resourcePath.startsWith("/")) {
            resourcePath = "/" + resourcePath;
        }

        // 1. 클래스패스에서 리소스 스트림 확보 (JAR 내부 파일 읽기용)
        // getClass().getResourceAsStream()을 사용하면 배포 환경에서도 문제없음
        try (InputStream inputStream = JsonDataLoader.class.getResource(resourcePath).openStream()) {
            if (inputStream == null) {
                throw new Exception("리소스를 찾을 수 없습니다 (null stream): " + resourcePath);
            }

            // 2. 스트림 내용을 문자열로 변환
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder jsonContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line).append("\n");
                }
                return jsonContent.toString();
            }
        } catch (Exception e) {
            // 디버깅을 위해 에러 로그 출력
            System.err.println("[JsonDataLoader] 읽기 실패: " + resourcePath);
            throw e;
        }
    }

    /**
     * buildings.json 파일을 읽어서 Building 리스트로 변환합니다.
     */
    public static List<Building> loadBuildings() {
        try {
            // [수정] 경로를 /resources/data/... 로 명시
            String json = readJsonFile("/resources/data/buildings.json");
            Building[] buildings = gson.fromJson(json, Building[].class);
            return Arrays.asList(buildings);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList(); // 에러 발생 시 빈 리스트 반환
        }
    }

    /**
     * facilities.json 파일을 읽어서 Facility 리스트로 변환합니다.
     */
    public static List<Facility> loadFacilities() {
        try {
            // [수정] 경로를 /resources/data/... 로 명시
            String json = readJsonFile("/resources/data/facilities.json");
            Facility[] facilities = gson.fromJson(json, Facility[].class);
            return Arrays.asList(facilities);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList(); // 에러 발생 시 빈 리스트 반환
        }
    }
}
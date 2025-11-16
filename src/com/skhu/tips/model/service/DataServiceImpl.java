package com.skhu.tips.model.service;

import java.util.List;

import com.skhu.tips.model.entity.Building;
import com.skhu.tips.model.entity.Facility;

/**
 * @class DataService
 * @brief 애플리케이션의 핵심 데이터(Building, Facility)를 담고 있는 저장소
 * 핵심 로직을 상단에, 데이터 초기화 로직을 하단에 배치
 */
public class DataServiceImpl implements DataService {

    // --- 1. Fields (데이터 저장소 및 상태) ---

    private final List<Building> allBuildings;
    private final List<Facility> allFacilities;

    private Building selectedBuilding;
    private Facility selectedFacility;


    // --- 2. Constructor (생성자) ---

    public DataServiceImpl() {
        // JSON 파일에서 데이터 로드 시도
        List<Building> buildings;
        List<Facility> facilities;
        
        try {
            buildings = JsonDataLoader.loadBuildings();
            facilities = JsonDataLoader.loadFacilities();
            
            System.out.printf("[DataService] JSON 데이터 로드 성공. Buildings: %d, Facilities: %d%n",
                              buildings.size(), facilities.size());
        } catch (Exception e) {
            System.err.println("[DataService] JSON 데이터 로드 실패: " + e.getMessage());
            e.printStackTrace();
            // 폴백: 빈 리스트로 초기화
            buildings = new java.util.ArrayList<>();
            facilities = new java.util.ArrayList<>();
            System.out.println("[DataService] 빈 데이터로 초기화되었습니다.");
        }
        
        // final 필드에 할당
        this.allBuildings = buildings;
        this.allFacilities = facilities;
    }


    // --- 3. Public API (데이터 접근 및 상태 관리) ---

    @Override
	public List<Building> getBuildings() { return allBuildings; }
    @Override
	public List<Facility> getFacilities() { return allFacilities; }

    @Override
	public Building getSelectedBuilding() { return selectedBuilding; }
    @Override
	public Facility getSelectedFacility() { return selectedFacility; }

    @Override
	public void setSelectedBuilding(Building building) {
        this.selectedBuilding = building;
    }
    @Override
	public void setSelectedFacility(Facility facility) {
        this.selectedFacility = facility;
    }

    /**
     * @brief ID로 건물을 찾는 헬퍼 메소드
     */
    @Override
	public Building getBuildingById(int id) {
        return allBuildings.stream()
                           .filter(b -> b.getId() == id)
                           .findFirst()
                           .orElse(null);
    }

    /**
     * @brief ID로 시설을 찾는 헬퍼 메소드
     */
    @Override
	public Facility getFacilityById(int id) {
        return allFacilities.stream()
                            .filter(f -> f.getId() == id)
                            .findFirst()
                            .orElse(null);
    }


}
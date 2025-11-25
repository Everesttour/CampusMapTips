package com.skhu.tips.controller;

import com.skhu.tips.model.entity.Building;
import com.skhu.tips.model.entity.Facility;
import com.skhu.tips.model.service.DataService;
import com.skhu.tips.view.map.MapPanel;

/**
 * @class MapControllerImpl
 * @brief MapController 구현체. Null 체크 제거 버전.
 */
public class MapControllerImpl implements MapController {

    private MapPanel mapPanel;
    private DataService dataService;
    private PanelController panelController;

    // =======================================================================
    // --- 1. Configuration & Initialization ---
    // =======================================================================

    @Override
    public void configure(MapPanel mapPanel, DataService dataService, PanelController panelController) {
        this.mapPanel = mapPanel;
        this.dataService = dataService;
        this.panelController = panelController;

        // 컨트롤러 등록 (체크 없이 바로 호출)
        this.mapPanel.setupController(this);
    }

    @Override
    public void loadInitialData() {
        System.out.println("[MapController] 초기 데이터 로딩 시작...");

        // 데이터 로딩 (체크 없이 바로 호출)
        this.mapPanel.setMapData(
            dataService.getBuildings(),
            dataService.getFacilities()
        );
    }

    // =======================================================================
    // --- 2. Event Handling ---
    // =======================================================================

    @Override
    public void onBuildingClicked(Building building) {
        System.out.println("[MapController] 건물 클릭됨: " + building.getName());
        // 체크 없이 바로 호출
        panelController.openBuildingDetail(building);
    }

    @Override
    public void onFacilityClicked(Facility facility) {
        System.out.println("[MapController] 시설 클릭됨: " + facility.getName());
        // 체크 없이 바로 호출
        panelController.openFacilityDetail(facility);
    }

    // =======================================================================
    // --- 3. Business Logic ---
    // =======================================================================

    @Override
    public void focusOn(Building building) {
    	this.mapPanel.setSizeMap2(building);
    }

    @Override
    public void focusOn(Facility facility) {
    	this.mapPanel.setSizeMap2(facility);
    }

    @Override
    public void switchToBuildingView() {
    	this.mapPanel.setZoomLevel(1.0);
        this.mapPanel.setSizeMap(0, 0);
    }

    @Override
    public void switchToFacilityView() {
    	this.mapPanel.setZoomLevel(1.05);
        this.mapPanel.setSizeMap(0, 0);
    }
    
    
}
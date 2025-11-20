package com.skhu.tips.controller;

import com.skhu.tips.model.entity.Building;
import com.skhu.tips.model.entity.Facility;
import com.skhu.tips.model.service.DataService;
import com.skhu.tips.view.map.MapPanel;

/**
 * @class MapControllerImpl
 * @brief MapController 구현체. 2단계 초기화 패턴 적용.
 */
public class MapControllerImpl implements MapController {

    private MapPanel mapPanel;
    private DataService dataService;
    private PanelController panelController;

    // =======================================================================
    // --- 1. Configuration & Initialization (초기화 로직 분리) ---
    // =======================================================================

    /**
     * @brief [Step 1] 의존성 주입 (Wiring)
     * 단순히 객체 주소를 변수에 저장하기만 합니다. 절대 충돌이 나지 않습니다.
     */
    @Override
    public void configure(MapPanel mapPanel, DataService dataService, PanelController panelController) {
        this.mapPanel = mapPanel;
        this.dataService = dataService;
        this.panelController = panelController;
    }

    /**
     * @brief [Step 2] 초기 데이터 로딩 (Logic)
     * Main에서 모든 설정이 끝난 뒤 호출하므로, 안전하게 다른 객체를 사용할 수 있습니다.
     */
    @Override
    public void loadInitialData() {
        if (this.mapPanel != null && this.dataService != null) {
            System.out.println("[MapController] 초기 데이터 로딩 시작...");
            this.mapPanel.setMapData(
                dataService.getBuildings(),
                dataService.getFacilities()
            );
        }
    }

    // =======================================================================
    // --- 2. Business Logic ---
    // =======================================================================

    @Override
    public void focusOn(Building building) {
        System.out.println("[MapController] Focus on Building: " + building.getName());
        // TODO: 나중에 MapPanel에 이동 명령 내리기
    }

    @Override
    public void focusOn(Facility facility) {
        System.out.println("[MapController] Focus on Facility: " + facility.getName());
        // TODO: 나중에 MapPanel에 이동 명령 내리기
    }

    @Override
    public void switchToBuildingView() {
        // TODO: 뷰 모드 전환 로직
    }

    @Override
    public void switchToFacilityView() {
        // TODO: 뷰 모드 전환 로직
    }
}
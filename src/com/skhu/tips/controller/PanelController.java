package com.skhu.tips.controller;

import com.skhu.tips.model.entity.Building;
import com.skhu.tips.model.entity.Facility;
import com.skhu.tips.model.service.DataService;
import com.skhu.tips.view.panel.MainLeftPanel;

/**
 * @interface PanelController
 * @brief 좌측 패널(리스트, 팝업) 제어 기능을 정의하는 인터페이스 (계약).
 */
public interface PanelController {

    /**
     * @brief 건물 또는 시설 목록 뷰를 전환합니다.
     */
    void switchToBuildingView();
    void switchToFacilityView();

    /**
     * @brief 건물 상세 정보 팝업창을 엽니다.
     */
    void openBuildingDetail(Building building);

    /**
     * @brief 시설 상세 정보 팝업창을 엽니다.
     */
    void openFacilityDetail(Facility facility);

    // --- [요청사항 반영] DI를 위한 Setter ---

    /**
     * @brief PanelController가 MapController의 기능을 호출할 수 있도록
     * MapController 인터페이스를 주입받습니다. (Setter DI)
     * @param mapController MapController의 인터페이스
     */
    void initialize(MainLeftPanel mainLeftPanel, DataService dataService, MapController mapController);
}
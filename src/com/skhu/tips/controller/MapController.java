package com.skhu.tips.controller;

import com.skhu.tips.model.entity.Building;
import com.skhu.tips.model.entity.Facility;
import com.skhu.tips.model.service.DataService;
import com.skhu.tips.view.map.MapPanel;

/**
 * @interface MapController
 * @brief 지도 제어 기능을 정의하는 인터페이스 (계약).
 */
public interface MapController {

    /**
     * @brief 특정 건물의 위치로 지도의 초점을 이동시킵니다.
     */
    void focusOn(Building building);

    /**
     * @brief 특정 시설의 위치로 지도의 초점을 이동시킵니다.
     */
    void focusOn(Facility facility);

    /**
     * @brief 맵 뷰를 '건물 뷰' (넓은 상태)로 전환합니다.
     */
    void switchToBuildingView();

    /**
     * @brief 맵 뷰를 '시설 뷰' (좁은 상태)로 전환합니다.
     */
    void switchToFacilityView();

    // --- [요청사항 반영] DI를 위한 Setter ---

    /**
     * @brief MapController가 PanelController의 기능을 호출할 수 있도록
     * PanelController 인터페이스를 주입받습니다. (Setter DI)
     * @param panelController PanelController의 인터페이스
     */
    void initialize(MapPanel mapPanel, DataService dataService, PanelController panelController);
}
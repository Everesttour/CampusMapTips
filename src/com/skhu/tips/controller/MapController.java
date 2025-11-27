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
     * @brief 1단계: 의존성 주입 (Wiring)
     * 필요한 객체들을 멤버 변수에 할당만 합니다. 로직 실행은 하지 않습니다.
     */
    void configure(MapPanel mapPanel, DataService dataService, PanelController panelController);

    /**
     * @brief 2단계: 초기 데이터 로딩 (Logic)
     * 모든 의존성 설정이 완료된 후 호출되어, 실제 데이터를 뷰에 뿌립니다.
     */
    void loadInitialData();

    /**
     * @brief 지도에서 건물이 클릭되었을 때 호출됩니다.
     */
    void onBuildingClicked(Building building);

    /**
     * @brief 지도에서 시설이 클릭되었을 때 호출됩니다.
     */
    void onFacilityClicked(Facility facility);
    
    /**
     * @brief 지도에서 앱정보 버튼이 클릭되었을 때 호출됩니다.
     */
    void onAppInfoClicked();
}
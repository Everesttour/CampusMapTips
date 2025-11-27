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
    
    /**
     * @brief 앱 정보 팝업창을 엽니다.
     */
    void openAppInfoDetail();

    // --- [개선된 DI 패턴 적용] ---

    /**
     * @brief 1단계: 의존성 주입 (Wiring)
     * 필요한 객체들을 멤버 변수에 할당만 합니다. 로직 실행은 하지 않습니다.
     */
    void configure(MainLeftPanel mainLeftPanel, DataService dataService, MapController mapController);

    /**
     * @brief 2단계: 초기 데이터 로딩 (Logic)
     * 모든 의존성 설정이 완료된 후 호출되어, 리스너를 등록하고 초기 데이터를 뷰에 채웁니다.
     */
    void loadInitialData();
}
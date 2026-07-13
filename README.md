# 성공회대학교 캠퍼스 꿀팁 지도

건물, 교내 시설, 학생 꿀팁을 지도에서 찾아보는 정적 웹앱입니다.

## 구성

- `src/`: 기존 Java Swing 데스크톱 앱과 원본 데이터·이미지
- `web/`: React + TypeScript + Vite 기반의 GitHub Pages 웹앱
- `.github/workflows/deploy-pages.yml`: `main` 브랜치 푸시 시 GitHub Pages 배포

원본 데이터는 `src/resources/data`에 유지합니다. 웹 빌드 시 JSON과 지도는 정적 파일로 복사되고, 상세 사진은 WebP로 축소·변환되어 배포물에만 포함됩니다.

## 로컬 실행

```bash
cd web
npm install
npm run dev
```

## GitHub Pages 활성화

GitHub 저장소의 **Settings → Pages → Build and deployment**에서 Source를 **GitHub Actions**로 한 번만 선택하세요. 이후 `main` 브랜치에 푸시하면 자동 배포됩니다.

기본 공개 주소는 `https://everesttour.github.io/CampusMapTips/`입니다.

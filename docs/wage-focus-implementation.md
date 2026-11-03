# ‘시급 우선’ Android 적용 결과

2026-11-03 · `design/android-ui-redesign`

사용자가 선택한 ‘시급 우선’ 시안을 실제 Jetpack Compose 홈 화면에 반영했다.
이 문서는 초기 리디자인 보고서의 홈 구성·색상 설명보다 우선한다.

## 반영한 화면

- **알바생 홈:** 짙은 녹색(#232820) 공고 면에 36sp 시급을 먼저 표시하고 가게·업종·시간·거리·노란 지원 버튼을 배치했다. 나머지 공고는 업종 아이콘과 시급이 있는 목록이다.
- **점주 홈:** 오렌지(#FF8546) 현황 요약에 실제 모집 수와 지원자 수를 표시하고, 짙은 공고 등록 버튼을 넣었다. 공고는 상태·지원자 수·업무·일정·시급을 보존한 구분선 목록으로 바꿨다.
- **공통:** 흰 읽기 배경, 역할별 브랜드 헤더, 노랑(#FFBD16)/오렌지 채움과 짙은 읽기용 강조색을 적용했다. 의미 없는 그림자나 그라디언트는 추가하지 않았다.

## 시안과 기능 보존의 차이

- 시안에 없던 즉시지원 토글과 각 공고의 직접 지원 버튼도 기존 기능을 유지하기 위해 제공한다.
- ‘시급 우선’은 읽는 순서다. 시급 높은 순으로 정렬하거나 서버의 공고 순서를 바꾸지 않았다.
- 전달된 첫 긴급 공고를 강조하며, 긴급 공고가 없을 때는 첫 일반 공고를 표시한다. 일반 공고를 긴급으로 표시하지 않는다.
- 나머지 모든 공고는 목록에서 접근할 수 있다. 상태가 APPLIED로 바뀌면 지원 완료로 표시하고 버튼을 비활성화한다.
- 마감 문구·시간·거리·금액은 기존 모델의 값을 사용한다. 지원자 수 null을 0으로 대체하지 않는다.
- 작은 화면·큰 글자에 대응해 FlowRow와 최소 높이를 사용했다. 시안의 작은 버튼보다 실제 조작 영역을 크게 유지했다.
- 넓은 창의 기존 grid·navigation rail과 기존 탐색·지도·필터 화면을 유지했다.

## 변경 범위

`WorkerHomeScreen.kt`, 신규 `WorkerFocusJobCard.kt`, `OwnerHomeScreen.kt`, `OwnerJobCard.kt`,
신규 `HomeBrandHeader.kt`, Color/Dimens 토큰, 문자열 및 디자인 문서.
ViewModel·domain·repository·API·세션·라우트·Gradle 의존성과 기존 테스트에는 변경이 없다.

작업 폴더:
`C:/Users/leese/Documents/Codex/2026-11-02/android-ui-kotlin-android-ui-ui/work/android-redesign`

## 검증

- `compileDebugKotlin` 통과.
- 필수 `./docs/scripts/verify.sh` 실행: 67개 중 66개 통과, 1개 실패. 실패는 기존 `ProfileCameraFilesTest.refusesTraversalGalleryNamesAndSymlinkTargets`의 Windows 심볼릭 링크 생성 권한 문제다. 앞선 변경 전 커밋에서도 동일하게 재현됐다.
- 확인한 21개 불투명 색상 쌍 모두 4.5:1 이상. 짙은 면 위 주요 글자 15.04:1, 보조 글자 8.73:1, 오렌지 면 위 보조 글자 4.96:1.
- `git diff --check` 통과. 양쪽 홈의 320/360/840dp 및 200% 글자 Preview 소스를 보존했으며 Compose 컴파일에 포함됐다.
- 실제 Android 실행, 네이티브 프리뷰 렌더, 기기 스크린샷, TalkBack·회전·IME·실제 서버 플로우는 이번 수정에서 검증하지 않았다. 채팅의 가상 화면을 Android 스크린샷으로 간주하지 않는다.
- 사용자 컴퓨터의 화면을 조작하지 않았다. 기존 작업 폴더의 변경 사항도 건드리지 않았다.

최종 lint 및 APK 빌드 결과와 커밋은 아래 기록에 덧붙인다.

## 산출물

- `긴급알바-wage-focus-demo.apk`: 역할 선택 후 양쪽 홈을 볼 수 있는 기존 mock 모드의 debug APK.
- `긴급알바-wage-focus-live.apk`: 기존 로그인부터 시작하는 live 모드의 debug APK.
- `contrast-check-wage-focus.json`, `verification-wage-focus.log`, `verification-wage-focus-live.log`, `verification-wage-focus-demo.log`: 계산·검증 근거.
- 원격 push, 이슈·PR 생성 및 merge는 하지 않는다.

스타일 가이드, 선택 결정, 프로젝트 현황 문서를 갱신했다. failure-memory는 수정하지 않았다.

## 최종 기록

- Live: `gradlew.bat lintDebug assembleDebug --console=plain` 성공. Lint 0 errors, 36 warnings. 새로 쓰이지 않게 된 이전 홈 문자열 등 리소스 경고와 기존 의존성/Manifest 경고가 남아 있다.
- Demo: `gradlew.bat assembleDebug "-Pdemo.enabled=true" --console=plain` 성공. 저장한 live APK의 USE_DEMO_DATA=false, demo APK의 USE_DEMO_DATA=true를 확인했다.
- 단위 테스트 전체 성공으로 표시하지 않는다. 필수 verify.sh는 앞서 설명한 기존 카메라 테스트 1개 때문에 종료 코드 1이었다. 이후 lint/APK만 별도로 완료했다.
- 구현 커밋: `7b1ad93` (`feat: 선택한 시급 우선 홈 디자인 적용`). 이후 결과 문서만 별도 커밋으로 기록한다.
- `delivery-manifest-wage-focus.json`에 최종 Git 상태, 커밋과 두 APK의 SHA-256을 기록한다.


## 2026-11-03 실제 Android 화면 캡처 후속

초기 APK 검증 이후, 캡처 전용 AOSP Android 15(API 35) 이미지를 작업 공간 SDK에 설치하고 사용자 기존 AVD와 분리된 창 없는 테스트 에뮬레이터에서 데모 APK를 실행했다.
역할 선택 → 알바생 홈 → 뒤로 → 점주 홈 이동을 확인했으며 두 홈을 1080×2400/420dpi 원본 PNG로 캡처했다.
이번 두 캡처에서는 주요 글자·버튼의 잘림이 보이지 않았다. 전체 화면 크기·큰 글자·TalkBack·실서비스 API/업무 플로우 검증까지 완료한 것으로 해석하지 않는다.
앱 소스 및 APK는 변경하지 않았다. 사용자 데스크톱 UI를 조작하지 않았다.
파일: outputs/screenshots/worker-home.png, owner-home.png, capture-info.json; ZIP: outputs/시급우선-실제앱-캡처.zip.

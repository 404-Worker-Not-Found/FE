# 긴급알바 Android UI 리디자인 보고서

2026-11-02 · 로컬 변경 · 실제 기기 시각 QA 대기

## 1. 저장소와 안전한 작업 분리

- 사용자가 확인한 원본: `C:/Users/leese/Documents/Codex/2026-11-02/https-github-com-404-worker-not/work/FE`
- 시작 커밋: `8f8f2d5`, 시작 브랜치: `feature/#21-auth-job-integration`. 시작 시 작업 트리 clean.
- 새 브랜치: `design/android-ui-redesign` (기존 동명 브랜치 없음).
- 리디자인 worktree: `C:/Users/leese/Documents/Codex/2026-11-02/android-ui-kotlin-android-ui-ui/work/android-redesign`
- 기존 개발 폴더와 브랜치는 그대로 유지했다. 현재 기능 통합 커밋을 보존하기 위해 develop 대신 현재 HEAD에서 분기했다.
- 원격 push, 이슈·PR 생성, merge는 하지 않았다. 원래 인계 문서의 다른 작업도 실행하지 않았다.

## 2. 확인한 Android UI 스택과 구조

|항목|현재 구조|
|---|---|
|UI|Kotlin 2.0.21 + Jetpack Compose. 화면용 XML 레이아웃은 없으며 XML은 리소스·Manifest·FileProvider 등에 사용|
|Material|androidx.compose.material3, 해석된 버전 1.3.1; Compose BOM 2024.10.01|
|빌드|AGP 8.7.3, Gradle 8.11.1, JDK 17 타깃; compile/target 35, min 26|
|Theme|core/designsystem의 Color / Type / Theme / Shape / Dimens; AppRole과 LocalRoleColors로 역할별 스타일 전달|
|폰트|실제 구현은 Android 시스템 폰트. 문서의 Pretendard/Inter 파일은 번들되지 않음|
|화면|auth: 로그인/가입/비밀번호 재설정. owner: 홈·지원자·근무·재매칭·정산·지난 공고·프로필. worker: 홈·탐색·상세·지원현황·매칭결과·근무·채팅·프로필·신뢰도. job: 공고 작성/게시·결제 안내|
|공통 UI|AppTopBar / AppBottomBar / AppCard / AppPrimaryButton / 상태·위험 배지 / 선택 칩 / Stepper / SectionLabel|
|상태|MVVM, Hilt ViewModel, StateFlow와 collectAsStateWithLifecycle; 로컬 탭은 rememberSaveable|
|화면 이동|AppNavHost + AppRoute + Navigation Compose 2.8.4; 역할별 root의 기존 콜백 유지|
|데이터·비즈니스|domain 모델·정책 / repository interface / data 구현·DTO; Retrofit·OkHttp·Gson, Coroutines·Flow|
|실서비스/데모|기본은 live, 명시적인 -Pdemo.enabled=true만 mock; 실패 시 mock 전환 없음|

테마·리소스·ui 계층만 변경했다. API·네트워크·세션·ViewModel·domain·repository·내비게이션 그래프·테스트·Gradle 의존성에는 변경이 없다.
기존 문서의 오래된 mock-first 설명과 현재 live 기본 상태가 다르므로 현재 Gradle·AppNavHost·최근 통합 체크포인트를 기준으로 판단했다.
지도·실시간 처리 등의 기존 플레이스홀더를 실제 SDK 연결로 표현하지 않았다.

## 3. 지정 스킬 비교와 적용

|후보|실제 설치 방식 / 지원|판단|
|---|---|---|
|[hhushhas/material-ui-skill](https://github.com/hhushhas/material-ui-skill)|현재 realhasanshoaib 저장소로 이동. material-ui 폴더를 Codex skills에 복사/심볼릭 링크. 네이티브 Compose M3, 로컬 Material Symbols 선호|호환됨. 기존 Material Icons를 유지하고 접근성·레이아웃 기준을 통합하기 위해 이번에는 중복 설치하지 않음|
|[skydashnet/material-design-3-ui-skill](https://github.com/skydashnet/material-design-3-ui-skill)|Windows install.ps1 및 portable SKILL.md + references 제공. Codex 포함 여러 에이전트와 Compose 지원|선택. 정보 위계→semantic tokens→컴포넌트→상태→접근성 순서를 실제 구현에 적용|
|[wshobson/mobile-android-design](https://github.com/wshobson/agents/blob/main/plugins/ui-design/skills/mobile-android-design/SKILL.md)|ui-design 플러그인은 Claude Code의 /plugin install ui-design 방식. 개별 스킬 본문은 Compose·M3 지침|Android 기준은 호환되나 플러그인 설치 명령은 현재 Codex용이 아니며 선택 스킬과 중복되어 설치하지 않음|

기존 설치된 사용자 스킬과 프로젝트 스킬에서 해당 세 스킬은 발견되지 않았다.
Codex skill-installer로 선택 패키지를 다운로드한 뒤 upstream skill-files.txt의 portable 파일과 MIT 라이선스를
프로젝트 `.agents/skills/material-design-3-ui`에 설치했다. 글로벌 설정이나 외부 설치 스크립트를 실행하지 않았다.
패키지 버전은 1.1.1이며 출처를 SOURCE.md에 기록했다. 다음 턴부터 프로젝트 스킬로 검색할 수 있다.
SKILL.md와 color, typography, shape/elevation, spacing/layout, component-selection, adaptive/navigation,
accessibility, anti-patterns 지침을 읽고 적용했다. 다크 모드·dynamic color 권고는 기존 제품 제약에 맞춰 적용하지 않았다.

## 4. 기존 UI 진단과 디자인 방향

|문제|사용 영향|수정|
|---|---|---|
|노랑·오렌지 원색 위 흰 글자|일반 글자 대비 각각 1.75:1 / 2.53:1|브랜드 채움과 읽기용 강조색을 분리. CTA에 어두운 글자 적용|
|#BBBBBB 섹션 레이블|알바생 배경에서 1.89:1, 제목을 구분하기 어려움|16sp 섹션 제목과 heading semantics|
|일부 type 역할만 정의|기본 M3 타입과 자체 크기가 섞임|15개 type 역할과 명시적인 행간 정의|
|worker에서 기본 M3 primary가 owner 색|텍스트 버튼·필터 등 역할 색상이 섞일 수 있음|role별 전체 ColorScheme 지정|
|고정 헤더와 반복 카드|작은 창·큰 글자에서 공고 영역이 좁아짐|스크롤 피드, 일반 목록 구분선, 긴급 공고만 tint 카드|
|24dp 클릭 아이콘·34dp 지원 버튼·36dp stepper|터치하기 어려움|IconButton과 최소 48dp 조작 영역|
|고정 폭 긴급 카드와 고정 높이 버튼|창 너비·글자 확대에 취약|부모에 따른 카드 너비, heightIn, FlowRow|
|모든 너비에 하단 내비게이션·단일 열|큰 화면에서 읽기 효율 저하|600dp부터 레일, 홈 적응형 다열 grid|
|동작하지 않는 검색 입력 모양|검색 기능으로 오해 가능|실제 동작하는 필터 버튼과 화면 제목으로 정리. 기존 검색 기능을 제거한 것이 아니라 비동작 장식을 정리|

따뜻한 역할별 브랜드는 유지하고, 정보의 순서와 면의 역할을 명확히 했다. 점주는 현황→등록→내 공고,
알바생은 주변 알바→즉시지원 필터→긴급→다른 공고 순서다. 공고 안에서는 근무지·업종/거리·시간을 먼저 읽고
시급과 지원 행동을 함께 찾게 했다. 모든 영역에 그림자나 큰 곡률을 추가하지 않았다.

공식 [Android M3 Compose](https://developer.android.com/develop/ui/compose/designsystems/material3),
[접근성 기본 동작](https://developer.android.com/develop/ui/compose/accessibility/api-defaults),
[창 크기 분류](https://developer.android.com/develop/ui/compose/layouts/adaptive/use-window-size-classes)를 참고했다.
M3 웹사이트는 JavaScript 요구로 본문을 읽지 못해 Android 공식 문서를 구현 기준으로 삼았다.
[Mobbin](https://mobbin.com/discover)은 공개 접근에서 로그인 안내만 확인돼 개별 앱 화면을 조사했다고 주장하지 않는다.
다른 앱의 화면이나 자산은 복제하지 않았다.

## 5. 변경한 디자인 시스템과 화면

- **토큰:** 밝은 orange/yellow 채움 유지, 짙은 텍스트·선택색 분리, 중립 배경, 대비를 높인 상태색.
- **Typography:** 명시적인 15개 역할. 홈 headline 28/36sp, 공고 title 16/24sp, 본문 14/22sp, 시급 title 20/28sp.
- **Spacing/shape/elevation:** 4/8/12/16/24/32dp, 화면 여백 20dp, 카드 12dp·내부 8dp·시트 24dp. 일반 그림자 0.
- **동작 상태:** 기본 Material ripple/focus와 선택·비활성 semantics 사용. loading/error/retry/empty/applied 및 서버 미제공 정보 유지.
- **접근성:** 아이콘 이름, heading, 선택 그룹, 행 전체 Switch, 최소 버튼 크기, 자동 줄바꿈. 색상만으로 상태를 전달하지 않음.
- **점주 홈:** 현황 요약, 등록 CTA, 적응형 공고 grid. 필수 상태·지원자 수·업무·일정·시급 보존.
- **알바생 홈:** 실제 이름처럼 보이던 하드코딩 인사말 대신 목적 중심 헤더. 긴급 컬렉션·추천 목록·즉시 지원 보존.
- **탐색:** 필터/목록/지도 동작 보존, 읽기 쉬운 모드 선택, 긴급 카드가 가로 목록뿐 아니라 전체 폭에서도 사용 가능.
- **상세:** 반복 카드를 연속적인 섹션·구분선으로 정리. 가게/시급 breakdown/일정/카운트다운/업무/지원 현황/반경/하단 지원 동작 보존.
- **로그인:** 도입 문구·타입 위계·읽기 폭 조절. 검증·인증·전송·가입·재설정 동작은 그대로.
- **프로필·근무·지원자·채팅 등:** 공통 스타일 반영 및 글자 강조색 일관화. 프로필 토글 최소 높이 개선.
- **필터 시트:** 선택색·글자 대비 개선, 내용 스크롤 허용, 칩 터치 높이 확보.
- **적응형 shell:** 600dp 미만 하단 bar / 이상 rail, 동일 탭 순서와 root의 rememberSaveable 선택 상태 유지.
- **프리뷰:** 양쪽 홈 각각 320dp / 360dp / 840dp / 200% 글자 Preview를 추가. 프리뷰 소스는 컴파일 대상이며 실제 렌더 결과는 확인하지 못했다.

전체 규칙은 저장소 `docs/agent/style_guide.md`에 기록했다. 기존 시각 보존 결정에 대한 변경은 `docs/agent/decisions.md`에 추가했다.
failure-memory는 변경하지 않았다. 아직 기기 검증이 남아 있어 상용 출시 준비 완료로 판단하지 않는다.

## 6. 검증 결과

`JAVA_HOME=C:/Program Files/Android/Android Studio/jbr` 및 기존 작업 공간의 Android SDK를 환경변수로만 사용했다.
초기 Gradle 캐시 쓰기 제한은 실행 권한 확대로 해결했다. 빌드 설정·SDK 버전을 바꾸지 않았다.

|검증|결과|
|---|---|
|변경 전 데모 assembleDebug|통과. 비교용 APK를 수정 전에 보존|
|변경 후 compileDebugKotlin|통과|
|저장소 필수 ./docs/scripts/verify.sh|실행. testDebugUnitTest에서 기존 Windows 환경 의존 테스트 실패로 전체 명령 실패|
|단위 테스트|67개 중 66개 통과, 1개 실패, 0개 skip|
|실패 원인|ProfileCameraFilesTest.refusesTraversalGalleryNamesAndSymlinkTargets:27의 Files.createSymbolicLink에서 권한 부족|
|변경 전 재현|별도 detached worktree 8f8f2d5에서 같은 테스트를 실행: 2개 중 1개 통과, 같은 1개 실패. UI 회귀가 아닌 기존 환경 문제임을 확인|
|최종 live lint / assemble / unit tests|아래 최종 빌드 기록 참조. --continue로 테스트 실패와 lint/APK 결과를 분리|
|토큰 대비 계산|현재 토큰의 17개 불투명 전경/배경 쌍 모두 4.5:1 이상. 계산 원본 contrast-check.json|
|git diff --check|통과|
|기능 계층 변경 점검|API/네트워크/세션/라우트/ViewModel/domain/data/기존 테스트/의존성 변경 없음|
|실제 주요 플로우·실계정·TalkBack·IME·회전|기기 없음으로 미검증|
|작은 화면·200% 글자·600dp 경계·wide|소스의 줄바꿈·최소 높이·적응형 구조 및 Preview 구성 점검. 렌더·실행 검증은 미실시|
|다크 모드|기존 라이트 전용 제약 유지. 다크 변형 검증 대상 아님|

버튼 대비 계산: 알바생 1.75 → **8.74:1**, 점주 2.53 → **6.02:1**.
대비 계산은 알파 합성·사진·실제 렌더링·TalkBack 검증을 대체하지 않는다.
기존의 dependency update 경고와 Divider deprecation 등은 이번 작업에서 기술 스택 변경으로 해결하지 않았다.

## 7. 기존 / 변경 UI 스크린샷

**실제 스크린샷은 확보하지 못했다.** adb devices 결과 연결 기기가 없고, 기존 Pixel_8 AVD 설정은 남아 있지만
설정이 가리키는 emulator.exe와 system image가 존재하지 않는다. 현재 빌드용 SDK에도 emulator/system-images가 없다.
따라서 가짜 HTML 렌더나 디자인 이미지를 앱 실행 스크린샷으로 대신하지 않았다.

변경 전·후 데모 APK를 함께 제공한다. 동일한 package ID를 사용하므로 한 기기에서 순서대로 설치해 같은 fixture 화면을 비교할 수 있다.
라이브 APK는 인증 화면에서 시작하며 실제 계정 검증은 수행하지 않았다. APK는 개발용 debug 서명이다.

추가 캡처 목록:

1. 동일 기기 360×800dp: 점주 홈, 알바생 홈, 탐색 필터, 공고 상세, 로그인 전후 비교.
2. 320dp 및 200% 글자: 공고명·일정·시급·지원 버튼 줄바꿈, 필터 적용 버튼 접근성.
3. 599/600/840dp, 짧은 가로 화면: bar↔rail 선택 유지, 여러 열 카드 정렬, 시스템 inset.
4. 지원→지원완료→현황, 공고→지원자, 프로필·근무·채팅 이동, 오류→재시도.
5. TalkBack 이름·선택·순서, 키보드 포커스, 시스템 글자 크기, IME.

## 8. 자체 평가와 추가 개선

|항목|0–2 점수|근거|
|---|---|---|
|작업 목적 명확성|2|두 역할의 핵심 작업을 홈 위계에 반영|
|정보 위계|2|제목·수치·메타데이터·액션을 역할별로 분리|
|컴포넌트 의미|1|native Material와 선택 semantics 적용; 기기 보조기술 검증 대기|
|토큰 일관성|1|공통 테마 정리; 기존 화면의 개별 sp/dp·문자열은 일부 남음|
|적응형 동작|1|홈 grid·shell·본문 폭 개선; 모든 상세 화면의 태블릿 최적화와 회전 QA는 후속|
|상태·피드백|1|기존 상태·콜백 보존, 핵심 홈 빈 상태 보완; 실제 서버 플로우는 미검증|
|접근성|1|대비 계산·핵심 조작 크기·명명 개선; TalkBack/큰 글자 실기기 미검증|
|시각적 절제|2|일반 목록·상세의 불필요한 카드 제거, 장식성 그림자/gradient 없음|

우선 후속은 실제 화면 캡처와 반복 조정이다. 이후 모든 기존 화면의 남은 인라인 type/spacing/문자열을
단계적으로 정리하고, 프로필·신뢰도·근무 상세의 긴 콘텐츠와 작은 화면을 검증해야 한다.
서버·지도·결제·GPS 등 기존 미완 기능은 리디자인의 완료로 해결된 것으로 표시하지 않는다.
Windows 심볼릭 링크 생성이 가능한 개발 환경에서 기존 테스트 1개를 다시 실행해야 한다.

## 9. 로컬 커밋 및 산출물

디자인 시스템/스킬/가이드, 화면 개선, 검증 보고서 순으로 로컬 커밋을 나눈다.
최종 커밋 목록과 빌드 상태는 아래에 덧붙인다. 원격 작업은 하지 않는다.

### 최종 빌드 기록

- Live: `gradlew.bat lintDebug testDebugUnitTest assembleDebug --continue --console=plain` 실행. compile/assembleDebug/lintDebug 완료. Lint 0 errors, 30 warnings. 단위 테스트 67개 중 66개 통과, 1개 실패이므로 전체 명령의 종료 코드는 1이다.
- 실패 테스트는 Windows 심볼릭 링크 생성 권한 문제이며 변경 전 커밋에서도 재현됐다. 테스트를 삭제·skip하거나 프로덕션 코드를 바꿔 우회하지 않았다.
- Demo: `gradlew.bat assembleDebug "-Pdemo.enabled=true" --console=plain` 성공. Live와 demo를 별도 이름으로 저장했다. Live 저장 시 USE_DEMO_DATA=false, demo 저장 시 true를 확인했다.
- `verification-live.log`, `verification-demo.log`, `lint-results.txt`, `camera-test-before.xml`, `camera-test-after.xml`, `contrast-check.json`에 근거를 보존했다.
- 로컬 커밋: `0de3cee` 디자인 시스템·접근성·스킬·가이드, `5a5cf1c` 핵심 화면·적응형 UI. 이 보고서와 프로젝트 상태 문서는 별도 docs 커밋이다. 최종 3개 커밋과 APK SHA-256은 함께 제공한 `delivery-manifest.json` 참조.
- 후속 작업 폴더는 이 보고서 첫 절의 `work/android-redesign`이다. 원본 FE 체크아웃은 기존 브랜치로 유지된다.

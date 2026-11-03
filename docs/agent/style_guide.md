# 긴급알바 디자인 시스템

2026-11-02 UI 리디자인. Kotlin / Compose Material 3 유지. 라이트 모드 전용.
점주 오렌지와 알바생 노랑의 역할 구분을 보존한다. 아래 값은 `core/designsystem`에서 구현한다.

## 디자인 방향

가까운 일자리를 빠르게 비교하고 안전하게 연결하는 실용적인 마켓플레이스.
사용자가 선택한 최종 방향은 **시급 우선**이다. 점주는 모집 현황과 공고 관리,
알바생은 시급 → 근무지 → 시간·거리 → 지원 순서로 읽는다.
알바생 홈 첫 공고는 짙은 녹색 면과 큰 시급, 노란 지원 버튼으로 강조한다.
점주 홈은 오렌지 현황 면과 짙은 등록 버튼을 사용한다. 나머지 공고는 구분선이 있는 목록이다.
즉시지원 필터는 배경을 채우지 않는 작은 행으로 남긴다.
정보 영역을 무조건 카드로 감싸지 않는다. 장식용 그라디언트·그림자·이모지 제목을 추가하지 않는다.

## Color tokens

|역할|토큰|값|
|---|---|---|
|점주 브랜드 채움|Primary|#FF8546|
|점주 글자·선택·기본 M3 primary|PrimaryEmphasis|#A33E13|
|점주 강조 배경|PrimaryLight|#FFEDE2|
|알바생 브랜드 채움|Worker|#FFBD16|
|알바생 글자·선택·기본 M3 primary|WorkerEmphasis|#795800|
|알바생 강조 배경|WorkerLight|#FFF4CF|
|브랜드 채움 위 글자|OnAccent|#20231F|
|점주/알바생 화면 배경|Surface / WorkerSurface|#FFFFFF / #FFFFFF|
|카드·폼 / 중립 면|CardSurface / SurfaceMuted|#FFFFFF / #F0EFEA|
|기본 / 보조 / 힌트 글자|TextMain / TextSub / TextPlaceholder|#20231F / #60645B / #60645B|
|구분선|Border|#E2E1DA|
|성공|Success / SuccessBg|#216B43 / #E8F4EB|
|주의|Warning / WarningBg|#855000 / #FFF1D9|
|오류·노쇼·긴급|Danger / DangerBg|#B32F32 / #FCEAEC|
|근무 중|WorkingChip / WorkingChipBg|#474EAD / #EEF0FF|

밝은 브랜드 원색은 채움에, 짙은 emphasis는 글자·선택·아이콘에 사용한다.
`AppTheme.role.accentPressed`는 기존 호출 호환성을 위해 이름을 유지한 읽기용 강조 토큰이다.
MaterialTheme의 primary는 읽기용 강조색, primaryContainer는 역할별 tint로 연결한다.
onSurfaceVariant, outline, outlineVariant, errorContainer까지 정의해 기본 보라색이 섞이지 않게 한다.
일반 글자 대비 목표 4.5:1 이상. 비활성 상태는 활성 상태와 구별하되 내용을 읽을 수 있게 한다.

### 시급 우선 면 색상

|역할|토큰|값|
|---|---|---|
|시급 강조 면|FocusSurface|#232820|
|주요 글자|FocusContent|#FFFFFF|
|보조 글자|FocusSub|#C0C8B7|
|내부 구분선|FocusOutline|#48503E|
|마감 표시|FocusDeadline|#FFD163|
|오렌지 면 위 보조 글자|OnOwnerAccentSub|#602408|

이 짙은 면은 중요한 공고 한 단위를 강조하는 컴포넌트다. 앱의 다크 모드가 아니다.

## Typography

추가 폰트 의존성 없이 Android 시스템 한글 폴백을 사용한다. Pretendard/Inter는 번들되어 있지 않으므로 적용했다고 표시하지 않는다.

|역할|크기 / 행간|굵기|
|---|---|---|
|화면 주요 제목 headlineMedium|28 / 36sp|Bold|
|수치 headlineLarge|32 / 40sp|Bold|
|화면 헤더·시급 titleLarge|20 / 28sp|Bold|
|공고명·섹션 titleMedium|16 / 24sp|SemiBold|
|짧은 제목 titleSmall|14 / 20sp|SemiBold|
|본문 bodyLarge / bodyMedium|16 / 24sp, 14 / 22sp|Regular|
|보조 bodySmall|12 / 18sp|Regular|
|버튼 labelLarge|14 / 20sp|SemiBold|
|메타정보 labelMedium / labelSmall|12 / 18sp, 11 / 16sp|SemiBold / Medium|

display·headline·title·body·label의 15개 역할 전체를 정의한다. 글자 확대를 막거나 글자 크기를 자동 축소하지 않는다.

## Spacing / shape / elevation

- 간격: 4, 8, 12, 16, 24, 32dp. 화면 좌우 20dp, 카드 내부 16dp.
- 일반 그룹 카드 12dp, 내부 컨트롤·상태 배지 8dp, 버튼 12dp, 바텀시트 24dp.
- pill은 선택 칩 등 짧은 집합에 한정한다. 모든 요소를 pill로 만들지 않는다.
- 일반 카드·버튼 그림자 0dp. 카드 경계와 면의 명도 차로 그룹을 구분한다.
- 최소 터치 영역 48dp. 주요 CTA 최소 높이 52dp. 고정 높이 대신 heightIn으로 글자 확대를 허용한다.

## Components / interaction

- Primary: 역할 브랜드 채움 + OnAccent. Secondary: outlined / text button. 파괴적 동작: Danger outlined.
- 홈 강조 공고: 36/44sp 시급, 가게명·업종, 시간·거리, 노란 지원 버튼. 서버가 준 마감 정보가 있으면 표시한다.
- 공고 선택·정렬 정책을 바꾸지 않는다. 전달된 긴급 공고 첫 항목을 강조하며, 긴급 공고가 없으면 일반 공고 첫 항목을 표시한다. 긴급 여부가 거짓이면 긴급 문구를 붙이지 않는다.
- 다른 홈 공고는 가게·시간·거리/업종·시급을 담은 행이며, 시안에 없던 직접 지원 버튼도 기존 기능 보존을 위해 제공한다.
- 탐색 화면의 기존 긴급 카드·지도 핀·필터는 계속 사용할 수 있다. 시급 우선 레이아웃은 홈에 적용한다.
- 점주 현황은 실제 모집 수·지원자 수만 표시한다. 지원자 수 null은 0으로 바꾸지 않고 기존 미제공 문구를 사용한다.
- 상태 배지에는 항상 상태명을 표시한다. 지원 후 버튼은 기존 APPLIED 상태에 따라 지원 완료로 비활성화한다.
- 기본 Material ripple / focus / disabled 동작을 유지한다. 새 애니메이션은 추가하지 않는다.
- 선택 칩은 selectableGroup / RadioButton 역할, 목록·지도 전환은 Tab 역할.
- 즉시지원 토글은 행 전체가 한 개의 Switch 역할이며 내부 Switch는 중복 액션을 만들지 않는다.
- 아이콘: 기존 Material Icons Outlined를 재사용한다. 하단 선택은 tint 배경과 텍스트·선택 semantics로 구별한다.
- 뒤로·알림·필터 아이콘은 IconButton과 접근성 이름을 사용한다.
- loading / error / retry / empty / applied와 서버 미제공 상태를 유지한다. 가짜 추천·거리·지원자 수를 만들지 않는다.

## Adaptive layout

- 600dp 미만: 기존 목적지 순서의 하단 NavigationBar. 600dp 이상: 동일 목적지의 NavigationRail.
- 점주/알바생 홈은 최소 셀 너비 280dp의 적응형 grid. 헤더·현황·시급 강조 공고는 전체 너비 span.
- 짧은 화면에서 헤더도 피드와 함께 스크롤. 모든 긴급 공고는 첫 강조 공고와 나머지 목록에서 계속 접근 가능하다.
- 넓은 홈 콘텐츠 최대 1040dp, 로그인·공고 상세 본문 최대 640dp.
- safeDrawing / Scaffold insets / IME inset을 보존한다. rail은 짧은 창에서 스크롤할 수 있다.
- 접힌 화면·실기기·TalkBack·200% 글자·회전 및 모든 화면의 레이아웃은 기기 QA가 추가로 필요하다.

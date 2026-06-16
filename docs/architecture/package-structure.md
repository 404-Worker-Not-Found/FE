# Package Structure

이 문서는 Android(Kotlin) 프론트엔드 앱의 패키지 구조와 계층 규칙을 정의합니다.

## 기본 방향

- 코드는 기능(feature) 단위로 묶는다.
- 아키텍처는 MVVM을 따르고, UI / ViewModel / domain / data 계층을 구분한다.
- UI(Composable)는 ViewModel이 노출하는 불변 상태를 관찰한다.
- 비즈니스 로직은 Composable이 아니라 ViewModel/domain에 둔다.
- 네트워크 호출은 repository를 통해서만 한다. Composable과 ViewModel은 Retrofit service를 직접 호출하지 않는다.
- 네트워크 DTO는 data 계층에 두고 domain 모델로 변환한다. DTO를 UI로 그대로 노출하지 않는다.
- 백엔드 계약(endpoint, 응답 형태, 에러 코드)을 임의로 만들지 않는다.

## 루트 패키지

루트 패키지와 `applicationId`는 `com.workernotfound.app`로 확정되어 있다 (결정: `docs/agent/decisions.md` - "Application ID and Root Package").

- `com.workernotfound.app`

## 공통 패키지 구조

```text
com.workernotfound.app
├── MainActivity.kt
├── App.kt                      // @HiltAndroidApp Application
├── core
│   ├── designsystem            // 테마, 색상, 타이포, 공통 컴포넌트
│   ├── ui                      // 공통 UI 유틸, 상태 표현(UiState/UiEvent 베이스)
│   ├── network                 // Retrofit/OkHttp 설정, 인터셉터, 토큰 갱신
│   ├── navigation              // 내비게이션 그래프, 라우트 정의
│   ├── datastore               // 토큰/설정 로컬 저장
│   └── common                  // Result 래퍼, 확장 함수, 공통 util
├── di                          // Hilt 모듈 (앱 전역 의존성)
└── feature
    └── {feature}
        ├── ui                  // Composable 화면 및 컴포넌트
        ├── viewmodel           // {Screen}ViewModel, {Screen}UiState
        ├── domain              // 유스케이스, domain 모델, repository 인터페이스
        └── data                // repository 구현, remote(DTO/ApiService), 매퍼
```

## feature 예시

UI 정의서(`docs/UI_Specification.md`)의 화면을 feature로 나눈 예:

```text
feature
├── auth                        // 온보딩/스플래시, 회원가입, 로그인, 자동 로그인
├── job                         // 점주 공고 등록, 알바생 공고 탐색/상세
├── applicant                   // 지원자 목록/상세, 매칭 확정 (점주)
├── matching                    // 지원 현황, 매칭 결과, 재매칭 (알바생)
├── work                        // 근무 관리, GPS 출근, 노쇼 처리, 정산
├── chat                        // 점주-알바생 채팅
├── mypage                      // 프로필 설정, 알림, 계정
└── trust                       // 신뢰 점수, 노쇼 위험도, 리뷰
```

auth feature 내부 예:

```text
feature/auth
├── ui
│   ├── SplashScreen.kt
│   ├── OnboardingScreen.kt
│   ├── SignUpScreen.kt
│   └── LoginScreen.kt
├── viewmodel
│   ├── LoginViewModel.kt
│   └── LoginUiState.kt
├── domain
│   ├── model
│   │   └── AuthSession.kt
│   ├── repository
│   │   └── AuthRepository.kt        // 인터페이스
│   └── usecase
│       └── LoginUseCase.kt
└── data
    ├── AuthRepositoryImpl.kt
    ├── remote
    │   ├── AuthApiService.kt
    │   └── dto
    │       ├── LoginRequestDto.kt
    │       └── LoginResponseDto.kt
    └── mapper
        └── AuthMapper.kt
```

## 계층 책임

### core

앱 전역에서 공유하는 기반 코드를 둡니다.

- `designsystem`: Compose 테마, 색상, 타이포그래피, 재사용 UI 컴포넌트
- `ui`: 공통 UI 상태/이벤트 베이스, 화면 공통 유틸
- `network`: Retrofit/OkHttp 설정, 인증 토큰 인터셉터, 토큰 갱신 처리
- `navigation`: 내비게이션 그래프와 라우트 정의
- `datastore`: 토큰, 사용자 설정 로컬 저장
- `common`: `Result` 래퍼, 공통 확장 함수, 유틸

### feature/{feature}

화면 기능 단위 코드를 둡니다.

- `ui`: Composable 화면과 컴포넌트. 상태를 관찰하고 이벤트를 위로 전달한다.
- `viewmodel`: `{Screen}ViewModel`과 `{Screen}UiState`. 상태를 `StateFlow`로 노출한다.
- `domain`: 유스케이스, domain 모델, repository 인터페이스.
- `data`: repository 구현, remote(ApiService/DTO), DTO ↔ domain 매퍼, 로컬 데이터 소스.

### di

Hilt 모듈을 둡니다. repository 바인딩, 네트워크 의존성, DataStore 등 전역 의존성을 제공합니다.

## 계층 참조 규칙

계층 흐름은 다음과 같습니다.

```text
UI(Composable) -> ViewModel -> domain(UseCase/Repository 인터페이스) -> data(Repository 구현)
```

- UI는 ViewModel의 상태를 관찰하고 이벤트만 전달한다.
- UI는 repository, ApiService, DTO를 직접 참조하지 않는다.
- ViewModel은 domain(UseCase 또는 repository 인터페이스)을 통해 데이터에 접근한다.
- repository 구현(data)만 remote/local 데이터 소스를 참조한다.
- DTO는 data 계층 밖으로 나가지 않는다. domain/UI는 domain 모델만 사용한다.
- domain 모델은 UI, ViewModel, DTO에 의존하지 않는다.

## 상태 규칙

- 화면 상태는 단일 불변 객체(`{Screen}UiState`)로 표현하고 `StateFlow`로 노출한다.
- 사용자 입력은 ViewModel 함수로 받는다. 가변 상태를 UI에 직접 노출하지 않는다.
- 내비게이션/토스트 같은 일회성 이벤트는 지속 상태와 분리한다.
- 데이터 작업 결과는 로딩/성공/에러를 명시적으로 표현한다.

## 적용 기준

- 새 코드는 이 구조를 기본으로 따른다.
- 기능이 작거나 아직 단순한 경우 불필요한 빈 패키지나 클래스를 미리 만들지 않는다.
- remote, datastore, usecase 등은 실제 필요가 생길 때 추가한다.
- 단일 `app` 모듈에서 시작하되, 빌드 시간이나 경계 분리 필요가 커지면 멀티 모듈 전환을 결정 문서에 기록하고 도입할 수 있다.
- 이 문서의 규칙과 다른 구조가 필요하면 이유를 설명하고 결정 문서에 기록한다.

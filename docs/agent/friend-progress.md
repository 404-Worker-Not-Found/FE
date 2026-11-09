# 친구 에이전트 진행 상태

갱신: 2026-11-02 (Asia/Seoul). 브랜치: `feature/#21-auth-job-integration`.

## 복원 위치 / 작업 범위

- 작업 저장소: 이 채팅의 `work/FE`. 생성 폴더가 비어 있어 원격 브랜치를 독립 clone했다. 기존 다른 로컬 저장소/변경을 수정하지 않았다.
- FE 시작 `78e4e0f`, BE 확인 기준 `e24145f` (인계 문서의 `0c3b320`보다 최신). AGENTS와 지정 문서, 화면/스타일/코딩 지침 및 실제 feature/Gradle/resources를 읽었다.
- `feature/auth`, `feature/job` 및 공유 AppNavHost의 최소 인증 진입 변경만 구현했다. `feature/worker`, `feature/owner`, GPS/정산/지난 공고/카메라는 수정하지 않았다.
- 커밋: `6a7d656` 인증 화면/역할별 가입·로그인, `81490fb` 공고 생성 계약/결제·공개 상태 조회 준비, `1d054d9` 검증/재개 기록. 자기 원격 브랜치 push 완료.
- develop 대상 draft PR: https://github.com/404-Worker-Not-Found/FE/pull/23 . 작성자 담당 지정 및 #19 통합 브랜치 의존성 명시 완료. merge하지 않았다.

## 완료한 코드

- 로그인, 서버 본인 조회를 통한 저장 세션 확인 및 OWNER/WORKER 진입. AuthRepository, Keystore 암호화 저장, 기존 회전 갱신을 재사용한다.
- 이메일/SMS 발송·검증 및 역할별 회원가입. verified=false를 인증 완료로 처리하지 않는다. 점주 사업자 검증은 서버 가입 절차를 따른다. 알바생은 희망 시급/반경/업종/요일·가능 시간/기준 위치를 입력한다.
- 비밀번호 재설정 전용 코드 발송 및 코드+새 비밀번호 제출.
- 자격증명·인증코드는 ViewModel 메모리에서만 사용한다. SavedStateHandle/rememberSaveable, 소스·문서·로그에 저장하지 않는다. 인증 DTO/상태의 진단 문자열에도 자격증명을 포함하지 않는다.
- 공고 생성 어댑터는 본인 `GET /api/members/me`의 `ownerProfile.ownerProfileId`를 businessId로 사용한다. 회원 ID가 아니다. BE `docs/architecture/business-ownership-mvp.md`와 OwnerBusinessFindService에서 확인했다. 가게명·주소·좌표도 본인 프로필을 사용한다.
- 제목/업무/근무일·자정 넘김/기본·선택 추가 시급/인원/긴급도/지원 마감을 입력하고 실제 생성 DTO로 매핑한다. 모집 반경·임의 자동 가산을 보내지 않는다.
- 카테고리 공급을 repository 경계로 분리했다. 공개 조회 계약이 없어 빈 카탈로그/준비 중 안내로 **새 등록을 차단**한다. 고정 ID나 내부 ID 직접 입력으로 우회하지 않는다. 생성 API 어댑터는 준비됐지만 실사용 등록이 가능한 상태로 보고하지 않는다.
- 기존 공고 ID로 결제 상태 조회/재개 가능. 생성 응답의 실제 ID를 보존하도록 준비했고, payment-order와 payment 주문의 ID·공고 ID·결제용 버전·금액·통화를 대조한다. latestChange=PENDING이면 이전 주문을 사용하지 않는다.
- 결제 주문 생성·예치와 공고 OPEN을 구분한다. 서버 OPEN만 모집 공개로 표시한다. 생성은 멱등키 계약이 없으므로 결과 불명 시 자동 재시도하지 않는다.

## 미완 / 외부 의존성

- 카테고리 조회·선택 계약이 필요하다. 계약 확정 후 `JobPublicationRepository.categories()`를 실제 카탈로그에 연결해야 새 공고 저장을 활성화할 수 있다.
- Toss 테스트 클라이언트 설정·승인된 성공/실패 콜백이 없다. checkout 및 callback→confirm 실행 연결은 미완이다. 앱에서 결제/승인 요청을 실행하지 않는다. 실제 금전 결제 금지.
- 카카오/네이버 provider/앱 callback 설정 전 소셜 로그인 보류. Google 계정은 용도 확인 전 보류로 사용하지 않는다.
- 사용자가 알바생 테스트 계정만 전달했다. 자격증명은 기록하지 않는다. 점주 계정·등록 사업장 테스트 데이터가 없다. 전달된 실계정으로 앱 로그인/본인 조회 및 기기 E2E는 미검증이다.
- 배포 Swagger Basic 인증정보가 없어 이번 추가 계약의 배포 OpenAPI 대조는 미완이다. BE source 계약으로 구현했다.
- `adb devices`는 권한 범위에서 daemon 시작 후 연결 기기가 없음을 확인했다. 기기 화면·입력·회전/재시작·실계정 QA는 후속이다.

## UI/BE 차이

- SMS는 UI 명세상 선택이나 BE SignupService에서 필수다.
- BE 조건: 이름 최대 50자, 비밀번호 8~100자. 알바생 희망 조건·기준 위치·가능 시간이 추가 필수다. 현재 하나의 업종과 한 개 요일·시간 구간을 입력한다.
- 별도 공개 사업자 검증 endpoint를 만들지 않고 가입 시 검증한다.
- 임시 비밀번호 발송 대신 인증코드로 새 비밀번호를 설정한다.
- 공고 생성에 제목/실제 카테고리 식별자/지원 마감/긴급도가 필요하다. 공개 카테고리 선택 계약 부재를 새 등록 차단으로 명시한다.
- 공고 저장은 비공개 결제 대기이며 서버 OPEN 확인 전 공개 성공으로 안내하지 않는다.

## 검증

- 초기 SDK/Build Tools/오프라인 의존성 부재는 작업 폴더의 Android SDK와 기존 JDK/Gradle 캐시로 해결했다. JDK: Android Studio `jbr`, SDK: 인접 `work/android-sdk`. 경로/키를 Git에 추가하지 않았다.
- `compileDebugKotlin`: 통과.
- 최종 `lintDebug testDebugUnitTest --tests 'com.workernotfound.app.feature.auth.*' --tests 'com.workernotfound.app.feature.job.*' assembleDebug`: 추가 테스트 9개 통과, lint/APK 성공. lint 0 errors, 28 warnings.
- 최종 필수 `./docs/scripts/verify.sh`: 67 tests 중 66 통과, 기존 카메라 테스트 1 실패. lint와 assemble 단계는 통과했다.
- 실패: `ProfileCameraFilesTest.refusesTraversalGalleryNamesAndSymlinkTargets`의 `Files.createSymbolicLink`에서 Windows '클라이언트가 필요한 권한을 가지고 있지 않습니다'. 해당 코드/테스트는 HEAD와 동일하고 이번 변경과 무관한 환경 권한 문제다. 테스트를 삭제/비활성화하거나 전체 통과로 기록하지 않았다.
- `git diff --check` 통과, 변경 파일 비밀 패턴 검사 0건. 사용자 자격증명/토큰을 소스·문서·로그에 추가하지 않았다.

## 다음 행동

1. 코드·문서 검증, 자기 브랜치 push, draft PR #23 생성 완료. 리뷰/외부 계약 확인 대기. 리뷰 승인 없이 merge하지 않는다.
2. 카테고리 선택 계약·점주 테스트 계정·사업장·Toss 설정을 받은 뒤 새 등록/테스트 결제 연결 및 계정/기기 E2E를 수행한다. 이때 BE 소스와 배포 OpenAPI를 다시 대조한다.

## 자동 재개 예약

- 사용자 요청으로 `fe-00-45` 일회성 재개 등록: **2026-11-03 00:45 Asia/Seoul**.
- 재개 시 이 문서·Git 로컬/원격 상태·PR·검증 결과를 비교하여 중단 위치를 스스로 복원한다. 이미 완료된 작업은 중복 수행하지 않는다.
- 예약 자체가 계정 사용 한도 회복을 보장하지 않는다. 독립적으로 가능한 작업이 없고 위 외부 계약이 계속 없으면 미완 상태를 사실대로 보고한다.
- 문서 업데이트 점검: PROJECT_CONTEXT 및 backend-integration의 현황을 갱신했다. 새 정책 변경이나 사용자 승인 실패 기억 추가는 없어 decisions/failure-memory/checklists/coding-rules는 변경하지 않았다.

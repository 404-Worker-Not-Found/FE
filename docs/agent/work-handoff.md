# 작업 분담 및 자동 재개 체크포인트

갱신: 2026-11-02. 사용자의 요청으로 작업을 두 담당자에게 분리한다. 토큰 잔량을 가정하지 말고 아래 파일 소유권으로 작업 중복을 막는다.

## 브랜치와 담당

| 담당 | 브랜치 | 범위 |
|---|---|---|
| 친구 에이전트 | `feature/#21-auth-job-integration` | 인증 화면·회원가입·로그인 및 유형별 진입, 기존 AuthRepository 연결, 공고 등록 필수 입력·사업장/카테고리 식별자 계약, 결제 대기·Toss 테스트 결제·게시 흐름 |
| 이 채팅 | `feature/#19-api-integration` | GPS 출근·위치 권한/실좌표/서버 출근 정책, 점주 정산 조회·지난 공고, 기존 UI 잔여 확인, 브랜치 통합·회귀 검증 |
| UI 전용 기록 | `feature/#18-ui-completion` / PR #20 | API 없는 UI 변경의 독립 기록. 이름/사진/일정 UI와 카메라 촬영 체크포인트가 들어 있다. 새 작업은 이 채팅 담당과 조율한다. |

친구 브랜치는 독립 실행 가능한 API/UI 통합 체크포인트에서 분기한다. `develop`에는 아직 병합하지 않았다. 친구는 자기 브랜치에만 push하고 이 채팅 브랜치를 force-push/rebase/reset하지 않는다. 각 담당은 상대 브랜치의 작업을 덮어쓰지 않는다.

### 파일 소유권

- 친구: `feature/auth/**`, `feature/job/**`와 인증/공고/결제 UI에 필요한 feature-scoped 코드. 기존 `core/network`의 인증 지원 변경은 필요 최소한으로 한다.
- 이 채팅: `feature/worker/**`의 GPS·근무·신뢰도 표시, `feature/owner/**`의 정산·지난 공고, `core/location/**` 등 위치 지원, 기존 프로필 UI의 카메라 검증.
- `AppNavHost`/routes, `NetworkModule`, Gradle/catalog, Manifest는 공유 파일이다. additive하게 변경하고 상대 작업을 가져올 때 기능을 함께 보존한다. 문자열은 기능별 XML 파일을 우선한다.
- 공통 현황은 `docs/backend-integration.md`, 친구 작업 상태는 `docs/agent/friend-progress.md`, 이 채팅의 현재 상태는 이 문서에 갱신한다. 미구현/막힌 항목과 검증 완료 항목을 구별한다.

## 인계 기준 상태 (과거 체크포인트)

- API 기반 체크포인트 `1dec0eb`: 공개 조회·지원/취소·매칭 제안/수락/거절·실제 채팅방 연결·채팅 조회/전송, 점주 지원자/근무·시작/완료, 프로필 조회/이름 수정/로그아웃, 암호화 세션/회전 갱신이 구현되어 있다. 이 시점 전체 검증은 56 tests, 실패 0, lint/debug APK 성공이었다.
- UI 카메라 체크포인트 `1fab678`: 외부 카메라 촬영·앱 cache FileProvider·촬영 취소/실패 안내·파일 검증/정리가 추가되었다. 당시 미완료였던 촬영 완료/취소 기기 검증은 이후 완료했다. API 모드의 사진 업로드는 계약이 없어 비노출이다.
- 두 하위 에이전트가 사용량 제한으로 종료되었다. API 에이전트의 GPS 작업은 아직 소스 변경을 시작하지 않은 상태였고, UI의 미커밋 카메라 변경은 위 커밋으로 보존했다. 새 하위 에이전트를 곧바로 다시 실행해 사용량을 소모하지 않는다.
- 인계 당시에는 정산/지난 공고 계약 조사만 끝났으며, 이후 `8a1e861`에서 구현했다. 최신 상태는 아래 체크포인트를 따른다.
- API 기본 모드는 live이고 `-Pdemo.enabled=true`는 명시적 mock이다. live 실패 후 mock으로 대체하거나 가짜 성공을 반환하지 않는다.

## 계정과 외부 의존성

- 사용자가 별도 채팅으로 알바생 테스트 로그인 정보를 전달했다. 자격증명은 Git·이 문서·프롬프트·BuildConfig·로그에 넣지 않는다. 실제 로그인 200 및 WORKER 역할을 확인했다. 본인 근무·지원·매칭·정산 조회는 모두 200/빈 목록이었고 임시 세션은 로그아웃했다. 친구가 필요하면 사용자에게 별도 전달받는다.
- 함께 전달된 구글 계정은 용도가 확인되지 않아 사용자가 보류했다. 앱 점주 계정으로 가정하거나 해당 메일함에 로그인하지 않는다.
- 점주 테스트 계정, 등록 사업장과 테스트 결제 환경은 여전히 미확인이다. 실제 금전 결제를 수행하지 않는다.
- BE: `https://github.com/404-Worker-Not-Found/BE.git`, 확인 기준 `0c3b320`. API: `https://api.15-165-106-227.sslip.io/`.
- Swagger Basic 계정은 앱 로그인 계정과 다르다. 인증정보는 저장하지 않는다. 계약은 BE 소스와 서비스별 OpenAPI를 대조한다.

## 인계 당시 작업 계획 (1~5 완료, 6 대기)

1. 카메라 변경을 포함한 통합 체크포인트 전체 검증 후 브랜치를 push하고 친구에게 인계한다.
2. 알바생 계정으로 안전한 로그인·본인 조회 등 read-only 검증을 수행하되 토큰/비밀번호를 출력하거나 저장소에 넣지 않는다. 로그인 UI 구현은 친구 담당이다.
3. GPS 출근: `POST /api/works/me/{workId}/check-in`의 정확한 메서드/경로를 controller에서 다시 확인한다. `CheckInRequest`는 latitude/longitude이며 정책 응답은 radiusMeters/earlyWindowSeconds/lateWindowSeconds/timeZone이다. 실제 위치, 권한 거부·위치 비활성·시간 초과·반경 밖·서버 실패를 처리하고 서버 수락 후에만 출근 성공으로 표시한다. 신뢰도 summary 미지원이 근무/출근 영역까지 가리지 않게 분리한다.
4. 정산: 공개 API는 `GET /api/payments/me/settlements` (beforeId cursor, size<=100)와 `/{id}`의 read-only 조회이다. 금액은 BigDecimal, 통화/RESERVED·ON_HOLD·PAYOUT_REQUESTED·PAYOUT_FAILED·PAID/holdReason/payoutReviewRequired/paidAt를 그대로 모델링한다. 별도 지급 승인 endpoint를 발명하지 않는다. 서버 미제공 시급별 정산 breakdown을 합계에서 역산하지 않는다.
5. 지난 공고: 본인 근무·본인 공고·정산을 조합한다. 한 공고에 여러 근무자가 있을 수 있어 postingId 하나로 서로 다른 근무/정산을 덮어쓰지 않는다. 완료/노쇼/취소/실패를 구별하고, paidAt 없음이 곧 분쟁이라는 기존 mock 가정을 제거한다.
6. 친구 변경과 통합 후 전체 `./docs/scripts/verify.sh` 및 필요한 demo 검증/기기 QA를 수행한다. SDK/JDK 경로는 각 환경에서 찾고 gitignore된 local.properties나 환경변수로 설정한다.

## 자동 재개

- 이 채팅에 최초 22:10 일회성 예약을 등록했으며, 이후 사용자의 요청으로 2026-11-03 00:45 Asia/Seoul로 변경했다 (`fe-22-10`, ACTIVE).
- 재개 시 사용자에게 상태를 재전달받지 않는다. 이 문서와 git status/worktree/branch log, 원격 PR·친구 진행 문서를 비교해서 현재 위치를 복원한다.
- 재개 예약 자체가 토큰 충전을 보장하지는 않는다. 실제 실행 가능 여부는 그 시점의 계정 사용 한도에 따른다.
- 중단 전에는 완료된 단위를 커밋하고 이 문서에 다음 행동·미완 파일·마지막 검증을 남긴다. 친구 담당 파일은 수정하지 않는다.

## 인계 후 이 채팅 진행 상태

- 주 작업 worktree는 `/private/tmp/fe-api-integration`, 브랜치는 `feature/#19-api-integration`이다. 친구 브랜치의 인계 기준은 `78e4e0f`이며 그 이후 이 채팅 변경은 친구 브랜치에 강제로 반영하지 않는다.
- 실제 정산 read-only 조회, cursor 전체 조회, BigDecimal 금액 표시, 지급 상태·보류 사유 표시와 완료 근무→정산 이동을 구현했다. 지급 승인 API가 없어 live 지급 승인 버튼은 제거했다.
- 지난 공고는 근무/본인 공고/정산을 조합하고 다중 근무자를 `workId`별 항목으로 분리했다. 취소·실패·노쇼·마감을 구별하며 지급일시 미제공을 분쟁으로 추정하지 않는다.
- 위 변경은 전체 verify.sh(lint·unit tests·debug APK)를 통과했다. 테스트 62개, 실패 0개다. 이후 GPS 구현도 추가했다. 아래 최신 체크포인트를 따른다.


## 최신 체크포인트: GPS 출근 연결

- `DeviceLocationProvider`는 정확한 위치 권한·GPS 활성 여부를 확인하고 실제 위치를 읽는다. API 26~29와 API 30+ 경로, 20초 제한과 취소 정리를 포함한다. 좌표는 저장·로그하지 않는다.
- 서버 attendance-policy의 반경·출근 가능 시간·시간대를 WorkAttendance domain 모델로 매핑하고, 출근 전 실제 위치 확인으로 반경 내 여부를 확인한다. 제출 시 위치를 새로 읽고 서버가 해당 workId의 checkedInAt를 확인한 경우에만 완료로 표시한다.
- live 신뢰도 화면은 미지원 통계 때문에 출근 UI가 가려지지 않도록 독립 근무/출근 패널을 표시한다. 근무 화면 복귀 시 서버 상태를 새로 읽는다. demo의 기존 신뢰도 UI는 유지한다.
- 전체 verify.sh 통과: 66 tests, 실패 0, lint/debug APK 성공. 실제 알바생 계정에 배정 근무가 0건이어서 실서버 출근 성공과 실제 GPS permission/반경 UI 전체 E2E는 아직 검증하지 못했다.
- 이후 demo 회귀 검증·카메라 촬영/취소 기기 QA·API draft PR #22 등록을 완료했다. 친구가 인증 화면과 공고 생성/결제를 구현하면 변경을 확인하고 통합한다. 실제 근무 테스트 데이터/점주 계정은 백엔드에 요청해야 한다.


## 최신 검증 및 다음 재개 위치

- 제 담당 정산/지난 공고 커밋은 `8a1e861`, GPS 출근 커밋은 `be814b6`이며 원격 API 브랜치에 push했다. API 초안 PR은 #22다. 친구 브랜치는 `78e4e0f` 체크포인트를 유지한다.
- live/demo 전체 lint·unit tests·debug APK를 검증했고 테스트 66개가 통과했다.
- 데모 기기 QA에서 캐시 프로필이 동일한 StateFlow 값이라 다시 emit되지 않을 때 로딩 뒤 프로필이 사라지는 문제를 발견했다. load 완료 시 repository의 현재 값을 UI 상태에 반영하도록 수정했다.
- 에뮬레이터의 실제 카메라 앱 진입→가상 사진 촬영→완료→프로필 미리보기 복귀를 확인했다. 다시 촬영 후 취소 안내와 기존 사진 유지도 확인했고 앱 camera cache 파일 수는 성공 후 1개, 취소 후에도 1개로 실패 임시파일 정리를 확인했다.
- 다음 실행은 친구 브랜치/진행 문서와 이슈 #21 및 API PR #22의 의존 관계를 먼저 확인한다. 친구 PR 번호는 아직 미정이다. 기존 API/UI 구현을 다시 만들지 말고 친구 인증 화면과 통합 가능한지 검토한다. 실제 출근 성공은 배정된 테스트 근무가 필요하고 점주 정산 성공은 점주 계정/데이터가 필요하다.
- 구글 계정 보류는 계속 유지한다. 사용자에게 이미 받은 상태를 재설명해 달라고 하지 않는다.

## 추가 코드 검토

- 친구 원격 브랜치는 재확인 시에도 `78e4e0f`였다. 친구 변경을 통합한 것으로 표시하지 않는다.
- 근무 관리의 시작 1시간 전부터 진행 중 목록에 포함하는 기준은 UI 명세 3-6의 동작이므로 유지한다. 실제 서버 상태가 IN_PROGRESS일 때 카드 배지가 출근 완료에 가려지는 문제는 수정했다.
- 출근 목록 새로고침 중 이전 카드의 위치 확인/출근 버튼도 비활성화하고 ViewModel에서 중복 진입을 막았다. 조회와 위치 확인이 동시에 진행되어 이전 근무 상태로 준비되는 경로를 제거했다.
- 추가 수정 후 `./docs/scripts/verify.sh` 전체 검증을 다시 통과했다(66 tests, 실패/오류 0, lint/debug APK 성공). 이 검토 시점에는 기존 에뮬레이터가 종료되어 추가 기기 QA는 실행하지 못했으며, 앞선 카메라 검증 결과와 구별한다.
- 남은 외부 의존성은 친구 인증/공고 변경, 배정된 알바생 테스트 근무, 점주 앱 계정/사업장/결제 sandbox 정보다. 실서버 출근 성공·점주 정산 성공은 아직 미검증이다.
- 사용자가 점주 직접 가입을 요청해 최신 BE `e24145f`까지 조사했다. 이메일/SMS 인증 및 정상 영업 사업자 확인이 모두 필요하며 공개 테스트 우회는 없다. 가입에 쓸 수 있는 허가된 정보의 유무를 사용자에게 질문했다. 구글 계정 보류는 해제되지 않았다. 임의 계정/사업자 정보로 가입을 시도하거나 계정이 생성됐다고 표시하지 않는다.
- 최신 BE에서 Android 배포 설정 전환이 병합된 것을 확인해 프론트 배포 도메인 설명도 갱신했다. API/가입 조건 변경은 없다.

## 점주 데모 추가 검증

- 사용자는 가입용 이메일/휴대폰/사업자 정보가 없으므로 점주 계정 생성은 진행하지 않았다. 사용자 요청에 따라 데모 점주로 화면 검증을 계속하되 실서버 점주 가입/인증 성공으로 간주하지 않는다.
- 에뮬레이터에서 점주 근무 완료→정산 계산→데모 지급 승인→근무 목록 제거→지난 공고의 금액/지급 시각 반영을 확인했다. 데모 노쇼 확정→재매칭 성공도 확인했다. 실제 지급/노쇼 API 성공을 뜻하지 않는다.
- 조기 종료한 데모 근무의 리뷰가 예정 종료 시각까지 잠기는 문제를 발견했다. 종료 시각을 지급 승인 시각과 별도로 기록하고 리뷰 가능 기간은 실제 종료 시각부터 계산한다. 예정 근무 시간 표시는 보존한다. 수정 APK에서 즉시 리뷰 활성화/7일 안내를 재검증했다.
- live 전체 verify.sh와 demo lint/test/assemble 모두 통과했다. 67 tests, 실패/오류 0이다. 비밀정보 추가 없음. 친구 변경과의 통합은 이 체크포인트 이후 별도로 검증한다.

## 최신 통합 체크포인트: 친구 인증/공고 포함

- 친구가 `8f8f2d5`까지 push했고 draft PR #23을 생성했다. 해당 커밋을 #19 API 브랜치에 통합했다. develop/친구 브랜치를 직접 수정하거나 PR을 merge하지 않았다. 친구가 추가 작업을 이어갈 경우 원격 변경을 다시 확인한다.
- 충돌은 PROJECT_CONTEXT/backend-integration 문서 2개였으며 양쪽 완료 사항과 미완 항목을 보존했다. 친구 인증/공고 소스는 변경하지 않았다.
- 통합 live verify.sh와 demo lint/test/assemble 모두 통과했다. 76 tests, 실패/오류 0이다. Windows 권한 때문에 친구가 실행하지 못한 기존 카메라 symlink 테스트도 여기서는 통과했다.
- 실제 WORKER 계정으로 Android 앱 로그인, 서버 본인 조회에 의한 저장 세션 확인/재시작 자동 로그인, 근무 관리/출근 패널의 빈 목록, 실제 프로필 표시, 서버 로그아웃과 재시작 후 로그아웃 유지까지 확인했다. 자격증명/토큰을 파일이나 Git에 추가하지 않았다. 기기의 설치 앱은 통합 live 모드이며 로그아웃된 상태다.
- 친구의 미완 항목을 완료로 바꾸지 않는다: 카테고리 카탈로그 계약이 없어 새 공고 생성은 UI에서 차단되며 Toss checkout/callback 실행 연결은 미완이다. 점주 실계정/사업장/실제 배정 근무/소셜 설정도 여전히 없다. 회원가입 성공·실제 결제·실제 출근 성공은 미검증이다.
- 다음 재개: #19 PR #22와 친구 PR #23의 새 커밋을 확인한다. 통합한 인증/GPS/정산을 다시 구현하지 않는다. 외부 계약/데이터가 도착하면 위 미완 흐름부터 검증한다. 당시의 22:10 예약은 이후 사용자 요청으로 Nov 3 00:45 KST로 변경했다.

## 전체 설정·잔여 구현 감사

- 사용자가 지도 연결 누락을 지적해 명세 전체와 SDK/manifest/실제 소스/BE 공개 endpoint를 대조했다. 결과는 `docs/integration-readiness-audit.md`다. 앞으로 재개 시 이 문서도 읽고 완료 범위를 과장하지 않는다.
- 지도는 Naver SDK/실제 핀/현재 위치가 미연결이다. 사용자/계정 소유자의 Naver Cloud Maps Dynamic Map 활성화, `com.workernotfound.app` 등록, Client ID가 필요하다. 키만 제공되면 완성되는 상태가 아니며 프론트 교체 구현도 남아 있다.
- 사람/백엔드 입력: 지도 키, 카테고리 계약, OWNER/배정 근무 데이터, Toss 테스트 설정, 이메일/SMS/국세청 발송·검증 설정 확인, 소셜 앱/콜백, 푸시 제공자/프로젝트·발송 계약, 계약서/신뢰도 등 정책·공개 계약, 실제 단말 검증, 외부 배포 시 서명/스토어 설정.
- 프론트가 추가 구현할 수 있는 항목도 누락하지 않는다: 지도/주소·좌표/위치 탐색, 실시간 채팅/sync/읽음, 알림 목록, 연락처 변경, 탈퇴, 리뷰/이력, 근무 취소, 온보딩. 연락처·탈퇴·리뷰·취소·알림 REST는 BE 공개 API가 이미 있으며 단순히 API 미지원으로 표시하지 않는다.
- 서버 운영 비밀 설정/콘솔은 확인하지 않았으므로 설정이 없다고 단정하지 않는다. 이번 작업은 문서 감사이며 앱 소스 변경/새 앱 테스트 실행은 없다. 76 tests는 이전 통합 검증의 결과다. 비밀정보 추가 없음. 재발 방지용 readiness 보고 체크리스트를 추가했으며 failure-memory는 승인 없이 수정하지 않았다.

## 계속 구현: 기능별 push 체크포인트

- 사용자가 가능한 구현을 계속하고 기능 완료마다 GitHub에 push하라고 요청했다. 사람의 설정은 `docs/human-setup-guide.md`에 어려운 순서와 실행 절차로 정리했다.
- 다음 구현 순서: 알림 REST 목록/읽음 → 연락처 변경·탈퇴 → 리뷰/이력·취소 → 채팅 실시간/sync/읽음 → 지도/위치 및 온보딩. 외부 계약/키 없는 부분은 성공으로 꾸미지 않고 독립적으로 가능한 작업을 진행한다.
- 각 단위에 코드/필요한 회귀 검증/전체 verify/문서 상태를 남기고 #19 브랜치에 commit/push한다. 중단 시 마지막 체크포인트와 남은 작업을 이 문서로 복원한다. 다른 작업자의 후속 원격 커밋을 확인하고 보존한다.

### 완료 단위 1: 알림 REST

- `feature/notification`에 DTO→domain, cursor 목록/더 보기, unread count, 서버 응답 확인 후 읽음 처리, 오류/재시도 및 관련 업무 이동을 구현했다. 양 역할 홈에서 알림에 진입한다. demo는 임의 알림 이벤트를 만들지 않고 빈 목록이다.
- backend NotificationController/DTO와 경로·필드·cursor를 대조했고 계약 테스트 2개를 추가했다. 전체 verify.sh 통과(78 tests, 실패/오류 0, lint/debug APK 성공). 실제 알림 이벤트 수신/읽음 업무 E2E는 테스트 데이터가 없어 미검증이며 FCM 푸시 연결을 뜻하지 않는다.
- 다음 작업: 연락처 변경 및 탈퇴의 멱등키·비동기 상태 처리. 실제 테스트 계정의 연락처를 변경하거나 탈퇴시키지 않는다.

### 완료 단위 2: 연락처 변경·탈퇴

- 양 역할 live 마이페이지→계정 정보 관리에 이메일/휴대폰 인증 발송·6자리 코드·변경, 탈퇴 재확인과 서버 요청/상태 조회를 연결했다. `feature/account`에 별도 DTO/domain/repository/UI를 둔다.
- 요청 UUID를 SavedStateHandle에 보존하고 완료를 수락 응답만으로 추정하지 않는다. target/code는 메모리에만 두고 진단 문자열에서도 제외한다. SUCCEEDED/REJECTED 이후 기존 요청 세션만 비교해서 지우고 다시 로그인하도록 안내한다.
- BE 탈퇴 API는 비밀번호 재확인 body가 없으므로 별도 비밀번호 전송을 발명하지 않았다. pending 이후 JWT가 만료되거나 계정이 이미 탈퇴되어 상태 조회가 401이면 완료로 간주하지 않는다. 서버 계정 상태/토큰 수명 때문에 장시간 처리 결과 확인에는 제약이 남는다.
- 전체 verify.sh 통과(81 tests, 실패/오류 0). 실제 테스트 계정의 연락처 변경/탈퇴 및 발송은 수행하지 않았다. 다음 작업은 리뷰/이력이다.

### 완료 단위 3: 실제 리뷰

- `feature/review`에 작성 가능 근무/받은 리뷰·서버 평균·페이지 더 보기/1~5점·선택 코멘트 최대 500자/등록을 구현했다. 작성 대상을 실제 공고의 가게/업무와 조합한다. 대상 workId·상대 회원·내용이 서버 수락 응답과 일치해야 성공으로 반영한다.
- 작성 기한은 pending 응답의 writableUntil을 그대로 표시하며 live에 demo의 7일을 적용하지 않는다. 응답 불명 시 동일 내용 재시도만 허용하고 다른 내용으로 자동 재전송하지 않는다. 작성 성공 후 대상에서 제거한다. 작성자 이름/회원 ID는 서버가 제공하지 않아 추정하지 않는다.
- 점주 마이페이지/지난 공고, 알바생 신뢰도(출근)에서 실제 리뷰 화면으로 이동한다. demo 리뷰 placeholder/기존 데이터는 유지한다. 신뢰점수/전자 계약서/전체 근무 이력·정산 연결이 모두 완성된 것으로 표시하지 않는다.
- 전체 verify.sh 통과(84 tests, 실패/오류 0). 실제 리뷰를 테스트 계정에 등록하지 않았다. 다음 작업은 근무 취소/합의다.

### 완료 단위 4: 근무 취소 합의

- `feature/cancellation`에 본인 근무의 취소 요청/목록/상대 승인·거절/본인 철회를 연결했다. 요청은 UUID 멱등키와 같은 사유를 유지하며 결과 불명 시 자동으로 새 요청을 만들지 않는다.
- 실제 confirmedAt·SCHEDULED·미출근·서버 정책 시간대를 반영한 시작 전 조건과 pending 중복 조건을 사용한다. 서버도 최종 검증한다. 승인/거절/철회는 해당 work/request/응답 상태가 일치해야 반영한다. 사용자 재확인 후 실행한다.
- 점주 근무 상세, 알바생 근무 카드에서 이동한다. 승인 전에는 취소 완료로 표시하지 않는다. 원본 근무 목록은 복귀 시 서버를 다시 조회한다.
- 전체 verify.sh 통과(87 tests, 실패/오류 0). 실서버 근무 데이터가 없어 실제 취소 요청/승인은 실행하지 않았다. 다음 작업은 실시간 채팅/sync/읽음 및 점주 채팅 진입이다.

### 완료 단위 5: 실시간 채팅·sync·읽음

- OkHttp WSS/STOMP CONNECT Bearer 인증, 참여 방 구독, heartbeat/연결 제한·재연결·화면 pause 시 해제를 구현했다. 소켓은 변경 알림만 전달하고 실제 메시지는 공개 REST sync cursor로 가져온다. 초기 조회/증분 동기화 cursor와 전송 성공 메시지를 구별해 누락을 방지한다.
- 실제 보이는 메시지 ID까지만 RESUMED 상태에서 read-position을 보낸다. 서버 receipt와 상대 읽음 위치를 반영하고 foreground에서만 주기적으로 sync/읽음 상태를 보완한다. 점주 마이페이지에서 채팅 목록/방에도 진입한다.
- STOMP 분할/UTF-8 content-length/널 body/헤더 escape·크기 제한, 실제 MockWebServer WebSocket CONNECT/구독/Origin 부재, sync/read 공개 계약과 cursor/receipt 경계 테스트를 추가했다. 전체 verify.sh 92 tests 통과(실패/오류 0). 서버의 실제 두 계정 채팅방 E2E는 빈 데이터라 미검증이다.
- 사용자에게 새 테스트 계정 4개를 전달받아 각각 고유 deviceId로 로그인/본인 조회/근무·리뷰 대상·채팅·알림 조회를 확인했다. OWNER 2개와 WORKER 2개이며 점주 사업장 연결도 확인했다. 공고/근무/리뷰 대상/채팅/알림은 모두 0건이고 임시 세션은 모두 로그아웃했다. 계정/비밀번호/토큰은 Git/문서에 기록하지 않는다. 구글 계정 보류는 유지한다.
- 다음 작업: 키 주입 가능한 Naver SDK 및 실제 위치·주소 처리 준비, 온보딩, 근무 이력/정산. 지도 키는 아직 발급되지 않았다고 사용자가 답했다. 계정을 또 요청하지 말고 업종 계약/공고·배정 근무 데이터/Toss 설정을 확인한다.

### 완료 단위 6: 지도 SDK·주소 선택 기반

- Naver Map SDK 3.24.0, 전용 Maven, `naver.mapClientId` Gradle/local.properties→BuildConfig를 연결했다. 키가 비어 있으면 SDK를 초기화하거나 MapView를 만들지 않고 준비 중을 표시한다. 실제 지도 인증/타일은 아직 키가 없어 미검증이다.
- 공통 AppMap은 MapView lifecycle/저장 상태/메모리 정리/유효 좌표 마커/지도 선택 및 인증 실패 안내를 처리한다. 주소 resolver는 Android Geocoder로 실제 주소 결과만 반환하며 별도 provider가 없는 기기/오류/시간 제한도 처리한다.
- 회원가입에서 위도·경도 직접 입력을 주소 검색→후보 선택/현재 GPS/지도 선택 흐름으로 대체했다. 주소를 수정하면 선택 좌표를 지워 이전 주소의 좌표로 가입하지 않게 한다. 메일/SMS/사업자 검증은 기존 실제 API 조건을 유지한다.
- 전체 verify.sh 통과(93 tests, 실패/오류 0). SDK D8의 stack-map 경고와 native symbol strip 안내가 있지만 빌드 실패는 없었다. 외부 라이브러리 경고를 숨기기 위해 도구 버전을 변경하지 않았다.
- 다음 작업: 탐색 실제 주소 핀·GPS 거리 query, 공고 상세/점주 근무지 지도 연결, 온보딩·근무 이력. 서버 공고 목록/상세에 좌표가 없어 주소 변환으로 핀을 구성하되 미변환 주소에 임의 좌표를 넣지 않는다.

### Completed unit 7: GPS search and live maps

- GPS permission/failure/retry sends actual workerLat/workerLng and optional maxDistanceKm. Invalid coordinate pairs and radius queries without location are rejected. Location survives filter edits; old search jobs are cancelled.
- Live search uses address-derived pins, detail uses an address map, owner attendance shows the server workplace coordinate. Missing locations are explicit; no fake pins/current positions outside demo. Preview sits below the map to preserve Naver attribution. Continuous worker tracking remains unsupported by the confirmed public contract.
- Full verify.sh passed: 95 tests, zero failures/errors. Map key is still absent: actual auth/tiles and device geocoding require QA after issuance. No secrets added. Next: first-run onboarding/splash, worker history.

### Completed unit 8: startup and onboarding

- Live entry now shows a two-second logo/slogan splash and three first-run introduction pages with start/previous/next. Completion is a non-sensitive preference written on IO; write/read failures expose retry. Existing stored sessions skip first-run slides and continue to existing real-token validation. Demo role entry is preserved.
- Full verify.sh passed: 95 tests, zero failures/errors. First compile found an absent spacing token; corrected to existing cardGap and reran successfully. No secrets added. Next: worker history/settlement and native/demo regression.

### Completed unit 9: worker work and settlement history

- Worker attendance/trust entry now links to terminal work history (COMPLETED/CANCELED/FAILED/NO_SHOW), real job context, next-day schedule, server completedAt and participant settlement records. Reuses existing participant settlement source/card; preserves exact BigDecimal/status/hold/paidAt and never computes a payout from wages.
- Exact work/job settlement matching rejects ambiguous records; unknown work states are not presented as completed. New contract tests cover terminal status boundaries and mismatched/duplicate payout records.
- Full verify.sh passed: 97 tests, zero failures/errors. Existing shared AppTopBar positional argument caused the first compile failure, corrected to named onBack and verified again. Actual historical business data is absent, so populated E2E is pending. No secrets added. Next: publication date/time/location UX, live/demo regression and latest handoff/audit.

### Completed unit 10: publication schedule/location input

- Added calendar/date and 24-hour time controls for work date/start/end/deadline. Deadline date selection continues to explicit time confirmation; dismiss does not silently change the form. Existing ISO text entry remains available.
- Owner publication location now reuses actual address/GPS/map selection while preserving business ID/name. Editing the address invalidates old coordinates until a new location is selected. Draft validation rejects blank address/out-of-range coordinates. After create is attempted, all draft mutation methods remain frozen; resume-by-real-job-ID remains available.
- Full verify.sh passed: 97 tests, zero failures/errors, including location invalidation/coordinate bounds regression. Confirmed backend JobCommandService accepts address/lat/lng from this public request. Category catalog and Toss setup still required for publication E2E.
- Automation fe-22-10 was updated at the user's request: ACTIVE, one run Nov 3 00:45 KST, same chat; prompt inspects status/dirty files/logs/latest handoff and resumes without asking the user to restate status. Existing name/prompt previously disagreed with RRULE; tool update and persisted file were checked.
- Next: live/demo build and emulator smoke, update current inventory/PR22 and verify friend remote changes.

### Completed unit 11: contact code retry boundary

- Inspection found invalid/expired verification codes left the command UI locked. Confirmed AUTH-400-005 in BE ContactChangeTransactionService occurs before command persistence. Only that exact error now unlocks code correction/new command; transport errors, conflict and server failures preserve the ambiguous existing key. No automatic duplicate command is created.
- Contract test checks exact business-code/HTTP status boundaries and malformed bodies. Full verify.sh passed: 98 tests, zero failures/errors. No actual contact mutation/withdrawal was run.
- Live emulator worker1 login succeeded. Notification unread=0/list empty, real profile, account settings, attendance empty and terminal work history empty render correctly. First-run onboarding 1/2/3 and start→login were checked. Populated E2E remains pending. Native test used the pre-retry-fix APK; the retry fix itself is contract/build verified.

### Completed unit 12: visible matching freshness

- Worker application list/matching result and owner applicant board now re-query the confirmed REST contract on resume and every 15 seconds while the destination is RESUMED. No new matching socket contract is assumed, and no poll is scheduled while paused. Overlapping reads are guarded.
- Application cancellation and matching response cancel old reads and block poll overlap; result countdown has only one active job. New proposal IDs reset prior response UI, and eventual chat-room creation is picked up by the next query. Demo timers are preserved.
- Full verify.sh passed: 98 tests, zero failures/errors. Populated proposal/room E2E still awaits backend work data. Next: complete owner native smoke and final live/demo checkpoint.

### Completed unit 13: role colors for Material controls

- Native worker map QA exposed Material TextButton/inputs still inheriting owner orange despite custom worker cards using yellow. The shared light-only theme now derives Material primary/secondary/container/background colors from the existing role tokens; worker text uses WorkerEmphasis and owner retains orange. No hardcoded screen colors or dark variants added.
- Included in full verify.sh: 98 tests, zero failures/errors. Key presence was discovered in ignored local.properties during native QA (no value printed or committed); the live Naver SDK displayed actual Seoul map tiles with attribution visible. Job pin/real workplace/device geocoder QA still needs populated data. Update older absent-key notes accordingly; do not request another key blindly.

### Completed unit 14: native UI follow-ups

- Owner chat list no longer repeats its title below the back toolbar. Publication calendar disables dates outside today/tomorrow in the backend time zone, matching the server's publication restriction. Existing ISO validation still guards final submission.
- Full verify.sh passed: 98 tests, zero failures/errors. Native read-only smoke confirmed worker and owner login/profile, notification empty list, account form, worker attendance/history and both roles' reviews/chat empty state. Owner publication displays actual business/address/map and calendar opens. No job was posted, no review/cancellation submitted, no contact/account mutation performed.
- Demo final verification passed: 98 tests, zero failures/errors; saved current live APK at /private/tmp/fe-final-live.apk. Before resuming native tests, query actual UI/device state: multiple independent app installers/chats can share the emulator, so never infer the installed mode from a Gradle output filename.

## Latest recovery state (Nov 2 evening, supersedes old checkpoints)

- Main branch `feature/#19-api-integration`, latest implementation `4db8e04`; friend `feature/#21-auth-job-integration` remains `8f8f2d5` and is already integrated. PR22 is draft, targets develop; scope/body was updated. No PR/develop merge performed.
- Full repository verify.sh live passed and explicit demo lint/unit/APK passed after latest source changes: 98 tests per mode, zero failures/errors. Build output restored to live mode successfully. Tracked credential scan passed and local.properties is untracked/ignored; map key stays local.
- Native live: worker1 and owner1 real login/profile, worker notifications/account/attendance/history/reviews/chat empty states, owner reviews/chat and publication form/calendar. Actual Naver tiles visible, attribution preserved. No populated matching/check-in/payout/two-party chat event was available. No contact/review/cancellation/withdrawal/job mutation executed.
- Remaining inputs: confirmed category ID/name catalog or public API; test jobs/works/proposals/chat rooms for provided accounts; Toss public test client key/callback agreement; OAuth registration/callback agreement; push provider and device token/send contract; actual continuous location/trust/contracts/photo upload contracts; physical device QA. Do not request the four accounts or a new map key again. Do not use the held Google account.
- Next independent work: inspect native address lookup result and latest UI, logout test session, verify clean/pushed status. Once external inputs arrive, implement the specific missing checkout/OAuth/push/catalog adapters and run populated end-to-end flows. Do not invent provider keys, redirects, category IDs, policy scores or business data.
- ACTIVE one-shot heartbeat `fe-22-10` is now Nov 3 00:45 KST in this same chat (its ID is historical). Prompt reads dirty state, logs, remote changes and this document before acting. User does not need to restate the interruption point.

### Isolated native QA checkpoint

- Emulator-5554 unexpectedly switched to a fullscreen map. Its installed APK matched neither the saved initial nor the latest verified live APK by SHA-256, confirming another installer/chat changed the shared app during QA. Do not diagnose that screen as our Compose layout or claim its later address lookup outcome. Earlier worker/owner screens were observed, but avoid changing another tester's active app.
- The first read-only duplicate test_avd failed because the existing instance was writable. Created an independent temporary AVD fe_agent_qa (ANDROID_AVD_HOME=/private/tmp/fe-qa-avds) on port 5556, no snapshot/window. Boot succeeded; log /private/tmp/fe-isolated-emulator.log. Our temporary helper /private/tmp/fe-native-ui.py now targets emulator-5556. Continue install/QA there with the verified current live APK, leaving 5554 alone. No account data changes were performed.

### User-directed token efficiency checkpoint

- User asked why token usage was high. Stop expanding QA environments and repeating broad builds for unchanged code. Batch necessary reads; run verification again only for a new code change, a failure, or a specific unresolved concern. Existing latest code already passed live/demo 98 tests, lint and APK.
- Independent fe_agent_qa on emulator-5556 booted and verified live APK was installed; first-run onboarding→login and owner login were checked. Address lookup was started but its final result has not been independently inspected. Do not claim geocoding success or spend repeated tokens polling this unchanged check. Shared emulator-5554 was modified by another installer; leave it alone.
- No source changes remain pending. Current durable work is on feature/#19-api-integration, PR22 stays draft. At 00:45, inspect current git/status, this handoff and any newly provided external inputs before choosing useful work; avoid redoing implemented features or issuing duplicate key/account requests.

## 2026-11-03 scheduled resume: category contract and test-key configuration

- Resumed without a user status handoff. Main worktree was clean at 6383505; fetched FE and BE remotes. Friend FE branch remains 8f8f2d5. BE origin/develop now points to 212b81d: new GET /api/jobs/categories with data.categories[{id,name,subCategories:[{id,name}]}]. BE reference checkout was preserved; inspected new files via git show.
- Connected actual category DTO/domain mapping and owner selection. Server IDs are preserved; hierarchical display labels disambiguate children. Invalid/duplicate IDs are rejected, no seed IDs guessed. Failed/empty catalog keeps create disabled. Business/address inputs remain available and independent catalog retry preserves the chosen location. Public mock contract tests cover HTTP path/hierarchy/ID validation.
- Deployed GET /api/jobs/categories was probed once and returned success=false, HTTP-style status 400, GLOBAL-400-001. New source contract and deployed behavior disagree; do not claim live catalog/registration success. Asked user to confirm backend deployment.
- Received a Toss test public client key through the updated resume prompt. Stored only in ignored local.properties under toss.clientKey; Gradle property/BuildConfig wiring added with test_ck_ guard. No value in tracked files/docs. Checkout/callback/confirm execution remains incomplete. BE confirms public POST /api/payments/orders/{orderId}/confirm body {paymentKey,amount}; no mobile secret key. Asked for paired server-key confirmation and success/failure return URLs; do not invent callbacks or execute a payment.
- User asked to keep working during sleep. Explained true macOS sleep stops local execution; started caffeinate -i -s (exec session 98961) to prevent idle sleep while computer remains open/app running. No closed-lid or power-loss guarantee.
- Full ./docs/scripts/verify.sh passed: 100 tests, zero failures/errors, lint and debug APK successful (/private/tmp/fe-category-final-verify.log). Demo was not re-run for this owner live-only catalog change; prior demo checkpoint remains 98 tests. Existing 98-test work was not re-run before code changes. Do not expand emulator QA or poll unchanged blockers.

### Current automation: every two hours (supersedes one-shot 00:45)

- User requested continued backend checks when no independent frontend work remains. Updated existing heartbeat fe-22-10 to ACTIVE, FREQ=HOURLY;INTERVAL=2, same chat. It fetches/compares BE and relevant deployed APIs, implements newly unblocked frontend work, verifies changed code and pushes checkpoints. No repeated unchanged builds or duplicate input requests. Quiet when unchanged; notify only meaningful completion/failure/new required input.
- Last FE implementation: 96764d7; BE source: 212b81d; deployed categories: 400. New frontend code passed 100 tests, lint and debug APK. Local Toss test key configured, checkout/callback/confirm execution remains incomplete. Caffeinate idle-sleep inhibition was started at user request; actual system sleep/power-off/lid closure cannot run local work.

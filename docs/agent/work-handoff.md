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

## 저장된 현재 상태

- API 기반 체크포인트 `1dec0eb`: 공개 조회·지원/취소·매칭 제안/수락/거절·실제 채팅방 연결·채팅 조회/전송, 점주 지원자/근무·시작/완료, 프로필 조회/이름 수정/로그아웃, 암호화 세션/회전 갱신이 구현되어 있다. 이 시점 전체 검증은 56 tests, 실패 0, lint/debug APK 성공이었다.
- UI 카메라 체크포인트 `1fab678`: 외부 카메라 촬영·앱 cache FileProvider·촬영 취소/실패 안내·파일 검증/정리가 추가되었다. UI 전용 전체 검증은 통과했지만 카메라 앱의 촬영 완료/취소에 대한 기기 검증은 아직 남아 있다. API 모드의 사진 업로드는 계약이 없어 비노출이다.
- 두 하위 에이전트가 사용량 제한으로 종료되었다. API 에이전트의 GPS 작업은 아직 소스 변경을 시작하지 않은 상태였고, UI의 미커밋 카메라 변경은 위 커밋으로 보존했다. 새 하위 에이전트를 곧바로 다시 실행해 사용량을 소모하지 않는다.
- 이 채팅의 정산/지난 공고 작업은 계약 조사까지만 끝났다. 아직 구현된 것으로 표시하지 않는다.
- API 기본 모드는 live이고 `-Pdemo.enabled=true`는 명시적 mock이다. live 실패 후 mock으로 대체하거나 가짜 성공을 반환하지 않는다.

## 계정과 외부 의존성

- 사용자가 별도 채팅으로 알바생 테스트 로그인 정보를 전달했다. 자격증명은 Git·이 문서·프롬프트·BuildConfig·로그에 넣지 않는다. 실제 로그인 200 및 WORKER 역할을 확인했다. 본인 근무·지원·매칭·정산 조회는 모두 200/빈 목록이었고 임시 세션은 로그아웃했다. 친구가 필요하면 사용자에게 별도 전달받는다.
- 함께 전달된 구글 계정은 용도가 확인되지 않아 사용자가 보류했다. 앱 점주 계정으로 가정하거나 해당 메일함에 로그인하지 않는다.
- 점주 테스트 계정, 등록 사업장과 테스트 결제 환경은 여전히 미확인이다. 실제 금전 결제를 수행하지 않는다.
- BE: `https://github.com/404-Worker-Not-Found/BE.git`, 확인 기준 `0c3b320`. API: `https://api.15-165-106-227.sslip.io/`.
- Swagger Basic 계정은 앱 로그인 계정과 다르다. 인증정보는 저장하지 않는다. 계약은 BE 소스와 서비스별 OpenAPI를 대조한다.

## 이 채팅이 이어갈 구체적 작업

1. 카메라 변경을 포함한 통합 체크포인트 전체 검증 후 브랜치를 push하고 친구에게 인계한다.
2. 알바생 계정으로 안전한 로그인·본인 조회 등 read-only 검증을 수행하되 토큰/비밀번호를 출력하거나 저장소에 넣지 않는다. 로그인 UI 구현은 친구 담당이다.
3. GPS 출근: `POST /api/works/me/{workId}/check-in`의 정확한 메서드/경로를 controller에서 다시 확인한다. `CheckInRequest`는 latitude/longitude이며 정책 응답은 radiusMeters/earlyWindowSeconds/lateWindowSeconds/timeZone이다. 실제 위치, 권한 거부·위치 비활성·시간 초과·반경 밖·서버 실패를 처리하고 서버 수락 후에만 출근 성공으로 표시한다. 신뢰도 summary 미지원이 근무/출근 영역까지 가리지 않게 분리한다.
4. 정산: 공개 API는 `GET /api/payments/me/settlements` (beforeId cursor, size<=100)와 `/{id}`의 read-only 조회이다. 금액은 BigDecimal, 통화/RESERVED·ON_HOLD·PAYOUT_REQUESTED·PAYOUT_FAILED·PAID/holdReason/payoutReviewRequired/paidAt를 그대로 모델링한다. 별도 지급 승인 endpoint를 발명하지 않는다. 서버 미제공 시급별 정산 breakdown을 합계에서 역산하지 않는다.
5. 지난 공고: 본인 근무·본인 공고·정산을 조합한다. 한 공고에 여러 근무자가 있을 수 있어 postingId 하나로 서로 다른 근무/정산을 덮어쓰지 않는다. 완료/노쇼/취소/실패를 구별하고, paidAt 없음이 곧 분쟁이라는 기존 mock 가정을 제거한다.
6. 친구 변경과 통합 후 전체 `./docs/scripts/verify.sh` 및 필요한 demo 검증/기기 QA를 수행한다. SDK/JDK 경로는 각 환경에서 찾고 gitignore된 local.properties나 환경변수로 설정한다.

## 자동 재개

- 이 채팅에 2026-11-02 22:10 Asia/Seoul 일회성 재개 예약을 등록했다 (`fe-22-10`).
- 재개 시 사용자에게 상태를 재전달받지 않는다. 이 문서와 git status/worktree/branch log, 원격 PR·친구 진행 문서를 비교해서 현재 위치를 복원한다.
- 재개 예약 자체가 토큰 충전을 보장하지는 않는다. 실제 실행 가능 여부는 그 시점의 계정 사용 한도에 따른다.
- 중단 전에는 완료된 단위를 커밋하고 이 문서에 다음 행동·미완 파일·마지막 검증을 남긴다. 친구 담당 파일은 수정하지 않는다.

## 인계 후 이 채팅 진행 상태

- 주 작업 worktree는 `/private/tmp/fe-api-integration`, 브랜치는 `feature/#19-api-integration`이다. 친구 브랜치의 인계 기준은 `78e4e0f`이며 그 이후 이 채팅 변경은 친구 브랜치에 강제로 반영하지 않는다.
- 실제 정산 read-only 조회, cursor 전체 조회, BigDecimal 금액 표시, 지급 상태·보류 사유 표시와 완료 근무→정산 이동을 구현했다. 지급 승인 API가 없어 live 지급 승인 버튼은 제거했다.
- 지난 공고는 근무/본인 공고/정산을 조합하고 다중 근무자를 `workId`별 항목으로 분리했다. 취소·실패·노쇼·마감을 구별하며 지급일시 미제공을 분쟁으로 추정하지 않는다.
- 위 변경은 전체 verify.sh(lint·unit tests·debug APK)를 통과했다. 테스트 62개, 실패 0개다. 다음 큰 작업은 GPS 출근이며 아직 GPS 소스는 추가하지 않았다.

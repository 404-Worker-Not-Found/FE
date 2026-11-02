# 백엔드 연결 현황

확인일: 2026-11-01. 배포된 8개 서비스 OpenAPI와 BE `0c3b320`의 controller/DTO를 대조했다. 민감한 Swagger 인증정보와 앱 로그인 자격증명은 이 문서 및 앱에 포함하지 않는다.

## 주소와 인증

- API base URL: `https://api.15-165-106-227.sslip.io/`. `api.baseUrl` Gradle property 또는 gitignore된 `local.properties`로 변경한다. HTTPS와 마지막 `/`가 필요하다.
- `/docs/{auth,member,job,matching,payment,work,chat,notification}/swagger-ui/index.html`은 문서이다. 제공된 Basic 인증은 문서 접근용이며 앱 JWT/로그인 계정과 별개이다.
- 실제 응답은 `success/status/code/message/data/...` envelope이다. 일부 Swagger auth/job 성공 응답은 내부 DTO만 표기하지만 BE controller 및 실제 공개 조회에서 `ApiResponse<T>`를 확인했다.
- 로그인은 `POST /api/auth/login` (`email/password/deviceId`), 갱신은 `POST /api/auth/tokens/reissue` (`refreshToken/deviceId`). 둘 다 `data` 안에 토큰 결과가 있다. Refresh Token은 회전한다.
- `AuthRepository`가 로그인·로그아웃을 제공한다. 비밀번호는 저장하지 않는다. 토큰은 Android Keystore AES-GCM으로 암호화하고 `noBackupFilesDir`에 저장한다. Bearer 첨부 및 401당 한 번의 갱신/재시도는 공통 OkHttp 계층에서 수행한다. 갱신 중 로그아웃/다른 로그인은 덮어쓰지 않는다.
- 실제 모드는 로그인·회원가입 화면에서 AuthRepository와 암호화 세션에 연결한다. 서버 본인 조회로 저장 세션을 확인하고 역할별로 진입한다. 개발용 토큰이나 비밀번호를 BuildConfig에 넣지 않는다. 실제 계정 검증 여부는 friend-progress.md를 확인한다.

## Android 앱에 프론트 배포 주소가 필요한가

Native Android REST 호출에는 웹 프론트 배포 도메인이나 localhost Origin이 필요하지 않다. 앱은 서버 URL로 나가는 HTTPS 요청을 보낸다. APK/AAB 배포와 API 주소는 다른 문제다.

BE의 `deploy/Caddyfile` `FRONTEND_ORIGIN`은 브라우저 CORS 설정이며 `compose.deploy.yml`은 이를 chat allowed origins에도 전달한다. `docs/operations/backend-deployment.md`는 웹 프론트 배포 예시와, 비브라우저 검증 시 placeholder Origin 유지 설명을 포함한다. Android만 사용하는 현재 구성에서 임의의 웹 도메인을 발급해서 넣을 필요는 없다. 백엔드 팀이 그 외 용도로 주소를 요청했는지 별도로 확인한다.

소셜 로그인 `OAuthLoginRequest.redirectUri`는 별도의 OAuth redirect 설정이다. 앱에 등록한 URI/scheme 또는 HTTPS App Link를 서버·OAuth 공급자와 합의해야 한다. HTTPS App Links를 선택하면 도메인의 `assetlinks.json`과 앱 서명 인증서 연결이 추가로 필요하다. 일반 REST 통신용 도메인과 혼동하지 않는다.

## 실행 모드

기본 빌드는 실제 서버 연결 모드이다. 기존 데모는 `./gradlew assembleDebug -Pdemo.enabled=true`로 명시적으로 선택한다. 실서버 오류 후 mock으로 전환하지 않는다. 아직 연결되지 않은 repository는 실제 모드에서 준비 중 오류를 반환하며 가짜 공고 등록/매칭/노쇼/지급 성공을 만들지 않는다. 첫 역할 선택 화면에 현재 모드를 안내한다.

## 현재 구현 화면 우선 연결

| 현재 화면 | 실제 연결 | 상태/제약 |
|---|---|---|
| 알바생 홈 3-1 | `GET /api/jobs/search`, `POST /api/applications` | 긴급은 `type=URGENT`, 별도 추천 API가 없어 일반 공고 영역에 비긴급 공고 표시. 지원은 세션 필요 |
| 공고 탐색 3-2 | `GET /api/jobs/search`, 지원 | 페이지 전체 조회, 시급/시간/긴급 서버 필터. 업종명은 확인된 서버 seed 이름을 표시하고 categoryName으로 필터링. 위치 미연결 상태에서 거리 필터를 좁히면 명시적 오류 |
| 공고 상세 3-3 | `GET /api/jobs/{id}`, 지원 | baseHourlyWage+extraWage 표시. 미제공 거리·반경·점수·지원자 수를 발명하지 않음 |
| 지원 현황 3-4 | `GET /api/applications`, `GET /api/jobs/{id}`, `PATCH /api/applications/{id}/cancel` | 지원 ID와 공고 ID 구분. 서버 수락 후 상태 갱신. 실제 모드의 타이머 매칭 시뮬레이션 제거. WebSocket 미연결 |
| 매칭 결과 3-5 | 지원·매칭 목록/상세, `PATCH /api/matchings/{id}/accept`·`decline`, 실제 방 조회 | PENDING 제안의 서버 expiresAt 사용. 수락 응답 CONFIRMED 확인. SELECTED는 최종 confirmation saga 이후 확정 상태. 방 목록의 matchingId로 실제 chatRoomId 조회 |
| 알바생 근무 목록 3-6 | `GET /api/works/me`, 공고 상세 조합 | 예정/진행 상태 표시. 출근·근무완료·취소 API 연결은 후속 |
| 채팅 목록/내역 | `GET /api/chat-rooms/me`, 방 상세, 메시지 내역, 공고 상세 | 모든 목록 페이지, 메시지 cursor 조회; 읽음·실시간 sync는 후속 |
| 채팅 전송 | `POST /api/chat-rooms/me/{id}/messages` | clientMessageId 유지 재시도, 서버 수락한 메시지만 추가 |
| 점주 홈 2-1 | `GET /api/jobs/me`, 공고 상세 | 실제 title, PAYMENT_PENDING/OPEN/MATCHING/CLOSED 및 funding-blocked 표시. 지원자 수 null은 미제공 |
| 점주 공고 등록 2-2 | 생성·주문/공개 상태 조회 구현 | 본인 ownerProfileId 및 사업장 정보 사용, 필수 입력 연결. 카테고리 조회 계약 부재로 새 등록 차단, 저장 어댑터만 준비. Toss 테스트 checkout 설정 대기, 실계정 E2E 미검증 |
| 점주 지원자 2-3 | 지원자 목록/상세, 수동 매칭 제안 생성 API | 페이지 사이 scoreBatchId 유지. 미제공 나이·평점·이력·신뢰도 등은 nullable. 제안 성공을 즉시 근무확정으로 표시하지 않음 |
| 점주 근무 2-4 | 근무 목록/상세, attendance-policy, start·complete API | 실제 startedAt 타이머와 서버 완료 역할/조기완료 정책 반영. 근무완료와 지급완료 분리 |
| 점주 노쇼/정산/지난 공고 | 준비 중 | 수동 노쇼 공개 API 없음. 정산·지난 공고 데이터 조합은 후속 |
| 점주·알바생 마이페이지 | `GET/PATCH /api/members/me`, AuthRepository 로그아웃 | 기본 프로필 조회/이름 변경 연결. API 없는 사진·알림 설정은 실제 모드 비노출. 로그인 UI 연결, 기기 E2E 후속 |
| 알바생 신뢰도 | 준비 중 | 리뷰 API는 있으나 신뢰점수·노쇼 위험도 등 전체 UI 계약 없음 |

## 확인된 UI와 백엔드 차이

사용자 지시에 따라 연동에서는 백엔드 계약을 우선한다. 구현하지 않은 화면을 임의로 먼저 만들지 않는다.

- 근무 가능 토글과 개인화 추천 데이터가 공개 조회에 없어 실제 모드에서는 긴급 공고 필터와 일반 공고로 표시한다. 개인 이름을 mock 이름으로 표시하지 않는다.
- 공고 생성은 `title` 필수이며 공고 생성 직후 공개가 아니라 결제 대기/예치금 확정 후 공개 흐름이다. 실제 모드는 ‘등록 완료=모집 공개’ 가정을 제거했다. businessId와 categoryId를 임의의 고정 숫자로 보내지 않는다.
- 업종 seed는 카페/음료, 일반음식점 등이다. 기존 UI의 카페/음식점 라벨과 달라 실제 모드 탐색 필터는 seed 이름으로 수정했다. 서버 카테고리 조회 API와 공고 생성용 ID 선택 계약은 후속이다.
- 공고 상세에는 가산 합계 `extraWage`만 있다. 긴급/시간대별 가산으로 임의 분해하지 않는다. applicantCount는 실제 구현상 null이다. 거리·모집 반경·매칭 점수·공개 상태/긴급도도 상세 응답에 없으므로 해당 표시/제약은 후속이다.
- 점주 공고 목록은 `title/status/confirmedCount` 등을 제공하지만 시급은 없어서 상세 조회를 조합한다. 긴급도는 목록/상세 모두 없으므로 긴급 배지를 추정하지 않는다.
- BE MatchingConfirmationCommandService가 근무·채팅 생성 후 Application.SELECTED를 설정하므로 SELECTED는 매칭완료이다. APPLIED와 매칭 PENDING을 조합해 제안 대기를 표시한다. 서버 expiresAt를 사용하고 앱의 고정 5분/자동 성공은 실제 모드에 적용하지 않는다.
- 점주 지원자 API의 totalScore·expectedArrivalMinutes를 연결했다. 프로필 사진·나이·신뢰도·근무 이력·누적 노쇼·경험 여부는 미제공 nullable로 처리한다. noShowProbability에서 LOW/MIDDLE/HIGH 임계값을 발명하지 않는다.
- 점주 근무 API는 attendance-policy에 맞춰 시작·완료한다. 실제 모드에는 수동 노쇼 버튼과 가짜 실시간 위치 지도를 노출하지 않는다. 수동 노쇼/실시간 위치/즉시 정산 승인 공개 API가 없다. internal endpoint를 모바일에서 호출하지 않는다.
- 채팅방 ID는 매칭/지원 ID와 다르다. 방 목록의 matchingId로 chatRoomId를 찾으며, 찾지 못하면 이동 버튼을 비활성화한다.
- 알림 읽기 API와 계정의 Push/이메일/SMS 수신 설정 API는 다르다. 후자는 계약에 없다.

## 후속 작업 (미구현 화면 포함)

1. 연결한 로그인/회원가입 UI를 점주/알바생 테스트 계정으로 검증하고 계정 분리·401 갱신·로그아웃을 기기에서 확인.
2. 사업장은 본인 프로필 ID로 연결. 카테고리 조회·선택 계약 확정 후 새 공고 저장 활성화 및 Toss 테스트 checkout/callback 연결.
3. 연결한 매칭 제안·수락·거절·실제 채팅방 조회를 테스트 계정으로 E2E 검증.
4. 점주 지원자 미제공 프로필/리뷰/경험 필드 계약 협의와 데이터 확장.
5. 알바생 출근의 GPS 권한/실좌표와 cancellation API 연결. 정산·실제 지급 정책 확인, 지난 공고 조회 연결.
6. 신뢰도/프로필/사진/알림 설정의 API 지원 범위 협의. 실시간 지원 상태·채팅 sync·푸시 후속.
7. 연결한 회원가입/비밀번호 재설정의 실계정 검증, 미구현 온보딩과 설정 대기 중인 소셜 로그인 후속.

## 검증 및 테스트 데이터

- Basic 인증으로 8개 Swagger/OpenAPI를 읽었고 BE controller와 DTO를 대조했다.
- 실제 `GET /api/jobs/search?page=0&size=20`: 200, success=true, jobs=[], totalCount=0. `GET /api/jobs/1`: 404. 현재 공개 공고가 없으므로 빈 목록이 정상이다.
- 운영 서버에는 계정/공고/결제 데이터를 임의로 생성하지 않았다. Swagger Basic 계정은 앱 테스트 계정이 아니다.
- 서버 팀에 OWNER/WORKER 앱 테스트 계정, 계정별 등록 사업장·카테고리, 공개 테스트 공고, 결제 테스트 모드/키·승인 범위를 요청해야 한다. 알바생 계정만 별도로 전달되었으나 실제 앱 로그인/본인 조회 및 기기 성공 흐름은 미검증이다.
- 로컬 계약 테스트: 실제 응답 envelope, flat query, null distance, 한국 시간 카운트다운. 인증 테스트: Bearer 첨부·access/refresh 회전·한 번 재시도·익명 401·갱신 거부·일시 장애·갱신 중 로그아웃/다른 계정 로그인 보존, 인증 처리 전 계정 변경 시 이전 요청 재실행 차단.


## 2026-11-02 인증·공고 작업 (#21)

BE 최신 `e24145f`의 auth/signup/verification/password-reset 및 job/payment controller·DTO·service를 확인했다. 이번 추가 계약의 배포 OpenAPI 대조는 별도 Swagger Basic 인증정보가 없어 미완이다.

- 실제 모드는 로그인, 암호화 세션 확인(본인 조회 및 기존 401 갱신), 역할별 홈 진입을 제공한다. 명시적 demo 모드는 역할 선택을 유지한다.
- 회원가입은 `POST /api/auth/signup/{owner|worker}`. 이메일·SMS 발송/검증은 `/api/auth/{email|sms}-verifications/{send|verify}`. 발송 응답의 verified=false를 실패로 처리하지 않고, 코드 확인 응답의 verified=true만 인증 완료로 사용한다.
- UI 명세의 선택 SMS와 달리 BE SignupService는 이메일/SMS 둘 다 필수다. BE DTO에 맞춰 이름 최대 50자, 비밀번호 8~100자, 알바생 희망 시급/활동 반경/업종/가능 시간/기준 위치를 입력한다. 현재 한 개 업종과 한 개 요일·시간 구간을 입력할 수 있다.
- 사업자번호는 별도 공개 검증 API를 발명하지 않고 점주 가입 시 서버 검증을 따른다. 카카오/네이버는 provider·callback 설정 확인 전 의존성으로 안내한다. Google 계정은 사용하지 않는다.
- 비밀번호 복구는 `/api/auth/password-resets/email-verifications/send` → `POST /api/auth/password-resets`의 email/verificationCode/newPassword다. UI 명세의 임시 비밀번호 발송과 다르다.
- `businessId = GET /api/members/me`의 `ownerProfile.ownerProfileId`이다. 회원 ID와 다르며 BE `docs/architecture/business-ownership-mvp.md`와 OwnerBusinessFindService가 이를 명시한다. 주소·좌표·가게명도 본인 사업장에서 가져온다. 클라이언트가 내부 endpoint를 호출하지 않는다.
- `POST /api/jobs`에 title/categoryId/사업장 정보/근무일·시간·isEndTimeNextDay/시급·선택 추가 시급/인원/urgencyLevel/지원 마감을 보낸다. 모집 반경은 생성 계약에 없으며 임의 자동 가산을 보내지 않는다. 마감은 한국 시간 현재 이후·시작 이전이다.
- 공개 category ID 목록 API는 확인되지 않았다. 고정 seed ID나 내부 ID 직접 입력 없이 카테고리 공급 경계를 분리했다. 업종 선택 계약이 확정될 때까지 새 등록은 차단하고 기존 공고 상태 조회만 가능하다.
- 생성 성공의 실제 ID를 저장하고 `GET /api/jobs/{id}/payment-order`를 조회한다. CREATED 주문만 `GET /api/payments/orders/{orderId}`와 ID·jobPostId·결제용 jobVersion·금액·KRW 통화를 대조한다. latestChange=PENDING이면 이전 주문을 사용하지 않는다. 생성 endpoint에 멱등키가 없어 요청 후 결과 불명 시 자동 재생성을 막고 점주 홈 확인/기존 ID 조회를 안내한다.
- 결제 주문 생성/READY/CONFIRMING/DEPOSITED와 공고 OPEN을 구분한다. OPEN은 job-service 조회로만 확인한다. 테스트 클라이언트 키·성공/실패 콜백 계약이 없어 Toss checkout·callback→confirm 연결은 아직 없다. 실제 결제 및 승인 요청을 실행하지 않는다.
- 계정·소셜·카테고리·Toss 의존성과 검증 결과는 `docs/agent/friend-progress.md`에 기록한다. 기존 GPS/정산/지난 공고 인벤토리는 #19 담당이 갱신한다.

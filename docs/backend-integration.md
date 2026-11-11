# 백엔드 연결 현황

전체 외부 설정·사람이 제공할 입력·잔여 프론트 구현은 [외부 설정 및 잔여 구현 점검](integration-readiness-audit.md)에 함께 기록한다. 이 문서의 연결 현황과 테스트 통과는 앱 전체 기능 완료를 뜻하지 않는다.

확인일: 2026-11-02. 배포된 8개 서비스 OpenAPI와 BE `0c3b320`의 controller/DTO를 대조했다. 이후 BE `e24145f`까지 재확인했고, 변경 범위는 Android 배포 설정·검증·문서이며 앱 API/가입 조건은 동일하다. 민감한 Swagger 인증정보와 앱 로그인 자격증명은 이 문서 및 앱에 포함하지 않는다.

## 주소와 인증

- API base URL: `https://api.15-165-106-227.sslip.io/`. `api.baseUrl` Gradle property 또는 gitignore된 `local.properties`로 변경한다. HTTPS와 마지막 `/`가 필요하다.
- `/docs/{auth,member,job,matching,payment,work,chat,notification}/swagger-ui/index.html`은 문서이다. 제공된 Basic 인증은 문서 접근용이며 앱 JWT/로그인 계정과 별개이다.
- 실제 응답은 `success/status/code/message/data/...` envelope이다. 일부 Swagger auth/job 성공 응답은 내부 DTO만 표기하지만 BE controller 및 실제 공개 조회에서 `ApiResponse<T>`를 확인했다.
- 로그인은 `POST /api/auth/login` (`email/password/deviceId`), 갱신은 `POST /api/auth/tokens/reissue` (`refreshToken/deviceId`). 둘 다 `data` 안에 토큰 결과가 있다. Refresh Token은 회전한다.
- `AuthRepository`가 로그인·로그아웃을 제공한다. 비밀번호는 저장하지 않는다. 토큰은 Android Keystore AES-GCM으로 암호화하고 `noBackupFilesDir`에 저장한다. Bearer 첨부 및 401당 한 번의 갱신/재시도는 공통 OkHttp 계층에서 수행한다. 갱신 중 로그아웃/다른 로그인은 덮어쓰지 않는다.
- 앱은 로그인·회원가입 화면에서 AuthRepository와 암호화 세션에 연결한다. 서버 본인 조회로 저장 세션을 확인하고 역할별로 진입한다. 개발용 토큰이나 비밀번호를 BuildConfig에 넣지 않는다. 친구 작업의 원본 검증은 friend-progress.md, 통합 후 검증은 이 문서와 work-handoff.md를 확인한다.

## Android 앱에 프론트 배포 주소가 필요한가

Native Android REST 호출에는 웹 프론트 배포 도메인이나 localhost Origin이 필요하지 않다. 앱은 서버 URL로 나가는 HTTPS 요청을 보낸다. APK/AAB 배포와 API 주소는 다른 문제다.

BE `e24145f`는 기존 웹 프론트 전제를 수정해 `FRONTEND_ORIGIN` 의존과 웹 프론트 CORS 허용을 제거했다. chat의 브라우저 허용 Origin은 API 호스트에서 파생하고, native Android는 Origin 없이 인증한다. BE 배포 문서에는 서버 반영 기록도 있다. Android용 프론트 배포 주소나 localhost 주소를 전달할 필요는 없다. OAuth 복귀 설정은 별개다.

소셜 로그인 `OAuthLoginRequest.redirectUri`는 별도의 OAuth redirect 설정이다. 앱에 등록한 URI/scheme 또는 HTTPS App Link를 서버·OAuth 공급자와 합의해야 한다. HTTPS App Links를 선택하면 도메인의 `assetlinks.json`과 앱 서명 인증서 연결이 추가로 필요하다. 일반 REST 통신용 도메인과 혼동하지 않는다.

## 실행 모드

아직 계약이 없는 기능(노쇼·재매칭, 정산 지급 승인, 신뢰도 점수, 프로필 사진, 알림 설정)은 화면을 유지하되 준비 중 오류를 반환하며 가짜 성공을 만들지 않는다.

## 현재 구현 화면 우선 연결

| 현재 화면 | 실제 연결 | 상태/제약 |
|---|---|---|
| 알바생 홈 3-1 | `GET /api/jobs/search`, `POST /api/applications` | 긴급은 `type=URGENT`, 별도 추천 API가 없어 일반 공고 영역에 비긴급 공고 표시. 지원은 세션 필요 |
| 공고 탐색 3-2 | `GET /api/jobs/search`, 지원 | 페이지 전체 조회, 시급/시간/긴급 서버 필터. 업종명은 확인된 서버 seed 이름을 표시하고 categoryName으로 필터링. 위치 미연결 상태에서 거리 필터를 좁히면 명시적 오류 |
| 공고 상세 3-3 | `GET /api/jobs/{id}`, 지원 | baseHourlyWage+extraWage 표시. 미제공 거리·반경·점수·지원자 수를 발명하지 않음 |
| 지원 현황 3-4 | `GET /api/applications`, `GET /api/jobs/{id}`, `PATCH /api/applications/{id}/cancel` | 지원 ID와 공고 ID 구분. 서버 수락 후 상태 갱신. 타이머 매칭 시뮬레이션 없음. WebSocket 미연결 |
| 매칭 결과 3-5 | 지원·매칭 목록/상세, `PATCH /api/matchings/{id}/accept`·`decline`, 실제 방 조회 | PENDING 제안의 서버 expiresAt 사용. 수락 응답 CONFIRMED 확인. SELECTED는 최종 confirmation saga 이후 확정 상태. 방 목록의 matchingId로 실제 chatRoomId 조회 |
| 알바생 근무 목록 3-6 | `GET /api/works/me`, 공고 상세 조합 | 예정/진행 상태 표시. GPS 출근은 독립 출근 패널에 연결. 근무 취소 API UI는 후속 |
| 채팅 목록/내역 | `GET /api/chat-rooms/me`, 방 상세, 메시지 내역, 공고 상세 | 모든 목록 페이지, 메시지 cursor 조회; 읽음·실시간 sync는 후속 |
| 채팅 전송 | `POST /api/chat-rooms/me/{id}/messages` | clientMessageId 유지 재시도, 서버 수락한 메시지만 추가 |
| 점주 홈 2-1 | `GET /api/jobs/me`, 공고 상세 | 실제 title, PAYMENT_PENDING/OPEN/MATCHING/CLOSED 및 funding-blocked 표시. 지원자 수 null은 미제공 |
| 점주 공고 등록 2-2 | 생성·주문/공개 상태 조회 구현 | 본인 ownerProfileId 및 사업장 정보 사용, 필수 입력 연결. 카테고리 조회 계약 부재로 새 등록 차단, 저장 어댑터만 준비. Toss 테스트 checkout 설정 대기, 실계정 E2E 미검증 |
| 점주 지원자 2-3 | 지원자 목록/상세, 수동 매칭 제안 생성 API | 페이지 사이 scoreBatchId 유지. 미제공 나이·평점·이력·신뢰도 등은 nullable. 제안 성공을 즉시 근무확정으로 표시하지 않음 |
| 점주 근무 2-4 | 근무 목록/상세, attendance-policy, start·complete API | 실제 startedAt 타이머와 서버 완료 역할/조기완료 정책 반영. 근무완료와 지급완료 분리 |
| 점주 정산/지난 공고 | 정산 cursor 조회와 본인 근무·공고 조합 | 서버 BigDecimal 금액·통화·지급 상태 표시. 다중 근무자는 workId별 구분. 지급 승인·수동 노쇼 공개 API는 없음 |
| 점주·알바생 마이페이지 | `GET/PATCH /api/members/me`, AuthRepository 로그아웃 | 기본 프로필 조회/이름 변경 연결. API 없는 사진·알림 설정은 비노출. 로그인 UI 연결, 기기 E2E 후속 |
| 알바생 신뢰도/출근 | GPS 정책 조회·실제 위치·check-in POST 연결 | 신뢰점수 통계는 미지원으로 분리. 시간/반경/정확한 권한을 확인하고 서버 수락 후 출근 완료 |

## 확인된 UI와 백엔드 차이

사용자 지시에 따라 연동에서는 백엔드 계약을 우선한다. 구현하지 않은 화면을 임의로 먼저 만들지 않는다.

- 근무 가능 토글과 개인화 추천 데이터가 공개 조회에 없어 긴급 공고 필터와 일반 공고로 표시한다. 개인 이름을 임의 이름으로 표시하지 않는다.
- 공고 생성은 `title` 필수이며 공고 생성 직후 공개가 아니라 결제 대기/예치금 확정 후 공개 흐름이다. ‘등록 완료=모집 공개’ 가정을 제거했다. businessId와 categoryId를 임의의 고정 숫자로 보내지 않는다.
- 업종 seed는 카페/음료, 일반음식점 등이다. 기존 UI의 카페/음식점 라벨과 달라 탐색 필터는 seed 이름으로 수정했다. 서버 카테고리 조회 API와 공고 생성용 ID 선택 계약은 후속이다.
- 공고 상세에는 가산 합계 `extraWage`만 있다. 긴급/시간대별 가산으로 임의 분해하지 않는다. applicantCount는 실제 구현상 null이다. 거리·모집 반경·매칭 점수·공개 상태/긴급도도 상세 응답에 없으므로 해당 표시/제약은 후속이다.
- 점주 공고 목록은 `title/status/confirmedCount` 등을 제공하지만 시급은 없어서 상세 조회를 조합한다. 긴급도는 목록/상세 모두 없으므로 긴급 배지를 추정하지 않는다.
- BE MatchingConfirmationCommandService가 근무·채팅 생성 후 Application.SELECTED를 설정하므로 SELECTED는 매칭완료이다. APPLIED와 매칭 PENDING을 조합해 제안 대기를 표시한다. 서버 expiresAt를 사용하고 앱의 고정 5분/자동 성공은 적용하지 않는다.
- 점주 지원자 API의 totalScore·expectedArrivalMinutes를 연결했다. 프로필 사진·나이·신뢰도·근무 이력·누적 노쇼·경험 여부는 미제공 nullable로 처리한다. noShowProbability에서 LOW/MIDDLE/HIGH 임계값을 발명하지 않는다.
- 점주 근무 API는 attendance-policy에 맞춰 시작·완료한다. 수동 노쇼 버튼과 가짜 실시간 위치 지도를 노출하지 않는다. 수동 노쇼/실시간 위치/즉시 정산 승인 공개 API가 없다. internal endpoint를 모바일에서 호출하지 않는다.
- 채팅방 ID는 매칭/지원 ID와 다르다. 방 목록의 matchingId로 chatRoomId를 찾으며, 찾지 못하면 이동 버튼을 비활성화한다.
- 알림 읽기 API와 계정의 Push/이메일/SMS 수신 설정 API는 다르다. 후자는 계약에 없다.

## 후속 작업 (미구현 화면 포함)

1. 연결한 로그인/회원가입 UI를 점주/알바생 테스트 계정으로 검증하고 계정 분리·401 갱신·로그아웃을 기기에서 확인.
2. 사업장은 본인 프로필 ID로 연결. 카테고리 조회·선택 계약 확정 후 새 공고 저장 활성화 및 Toss 테스트 checkout/callback 연결.
3. 연결한 매칭 제안·수락·거절·실제 채팅방 조회를 테스트 계정으로 E2E 검증.
4. 점주 지원자 미제공 프로필/리뷰/경험 필드 계약 협의와 데이터 확장.
5. 구현된 GPS 출근·정산 조회·지난 공고를 실제 배정 근무와 점주 계정으로 E2E 검증. 근무 취소 요청/합의 UI 연결과 실제 지급 정책 확인은 후속이다.
6. 신뢰도/프로필/사진/알림 설정의 API 지원 범위 협의. 실시간 지원 상태·채팅 sync·푸시 후속.
7. 연결한 회원가입/비밀번호 재설정의 실계정 검증, 미구현 온보딩과 설정 대기 중인 소셜 로그인 후속.

## 검증 및 테스트 데이터

### 점주 테스트 계정을 직접 만들 수 있는 조건

- `POST /api/auth/signup/owner`는 가입 목적 이메일 인증과 SMS 인증을 모두 요구한다. 인증번호는 무작위이며, 소스상 고정 테스트 코드/공개 우회 경로는 없다.
- 점주 생성 시 국세청 사업자 상태 조회에서 정상 영업(`01`)으로 확인되어야 한다. 이메일/전화/사업자 정보를 임의 생성해서 테스트 점주로 가입할 수는 없다.
- 사용자 소유 또는 사용 허가된 가입용 이메일·휴대폰과 사업자 정보가 있으면 공개 가입 API로 생성할 수 있다. 없다면 백엔드 운영 담당자가 개발용 seed/관리 절차로 테스트 OWNER 계정을 마련해야 한다. 앱에 인증 우회 기능을 추가하거나 internal API를 호출하지 않는다.
- 가장 빠른 E2E 준비 요청: OWNER 앱 로그인 정보, 이 계정의 테스트 사업장 `businessId`와 사용 가능한 `categoryId`, 기존 WORKER 계정에 연결된 테스트 근무(근무일/시간/좌표 포함), Toss 테스트 모드·공개 client key. 로그인 계정 생성과 유료 공고 게시/근무 데이터 준비는 별도 단계다. 실제 금전 결제는 수행하지 않는다.

- Basic 인증으로 8개 Swagger/OpenAPI를 읽었고 BE controller와 DTO를 대조했다.
- 실제 `GET /api/jobs/search?page=0&size=20`: 200, success=true, jobs=[], totalCount=0. `GET /api/jobs/1`: 404. 현재 공개 공고가 없으므로 빈 목록이 정상이다.
- 운영 서버에는 계정/공고/결제 데이터를 임의로 생성하지 않았다. Swagger Basic 계정은 앱 테스트 계정이 아니다.
- OWNER 앱 테스트 계정, 등록 사업장·카테고리, 공개 테스트 공고, 결제 테스트 모드/키·승인 범위는 미확인이다. WORKER 계정은 전달받아 API와 통합 Android 앱의 로그인·본인 프로필·저장 세션·로그아웃을 확인했다. 실제 업무 성공에는 배정 근무가 필요하다.
- 로컬 계약 테스트: 실제 응답 envelope, flat query, null distance, 한국 시간 카운트다운. 인증 테스트: Bearer 첨부·access/refresh 회전·한 번 재시도·익명 401·갱신 거부·일시 장애·갱신 중 로그아웃/다른 계정 로그인 보존, 인증 처리 전 계정 변경 시 이전 요청 재실행 차단.

## 2026-11-02 추가 확인

- 제공받은 알바생 테스트 계정으로 로그인 성공과 WORKER 역할을 확인했다. 본인 근무·지원·매칭·정산은 모두 200/빈 목록이었으며 임시 로그인 세션은 로그아웃했다. 자격증명은 저장하지 않았다. 구글 계정은 용도 확인 전 사용자 지시로 보류다.
- 정산 응답은 Spring page가 아닌 beforeId cursor 목록이다. 합계에서 시급별 breakdown을 역산하지 않으며 paidAt가 null이라고 분쟁 처리 중으로 표시하지 않는다.
- 지난 공고에는 같은 공고의 서로 다른 근무가 존재할 수 있어 항목/상세 이동을 workId로 구별한다. 근무 취소·실패를 완료로 바꾸지 않는다. 리뷰 상세 화면은 아직 후속이다.

- GPS 출근은 서버의 radiusMeters/earlyWindowSeconds/lateWindowSeconds/timeZone을 사용한다. 기기 위치를 확인한 뒤에만 출근 버튼을 활성화하고 제출 때 다시 위치를 읽는다. 서버 수락 없이 완료를 표시하지 않는다.
- 정확한 위치 권한 거부·GPS 비활성·현재 위치 없음·20초 제한·인증 반경 밖 상태를 구별한다. 실제 출근 E2E는 배정 근무 데이터가 없어 미검증이며 계약/경계 테스트와 전체 빌드는 통과했다.

- 최종 회귀 검증을 통과했다(66 tests). 외부 카메라 촬영 완료·프로필 미리보기·취소/기존 사진 유지와 임시파일 정리를 에뮬레이터에서 확인했다. GPS 실서버 성공은 배정 근무가 없어 후속이다.
- 조기 종료 후 리뷰 시작 시각을 예정 종료가 아닌 실제 종료로 수정하고 기기 재검증했다. 최신 검증은 67 tests, 실패/오류 0이다. 실서버 점주 계정/지급 검증은 후속이다.

## 2026-11-02 인증·공고 작업 (#21)

BE 최신 `e24145f`의 auth/signup/verification/password-reset 및 job/payment controller·DTO·service를 확인했다. 이번 추가 계약의 배포 OpenAPI 대조는 별도 Swagger Basic 인증정보가 없어 미완이다.

- 앱은 로그인, 암호화 세션 확인(본인 조회 및 기존 401 갱신), 역할별 홈 진입을 제공한다.
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

## 통합 검증 (#19 + #21)

- 후속 WSS/STOMP·heartbeat/재연결·REST cursor sync·실제 가시 메시지 read-position·상대 읽음/미읽음과 점주 채팅 진입을 구현했다. 전체 verify.sh 92 tests 통과. 새 OWNER 2개/WORKER 2개 계정의 로그인·본인/빈 업무 목록·점주 사업장은 실서버 확인했고 임시 세션은 로그아웃했다. 실제 방 채팅 E2E는 데이터 대기.

- 후속 근무 취소/합의: 요청/조회/승인/거절/철회와 양 역할 진입, 재확인·시작 전/미출근 조건·UUID 멱등키·서버 receipt 확인을 구현했다. 전체 verify.sh 87 tests 통과, 실서버 근무 변경은 하지 않았다.

- 후속 리뷰: 실제 작성 대상/받은 리뷰·평균/페이지/등록과 양 역할 진입을 구현했다. 서버 작성 기한과 성공 영수증(workId/상대/내용)을 검증하며 임의 신뢰점수를 만들지 않는다. 전체 verify.sh 84 tests 통과, 실제 계정에 리뷰를 생성하지 않았다.

- 후속 연락처 변경/탈퇴: account 목적 인증 발송→변경/탈퇴 요청→UUID 멱등키 유지→비동기 상태 조회→완료 후 재로그인 안내를 양 역할에 연결했다. BE가 비밀번호 재확인 body를 받지 않아 UI 명세와 달리 별도 비밀번호 요청을 만들지 않았다. 실제 정보 변경/탈퇴는 실행하지 않았고 계약/전체 검증 81 tests 통과. 장시간 pending/탈퇴 후 JWT 거부는 성공으로 추정하지 않는다.

- 후속 알림 REST 구현: 양 역할 홈→알림 목록/더 보기/unread count/서버 확인 후 읽음/관련 업무 이동 연결. 전체 verify.sh 78 tests 통과. 단말 푸시는 미연결이며 실제 이벤트 데이터 E2E는 별도다.

- 친구 브랜치 `8f8f2d5`를 API 브랜치에 통합했다. develop 및 친구 원격 브랜치는 변경하지 않았다. 인증/공고 코드와 기존 GPS·정산·지난 공고를 함께 빌드했다.
- 통합 verify.sh 통과: 76 tests, 실패/오류 0. 친구의 Windows 환경에서 권한 때문에 실패했던 기존 카메라 symlink 테스트도 이 환경에서는 통과했다. 테스트를 삭제하거나 비활성화하지 않았다.
- 실제 Android 에뮬레이터에서 WORKER 로그인→홈 진입→프로세스 종료/재시작 후 자동 로그인→근무 관리→출근 패널의 빈 근무 상태→본인 프로필→서버 로그아웃→프로세스 재시작 후 로그인 화면 유지를 확인했다. 계정/토큰 값을 기록하지 않았다.
- 점주 실계정, 회원가입/문자 인증, 실제 GPS 출근 성공, 공고 생성/결제/게시 성공은 이 결과에 포함하지 않는다. 새 공고 저장은 카테고리 계약이 없어 계속 차단하며 Toss 실행 연결도 미완이다.

## 2026-11-09 알바생 화면 UI 개선 (fix/worker-screen-ui)

- 홈·탐색의 추천 공고와 '즉시 지원 가능'은 `GET /api/members/me`의 workerProfile(활동 반경·기준 위치·희망 업종·희망 시급·가능 시간)과 검색 카드(status·remainingMinutes·distanceKm)로 앱에서 판정한다. 공고별 모집 반경과 추천 API는 서버에 없어 알바생 활동 반경을 사용한다.
- 거리는 기기의 마지막 위치, 없으면 등록한 기준 위치로 `workerLat/workerLng`를 보내 서버 distanceKm를 받는다. 검색 API에 키워드 파라미터가 없어 가게·업종명은 받은 결과에서 거르고, 동네는 Geocoder로 좌표를 찾아 그 위치로 다시 조회한다.
- 공고 상세 응답에는 긴급도·거리·마감·좌표가 없어 직전 검색 카드 값을 쓴다. 알림 등 카드 없이 열면 긴급 배지·지원 가능 판정은 표시하지 않는다. 가산 시급은 extraWage 합계만 있어 긴급/시간대로 나누지 않는다.
- 신뢰도 화면의 근무·노쇼 횟수, 리뷰 평균, 최근 리뷰(workId로 가게명 연결), 최근 근무 이력(평점·실제 정산액)은 실제 데이터다. 신뢰 점수·등급·노쇼 위험도는 matching-service 내부에만 있어 '준비 중'으로 표시한다.
- 알림 수신 설정(Push/이메일/SMS) API와 프로필 사진 저장 API가 없어 토글은 비활성, 아바타는 이니셜만 표시한다. BE에 FCM 기기 토큰 API(`PUT /api/notifications/me/device-tokens`)는 생겼지만 앱에 Firebase 설정(google-services.json)이 없어 미연결이다.
- 지도(탐색 지도 탭·상세 미니맵)는 코드가 연결돼 있으나 `local.properties`의 `naver.mapClientId`가 없으면 '지도 설정 안 됨'으로 표시된다.

# 외부 설정·잔여 구현 점검

확인일: 2026-11-02 (Asia/Seoul). FE 통합 기준 `e0020f4`, BE 확인 기준 `e24145f`.

화면 명세, Gradle/catalog, AndroidManifest, 실제 UI/DTO/repository와 BE 공개 controller/서비스를 대조했다. 클라우드 콘솔·운영 서버 비밀 설정은 열람하지 않았으므로 발급/설정 여부가 불명인 항목은 미확인으로 적는다. 76개 테스트 통과는 지도·푸시·결제까지 전체 앱이 완성됐다는 뜻이 아니다.

## 1. 사람이 등록·제공·확인해야 하는 항목

| 우선순위 | 항목 | 담당 / 필요한 입력 | 현재 상태와 이후 구현 |
|---|---|---|---|
| 높음 | 네이버 지도 | 계정 소유자: Naver Cloud Maps 애플리케이션 등록, Dynamic Map 선택, Android 패키지 `com.workernotfound.app` 등록, Client ID 제공 | SDK 의존성/저장소/활성 인증 설정 모두 없음. 탐색 지도·상세 미니맵·점주 위치 도식은 placeholder. 키를 받으면 SDK·마커·카메라·위치/오류 처리 연결이 필요함 |
| 높음 | 업종 카탈로그 | 백엔드: 실제 사용할 category ID/name 목록의 공식 계약 또는 조회 API 제공 | 새 공고 저장이 비활성화됨. 확인된 카탈로그를 repository에 연결해야 함. 임의 seed ID를 추정하지 않음 |
| 높음 | 점주/근무 테스트 데이터 | 백엔드: OWNER 앱 로그인, 등록 사업장, WORKER에 연결된 테스트 근무와 일정/좌표, 공고/매칭/채팅방 | WORKER 로그인만 실기 검증됨. OWNER/배정 근무 없음. 데모 계정으로 실제 서버 인증을 대신할 수 없음 |
| 높음 | Toss 테스트 설정 | 백엔드/가맹점 계정 소유자: 서버 테스트 키와 짝이 맞는 공개 client key, 테스트 사용 가능 여부, 성공/실패 복귀 경로 및 웹훅 설정 확인 | 결제/공개 상태 조회까지만 연결. checkout, 앱 복귀, confirm 연결은 미완. `TOSS_ENABLED` 실제 값은 미확인. 시크릿 키는 서버에서만 보관 |
| 기능 사용 시 필수 | 가입·복구 인증의 외부 발송 | 백엔드: SMTP 계정/앱 비밀번호, SOLAPI 계정·인증된 발신번호, 실제 발송 가능 여부 확인 | API/UI는 있으나 실제 이메일/SMS 발송·코드 입력 성공은 미검증. 프론트에 서버 발송 비밀키를 주는 작업이 아님 |
| 점주 직접 가입 시 필수 | 사업자 검증 | 백엔드: 국세청 API 설정 확인. 가입자: 사용 허가된 정상 영업 사업자 정보 | 서버에서 정상 영업 여부 확인 필수. 설정 여부는 미확인이며 사용자에게 가입용 정보가 없음. 테스트 점주 seed가 가장 빠른 별도 대안 |
| 소셜 사용 시 필수 | 카카오/네이버 로그인 | 각 개발자 콘솔 계정 소유자: 앱 등록/로그인 활성화/필요한 테스트 사용자 등록. 프론트·백엔드: 인가 코드 흐름과 redirectUri 합의 | 공급자 SDK/브라우저 인증·앱 복귀 intent-filter 없음. 현재 BE는 authorizationCode+redirectUri 계약. 네이티브 SDK 토큰을 그대로 대입할 수 없음. Android SDK 방식이면 플랫폼/키해시 등 해당 방식의 등록이 추가로 필요함 |
| 푸시 사용 시 필수 | 푸시 전송 서비스 | 팀: 제공자 결정. FCM 사용 시 프로젝트 소유자가 Android 앱 등록과 `google-services.json` 제공, 백엔드 담당자가 서버 발송 권한 설정 | 앱 FCM 의존성/수신 서비스/알림 권한 요청 없음. BE에도 기기 토큰 등록·푸시 발송 연결이 확인되지 않음. 알림 DB/조회 API와 단말 푸시는 별개이며 양쪽 구현 필요 |
| 명세 완성 시 필요 | 전자 근로 계약서·정책 | 기획/백엔드: 표시할 실제 계약서 내용·문서 제공 방식·서명 범위 확정. 신뢰점수/위험등급/추천 등 공개 응답 계약 확정 | 계약서 UI/문서 API 없음. 신뢰도 전체 summary·예측 등급 기준·개인화 추천도 현재 화면과 맞는 공개 계약이 없음. 임의 문서·점수/등급·추천을 만들지 않음 |
| 최종 검증 시 필요 | 실제 Android 단말 | 테스트 담당자: 사용 가능한 단말, 위치 권한/GPS 및 테스트 근무 장소 조건, 점주·알바생 두 계정 | 에뮬레이터 로그인/사진/데모 검증과 별도로 실제 GPS 반경, 권한 거부, 앱 종료 상태 푸시, 기기 간 채팅/매칭 검증이 남음 |
| 외부 배포 시에만 | 앱 배포 설정 | 배포 담당자: APK 직접 전달/Play 배포 방식, 최종 앱 식별자·서명키 소유/보관, 스토어 등록 자료 결정 | 현재 release signing 설정 없음. 스토어 배포 시 계정/등록 정보와 해당 심사 자료가 추가로 필요함. API 연결을 위해 웹 프론트 도메인을 만들 필요는 없음 |

지도 Client ID와 네이버 로그인 client ID는 서로 다른 서비스의 설정이다. Firebase 서버 서비스 계정, Toss secret, SMTP/SOLAPI/국세청 비밀키는 앱에 넣지 않는다. Android 등록에 필요한 현재 패키지는 위 값이며, 필요한 서명 지문/키해시는 개발 환경에서 추출할 수 있다.

## 2. 외부 키만으로 해결되지 않는 프론트 잔여 구현

| 항목 | 확인한 상태 / 남은 작업 |
|---|---|
| 실제 지도와 위치 기반 탐색 | `WorkerJobMapView`는 목록 순서에 따라 핀을 배치하고 가운데 점을 현재 위치처럼 그리는 도식이다. 실제 좌표/사용자 위치가 아니다. `MiniMap`, `MapPlaceholder`, `OwnerLiveLocationMap`도 대체 필요. 탐색 repository는 위치 없이 요청하고 거리 필터를 차단한다. 출근에 구현한 GPS를 탐색·거리·위치 선택에도 연결해야 함 |
| 주소↔좌표 | 가입 화면은 위도/경도를 직접 입력받고 공고는 프로필 위치를 재사용한다. 사용자가 주소/지도 선택으로 좌표를 얻는 흐름은 미완. Geocoding 제공 방식을 정해야 하며 별도 유료 API를 쓸 경우 해당 서비스 활성화/서버 인증 설정도 확인해야 함 |
| 실시간 위치 공유 | 지도 SDK만 연결해도 다른 알바생의 위치가 전송되지는 않음. 위치 전송·참여자 조회 계약, 공유 범위/주기/종료 조건과 앱 구현이 별도로 필요함. 현재 확인한 work 공개 API에는 연속 위치 공유가 없음 |
| 실시간 채팅/읽음 | BE WSS/STOMP, 메시지 sync, read-position 공개 계약이 이미 있음. 프론트는 REST 조회/전송만 연결되어 자동 수신·재연결·sync·읽음 처리가 남음. 별도 유료 채팅 SDK나 키를 받아야 하는 항목이 아님 |
| 알림 목록/읽음 | 후속 구현 완료: `GET /api/notifications/me`, `/unread-count`, `PATCH /{id}/read`에 연결된 화면/cursor/업무 이동과 양 역할 홈 진입을 추가함. 서버가 읽음으로 응답한 뒤 반영. 계약 테스트/전체 빌드 통과, 실제 이벤트 E2E는 데이터 대기. FCM 푸시와는 별개 |
| 연락처 변경 | 후속 구현 완료: `/api/auth/account` 인증 발송/변경/상태 조회, 이메일·전화 입력, 코드와 멱등키·처리 상태 UI 연결. 계약 테스트/전체 빌드 통과. 실제 수신·변경 성공 검증은 동의한 계정/수신 정보가 필요함 |
| 회원 탈퇴 | 후속 구현 완료: `POST /api/auth/account/withdrawals`, `GET /{key}`와 재확인/멱등키/비동기 상태 UI 연결. BE는 비밀번호 body 없이 처리하므로 해당 계약을 따름. 기존 테스트 계정은 삭제하지 않았으며 실서버 완료 E2E는 미검증. 탈퇴 후 401을 성공으로 추정하지 않음 |
| 리뷰/이력 | 리뷰 작성·작성 대상·받은 리뷰 API가 이미 있음. 프론트 작성 화면/목록 연동이 미완이고 신뢰도 summary 미지원과 분리해야 함. BE 기본 리뷰 기간은 설정 가능한 14d이고 demo UI는 7일이므로 실제 모드에서 서버 기한을 따라야 함. 운영 값은 미확인 |
| 근무 취소/합의 | 본인 근무 cancellations의 요청·조회·승인·거절·철회 API가 존재하나 프론트에 미연결. 지원 취소 구현과는 별개 |
| 프로필 사진/수신 설정 | 카메라/갤러리 미리보기는 데모 세션 범위. 서버 사진 업로드/알림 수신 설정 계약은 확인되지 않아 live 비노출. 저장소 키를 프론트에 주는 것만으로 해결되지 않음 |
| 온보딩/입력 UX | 최초 1회 온보딩 슬라이드 미구현. 실제 공고 폼 날짜·시간·마감 입력 UX와 가입의 좌표 직접 입력도 보완 필요. 외부 계정 발급 때문에 못 하는 작업으로 분류하지 않음 |
| 매칭·추천·신뢰도 | 실서버 매칭 제안·수락 경로는 연결됐지만 두 역할 업무 E2E/상태 자동 갱신이 미검증. 비긴급 목록을 개인화 추천으로, noShowProbability를 임의 LOW/MIDDLE/HIGH로 간주하지 않음 |

수동 노쇼·즉시 지급 승인 등 공개 API가 없는 동작은 서버 정책에 맞게 UI에서 구분한다. internal endpoint를 모바일에서 호출하지 않는다.

## 3. 바로 전달할 최소 요청

사용자/지도 계정 소유자:

> Naver Cloud Maps에서 Dynamic Map을 선택하고 Android 패키지 `com.workernotfound.app`을 등록한 애플리케이션의 Client ID가 필요합니다. 소셜 로그인용 네이버 앱 키와는 별개입니다.

백엔드 담당자:

> 실제 카테고리 ID/name 계약, 테스트 OWNER 계정과 사업장, 기존 WORKER와 연결된 테스트 공고·근무·매칭 데이터를 부탁드립니다. Toss 테스트 사용 가능 여부와 해당 서버 키에 맞는 공개 client key/복귀·웹훅 설정도 확인 부탁드립니다. 메일/SMS/국세청 인증은 운영 서버 설정 및 발송 가능 여부만 확인해 주시고 서버 비밀키를 프론트에 전달할 필요는 없습니다. 푸시는 제공자/단말 토큰 등록·발송 계약을 함께 정해야 합니다.

## 근거

- FE: `app/build.gradle.kts`, `gradle/libs.versions.toml`, `settings.gradle.kts`, `app/src/main/AndroidManifest.xml`.
- FE: `WorkerJobMapView.kt`, `WorkerJobDetailScreen.kt`, `WorkerJobRepositoryImpl.kt`, `OwnerLiveLocationMap.kt`, `AuthScreen.kt`, `JobPublicationRepositoryImpl.kt`, `WorkerTrustRepositoryImpl.kt`.
- BE: auth `AccountContactController`/`AccountWithdrawalController`/`SignupService`, member `ReviewController`/`ReviewProperties`/`OwnerCommandService`, work `WorkCancellationController`, chat controllers/`ChatSocketConfiguration`, notification `NotificationController`/`NotificationService`, payment `OrderController`, `deploy/env.example`.
- [Naver Map Android SDK 시작하기](https://navermaps.github.io/android-map-sdk/guide-ko/1.html)
- [Firebase Android 프로젝트 등록](https://firebase.google.com/docs/android/setup), [FCM Android 시작하기](https://firebase.google.com/docs/cloud-messaging/android/get-started?hl=ko)
- [카카오 로그인 설정](https://developers.kakao.com/docs/ko/kakaologin/prerequisite), [네이버 로그인 가이드](https://developers.naver.com/docs/login/devguide)
- [Toss 결제창 연동](https://docs.tosspayments.com/guides/v2/payment-window/integration)

문서 점검 작업으로 앱 소스 변경 없음. 기존 76 tests 검증 이후 새 앱 검증을 실행한 것으로 표시하지 않는다. 외부 콘솔 등록과 실서버 설정 상태는 계속 미확인이다.

# 공고 테스트 결제 연결

2026-11-09. 사용자 요청으로 결제창 진입뿐 아니라 callback 검증→서버 승인→공고 상태 재조회까지 연결했다.

## 동작

- `PAYMENT_PENDING` / 주문 `CREATED` / 결제 `READY`, 변경 처리 중이 아닌 실제 주문에 테스트 결제 버튼을 표시한다.
- 버튼을 누를 때 job/payment-order 및 payment order를 다시 읽고 jobId/orderId/jobVersion/금액/통화를 대조한다. 이전 주문/바뀐 금액으로 결제를 열지 않는다.
- Toss JS v2 결제창을 앱 WebView에서 사용한다. 초기 화면과 복귀 URL은 Android의 앱 내부 HTTPS origin(appassets.androidplatform.net)이며 WebView가 직접 처리한다. 해당 경로를 백엔드 API로 발명하거나 원격 서버에 배포하지 않는다. local callback은 네트워크로 전송하지 않는다. iframe이나 native JS bridge는 사용하지 않는다.
- 성공 URL의 origin/path/nonce 및 단일 orderId/amount/paymentKey 값을 검증한다. 실패/취소/변조 응답을 승인으로 처리하지 않는다.
- 공개 `POST /api/payments/orders/{orderId}/confirm`, body `{paymentKey,amount}`로 서버 승인을 요청한다. 이 직전에 최신 주문 ID/버전/금액을 다시 대조한다. 상점 secret은 서버에만 있다.
- 승인 결과의 주문 identity를 다시 확인한다. 이미 DEPOSITED이면 중복 승인하지 않는다. CONFIRMING은 서버 처리 중이며, callback 성공 자체를 공고 게시 성공으로 표시하지 않는다. 부모 화면을 새로 조회해 서버 OPEN만 모집 시작으로 표시한다.
- 네트워크 결과 불명 시 같은 승인 정보만 메모리에서 재시도한다. 새 결제창을 자동으로 만들지 않는다. 프로세스가 종료되면 paymentKey는 보관하지 않고 기존 공고 ID로 서버 상태를 다시 확인해야 한다.
- 외부 카드 앱은 알려진 결제 스킴만 ACTION_VIEW로 전달하며 누락 앱/실패를 안내한다. 임의 intent component/extras는 전달하지 않는다. 앱스킴 조회 manifest 추가. 파일 접근/콘텐츠 접근/혼합 HTTP 콘텐츠는 차단하고 SSL 오류를 우회하지 않는다.

## 설정 및 범위

- 전에 전달받은 테스트 client key를 ignored local.properties의 toss.clientKey에 복원했다. 값은 이 문서나 Git에 넣지 않는다. BuildConfig의 test_ck_ guard를 유지한다. 실금전 결제 키는 사용하지 않는다.
- 기존 작업자의 인증/지도/통합 UI 미커밋 변경은 보존했다. 결제 기능은 feature/payment에 분리했고 공고 완료 상태 영역의 안내를 결제 컨트롤로 연결했다.
- 결제창이 열려도 서버 TOSS_ENABLED/secret 짝, 승인·이벤트 소비·공고 공개가 모두 정상이라는 의미는 아니다. 실제 테스트 결제 후 DEPOSITED 및 OPEN을 각각 확인해야 한다.
- 이 작업으로 공고 예치금 결제 경로를 추가했으나 소셜 로그인/푸시/기타 미완 기능이 모두 해결되는 것은 아니다.

## 검증

- live: ./docs/scripts/verify.sh 통과. demo: lintDebug/testDebugUnitTest/assembleDebug 통과. 각 104 tests, failures/errors 0.
- MockWebServer 공개 승인 경로/body, 금액·주문·버전·통화 mismatch, callback origin/path/nonce/중복 query/paymentKey 검증을 추가했다. 로그/진단 문자열에 키를 포함하지 않는다.
- emulator-5556에서 기존 실제 공고 #5를 조회하고 44,080원 주문으로 Toss 테스트 신용/체크카드 선택 화면까지 열리는 것을 확인했다. 테스트 표시가 있는 실제 provider 화면이다.
- 카드 정보 입력·약관 동의·최종 결제·실제 서버 confirm 및 OPEN 전환은 대신 실행하지 않았다. 사용자에게 테스트 결제의 나머지 단계를 넘겼다. 두 번째 에뮬레이터에 결제창을 열어 두었다.
- build/presentation/server.apk와 demo.apk를 갱신했고 두 에뮬레이터에도 서버 실행본을 갱신했다. 앱 설치 시 열려 있던 화면 상태는 다시 확인해야 한다.

## 근거

- BE 66e388a: OrderController / ConfirmOrderRequest / OrderApplicationService / OrderTransactionService.
- https://docs.tosspayments.com/guides/v2/payment-window/integration
- https://docs.tosspayments.com/guides/v2/webview
- https://docs.tosspayments.com/sdk/v2/js/environment
- https://developer.android.com/develop/ui/views/layout/webapps/load-local-content

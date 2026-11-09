# 친구 에이전트에게 전달할 프롬프트

아래 내용을 그대로 전달하세요. 자격증명은 포함되어 있지 않습니다.

```text
저장소: https://github.com/404-Worker-Not-Found/FE.git
당신 작업 브랜치: feature/#21-auth-job-integration
관련 이슈: #21. 이 브랜치에는 현재까지의 API/UI 통합 체크포인트가 들어 있다.

이 브랜치를 fetch/pull해서 작업을 시작해라. 기존 로컬 변경이 있으면 보존하고 별도 worktree를 사용해라. AGENTS.md와 docs/agent/decisions.md, failure-memory.md, work-handoff.md, docs/backend-integration.md를 먼저 읽고, 화면 변경 전 UI_Specification.md/style_guide.md/coding-rules.md를 확인해라.

당신 담당은 더 많은 작업량이 필요한 인증과 공고 등록·결제 흐름이다.
1. 기존 AuthRepository/암호화 토큰 저장/갱신을 재사용해 로그인 화면과 점주·알바생별 진입을 연결한다. 필요한 회원가입·이메일/문자 인증·사업자 검증·비밀번호 복구는 실제 BE 계약으로 구현한다. 소셜 로그인은 callback/provider 설정이 없으면 의존성을 명확히 남기고 가능한 작업부터 계속한다.
2. 기존 공고 등록 화면을 BE 필수 입력(title, businessId, categoryId 등)에 맞추고, 결제 대기→테스트 결제→공개 상태를 구별한다. 사업장/카테고리 식별자 조회나 테스트 결제 설정이 계약에 없으면 임의 고정 ID·endpoint·성공을 만들지 말고 백엔드 확인 항목으로 남긴다.
3. 테스트 계정/키는 사용자에게 별도로 전달받아라. Git·프롬프트·BuildConfig·로그에 비밀번호/토큰을 넣지 말고 실제 금전 결제를 수행하지 마라. 전달된 구글 계정은 사용자가 용도 확인 전 보류했으므로 사용하지 마라.

BE 저장소는 https://github.com/404-Worker-Not-Found/BE.git 이고 API base는 https://api.15-165-106-227.sslip.io/ 이다. Swagger만 믿지 말고 BE controller/DTO와 대조해라. UI/API 충돌은 사용자 지시대로 BE에 맞추고 차이를 기록해라. 인증정보를 모르면 endpoint/계정을 추정하지 마라.

다른 에이전트는 feature/#19-api-integration에서 GPS 출근·근무·점주 정산/지난 공고·UI 잔여 및 통합 검증을 맡는다. feature/worker와 feature/owner의 해당 부분은 수정하지 마라. 당신은 feature/auth와 feature/job 중심으로 작업하고 공유 navigation/DI/Gradle/Manifest는 additive하게 최소 변경해라. 문자열은 기능별 XML로 분리해라.

작업 중 docs/agent/friend-progress.md에 완료/미완/막힌 계약/검증/다음 행동을 남겨 중단돼도 자동 복원되게 해라. 기존 사용자 변경을 보존하고 작은 단위로 commit/push해라. 전체 ./docs/scripts/verify.sh와 필요한 검증을 수행한 뒤 develop 대상 draft PR을 만들고 #19 통합 브랜치 의존성을 명시해라. 리뷰 승인 없이 merge하지 마라. 타 브랜치 force-push/reset/rebase 금지. 진행이 막히면 독립적으로 가능한 일을 계속해라.
```

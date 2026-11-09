# Agent Failure Memory

This file records repeated or high-risk mistakes identified by the user and the correction rules future agents should follow.

Use this format:

```md
## YYYY-MM-DD - Short Title

Context:
- What the agent was trying to do.

Mistake:
- What went wrong.

Why it was wrong:
- Why this caused risk, confusion, or incorrect behavior.

Correct behavior:
- What future agents should do instead.

Related files:
- Optional file paths.
```

Keep entries short, concrete, and actionable.

<!--
No entries yet. Add an entry only when the user identifies a repeated or high-risk
mistake and asks for it to be recorded (see docs/agent/decisions.md -
"Failure Memory Requires User Identification").
-->

## 2026-11-08 - Verify before handing UI work to the user

Context:
- Emulator Korean keyboard installation and setup.

Mistake:
- Claimed readiness after installation and instructed the user to find an unverified Always show option that did not exist in the installed version.

Why it was wrong:
- Wasted user time and left recovery unfinished.

Correct behavior:
- Execute available checks and commands directly, inspect the installed version and actual UI, verify visible keyboard and Korean composition before claiming success, and continue recovery after failures.
- Keep an up-to-date checkpoint for pending work and honor requested resume scheduling.

## 2026-11-09 - 사용자 흐름보다 구현 편의를 우선한 점주 UI

Context:
- 점주 공고관리·공고 등록·마이페이지 구현과 사용자 점검.

Mistake:
- 공고관리에서 현재 공고를 누락하고, 저장된 주소를 다시 입력하게 하며, 날짜·시간 입력 방식과 이동 버튼을 중복 제공했다.
- 결제 복구용 내부 공고 ID를 일반 사용자가 직접 입력하게 했다.

Why it was wrong:
- 사용자가 이미 제공한 정보를 반복 입력하고 내부 구현을 이해해야 하며, 화면 이름과 실제 행동이 일치하지 않았다.

Correct behavior:
- 저장된 정보를 재사용하고 날짜·시간처럼 구조화된 값은 일관된 선택 UI로 제공한다.
- 내부 ID는 목록 선택과 탐색 상태로 전달하고 사용자 입력으로 노출하지 않는다.
- 상단 뒤로가기와 하단 주요 탭의 역할을 유지하며 같은 목적의 버튼을 불필요하게 중복하지 않는다.
- 개별 화면뿐 아니라 등록 → 목록 반영 → 상세/결제 재개 흐름과 빈 상태·오류 상태를 검증한다.

Related files:
- `docs/UI_Specification.md`
- `app/src/main/java/com/workernotfound/app/feature/owner/ui/OwnerPostingManagementScreen.kt`
- `app/src/main/java/com/workernotfound/app/feature/job/ui/JobPublicationScreen.kt`

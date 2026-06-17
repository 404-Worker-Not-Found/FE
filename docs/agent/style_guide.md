# 긴급알바 앱 디자인 스타일 가이드

**앱 개요:** 긴급 구인·구직 매칭 앱. 점주(사장)와 알바생 두 가지 역할.

> **다크모드 미지원 — 라이트 모드 전용으로만 구현.**

---

## 컬러 시스템

| 토큰 | 값 | 용도 |
|---|---|---|
| Primary (점주) | `#FF7E36` | 점주 CTA 버튼, 강조, 탭바 active |
| Primary Light | `#FFF0E6` | 점주 배경 틴트 |
| Worker (알바생) | `#FFB703` | 알바생 CTA 버튼, 강조, 탭바 active |
| Worker Emphasis | `#CC8F00` | 알바생 텍스트 강조, pressed |
| Worker Light | `#FFF8E0` | 알바생 배경 틴트 |
| Surface | `#FAF8F5` | 점주 화면 배경 |
| Worker Surface | `#FFFDF5` | 알바생 화면 배경 |
| App Border | `#EEEBE6` | 카드 테두리, 구분선 |
| Text Main | `#2C2C2C` | 기본 텍스트 |
| Text Sub | `#767676` | 보조 텍스트 |
| Text Placeholder | `#BBBBBB` | 힌트, 비활성 |
| Success | `#2EBD6B` | 완료, 승인, LOW 위험 |
| Success BG | `#E8F7EF` | 성공 배경 |
| Warning | `#F5A623` | 경고 |
| Warning BG | `#FFF4E0` | 경고 배경 |
| Danger | `#E94B4B` | 노쇼, 오류, 위험 |
| Danger BG | `#FDE8E8` | 오류 배경 |

---

## 타이포그래피

- **폰트:** Pretendard (한국어), Inter (숫자·영문)
- **앱 기본:** 14sp, weight 400
- **타이틀 (화면 헤더):** 18sp, Bold
- **섹션 레이블:** 11~12sp, Bold, color `#BBBBBB`
- **카드 타이틀:** 14~15sp, Bold
- **보조 텍스트:** 12~13sp, Regular
- **금액 강조:** 18~20sp, Bold, `#CC8F00` (알바생) 또는 `#FF7E36` (점주)

---

## 모양 & 레이아웃

- **카드 radius:** 16dp (일반 카드), 12dp (내부 요소), 24dp (바텀시트)
- **버튼 radius:** 14~16dp (일반), 999dp (pill/chip)
- **카드 elevation:** 0 — shadow 대신 `1dp stroke #EEEBE6` 사용
- **CTA 버튼 shadow:** `0 4dp 14dp rgba(255,183,3,0.35)` (Worker) / `rgba(255,126,54,0.35)` (Owner)
- **화면 좌우 패딩:** 16dp
- **카드 내부 패딩:** 16dp
- **카드 간격:** 12dp

---

## 컴포넌트 패턴

### 탭바 (BottomNav)

- **점주:** Home / Zap(긴급) / Clock(근무) / Chat / User, active `#FF7E36`
- **알바생:** Home / Search / Clock / Chat / User, active `#CC8F00`
- 배경 white, 상단 border `#EEEBE6`

### 긴급 배지

- `⚡ 긴급` — background `#FDE8E8`, text `#E94B4B`, pill shape

### 상태 배지 (chip)

| 상태 | 배경 | 텍스트 |
|---|---|---|
| 매칭완료 | `#E8F7EF` | `#2EBD6B` |
| 매칭중 | `#FFF8E0` | `#CC8F00` |
| 근무중 | `#EEF2FF` | `#6366F1` |
| 노쇼 | `#FDE8E8` | `#E94B4B` |

### 노쇼 위험도

- LOW: `#2EBD6B`
- MIDDLE: `#F5A623`
- HIGH: `#E94B4B`

### 토글 스위치

- ON: `#FFB703` (알바생) / `#FF7E36` (점주)
- OFF: `#EEEBE6`

### 지도 (커스텀 뷰)

- 배경: `#E8E4DC`, 블록: `#D4CFC8`, 공원: `#C8DFC8`
- 가게 핀: `#FF7E36` 원, 알바생 위치: `#3B82F6` dot
- 탐색 지도 시급 말풍선: `#FFB703`, 긴급은 ⚡ 포함

---

## 화면별 배경색

| 화면 | 배경색 |
|---|---|
| 공통 / 점주 화면 | `#FAF8F5` |
| 알바생 화면 | `#FFFDF5` |
| 스플래시 | `#FF7E36` (전체) |
| 카드 / 폼 영역 | `#FFFFFF` |

---

## 톤 & 무드

둥글고 따뜻한 느낌. 직각 없음. 그림자보다 테두리 선호. 오렌지(점주)·노랑(알바생)으로 역할 구분. 긴급함은 red accent. 당근마켓 스타일의 친근한 마켓플레이스 UI.

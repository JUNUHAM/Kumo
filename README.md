<div align="center">

# ☁️ KUMO : Location-Based Job Platform

**지도 기반 한/일 글로벌 구인구직 매칭 플랫폼**

[![Java](https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=java&logoColor=white)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)]()
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)]()
[![JavaScript](https://img.shields.io/badge/JavaScript-ES6+-F7DF1E?style=flat-square&logo=javascript&logoColor=black)]()

</div>

<br>

## 📝 프로젝트 소개
**KUMO**는 일본(도쿄, 오사카) 지역의 구인구직 정보를 지도 위에서 한눈에 확인하고, 내 주변의 일자리를 손쉽게 탐색할 수 있는 **지도 기반 매칭 플랫폼**입니다.
한국어와 일본어를 동시 지원하며, 구직자와 구인자 간의 실시간 1:1 채팅 기능을 제공하여 빠른 소통을 돕습니다.

> 💡 **데모 영상 (준비 중)** > *(여기에 나중에 실제 지도화면이나 채팅 작동 GIF를 넣으시면 아주 멋집니다!)*

<br>

## ✨ 주요 기능 (Key Features)

### 🗺️ 스마트 맵 탐색
- **구글 맵 연동 & 클러스터링:** 수많은 공고를 맵 위에서 깔끔하게 그룹화하여 렌더링
- **GPS 기반 주변 탐색:** 내 위치를 기반으로 반경 내의 일자리 필터링 (도쿄/오사카 지역 완벽 지원)

### 💼 맞춤형 공고 관리
- **다국어 지원:** 사용자 설정에 따라 한국어(KR) / 일본어(JP) 공고 내용 및 UI 자동 변환
- **바텀시트 & 플로팅 카드:** 지도를 이동하며 직관적으로 공고 요약 정보 확인
- **최근 본 공고 & 찜하기(Scrap):** 관심 있는 공고를 로컬/DB에 저장하여 모아보기

### 💬 실시간 소통 및 관리
- **1:1 실시간 채팅 (STOMP/WebSocket):** 공고별로 구인자-구직자 간 즉각적인 채팅방 생성 및 대화
- **어드민 크롤링 데이터 분리:** 자체 등록 공고와 시스템(Admin) 수집 공고를 분리하여 체계적인 데이터 관리
- **악성 공고 신고 기능:** 스팸, 허위 매물 차단을 위한 모달 및 신고 접수 시스템

<br>

## 🛠 기술 스택 (Tech Stack)

| 구분 | 기술 스택 |
| :--- | :--- |
| **Frontend** | HTML5, CSS3(Variables), JavaScript (Vanilla + jQuery), Thymeleaf |
| **Backend** | Java 17, Spring Boot, Spring Security, Spring Data JPA |
| **Database** | MySQL (Geocoding & Spatial Data), Hibernate |
| **API & Tools** | Google Maps API (MarkerClusterer), WebSocket(STOMP) |

<br>

## 📌 릴리즈 로드맵 (TODO List)

- [x] 🗺️ **지도 기본 기능 구현** (Google Maps 연동 및 마커 클러스터링)
- [x] 📱 **사이드바 / 바텀시트 UI 구현** (반응형 뷰 적용)
- [x] 📍 **내 주변 일자리 탐색** (GPS 기반 반경 필터링 완료)
- [x] 🔍 **검색 및 필터링** (지역/키워드 연동 검색 구현 완료)
- [x] ⭐ **최근 본 공고 및 찜하기(Scrap) 기능 구현**
- [x] 💬 **실시간 1:1 채팅 기능 도입**
- [x] 🌐 **한국어/일본어 다국어(i18n) 시스템 적용**
- [x] 🔔 알림(Push) 시스템 및 안읽은 메시지 뱃지 고도화
- [x] 🛠️ 통합 관리자(Admin) 대시보드 구축

<br>

## 🤝 협업 규칙 (Contributing)

원활한 팀 프로젝트 진행을 위해 아래 사항을 꼭 지켜주세요! 😊

1. **브랜치(Branch) 생성:** 작업 시 반드시 `feature/기능명` 형태로 브랜치를 파서 작업해 주세요.
2. **구조 변경 사전 공유:** 데이터베이스 테이블 구조(Entity)를 변경하거나, 핵심 비즈니스 로직 및 공통 컴포넌트(Header/Footer/BaseEntity)를 수정·포크 하실 때는 **반드시 팀원들에게 먼저 공유**해 주세요! 🚨
3. **커밋 메시지 규칙:** - `[FEAT]` : 새로운 기능 추가
    - `[FIX]` : 버그 및 에러 수정
    - `[REFACTOR]` : 코드 리팩토링
    - `[CHORE]` : 환경설정, 빌드 업무, 패키지 매니저 설정 등

---
*Copyright © 2026 KUMO Project Team. All rights reserved.*
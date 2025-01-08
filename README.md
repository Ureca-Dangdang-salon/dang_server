# 댕댕살롱 (Dang Dang Salon)

**Client Repo**: [DangDang Salon Client](https://github.com/Ureca-Dangdang-salon/dang_client)

![댕댕살롱 소개 이미지](https://github.com/Ureca-Dangdang-salon/dang_client/raw/main/public/images/%EC%86%8C%EA%B0%9C%EA%B8%80.jpg)

## 🏆 프로젝트 개요
**댕댕살롱**은 반려견과 미용사를 연결하여 맞춤형 미용 서비스를 제공하는 중개 플랫폼입니다. 

> LG U+ 유레카 비대면 1조 최종융합 프로젝트 <br>
> 개발기간: 2024.11.12 ~ 2024.12.24 (7주)

### 🎯 목표
- 반려견의 특성과 지역에 맞는 미용사 추천
- 사용자가 간편하게 견적 요청, 결제, 리뷰를 작성할 수 있는 환경 제공
- 미용사가 전문성을 홍보하고 사용자와 소통할 수 있는 기회 마련
- 안정성과 신뢰성을 갖춘 서비스 제공

## 👨🏻‍💻 팀원
| <img src="https://github.com/skang9810.png" width="100px;" alt="강서진"/> | <img src="https://github.com/clov2r.png" width="100px;" alt="최명지"/> | <img src="https://github.com/wjdalsths.png" width="100px;" alt="손정민"/> | <img src="https://github.com/ldhapple.png" width="100px;" alt="이도현"/> | <img src="https://github.com/tkamo2006.png" width="100px;" alt="한유성"/> | <img src="https://github.com/99MinSu.png" width="100px;" alt="이민수"/> |
| :---: | :---: | :---: | :---: | :---: | :---: |
| [강서진](https://github.com/skang9810) | [최명지](https://github.com/clov2r) | [손정민](https://github.com/wjdalsths) | [이도현](https://github.com/ldhapple) | [한유성](https://github.com/tkamo2006) | [이민수](https://github.com/99MinSu) |
| <b> FE </b> | <b> FE </b> | <b> FE </b> | <b> BE </b> | <b> BE </b> | <b> BE </b> |

## 👩‍💻 역할 분담 (R&R)
| 이름 | 역할 |
| -------------------------------------------------------------- | ----------------------------------------------------- |
| 강서진 | 내 정보 및 프로필 관리, 알림 기능, 쿠폰 이벤트 |
| 최명지 | 메인 페이지, 콘테스트 기능, 마이페이지 |
| 손정민 | 소셜 로그인 및 회원가입, 견적 서비스, 채팅 시스템 |
| 이도현 | 소셜 로그인 및 콘테스트, 채팅 시스템, 쿠폰 이벤트 |
| 한유성 | 리뷰 기능, 마이페이지, 쿠폰 이벤트 |
| 이민수 | 견적 서비스, PG 결제, 알림, AI 시뮬레이션 |

## 📋 주요 기능

### 🔑 소셜 로그인
- OAuth2 Code Grant 방식을 사용하여 소셜 로그인 구현
- 로그인 시 사용자 이름, 프로필 이미지 자동 가져오기
- 신규 사용자 자동 회원가입 및 추가 정보 입력
- **Details**: [소셜로그인 구현](https://ldhapple.github.io/posts/%EC%86%8C%EC%85%9C-%EB%A1%9C%EA%B7%B8%EC%9D%B8-(%EA%B5%AC%EA%B8%80,-%EB%84%A4%EC%9D%B4%EB%B2%84,-%EC%B9%B4%EC%B9%B4%EC%98%A4)/)

### 🐶 반려견 및 미용사 프로필 관리

https://github.com/user-attachments/assets/ce9372a0-4770-4664-a161-3f10fb7bedf6

- 반려견 정보 등록 및 관리: 이름, 사진, 나이, 종, 체중, 성격, 건강 상태 등
- 미용사 프로필 설정: 서비스 이름, 경력, 자격증, FAQ 등 세부 정보 관리

### 🏆 애견 미용 콘테스트

https://github.com/user-attachments/assets/65132820-295f-49eb-adfc-8dff1a8dcc93

- 반려견 미용 사진을 등록하여 콘테스트 참여
- 좋아요 투표를 통해 우승자 선정
- 우승자에게 쿠폰 지급 및 미용사 홍보 효과

### ✂️ 견적 요청 및 확인
<img src="https://github.com/user-attachments/assets/8fbd0a70-ad45-4f93-9171-40924db3fad5" width=30% height=50%>
<img src="https://github.com/user-attachments/assets/33c7cf24-0446-40c3-b2e3-023803778a1b" width=30% height=50%>
<img src="https://github.com/user-attachments/assets/974f15b7-aaad-40de-afe6-06da7a6e8c0b" width=30% height=50%>

- 사용자가 지역 기반으로 견적 요청
- 미용사가 견적서를 작성하여 사용자에게 전달
- 채팅을 통한 견적 협의 가능

### 💳 PG 결제 시스템

https://github.com/user-attachments/assets/59a3199b-a7aa-41ca-82d7-5b4c84daadba

- 신용카드 및 간편결제 지원
- 결제 취소 및 환불 기능 제공
- 멱등키를 활용해 같은 요청이 중복되는 것을 방지
- Retry를 3번 한정하고, 전부 실패 시 로그를 남기고 결제 상태를 저장하는 방식으로 사용자 경험 고려

### ✍️ 리뷰 서비스
- 서비스 이용 후 별점과 리뷰 작성 가능
- 미용사는 고객 리뷰 조회 및 관리

### 🔔 알림 서비스
- 새로운 견적, 결제 완료, 예약일 하루 전 알림 등 실시간 알림 제공
- FCM(Firebase Cloud Messaging)을 통한 구현
- Java Mail Sender를 통한 이메일 알림 구현
- FCM 구독 알림 및 쿠폰 이벤트 알림(kafka 활용) 추가 구현 
- **FCM 토큰 관리**: [FCM 구현](https://dlalstn1023.tistory.com/24)
- **Redis**: [알림 리스트 구현](https://dlalstn1023.tistory.com/25)
- **추가 기능**: [FCM Topic 및 쿠폰 알림 Kafka 활용](https://dlalstn1023.tistory.com/26)

### 🎁 쿠폰 서비스

https://github.com/user-attachments/assets/743cb1d7-08cb-4ea0-925d-c77a5b3c930b

- 선착순 쿠폰 발급 및 사용
- 대량 트래픽을 처리하기 위한 안정적인 시스템 설계
- 사용자 경험을 위한 실시간 대기열 정보 표시 (SSE)
- **Details**: [쿠폰 + 실시간 대기열 정리](https://ldhapple.github.io/posts/%EC%BF%A0%ED%8F%B0-%EC%9D%B4%EB%B2%A4%ED%8A%B8-%EA%B5%AC%ED%98%84/)

### 💬 채팅 서비스
- 1:1 실시간 채팅으로 견적 협의 및 커뮤니케이션 지원
- **WebSocket**: [WebSocket 구현](https://ldhapple.github.io/posts/%EC%B1%84%ED%8C%85-%EC%8B%9C%EC%8A%A4%ED%85%9C-1%EC%B0%A8-%EA%B5%AC%ED%98%84/)
- **STOMP**: [STOMP 프로토콜 전환](https://ldhapple.github.io/posts/%EC%B1%84%ED%8C%85-%EC%8B%9C%EC%8A%A4%ED%85%9C-2%EC%B0%A8-%EA%B5%AC%ED%98%84/)
- **Kafka**: [Kafka 도입](https://ldhapple.github.io/posts/%EC%B1%84%ED%8C%85-%EC%8B%9C%EC%8A%A4%ED%85%9C-%EC%B9%B4%ED%94%84%EC%B9%B4-%EB%8F%84%EC%9E%85/)

<br><br>**시연 영상**: https://youtu.be/i38VVIf9DYY

## 🔨 Tech Stack

- **Backend**: Java 17, Spring Boot 3.3, Spring Data JPA, OAuth2
- **Frontend**: React, Vite, Zustand, Material UI
- **Database**: MySQL, MongoDB, Redis, Flyway
- **Infra**: AWS EC2, S3, CloudFront, Docker, Nginx
- **CI/CD**: Github Actions, Prometheus, Grafana
- **Test Tools**: JUnit, Mockito, Jacoco, Rest-Assured

## 🗂️ 서비스 아키텍처

![서비스 아키텍처](https://github.com/user-attachments/assets/e82dc80c-5957-40fb-a5b3-5ea51806bfa5)

- **로드밸런서, Nginx**
  - 퍼블릭 서브넷에 배치된 로드밸런서가 트래픽을 처리하게 된다.
  - 정적 리소스 요청은 Nginx를 통해 캐싱되어 빠르게 처리되며, API 요청은 백엔드 로드밸런서로 전달되어 분산된다.
- **가용영역(AZ) 분산**
  - 서비스 중단에 대처하고자 두 개의 가용영역으로 리소스를 분산 배치 했다.
  - 백엔드 메인 서버는 각 AZ에 배치되어 로드밸런서가 트래픽을 분산처리하도록 구성했다.
- **데이터베이스 구성**
  - MySQL (RDS): Primary 노드와 Replica 노드로 구성해 장애에 대비한다.
  - Redis (Elasticache): 마찬가지로 Primary 노드와 Replica 노드로 구성해 장애에 대비한다.
- **MongoDB, Kafka**
  - MongoDB와 Kafka는 각각 EC2 인스턴스에 직접 배치해 사용했다.
- **CloudFront, AWS S3**
  - 정적 파일을 빠르게 제공할 수 있도록 CDN, 버킷을 활용한다.
- **모니터링 도구**
  - Grafana, Loki, Scouter 등을 메인 서버에 함께 구성해 서버의 상태와 애플리케이션 로그를 모니터링한다.

- **Details**: [배포 구조 정리](https://ldhapple.github.io/posts/%EC%B5%9C%EC%A2%85-%EB%B0%B0%ED%8F%AC-%EA%B5%AC%EC%A1%B0%EB%8F%84/)

### CI/CD 파이프라인

![CI/CD 파이프라인](https://github.com/user-attachments/assets/5b62762d-645f-45a9-a1b7-96723e522b24)

- Github Actions와 Docker를 활용한 자동 배포
- React 정적 리소스는 Nginx로, Spring 서버는 Docker 컨테이너로 배포

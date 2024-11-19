# 댕댕살롱 Dang Dang Salon

**Client Repo**: [DangDang Salon Client](https://github.com/Ureca-Dangdang-salon/dang_client)

![alt text](https://github.com/Ureca-Dangdang-salon/dang_client/raw/main/public/images/%EC%86%8C%EA%B0%9C%EA%B8%80.jpg)

## 주요 기능

- 소셜 로그인
  - 소셜 로그인 시 사용자 이름, 프로필 이미지 정보를 가져옴.
  - 추가 정보 입력:
    - 지역, 이메일, 역할(사용자/미용사).
  - 신규 사용자일 경우 소셜 계정 정보를 기반으로 자동 회원가입 처리.
- 반려견 및 미용사 프로필 관리
    - 지역(필수) 및 반려견 프로필 등록(선택 가능).
    - 반려견 프로필:
      - 이름, 사진, 나이, 종, 체중, 성격, 건강 상태 등 관리.
  - 미용사 프로필:
    - 필수 정보:
      - 서비스 이름, 연락 가능 시간, 전화번호, 서비스 지역, 제공 가능한 서비스 목록, 사업자 번호, 가게 위치 정보.
    - 선택 정보:
      - 경력, 자격증, FAQ, 서비스 설명, 채팅 시작 문구.
- 애견 미용 콘테스트
  - **참여 방식**:
    - 참여자는 댕댕살롱 이용 결제 내역을 바탕으로 참가 가능.
    - 반려견 미용 사진 및 자랑 문구를 등록하여 시작.
  - **우승 기준**:
    - 콘테스트 페이지에서 좋아요를 가장 많이 받은 참가자가 우승.
    - 좋아요는 취소 가능하며, 중복 참여 및 중복 좋아요 방지.
  - **우승자 혜택**:
    - 쿠폰 지급.
    - 미용사 홍보(우승자의 미용사 정보 포함).
  - **우승자 선정**:
    - 이벤트 종료 시 스케줄러를 통해 좋아요 수 1등 산정 후 저장.
- 견적 요청 및 확인
  - **고객 견적 요청**:
    - 반려견 프로필 정보, 원하는 서비스, 추가 정보를 입력하여 견적 요청.
    - 지역 기반으로 해당 지역의 미용사들에게 요청 전달.
  - **미용사 견적 처리**:
    - 미용사는 고객의 요청을 확인하고 견적서를 작성해 보냄.
  - **고객의 선택**:
    - 견적서를 수락하여 결제 진행.
    - 추가 협의를 위해 채팅 시작 가능.
  - **채팅을 통한 협의**:
    - 수정된 견적서를 채팅으로 보내고 이를 바탕으로 결제 진행.
- PG 결제
  - **기능**:
    - 신용카드 및 간편결제(카카오페이, 네이버페이 등) 지원.
    - 결제 취소 및 환불 처리.
    - 결제 내역 조회 및 견적서 확인.
- 리뷰 서비스
  - **리뷰 작성**:
    - 서비스 이용 후 별점과 리뷰 작성 가능.
    - 리뷰 수정 및 삭제 기능.
  - **리뷰 관리**:
    - 사용자: 내가 쓴 리뷰 확인.
    - 미용사: 고객 리뷰 조회 및 관리.
- 알림 서비스
  - **알림 조건**:
    - 고객:
      - 견적 요청 수락/거절 (푸시) 알림.
      - 견적서 도착 (푸시) 알림.
      - 예약 취소 (푸시, 이메일) 알림.
    - 미용사:
      - 새로운 견적 요청 도착 (푸시) 알림.
      - 결제 완료 (푸시) 알림.
      - 예약일 하루 전 (푸시, 이메일) 알림.
      - 리뷰 작성 (푸시) 알림.
  - **알림 방식**:
    - 실시간 푸시 알림 또는 이메일.
- 쿠폰 서비스
  - **쿠폰 발급**:
    - 선착순 쿠폰 발급.
    - 발급 시 높은 트래픽을 대비한 안정성 고려.
  - **쿠폰 사용**:
    - 결제 시 금액 할인 적용.
  - **콘테스트 우승자 쿠폰**:
    - 우승자에게 추가적인 쿠폰 혜택 제공.
- 채팅 서비스
  - **기능**:
    - 1:1 실시간 채팅방 생성.
    - 미용사 프로필에 설정된 채팅 시작 문구가 있다면, 채팅방 생성 시 자동 발송.
  - **메시지 관리**:
    - 안 읽은 메시지부터 조회.
    - 채팅방 나가기 기능.
  - **견적 협의**:
    - 채팅을 통해 수정된 견적서를 주고받을 수 있음.

## 🔨 Tech Stack

- **Backend**: Java 17, Spring Boot 3.3, Spring Data JPA, OAuth2
- **DB**: Mysql, MongoDB, Redis, Flyway (Migration)
- **Test Tool**: JUnit, Mockito, Postman, Jacoco, Rest-Assured
- **Infra**: Nginx, Docker, AWS EC2, AWS S3, Github Actions, Prometheus, Grafana, AWS CloudFront

## ERD

![ERD](https://file.notion.so/f/f/e01fa883-e589-4301-9d54-e4c6b88f1ac1/b5c0c6aa-06cd-4091-9c12-91ffc1aed859/DangDangSalon_(3).png?table=block&id=d7f5d202-099d-4c26-bda7-892d19d4f099&spaceId=e01fa883-e589-4301-9d54-e4c6b88f1ac1&expirationTimestamp=1732075200000&signature=HBNrn3Vt_1gMuW7i6mwf5mIkngHVOrLSDcBVax5I7io&downloadName=DangDangSalon+%283%29.png)

## 서비스 요청 흐름도

![서비스 요청 흐름도](https://blog.kakaocdn.net/dn/uqxT8/btsKNyDEDiR/JUBVzkF6hpmpkizkcNBdaK/img.png)

## CI/CD

![CI/CD](https://blog.kakaocdn.net/dn/bgQxzO/btsKNpUzLo2/hIwvoaFXiNQ7WYPpz9aky1/img.png)
=======

# Devlog

개인 기술 블로그 개발.<br>
서버는 Oracle Cloud Free Tier의 VM (VM.Standard.A1.Flex)을 활용하여 구축하였습니다.

## Stack

- Frontend: React.js (CRA), flowbite, axios
- Backend: Spring Boot, Spring Security, JPA
- Database: MySQL (Local), Oracle DB (Product)
- DevOps: Github Actions, Nginx, Oracle Cloud

## Libraries

### Frontend

- react-router-dom
- react-toastify
- react-toc
- recoil
- @uiw/react-md-editor
- rehype-video
- mem

### Backend

- oauth2-client
- lombok
- jjwt
- jacocoTestCoverageVerification

## Project

웹서비스에서는 제대로 완성해본 프로젝트가 없어서 한번 도전해보고자 했습니다. 수익을 내고자 하는 프로젝트도 아니고, 기본기의 완성을 위한 목적이었기 때문에 `개인 블로그 (기술 블로그)`를 주제로 삼았습니다. 추가적으로 `LLM AI`를 활용하여 블로그에 적절히 기능을 넣어보는 시도도 해볼 생각입니다.

### 시스템 아키텍처

![시스템 아키텍처](https://github.com/westreed/Devlog/blob/main/readme_src/system_architecture.png)

### WAS 아키텍처

API 게이트웨이 패턴(Spring Cloud Gateway)을 적용했습니다.

LLM 서비스는 추후 확장을 생각해서 Langchain이나 여러 AI 모델을 활용하기 좋은 Python 서버로 결정하게 되었는데, `승인된 유저`만 LLM 서비스를 호출할 수 있게 하려면 JWT 검증이 필요하고 유저데이터 접근도 필요했습니다. 또한, 이런 문제를 해결하기 위해 Main 서버에서 먼저 검증하고 유저데이터도 처리한 후 LLM 서비스를 호출하는 방식은 Main 서버가 중계서버가 되어야 했습니다. 여기서 문제는 LLM 서비스의 Response Type은 SSE 통신이었기 때문에 Main 서버의 부담도 같이 늘어나기 때문에 API 게이트웨이 패턴을 적용하게 되었습니다.

#### 장점

- LLM 서비스의 Response Type은 SSE(Server Sent Event)로 응답을 보내는데, 중계서버 없이 바로 응답 가능.
- Gateway 서버에서 요청에 대한 전처리 (JWT 검증), 후처리 용이함.

#### 단점

- 개발, 배포 및 관리해야 하는 포인트 증가.

### E-R 다이어그램

### OAuth 2.0 로그인 구현

Frontend는 React, Backend는 Spring을 사용하여 웹서비스를 만들고 있다보니, OAuth 2.0 로그인 구현에 어려움이 있었습니다.

1. Frontend에서 OAuth 로그인을 처리하면, 구글의 경우 `google-client-id`, `google-client-secret`과 같은 환경변수를 프론트에서 관리해야 합니다.
2. Backend에서 OAuth 로그인을 처리하면, `Resource Server`로부터 받은 유저 정보를 기반으로 JWT를 발급할 수 있지만 프론트에 로그인이 완료되었음을 알리기 위한 방법이 필요합니다.
   1번 방법은 실제로 관련 라이브러리도 많이 존재했지만, 프론트에서 관리하는 것은 불안하다고 생각하여 2번 방법을 선택하게 되었습니다. 그렇다면, 프론트에서 로그인이 완료되었음을 어떻게 알릴 수 있을지 고민을 하게 되었습니다.

결론적으로, 백엔드 서버에서 로그인이 완료되었을 때 작동하는 `SuccessHandler`에서 set-cookie로 JWT를 함께 실어서 Redirect하는 방식으로 구현할 수 있었습니다. 이때, Redirect하는 주소는 프론트에서 로그인에 성공했음을 명확히 알 수 있도록 별도의 `callback` 경로로 설정하였습니다.

![OAuth 2.0 로그인 구현](https://github.com/westreed/Devlog/blob/main/readme_src/OAuth2_Sequencediagram.png)

### Redis Pub/Sub

Redis를 활용하여 Pub/Sub를 구현하였습니다. 메일서버와의 비동기 연결을 위해 사용했고, 메시지 전달 여부가 크게 중요하지 않았기 때문에 Kafka나 RabbitMQ보다는 Redis를 선택하게 되었습니다.

## TODO-List

- 프로필 페이지 만들기
- 알림 기능
- 게시글 태그 기능
- LLM AI를 활용한 게시글 작성 서포트 기능
- OpenGraph 적용하기 (react-helmet)
- SEO 최적화 (sitemap 생성, robots.txt)

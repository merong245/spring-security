# spring-security
스프링 시큐리티야~~  
vX놀아보자Xv

# AuthenticationManager와 Authentication

AuthenticationManager는 인터페이스로 Authentication하나만 가지고 있음

SpringContextHolder는 Authentication의 정보를 담고만 있을뿐 실제로 인증처리 및 관리는 AuthenticationManager가 해줌

인증 진행시 여러 인증 Provider가 동작 . 처음은 AnonymousProvider 이후 parent의 Provider가 동작

이후 SpringContextHolder가 가진 pricipal 객체는 내가 만든 UserDetailsService를 상속받아 생성한 객체  

# ThreadLocal 

Java.lang 패키지에서 제공하는 쓰레드 범위 변수로 쓰레드 수준의 데이터 저장소를 의미한다.
- 데이터 공유는 같은 쓰레드 내에서만 공유
- 같은 쓰레드인 경우 데이터를 메소드 파라미터로 전송할 필요가 없다.

직접적으로 사용하지는 않으나 알게 모르게 사용 중   
ex) @Transactional, SecurityContextHolder의 기본전략

# Authentication과 SecurityContextHolder
AuthenticationManager에게 인증을 받은뒤 Authentication은 어디서 무얼할까?

### UsernamePasswordAuthenticationFilter
- 폼 인증 처리를 해줌
- 인증된 Authentication객체를 SecurityContextHolder에 넣어줌
- SercurityContextHolder.getContext().setAuthentication(authentication);
- 즉, AuthenticationManager를 사용해서 인증받는 것이 이 필터이다!

### SecurityContextPersistenceFilter
- SecurityContext를 HTTP session에 캐싱하여 Authentication을 공유
- SecurityContextRepository를 교체하여 HTTP 세션이 아닌 다른 곳에 저장도 가능
- 즉, 이 필터를 통해 이미 인증이 된 사용자라면 추가로 인증할 필요없이 사용가능 -> 다른 요청이라도 동일한 객체를 리턴
  
스프링 시큐리티의 기본 전략은 세션을 사용하므로 클라이언트의 상태를 서버에 저장한다.  
무상태성을 유지하기 위해서는 매번 인증하는 JWT 같은 방식을 사용하여 SecurityContextHolder에 인증 정보를 넣어주는 역할을 하는 필터를 사용하는 방법이 있다.  

# Filter와 FilterChainProxy
- 스프링 시큐리티가 제공하는 모든 필터는 FilterChainProxy가 호출한다.
- request가 들어오면 SecurityFilterChain목록에서 매치되는 URL을 발견한다면 해당 filter를 동작시킨다. 

### 그럼 Filter목록들은 어디서 올까?  
과거에는 WebSecurityConfigurerAdapter에서 config을 설정할 때 생성하게 된다.
현재는 SecurityFilterChain를 설정해서 직접 빈으로 등록하면서 각 configure가 Filter가 된다.
  
### 어떤 Filter가 먼저 동작할까?
우선적으로 동작하고 싶은 필터가 있다면 @Order 어노테이션을 활용한다.  
모든 요청에 인증을 필요로하는 필터와 모든 요청을 허용하는 필터가 있으면 스프링부트가 에러를 발생시킨다.  
따라서 모든 요청에 인증을 필요로 하는 config에 @Order를 통해 우선순위를 높게준다면 해당 필터가 먼저 동작하여 모든 요청에 인증을 필요로 하게된다.
만약 모든 요청을 주고 일부를 권한만 막고 싶다면 Filter의 Order를 조작하는 것보다 antMathcer를 통해 전부 허용한뒤 세부 정보를 수정하는 것이 권장된다.  


사실 @EnableWebSecurity는 설정을 하지않아도 스프링부트가 자동으로 추가해준다.  

# DelegatingFilterProxy와 FilterChainProxy
스프링부트는 서블릿기반 웹 어플리케이션이다. 그리고 내장되어있는 기본 서블릿 컨테이너는 톰캣이다.  
서블릿 동작 전과 후에 일을 처리하는 필터를 서블릿 필터라고 한다.  

### DelegatingFilterProxy
- 이름에서부터 알듯이 자신이 직접 처리하지 않고 역할을 위임하는 필터
- 스프링 IoC 컨테이너에 들어있는 필터에 위임히기 위해서는 타겟 빈 이름을 설정해주어야만 한다.
- 스프링 부트 사용시 자동 등록된다.

### FilterChainProxy
- springSecurityFilterChain이라는 이름으로 빈에 등록되어있다.

따라서 스프링 부트를 사용하면 SecurityFilterAutoConfigure설정에 의해 DelegatingFilterProxy에 FilterChainProxy가 등록되어 스프링에 필터 처리를 위임하게 된다.  
즉, 기존에 설명했던 필터들은 전부 서블릿 필터들이고, DelegatingFilterProxy에 의해 위임받아 동작하는 것이다.  

# AccessDecisionManager
Access Control 결정을 내리는 인터페이스로 인가(Authorization)를 처리
- AffirmativeBased : 여러 Voter중 한명이라도 허용이라면 허용(Default)
- ConsensusBased : 다수결
- UnanimousBased : 만장일지

AccessDecisionVoter
- Authentication이 특정한 Object에 접근할 때 필요한 ConfigAttributes(permitAll(), hasRole() 등)를 만족하는지 확인
- WebExpressionVoter : 웹 시큐리티에서 사용하는 기본 구현체, ROLE_XXXX를 확인
- RoleHierarchyVoter : 계층형 ROLE 지원. ADMIN > MANAGER > USER   

AccessDecisionManager 또는 Voter를 커스텀하는법
User 페이지를 만들어서 유저가 접근할 수 있도록 권한을 부여한다. 어드민이 접근할 수 있을까?  
불가능하다. 어드민은 유저권한을 갖고있지 않기 때문이다.  
따라서 계층형 Role을 지원하는 RoleHierarchyVoter를 적용시키도록 Voter에 커스텀해야한다.(또는 ADMIN유저 생성시 다른권한을 코드상으로 부여해줄 수 있다.)  
  
구조를 보면 사실상 똑같은 AccessDecisionManager를 넣어서 따로 굳이 만들 필요 없이 기존 핸들러에 추가적으로 계충을 추가해주면 된다.(2.7버전에서는 빈으로 주입으로 넣어사용하기 때문에 이 방식 x)  

# FilterSecurityInterceptor
AccessDecisionManager를 사용해서 Access Control을 하거나 예외처리하는 필터  
대부분의 경우 FilterChainProxy로 가장 마지막 필터로 들어있다.  

# ExceptionTranslationFilter
Filter Chain에서 발생하는 AuthenticationException과 AccessDeniedException을 처리하는 필터  

### AuthenticationException이 발생 시 - 인증 확인
- AuthenticationEntryPoint 실행 -> 사용자가 인증하도록 처이 
- AbstractSecurityInterceptor 하위클래스(ex. FilterSecurityInterceptor)에서 발생하는 예외만 처리
- UsernamePasswordAuthenticationFilter에서 발생하는 인증 에러의 경우는??
  - AbstractAuthenticationProcessingFilter(UsernamePasswordAuthenticationFilter의 상위 클래스)에서 인증 에러를 발생시킨다. 
   
### AccessDeniedException 발생 시 - 권한 확인
- 익명 사용자라면 AuthenticationEntryPoint 실행 -> 사용자가 인증하도록 처리
- 익명 사용자가 아니라면 AccessDeniedHandler에게 위임 -> 권한없으므로 예외처리


# 스프링 시큐리티 적용 여부 설정
여태까지는 스프링 시큐리티가 필터를 적용해서 처리  
우리는 하나의 요청을 보내더라도 실제로 브라우저는 여러 요청을 보내게 된다.(ex. favicon.ico)  
따라서 favicon요청이 허가되지 않았더라면 인증되지 않은 사용자이므로 login페이지도 요청하게된다.    
동작을 확인하기 위해서는 FilterChainProxy에 디버거를 잡고 확인해보자.  
아무튼 favicon 요청에 대해서는 비인가 사용자가 요청할 수 있게 등록한다면 이런 문제는 없어지겠지만 매번 이러한 static자원을 등록해야할까?
- 스프링 시큐리티 5.7버전 이상부터는 configure를 커스텀하는 것이 아닌 WebSecurity 커스텀을 위해 WebSecurityCustomizer를 통해 등록한다.
  - 참고 : https://sprin7io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
- 하지만 webSecurity에 ignore를 적용하고 싶다면 httpSecurity에 permitAll을 사용하는 것을 공식홈페이지에서 권장한다.
- 이러한 방법을 사용하면 정적자원 요청시 수행되는 필터들이 없어지게 되고 성능상 장점을 가질 수 있다.(HttpSecurity수정은 아님!)
  
### 정적 주소를 왜 HttpSecurity가 아닌 WebSecurity를 설정해야하는가?
WebSecurity가 먼저 동작하기 때문이다.  
보안을 위해서 HttpSecurity는 SecurityFilterChain이 적용되고 CSRF, XSS 보안등이 적용된다.    
하지만 favicon같은 경우는 해당 보안이 필요없는 경우가 있기 때문에 Http 이전에 WebSecurity에서 허용하면 Filter가 적용이 안되어 성능상의 이점이 있다.  
또 인증을 무시하는 antMatchers가 5.7부터 없어졌기 때문에 HttpSecurity에 설정하는 것와 WebSecurity는 동작의 결과는 갖지만 내부 로직이 조금 다르다!  
따라서 동적으로 처리하는 경우는 가능한(거의 무조건) SecurityFilter를 타도록 HttpSecurity에서 처리하는 것이 좋다.  

# WebAsyncManagerIntegrationFilter
Async 웹 MVC를 지원하는 필터  
SecurityFilter의 최상위에서 동작하는 필터  
SecurityContext가 ThreadLocal에서만 동작한다. Async MVC를 사용하면 다른 쓰레드를 사용하게 되는데 동일한 쓰레드에서 사용할 수 있도록 지원하는 필터  
Async를 위해 Callable 객체를 생성해서 실행하면 서로 다른 쓰레드에서 실행되지만, SecurityContext에서 관리되는 Principal은 같은 객체임을 확인할 수 있다.

### 동작과정
1. PreProcess: SecurityContext 설정
2. Callable: 다른 쓰레드지만 동일한 SecurityContext참고
3. PostProcess; SecurityContext 정리

# SpringSecurity와 @Async
Controller가 아니라 Service가 Async한 경우는 어떨까?  
- @Async 어노테이션을 붙인다고 Async동작이 구현되지 않는다.
- Application에 @EnableAsync를 어노테이션을 적용시켜야한다.
- Service를 Async하게 사용하는 경우(@Async를 사용한 경우) 자동으로 SecurityContext가 공유되지 않는다.
- HttpSecurity에서 SecurityContextHolder에 strategy를 설정시켜서 변경할 수 있다.
```java
SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
``` 
위와 같은 코드를 통해 localThread의 하위 Thread들에 SecurityContext가 공유시킬 수 있다.  

# SecurityContextPersistenceFilter
SecurityContextRepository를 통해 기존 SecurityContext를 읽어오거나 초기화한다.  
SecurityFilter에 2번째에 위치하는 이유는 이미 Pricipal의 정보가 담긴 SecurityContext를 불러왔다면 재요청이 필요없이 동작하면되기 때문에 인증을 생략하려고!   
- 기본 전략은 HTTP Session 활용
- Spring-Session과 연동하여 세션 클러스터를 구현
  
# HeaderWriterFilter
Response Header에 시큐리티 관련 헤더를 추가해주는 필터  
실제로 우리가 설정을 건드릴 필요는 없는 필터 -> 사랑해요 스프링 시큐리티
- XContentTypeOptionsHeaderWriter : 마임 타입 스니핑 방어
- XXssProtectionHeaderWriter : 브라우저에 내장된 XSS 필터 적용
- CacheControlHeadersWriter : 캐시 히스토리 취약점 방어
- HstsHeaderWriter : HTTPS로만 소통하도록 강제
- XframeOptionsHeaderWriter : clickJacking 방어

# CsrfFilter 
CSRF 어택 방지 필터
- 의도한 사용자만 리소스를 변경할 수 있도록 허용하는 필터
- 4번째로 동작하는 필터
- 필요한 경우 HttpSecurity에서 disable로 변경가능 -> 권장하지않음
- PostMan을 통한 요청시 redirect시 CSRF토큰이 담기지 않아서 Unauthorized 에러가 발생한다.  

### CSRF 어택
- 인증된 유저의 계정을 사용해 악의적인 변경 요청을 만들어 보내는 공격  
- 따라서 CSRF 토큰을 통한 검증으로 보안처리를 한다.
- 서버에서 CSRF 토큰을 생성해서 보내주고, 이후 클라이언트 요청의 CSRF 토큰이 일치하지 않으면 요청처리를 하지 않음

### CORS
- 의도하지않은 도메인에서 보내는 요청  
- 브라우저의 same-origin 정책을 따르기 때문  

# LogoutFilter
- 로그아웃을 처리할 때, 동작하는 필터
- LogoutHandler와 LogoutSuccessHandler로 구성되어 있음 
  - LogoutHandler는 Composit 구성으로 실제로는 여러 Handler(Csrf, SecurityContext)로 구성되어있다.
  - LogoutSuccessHandler는 로그아웃의 후처리를 진행하는 역할(ex. 로그인 페이지로 이동)
- logout의 Get요청, 즉 페이지 요청은 해당 필터가 아닌 DefaultLogoutPageGeneratingFilter에서 동작하는 것이다.
- HttpSecurity에서 logout메서드를 통해 커스텀 가능

# UsernamePasswordAuthenticationFilter
- 폼 로그인을 처리하는 인증 필터 
### 동작과정
1. 입력 받은 username과 password로 토큰을 만든다.
2. 해당 토큰으로 AuthenticationManager를 사용하여 인증 시도.
3. AuthenticationManager가 여러 AuthenticationProvider를 사용하여 인증 시도
4. 그중 DaoAuthenticationProvider는 UserDetailService를 사용하여 UserDetails정보를 가져와 사용자가 입력한 password와 비교
5. 정상 처리가 된다면 SecurityContextHolder에 추가하여 로그인 관리한다.




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

# DefaultLogin/LogoutPageGeneratingFilter
GET으로 login 또는 logout 요청이 들어오면 페이지를 만들어주는 필터
- username, password의 parameter 이름 변경 가능
- custom한 페이지에서 로그인/로그아웃을 할 수 있도록 변경 가능
  - 이 경우는 해당 filter가 동작하지 않는다.
- custom한 페이지로 이동하도록 경로 설정 가능

### 커스텀 적용하기
1. HttpSecurity에 formLogin().loginPage()로 로그인 페이지등록 및 permitAll 설정
  - 페이지 생성 필터가 제거됨
  - permitAll을 해주지 않으면 너무 맣은 리다이렉션 수행 오류 발생
2. 커스텀한 login, logout 페이지 만들어주기
3. login만 커스텀하고 싶더라도 login,logout 페이지 생성 필터 두개 모두 제거되기 때문에 모두 만들어 주어야한다.

  
# BasicAuthenticationFilter
Basic 인증을 처리하는 필터

### Basic 인증?
- 요청헤더에 username과 password를 실어 보내면 브라우저 또는 서버가 그 값을 읽어서 인증하는 방식
  - Authorization : Basic ~~~~~~ (username:password를 BASE64 Encoding한 결과)
- 브라우저 기반 요청이 클라이언트의 요청을 처리할 때 자주 사용
- 보안에 취약하기 때문에 HTTPS 사용 권장
- curl -u username:password URL 의 방식으로 BASE 헤더를 넣어 확인할 수 있다.
- stateless하기 때문에 계속 헤더를 넣어주어야한다.
  - remeberme를 통해 헤더를 지속적으로 넣어 stateful하게 만들 수 있다.
  
# RequestCacheAwareFilter
요청 캐시 필터
- 현재 요청과 관련 있는 캐시된 요청이 있는지 찾아 적용하는 필터
  - 캐시된 요청이 없으면 현재 요청 처리
  - 캐시된 요청이 있으면 해당 캐시된 요청 처리

### 캐시된 요청?
wrappedSavedRequest에 캐시되어 저장되어있다.
캐시된 요청이란 인증되지 않은 사용자가 특정 경로에 접근할 때(ex. /dashboard), 해당 경로를 캐시하고, 로그인을 먼저하도록 /login 경로로 보내주고, 로그인이 완료되면 캐시한 경로로 다시 이동시키기위해 사용한다.  
  
# SecurityContextHolderAwareRequestFilter
시큐리티 관련 서블릿 API를 구현해주는 필터(직접 사용할 일은 거의 없음)
- HttpServeletRequest#authenticate
- HttpServeletRequest#login
- HttpServeletRequest#logout
- AsyncContext#start

# AnonymousAuthenticationFilter
아무 인증을 하지않은 요청이 주어질 때, SecurityContext에서 Authentication이 null이라면 익명 Authentication을 만들어 넣어주고,  null이 아니면 아무일도 하지 않는다.
- HttpSecurity객체에서 anonymous().principal().authorities().key()를 통해 기본으로 사용할 익명 Authentication객체를 설정해줄 수 있다.

### Null Object Pattern
null을 사용하는 것이 아니라 null을 대신하는 객체를 생성하여 로직을 처리하는 패턴
  
# SessionManagementFilter
1. 세션 변조 방지 전략 설정 : sessionFixation
   - none : 신경쓰지 않음
   - newSession : 매번 새로운 세션을 만든다 -> 기존 세션의 Attribute를 사용하지 않는다는 뜻
   - migrateSession : 세션을 이주시킴 (서블릿 3.0 컨테이너 default)
   - changeSessionId : 세션ID를 변경함 (서블릿 3.1 컨테이너 default)
2. 유효하지 않은 세션의 리다이렉트 시킬 URL 설정
   - invalidSessionUrl
   - 로그아웃이 발생했을 때
3. 동시성 제어 : maximumSessions
   - 추가 로그인을 막을지 여부 설정 (default - false)
4. 세션 생성 전략 : sessionCreationPolicy
   - IF_REQUIRED
   - NEVER
   - STATELESS
   - ALWAYS
   - 세션을 사용하는 이유 
     - RequestCacheAwareFilter의 캐시를 세션에다가 저장하여 사용하기 때문에 세션이 없어지면 캐시가 비워지기 때문에 의도대로 동작하지 않을 수 있다.

# ExceptionTranslationFilter
- FiltersecurityInterceptor보다 먼저 동작해야한다.
- 3회 이상 로그인 실패 시 접근을 막는 로직을 구현할 때 커스텀을 통해 적용할 수 있다.
- HttpSecurity에 exceptionHandling에서 accessDeniedPage(URL)를 통해 에러시 다른 페이지로 이동시킬 수 있다.
- 악의적인 접근을 확인하기 위해서는 accessDeniedHandler(Handler)를 생성하여 로그를 남기고 일정 횟수 반복시 로그인을 차단할 수 있다.(구현해야함)

# FilterSecurityInterceptor
Http 리소스 시큐리티 처리를 담당하는 필터 -> 주로 마지막으로 동작하는 필터이다.
- AccessDecisionManager를 사용하여 인가를 처리한다.
```java
    http
        .authorizeHttpRequests()
        .mvcMatchers("/", "/info", "/account/**", "/sign-up").permitAll()
        .mvcMatchers("/admin").hasRole("ADMIN")
        .mvcMatchers("/user").hasAuthority("ROLE_USER")
        .anyRequest().authenticated()
    ;
```

# RememberMeAuthenticationFilter
명시적으로 로그아웃을 하기 전까지 로그인 유지하기 기능을 담당하는 필터
- rememberMe 메서드를 통해 userDetail을 관리하는 Service Class를 등록한다.
- 토큰을 암호화/복호화할 키를 설정해준다.

### 동작과정
1. 로그인 정보로 RememberMeAuthenticationToken으로 인증 정보를 저장한다. -> 기존은 UsernameAuthenticationToken 정보로 저장된다.
2. rememberMeAuth가 null이 아니라면(로그인이 성공했다면) 인증 정보를 SecurityContextHolder에 저장한다.
3. 다음 request요청시 RememberMeAuthenticationFilter가 동작하면서 해당 정보가 존재하는지 확인한다. -> 명시적으로 로그아웃되었는지 확인하는 과정이다.
4. 존재한다면 해당 정보로 다시 쿠키에 세션정보를 넣어준다. -> 즉, 기존정보로 로그인을 하여 로그인이 유지된다.

# 커스텀 필터 추가하기
1. 필터 생성
  - 필터는 일반적인 서블릿 필터와 동일하기 때문에 서블릿 필터로 구현할 수 있다.
  - GenericFilterBean을 상속받아서 구현하는 것이 스프링 친화적이고 구현하기 쉽다
2. 현재 필터 다음오는 필터를 처리해주어야한다.
  - chain.doFilter를 동작시켜야 다음 필터로 넘어간다.
3. HttpSecurity에 addFilter로 필터를 추가시킨다.
  - addFilterBefore
  - addFilterAfter
  - ... 등이 있다!

# 메서드 시큐리티
메서드 단위에서 시큐리티를 동작하는 것  

### @Secured and @RollAllowed
스프링의 AOP 기술을 통해 빈에다가 @Secured("ROLE_USER") 등의 어노테이션을 붙이면 스프링 시큐리티가 처리를 해준다.  
일반적인 동작이라면 메소드가 실행되어 SecurityContext를 타고 들어가 NPE가 발생하게 된다.  
하지만 메서드 시큐리티를 이용하면 AOP를 통해 인가 단계에서 Excpetion에 발생하게된다.  
@RoleAllowed도 거의 동일한 동작을 수행
- 메소드 호출 이전에 권한 확인 불가
- 스프링 EL 사용 불가
### @PreAuthorized and @PostAuthorized
- 메서드 호출 이전 이후에 권한을 확인할 수 있다.
  - PreAuthorized는 호출 이전에, PostAuthorized는 호출 이후
- 스프링 EL사용하여 메소드 파라미터와 리턴값 검증 가능
### @PreFilter, @PostFilter 등등..
더 많은 건 ~~ 공식홈페이지나 벨덩 블로그를 참고하자.    
ps. 기본적으로 이전에 설정한 계층형 인가를 이해하지 못하게 때문에 필요한다면 MethodSecurity를 커스텀해야한다.

# @AuthenticationPrincipal
Principal은 자바 표준, SercurityContextHolder에서 얻은 Principal은 UserDetails이고 스프링 시큐리티가 제공한다.  
우리가 만든 유저, Account객체를 스프링 시큐리티가 관리하게 하고 싶다면 커스텀하는 방법이 있다.  
스프링 시큐리티의 User 속성을 상속받아서 Getter를 구현하면 스프링이 관리하도록 커스텀할 수 있다.


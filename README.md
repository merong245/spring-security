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




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

사실 @EnableWebSecurity는 설정을 하지않아도 스프링부트가 자동으로 추가해준다.  


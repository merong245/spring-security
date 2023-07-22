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
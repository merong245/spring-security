package com.example.springsecurity.account;

public class AccountContext {
    // Account를 관리하는 threadLocal 생성
    private static final ThreadLocal<Account> ACCOUNT_THREAD_LOCAL= new ThreadLocal<>();

    // threadLocal에 Account 추가
    public static void setAccount(Account account){
        ACCOUNT_THREAD_LOCAL.set(account);
    }

    // threadLocal에 있는 Account 불러오기
    public static Account getAccount(){
        return ACCOUNT_THREAD_LOCAL.get();
    }
}

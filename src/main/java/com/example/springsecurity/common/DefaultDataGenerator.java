package com.example.springsecurity.common;

import com.example.springsecurity.account.Account;
import com.example.springsecurity.account.AccountService;
import com.example.springsecurity.book.Book;
import com.example.springsecurity.book.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultDataGenerator implements ApplicationRunner {

    private final AccountService accountService;

    private final BookRepository bookRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Account moly = createUser("moly");
        Account holy = createUser("holy");

        createBook("spring security", moly);
        createBook("spring data", holy);
    }

    private void createBook(String title, Account account) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(account);
        bookRepository.save(book);
    }

    private Account createUser(String userName) {
        Account account = new Account();
        account.setUsername(userName);
        account.setPassword("pass");
        account.setRole("USER");
        return accountService.createAccount(account);
    }
}

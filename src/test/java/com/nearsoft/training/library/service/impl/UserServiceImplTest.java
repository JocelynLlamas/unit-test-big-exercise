package com.nearsoft.training.library.service.impl;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.nearsoft.training.library.config.LoanConfigurationProperties;
import com.nearsoft.training.library.exception.LoanNotAllowedException;
import com.nearsoft.training.library.model.Book;
import com.nearsoft.training.library.model.BooksByUser;
import com.nearsoft.training.library.model.User;
import com.nearsoft.training.library.repository.BookRepository;
import com.nearsoft.training.library.repository.BooksByUserRepository;
import com.nearsoft.training.library.repository.UserRepository;
import com.nearsoft.training.library.service.CardReaderService;
import com.nearsoft.training.library.service.UserService;

//import org.graalvm.compiler.debug.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

public class UserServiceImplTest {
    private Set<BooksByUser> borrowedBooks;

    //Mock usa la api y define que quiere que haga cuando se llama al método
    //Mockito es un framework para el mock
    @Test
    public void whenGetBorrowedBooks_thenBooksFromRepositoryAreReturned(){
        // Given:
        UserRepository userRepository = null;
        BooksByUserRepository otherRepository = Mockito.mock(BooksByUserRepository.class);
        UserServiceImpl userService = new UserServiceImpl(userRepository, otherRepository);
        String curp = "ABC";
        Set<BooksByUser> booksByUser = new HashSet<>();

        Mockito.when(otherRepository.findByCurp(curp)).thenReturn(booksByUser);

        // When:
        Set<BooksByUser> receivedBooksByUser = userService.getBorrowedBooks(curp);

        // Then:
        assertTrue(booksByUser == receivedBooksByUser);

        Mockito.verify(otherRepository).findByCurp(curp);
        Mockito.verifyNoMoreInteractions(otherRepository);
    }

    @Test
    public void givenAuser_whenRegisterLoanForAList_ThenSave(){
        // Given: 
        UserRepository userRepository = Mockito.mock(UserRepository.class); 
        BooksByUserRepository otherRepository = Mockito.mock(BooksByUserRepository.class); 
        UserServiceImpl userServiceImpl = new UserServiceImpl(userRepository,otherRepository); 
        User user = new User();
        String id = UUID.randomUUID().toString();
        String [] isbList={"ABC","DEF"};
        user.setCurp(id);
        
       
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));
        Mockito.when(otherRepository.findByIsbnAndCurp("ADC",id)).thenReturn(Optional.empty());
        Mockito.when(otherRepository.findByIsbnAndCurp("DEF",id)).thenReturn(Optional.empty());
        
        // When:
        userServiceImpl.registerLoan(user, isbList);
        // Then:
        Mockito.verify(userRepository).findById(id);

        Mockito.verify(otherRepository).findByIsbnAndCurp("DEF",id);
        Mockito.verify(otherRepository).findByIsbnAndCurp("ABC",id);
        Mockito.verify(otherRepository, Mockito.times(2)).save(any(BooksByUser.class));
        
        Mockito.verifyNoMoreInteractions(userRepository,otherRepository);      
    }

    @Test
    public void whenRegister_ThenSave(){
        // Given:
        BooksByUserRepository otherRepository = Mockito.mock(BooksByUserRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class); 
        UserServiceImpl userServiceImpl = new UserServiceImpl(userRepository,otherRepository);  
        BooksByUserRepository bookByUserRepository = Mockito.mock(BooksByUserRepository.class);
        String curp = "ABC"; 
        String [] isbnList = {"123"};
        String [] otherIsbnList = {"2345"};
        User user = new User();
        user.setCurp(curp);
        userRepository.save(user);
        
        
        // When:
        userServiceImpl.registerReturn(user, otherIsbnList);
        // Then:
        Mockito.verifyNoMoreInteractions(userRepository);        
    }

    @Test
    public void whenReadUser_ThenReturnUser(){
        // Given:
        String [] isbnList = {"123"};
        String curp = "123";
        CardReaderService cardReaderService = Mockito.mock(CardReaderService.class);
        UserService userService = Mockito.mock(UserService.class);
        LoanConfigurationProperties loanConfigurationProperties = Mockito.mock(LoanConfigurationProperties.class);
        BookRepository bookRepository = Mockito.mock(BookRepository.class);
        LoanServiceImpl loanServiceImpl = new LoanServiceImpl(cardReaderService, userService,loanConfigurationProperties, bookRepository);
        User user = new User();
        user.setCurp(curp);
        Set<BooksByUser> booksByUsers = new HashSet<>();

        Mockito.when(cardReaderService.readUser()).thenReturn(user);
        Mockito.when(userService.getBorrowedBooks(user.getCurp())).thenReturn(borrowedBooks);
        
        // When:
        loanServiceImpl.lendBooks(isbnList);
        // Then:
        //Mockito.verifyNoMoreInteractions(loanServiceImpl);       
    }

    @Test
    public void whenLendBooks(){
        // Given:
        String [] isbnList = {"123"};
        String curp = "12";
        Book book = new Book();
        Optional<Book> bookOptional = Optional.of(book);
        CardReaderService cardReaderService = Mockito.mock(CardReaderService.class);
        UserService userService = Mockito.mock(UserService.class);
        LoanConfigurationProperties loanConfigurationProperties = new LoanConfigurationProperties();
        BookRepository bookRepository = Mockito.mock(BookRepository.class);
        LoanServiceImpl loanServiceImpl = new LoanServiceImpl(cardReaderService, userService,loanConfigurationProperties, bookRepository);
        User user = new User();
        user.setCurp("123");
        loanConfigurationProperties.setMaxBooksPerUser(5);
        Set<BooksByUser> booksByUsers = new HashSet<>();

        Mockito.when(cardReaderService.readUser()).thenReturn(user);
        Mockito.when(userService.getBorrowedBooks(curp)).thenReturn(booksByUsers);
        Mockito.when(bookRepository.findById(isbnList[0])).thenReturn(bookOptional);
        
        // When:
        loanServiceImpl.lendBooks(isbnList);
        // Then:
        Mockito.verify(userService).registerLoan(user,isbnList);        
    }


    @Test
    public void thenReturnBooksTest(){
        // Given:
        String [] isbnList = {"123"};
        String [] otherIsbnList = {"2345"};
        String curp = "ABC"; 
        LoanConfigurationProperties loanConfigurationProperties = new LoanConfigurationProperties();
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        BooksByUserRepository booksByUserRepository = Mockito.mock(BooksByUserRepository.class);
        UserService userService = Mockito.mock(UserService.class);
        BookRepository bookRepository = Mockito.mock(BookRepository.class);
        CardReaderService cardReaderService = Mockito.mock(CardReaderService.class);
        UserService otherUserService = new UserServiceImpl(userRepository, booksByUserRepository);
        BooksByUser  booksByUsers = new BooksByUser();
        
        LoanServiceImpl loanServiceImpl = new LoanServiceImpl(cardReaderService,userService,loanConfigurationProperties, bookRepository);
        User user = new User();

        Mockito.when(cardReaderService.readUser()).thenReturn(user); 
        ((UserService) booksByUsers).registerReturn(user, otherIsbnList);
        
      //  Mockito.when(((UserService) booksByUsers).registerReturn(user, isbnList)).thenReturn(booksByUsers);
      
        
        // When:
        loanServiceImpl.returnBooks(isbnList);
        // Then:
        Mockito.verifyNoMoreInteractions(loanServiceImpl);        
    }

    @Test
    public void whenBorrowedBooksOnTimethrowLoan() throws LoanNotAllowedException{
        // Given:
        User user = new User();
        String [] isbnList = {"123"};
        CardReaderService cardReaderService = Mockito.mock(CardReaderService.class);
        UserService userService = Mockito.mock(UserService.class);
        LoanConfigurationProperties loanConfigurationProperties = new LoanConfigurationProperties();
        BookRepository bookRepository = Mockito.mock(BookRepository.class);
        LoanServiceImpl loanServiceImpl = new LoanServiceImpl(cardReaderService,userService,loanConfigurationProperties, bookRepository);
        user.setCurp("123");
        loanConfigurationProperties.setMaxBooksPerUser(5);
        userService.getBorrowedBooks(isbnList[0]);
        BooksByUser  borrowedBooks = new BooksByUser();
        String borrowedBooksOnTime = new String();

        ((UserService) borrowedBooks).getBorrowedBooks(user.getCurp());
        ((Set<BooksByUser>) borrowedBooks).add(new BooksByUser());
        //((UserService) borrowedBooksOnTime) = new HashSet<>();

        /*Assertions.assertTrhows(Exception.class, () ->{
            throw new LoanNotAllowedException("User return old loans");
        });*/

        // When:
        loanServiceImpl.returnBooks(isbnList);
        // Then:
        Mockito.verifyNoMoreInteractions(loanServiceImpl);        
    }



}

package com.dws.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;

public class AccountsServiceTest {

	private AccountsRepository accountsRepository;
	private NotificationService notificationService;
	private AccountsService accountsService;

	@BeforeEach
	void setUp() {
		accountsRepository = mock(AccountsRepository.class);
		notificationService = mock(NotificationService.class);
		accountsService = new AccountsService(accountsRepository, notificationService);
	}

	@Test
	void transferMoney_SuccessfulTransfer() {
		Account accountFrom = new Account("account1", new BigDecimal("1000"));
		Account accountTo = new Account("account2", new BigDecimal("500"));

		when(accountsRepository.getAccount("account1")).thenReturn(accountFrom);
		when(accountsRepository.getAccount("account2")).thenReturn(accountTo);

		accountsService.transferMoney("account1", "account2", new BigDecimal("200"));

		assertEquals(new BigDecimal("800"), accountFrom.getBalance());
		assertEquals(new BigDecimal("700"), accountTo.getBalance());

		// Update expected notification strings to match actual invocation
		verify(notificationService).notifyAboutTransfer(accountFrom, "Transferred 200 to account2"); // Corrected line
		verify(notificationService).notifyAboutTransfer(accountTo, "Received 200 from account1");
	}

	@Test
	void transferMoney_InsufficientBalance() {
		Account accountFrom = new Account("account1", new BigDecimal("100"));
		Account accountTo = new Account("account2", new BigDecimal("500"));

		when(accountsRepository.getAccount("account1")).thenReturn(accountFrom);
		when(accountsRepository.getAccount("account2")).thenReturn(accountTo);

		assertThrows(InsufficientBalanceException.class, () -> accountsService.transferMoney("account1", "account2", new BigDecimal("200")));

		verify(notificationService, never()).notifyAboutTransfer(any(), any());
	}

	@Test
	void transferMoney_ConcurrentTransfers() throws InterruptedException {
		Account accountFrom = new Account("account1", new BigDecimal("1000"));
		Account accountTo = new Account("account2", new BigDecimal("500"));

		when(accountsRepository.getAccount("account1")).thenReturn(accountFrom);
		when(accountsRepository.getAccount("account2")).thenReturn(accountTo);

		Runnable transfer1 = () -> accountsService.transferMoney("account1", "account2", new BigDecimal("100"));
		Runnable transfer2 = () -> accountsService.transferMoney("account1", "account2", new BigDecimal("200"));

		Thread t1 = new Thread(transfer1);
		Thread t2 = new Thread(transfer2);

		t1.start();
		t2.start();
		t1.join();
		t2.join();

		assertEquals(new BigDecimal("700"), accountFrom.getBalance()); // 1000 - 100 - 200
		assertEquals(new BigDecimal("800"), accountTo.getBalance()); // 500 + 100 + 200

		verify(notificationService, times(2)).notifyAboutTransfer(eq(accountFrom), anyString());
		verify(notificationService, times(2)).notifyAboutTransfer(eq(accountTo), anyString());
	}
}

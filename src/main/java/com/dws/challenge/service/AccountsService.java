package com.dws.challenge.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.repository.AccountsRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccountsService {

	private final AccountsRepository accountsRepository;
	private final NotificationService notificationService;

	public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
		this.accountsRepository = accountsRepository;
		this.notificationService = notificationService;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	// Transfer money between two accounts
	public synchronized void transferMoney(String accountFromId, String accountToId, BigDecimal amount) {
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Transfer amount must be positive.");
		}

		Account accountFrom = this.accountsRepository.getAccount(accountFromId);
		Account accountTo = this.accountsRepository.getAccount(accountToId);

		if (accountFrom == null) {
			throw new AccountNotFoundException("Account with ID " + accountFromId + " not found.");
		}
		if (accountTo == null) {
			throw new AccountNotFoundException("Account with ID " + accountToId + " not found.");
		}

		// Synchronize on both accounts to avoid deadlock
		synchronized (accountFrom) {
			synchronized (accountTo) {
				if (accountFrom.getBalance().compareTo(amount) < 0) {
					throw new InsufficientBalanceException("Insufficient balance in account " + accountFromId);
				}

				// Perform the transfer
				accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
				accountTo.setBalance(accountTo.getBalance().add(amount));

				// Log the transfer
				log.info("Transferred {} from {} to {}", amount, accountFromId, accountToId);

				// Send notifications to both accounts
				this.notificationService.notifyAboutTransfer(accountFrom, "Transferred " + amount + " to " + accountToId);
				this.notificationService.notifyAboutTransfer(accountTo, "Received " + amount + " from " + accountFromId);
			}
		}
	}
}

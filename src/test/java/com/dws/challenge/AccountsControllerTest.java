package com.dws.challenge;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.web.AccountsController;

public class AccountsControllerTest {

	private MockMvc mockMvc;

	@Mock
	private AccountsService accountsService;

	@InjectMocks
	private AccountsController accountsController;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(accountsController).build();
	}

	@Test
	void getAccount_Success() throws Exception {
		Account account = new Account("account1", new BigDecimal("1000"));

		when(accountsService.getAccount("account1")).thenReturn(account);

		mockMvc.perform(get("/v1/accounts/account1")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.accountId").value("account1")).andExpect(jsonPath("$.balance").value(1000));
	}
}

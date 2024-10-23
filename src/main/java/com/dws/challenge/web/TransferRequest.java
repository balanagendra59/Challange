package com.dws.challenge.web;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransferRequest {

	@NotNull
	private String accountFromId;

	@NotNull
	private String accountToId;

	@NotNull
	@Min(value = 1, message = "Transfer amount must be greater than zero.")
	private BigDecimal amount;
}

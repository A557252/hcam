package com.db.hcam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.db.hcam.domain.Account;
import com.db.hcam.exception.DuplicateAccountIdException;
import com.db.hcam.exception.InsufficientFundsException;
import com.db.hcam.exception.InvalidAccountException;
import com.db.hcam.exception.InvalidAmmountException;
import com.db.hcam.service.AccountsService;
import com.db.hcam.service.LockException;

/** This Class have only positive test case for transferAmount for all the negative and parallel/concurrent test please check AccountServiceParallelConcurrentTest
 * @author User
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {	
	@Autowired
	public AccountsService accountsService;
	private final String Account11 = "Id-130";
	private final String Account12 = "Id-131";	

	@Test
	public void addAccount() throws Exception {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		accountsService.createAccount(account);

		assertThat(accountsService.getAccount("Id-123")).isEqualTo(account);
	}

	@Test
	public void addAccount_failsOnDuplicateId() throws Exception {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		accountsService.createAccount(account);

		try {
			accountsService.createAccount(account);
			fail("Should have failed when adding duplicate account");
		} catch (DuplicateAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo(
					"Account id " + uniqueId + " already exists!");
		}
	}
	
	@Test
	public void when_transferAmount_from_Acc1ToAcc2_then_Balance_Should_debited_fromAc1_and_credited_toAcc2() throws InsufficientFundsException,
			InterruptedException, InvalidAccountException,
			InvalidAmmountException, LockException {	
		this.accountsService.createAccount(new Account(Account11,
				new BigDecimal("8000")));
		
		this.accountsService.createAccount(new Account(Account12,
				new BigDecimal("16000")));
		try {
			accountsService.transferAmount(Account11, Account12,
					new BigDecimal("500"), 2000l, TimeUnit.SECONDS);
		} catch (com.db.hcam.exception.LockException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(accountsService.getAccount(Account11).getBalance()
				.equals(new BigDecimal("7500")));
		assertTrue(accountsService.getAccount(Account12).getBalance()
				.equals(new BigDecimal("16500")));
	}	
}

package com.db.hcam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.db.hcam.domain.Account;
import com.db.hcam.exception.InsufficientFundsException;
import com.db.hcam.exception.InvalidAccountException;
import com.db.hcam.exception.InvalidAmmountException;
import com.db.hcam.service.AccountsService;
import com.db.hcam.exception.LockException;

/**
 * This Class have only positive test case for transferAmount for all the
 * negative and parallel/concurrent test please check
 * AccountServiceParallelConcurrentTest
 * 
 * @author User
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceConcurrentTestWithCompletableFutureTest {
	@Autowired
	public AccountsService accountsService;
	private final String Account1 = "Id-124";
	private final String Account2 = "Id-125";
	public static AtomicInteger transferAmountCounter = new AtomicInteger(0);
	public static AtomicInteger transferAmountOppositeCounter = new AtomicInteger(
			0);
	public static AtomicInteger transferNegativeTestCounter = new AtomicInteger(
			0);
	public final int threadcount =2000;
	public final long timeout = 5000;
	@Before
	public void setup() throws InvalidAccountException {
		this.accountsService.createAccount(new Account(Account1,
				new BigDecimal("8000")));
		
		this.accountsService.createAccount(new Account(Account2,
				new BigDecimal("16000")));	
	}
	
	@Test
	public void whenTransferAmountConcurrentlyToAccToFromAccThenInTearDownAccountSholdHaveSameValue() {
		IntStream.range(0, threadcount).parallel().forEach(
				thread -> {
					CompletableFuture.runAsync(() -> {						
						try {
							transferAmountAndIncrementCounter(Account1,
									Account2, "1", transferAmountCounter);
						} catch (InsufficientFundsException
								| InterruptedException
								| InvalidAccountException
								| InvalidAmmountException | LockException e) {
							
							fail("Exception should not occur while transfering amount from Account1 to Account2");
						}
					});
					CompletableFuture.runAsync(() -> {						
						try {
							transferAmountAndIncrementCounter(Account2,
									Account1, "1",
									transferAmountOppositeCounter);
						} catch (InsufficientFundsException
								| InterruptedException
								| InvalidAccountException
								| InvalidAmmountException | LockException e) {
							
							fail("Exception should not occur while transfering amount from Account2 to Account1");
						}
					}
					);
				});
	}
	private void transferAmountAndIncrementCounter(String fromAccount,
			String toAccount, String amount, AtomicInteger counter)
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmmountException, LockException {
		counter.incrementAndGet();
		try {
			accountsService.transferAmount(fromAccount, toAccount, new BigDecimal(
					amount), timeout, TimeUnit.MILLISECONDS);
		} catch (com.db.hcam.exception.LockException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
	@After
	public void tearDown() throws InvalidAccountException, InterruptedException {
		Thread.sleep(5000);
		System.out.println("transferAmountCounter "+transferAmountCounter.get());
		System.out.println("transferAmountOppositeCounter "+transferAmountOppositeCounter.get());
		assertEquals(new BigDecimal(8000),
				this.accountsService.getAccount(Account1).getBalance());
		assertEquals(new BigDecimal(16000),this.accountsService.getAccount(Account2).getBalance()
				);
		assertThat(transferAmountCounter.get()==threadcount);
		assertThat(transferAmountOppositeCounter.get()==threadcount);
		
	}
}

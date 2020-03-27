package com.db.hcam.repository;

import com.db.hcam.domain.Account;
import com.db.hcam.exception.DuplicateAccountIdException;
import com.db.hcam.exception.InvalidAccountException;

public interface AccountsRepository {

  void createAccount(Account account) throws DuplicateAccountIdException;
   Account getAccount(String accountId) throws InvalidAccountException;
  void clearAccounts();  
}

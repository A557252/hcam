package com.db.hcam.service;

import com.db.hcam.domain.Account;

public interface NotificationService {

  void notifyAboutTransfer(Account account, String transferDescription);
}

package mk.ukim.finki.iskacamebackend.scheduler

import mk.ukim.finki.iskacamebackend.service.intf.UserService
import mk.ukim.finki.iskacamebackend.service.intf.VerificationTokenService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScheduledCleanupService(
  private val verificationTokenService: VerificationTokenService,
  private val userService: UserService
) {

  @Scheduled(cron = "0 0 4 * * *")
  fun cleanupTokens() {
    verificationTokenService.cleanExpiredOrUsedTokens()
  }

  @Scheduled(cron = "0 30 4 * * *")
  fun deleteDisabledUsers() {
    userService.cleanUpDisabledUsers()
  }
}
package mk.ukim.finki.iskacamebackend.scheduler

import mk.ukim.finki.iskacamebackend.service.intf.VerificationTokenService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScheduledCleanupService(
  private val verificationTokenService: VerificationTokenService
) {

  @Scheduled(cron = "0 0 4 * * *")
  fun cleanupTokens() {
    verificationTokenService.cleanExpiredOrUsedTokens()
  }
}
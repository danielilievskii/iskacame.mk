package mk.ukim.finki.iskacamebackend.scheduler

import mk.ukim.finki.iskacamebackend.service.intf.PlacePollService
import mk.ukim.finki.iskacamebackend.service.intf.RefreshTokenService
import mk.ukim.finki.iskacamebackend.service.intf.UserService
import mk.ukim.finki.iskacamebackend.service.intf.VerificationTokenService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScheduledCleanupService(
  private val verificationTokenService: VerificationTokenService,
  private val userService: UserService,
  private val placePollService: PlacePollService,
  private val refreshTokenService: RefreshTokenService
) {

  @Scheduled(cron = "0 0 4 * * *")
  fun cleanupTokens() {
    verificationTokenService.cleanExpiredOrUsedTokens()
  }

  @Scheduled(cron = "0 30 4 * * *")
  fun deleteDisabledUsers() {
    userService.cleanUpDisabledUsers()
  }

  @Scheduled(fixedRate = 30000)
  fun endExpiredPolls() {
    placePollService.endExpiredPolls()
  }

  @Scheduled(cron = "0 0 5 * * SUN")
  fun cleanupRefreshTokens() {
    refreshTokenService.cleanExpiredOrRevoked()
  }
}
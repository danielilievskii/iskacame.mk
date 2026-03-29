package mk.ukim.finki.iskacamebackend.dto.response.gathering

import java.math.BigDecimal

data class DebtDto(
    val fromUserId: Long,
    val toUserId: Long,
    val amount: BigDecimal
)

data class UserBalanceDto(
    val userId: Long,
    var balance: BigDecimal
)
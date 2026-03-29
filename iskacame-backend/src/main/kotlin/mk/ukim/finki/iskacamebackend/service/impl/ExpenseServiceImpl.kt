package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.common.AuthExceptionMessages
import mk.ukim.finki.iskacamebackend.common.ExpenseExceptionMessages
import mk.ukim.finki.iskacamebackend.common.GatheringExceptionMessages
import mk.ukim.finki.iskacamebackend.dto.request.gathering.CreateExpenseRequest
import mk.ukim.finki.iskacamebackend.dto.request.gathering.CreatePaymentRequest
import mk.ukim.finki.iskacamebackend.dto.response.gathering.ExpenseSplitDto
import mk.ukim.finki.iskacamebackend.dto.response.gathering.ActivityDto
import mk.ukim.finki.iskacamebackend.dto.response.gathering.DebtDto
import mk.ukim.finki.iskacamebackend.dto.response.gathering.UserBalanceDto
import mk.ukim.finki.iskacamebackend.exception.BadRequestException
import mk.ukim.finki.iskacamebackend.exception.CustomAccessDeniedException
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.model.domain.Expense
import mk.ukim.finki.iskacamebackend.model.domain.ExpenseSplit
import mk.ukim.finki.iskacamebackend.model.domain.Payment
import mk.ukim.finki.iskacamebackend.repository.ExpenseRepository
import mk.ukim.finki.iskacamebackend.repository.ExpenseSplitRepository
import mk.ukim.finki.iskacamebackend.repository.PaymentRepository
import mk.ukim.finki.iskacamebackend.service.intf.AuthService
import mk.ukim.finki.iskacamebackend.service.intf.ExpenseService
import mk.ukim.finki.iskacamebackend.service.intf.GatheringService
import mk.ukim.finki.iskacamebackend.service.intf.PermissionService
import mk.ukim.finki.iskacamebackend.service.intf.UserService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.collections.filter

@Service
class ExpenseServiceImpl(
    private val expenseRepository: ExpenseRepository,
    private val splitRepository: ExpenseSplitRepository,
    private val paymentRepository: PaymentRepository,
    private val gatheringService: GatheringService,
    private val permissionService: PermissionService,
    private val userService: UserService,
    private val authService: AuthService
) : ExpenseService {

    @Transactional
    @PreAuthorize("@permissionService.isGatheringParticipant(#gatheringId, authentication.principal.id)")
    override fun createExpense(gatheringId: Long, request: CreateExpenseRequest) {

        val paidBy = userService.getUserById(request.paidByUserId)
        val gathering = gatheringService.getGatheringById(gatheringId)

        if (request.totalAmount <= BigDecimal.ZERO) {
            throw BadRequestException(ExpenseExceptionMessages.AMOUNT_NOT_VALID)
        }

        val expense = Expense(
            totalAmount = request.totalAmount,
            paidBy = paidBy,
            gathering = gathering
        )

        expenseRepository.save(expense)

        val userIds = request.splits.map { it.userId }
        val usersById = userService.getUsersByIds(userIds).associateBy { it.id!! }

        val splits = request.splits.map {
            val isUserGatheringParticipant = permissionService.isGatheringParticipant(gatheringId, it.userId)

            if (!isUserGatheringParticipant) {
                throw BadRequestException(GatheringExceptionMessages.USER_NOT_IN_GATHERING)
            }

            val user = usersById[it.userId]!!

            ExpenseSplit(
                expense = expense,
                user = user,
                amountOwed = it.amountOwed,
                isOwnShare = it.userId == paidBy.id
            )
        }

        splitRepository.saveAll(splits)
    }

    @Transactional
    @PreAuthorize("@permissionService.isGatheringParticipant(#gatheringId, authentication.principal.id)")
    override fun createPayment(gatheringId: Long, request: CreatePaymentRequest) {

        val currentUser = authService.getCurrentUser()

        if (request.amount <= BigDecimal.ZERO) {
            throw BadRequestException(ExpenseExceptionMessages.AMOUNT_NOT_VALID)
        }

        if (request.toUserId == currentUser.id) {
            throw BadRequestException(ExpenseExceptionMessages.SELF_PAYMENT_NOT_ALLOWED)
        }

        val isUserGatheringParticipant = permissionService.isGatheringParticipant(gatheringId, request.toUserId)

        if (!isUserGatheringParticipant) {
            throw BadRequestException(GatheringExceptionMessages.USER_NOT_IN_GATHERING)
        }

        val toUser = userService.getUserById(request.toUserId)
        val gathering = gatheringService.getGatheringById(gatheringId)

        val payment = Payment(
            fromUser = currentUser,
            toUser = toUser,
            gathering = gathering,
            amount = request.amount
        )

        paymentRepository.save(payment)
    }

    @Transactional
    @PreAuthorize("@permissionService.isGatheringParticipant(#gatheringId, authentication.principal.id)")
    override fun deletePayment(gatheringId: Long, paymentId: Long) {

        val currentUser = authService.getCurrentUser()

        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { ResourceNotFoundException(ExpenseExceptionMessages.PAYMENT_NOT_FOUND) }

        if (payment.gathering.id != gatheringId) {
            throw BadRequestException(ExpenseExceptionMessages.PAYMENT_NOT_FOUND)
        }

        if (payment.fromUser.id != currentUser.id) {
            throw CustomAccessDeniedException(AuthExceptionMessages.ACCESS_DENIED)
        }

        paymentRepository.delete(payment)
    }


    @PreAuthorize("@permissionService.isGatheringParticipant(#gatheringId, authentication.principal.id)")
    override fun getActivities(gatheringId: Long): List<ActivityDto> {

        val expenses = expenseRepository.findAllByGatheringId(gatheringId)

        val expenseActivityDtos = expenses.map { expense ->
            val splits = expense.splits.map { split ->
                ExpenseSplitDto(
                    userId = split.user.id!!,
                    amountOwed = split.amountOwed,
                    isOwnShare = split.isOwnShare
                )
            }

            ActivityDto.ExpenseActivityDto(
                id = expense.id!!,
                description = expense.description,
                totalAmount = expense.totalAmount,
                paidByUserId = expense.paidBy.id!!,
                splits = splits,
                createdAt = expense.createdAt!!
            )
        }

        val payments = paymentRepository.findAllByGatheringId(gatheringId)

        val paymentActivityDtos = payments.map {
            ActivityDto.PaymentActivityDto(
                id = it.id!!,
                fromUserId = it.fromUser.id!!,
                toUserId = it.toUser.id!!,
                amount = it.amount,
                createdAt = it.createdAt!!
            )
        }

        val activityDtos = expenseActivityDtos + paymentActivityDtos
        return activityDtos.sortedByDescending { it.createdAt }

    }

    @PreAuthorize("@permissionService.isGatheringParticipant(#gatheringId, authentication.principal.id)")
    override fun calculateDebts(gatheringId: Long): List<DebtDto> {

        val userBalances = calculateBalances(gatheringId)

        // Participants who are owed money
        val creditors = userBalances
            .filter { it.balance > BigDecimal.ZERO }
            .toMutableList()

        // Participants who owe money
        val debtors = userBalances
            .filter { it.balance < BigDecimal.ZERO }
            .toMutableList()

        val debts = mutableListOf<DebtDto>()
        var debtorIndex = 0
        var creditorIndex = 0

        while (debtorIndex < debtors.size && creditorIndex < creditors.size) {
            val currentDebtor = debtors[debtorIndex]
            val currentCreditor = creditors[creditorIndex]

            val debtAmount = minOf(-currentDebtor.balance, currentCreditor.balance)

            val debtDto = DebtDto(
                fromUserId = currentDebtor.userId,
                toUserId = currentCreditor.userId,
                amount = debtAmount
            )
            debts.add(debtDto)

            currentDebtor.balance += debtAmount
            currentCreditor.balance -= debtAmount

            if (currentDebtor.balance.compareTo(BigDecimal.ZERO) == 0) debtorIndex++
            if (currentCreditor.balance.compareTo(BigDecimal.ZERO) == 0) creditorIndex++
        }

        return debts
    }

    /**
     * Calculates the net balances for all users in a given gathering,
     * reflecting only the currently unpaid debts.
     *
     * The balance for each user is computed in four steps:
     *
     * 1. Each expense payer is credited with the full bill amount.
     * 2. Each payer's own share ([ExpenseSplit.isOwnShare] = true) is subtracted from their credit,
     *    since that portion was consumed by themselves and not owed back to them.
     * 3. Each non-own split is subtracted from the owing user's balance as an active debt.
     * 4. Each payment increases the payer's balance (they now owe less)
     *    and decreases the recipient's balance (they are owed less).
     *
     * @param gatheringId The unique identifier of the gathering whose balances should be calculated.
     * @return A list of [UserBalanceDto] objects, each containing a user ID and their resulting balance.
     */
    private fun calculateBalances(gatheringId: Long): List<UserBalanceDto> {

        val expenses = expenseRepository.findAllByGatheringId(gatheringId)
        val allSplits = splitRepository.findAllByExpenseGatheringId(gatheringId)
        val payments = paymentRepository.findAllByGatheringId(gatheringId)

        val balanceByUserId = mutableMapOf<Long, BigDecimal>()

        expenses.forEach {
            val userId = it.paidBy.id!!
            balanceByUserId[userId] = (balanceByUserId[userId] ?: BigDecimal.ZERO) + it.totalAmount
        }

        allSplits
            .filter { it.isOwnShare }
            .forEach {
                val payerId = it.expense.paidBy.id!!
                balanceByUserId[payerId] = (balanceByUserId[payerId] ?: BigDecimal.ZERO) - it.amountOwed
            }

        allSplits
            .filter { !it.isOwnShare }
            .forEach {
                val userId = it.user.id!!
                balanceByUserId[userId] = (balanceByUserId[userId] ?: BigDecimal.ZERO) - it.amountOwed
            }

        payments.forEach {
            val fromUserId = it.fromUser.id!!
            val toUserId = it.toUser.id!!
            balanceByUserId[fromUserId] = (balanceByUserId[fromUserId] ?: BigDecimal.ZERO) + it.amount
            balanceByUserId[toUserId] = (balanceByUserId[toUserId] ?: BigDecimal.ZERO) - it.amount
        }

        return balanceByUserId.map { (userId, balance) ->
            UserBalanceDto(userId, balance)
        }
    }
}
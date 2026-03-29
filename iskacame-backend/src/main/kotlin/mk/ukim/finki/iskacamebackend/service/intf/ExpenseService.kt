package mk.ukim.finki.iskacamebackend.service.intf

import mk.ukim.finki.iskacamebackend.dto.request.gathering.CreateExpenseRequest
import mk.ukim.finki.iskacamebackend.dto.request.gathering.CreatePaymentRequest
import mk.ukim.finki.iskacamebackend.dto.response.gathering.ActivityDto
import mk.ukim.finki.iskacamebackend.dto.response.gathering.DebtDto

interface ExpenseService {

    /**
     * Creates a new expense for the given gathering and saves the corresponding splits.
     *
     * The payer's own share is automatically marked via [ExpenseSplit.isOwnShare],
     * derived by comparing each split's userId against the paidByUserId in the request.
     * The payer's split is included to preserve the original total amount on the expense,
     * but is excluded from active debt calculations in [calculateBalances].
     *
     * @param gatheringId the ID of the gathering this expense belongs to
     * @param request the expense details including total amount, payer, and splits
     */
    fun createExpense(gatheringId: Long, request: CreateExpenseRequest)

    /**
     * Creates a direct payment from the currently authenticated user to another participant
     * in the gathering.
     *
     * Payments are first-class entities and are factored into [calculateBalances] to
     * reduce the payer's outstanding debt and the recipient's credit accordingly.
     *
     * @param gatheringId the ID of the gathering this payment belongs to
     * @param request the payment details including the recipient and amount
     */
    fun createPayment(gatheringId: Long, request: CreatePaymentRequest)

    /**
     * Deletes a payment from the given gathering.
     *
     * Once deleted, the payment is no longer factored into [calculateBalances],
     * restoring the affected balances as if the payment had never been made.
     *
     * @param gatheringId the ID of the gathering this payment belongs to
     * @param paymentId the ID of the payment to delete
     */
    fun deletePayment(gatheringId: Long, paymentId: Long)

    /**
     * Returns a chronologically ordered activity feed for the given gathering,
     * combining all expenses and payments into a single unified list.
     *
     * Each item is represented as a [ActivityDto], which is either:
     * - [ActivityDto.ExpenseActivityDto] — an expense with its splits
     * - [ActivityDto.PaymentActivityDto] — a direct payment between two participants
     *
     * @param gatheringId the ID of the gathering to fetch activities for
     * @return a list of [ActivityDto] sorted by creation date descending
     */
    fun getActivities(gatheringId: Long): List<ActivityDto>

    /**
     * Calculates the simplified list of debts for a given gathering,
     * minimizing the number of transactions needed to settle all balances.
     *
     * Uses a greedy two-pointer algorithm:
     * - Debtors (negative balance) are matched against creditors (positive balance)
     * - Each iteration settles as much as possible between the current debtor and creditor
     * - Pointers advance once a participant is fully settled
     *
     * @param gatheringId the ID of the gathering to calculate debts for
     * @return a list of [DebtDto] representing who owes whom and how much
     */
    fun calculateDebts(gatheringId: Long): List<DebtDto>

}
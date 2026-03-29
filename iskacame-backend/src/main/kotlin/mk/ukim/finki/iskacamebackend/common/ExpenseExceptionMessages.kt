package mk.ukim.finki.iskacamebackend.common

object ExpenseExceptionMessages {
    const val AMOUNT_NOT_VALID = "Amount must be greater than zero"
    const val SELF_PAYMENT_NOT_ALLOWED = "Cannot create payment to yourself"
    const val PAYMENT_NOT_FOUND = "Payment not found"
    const val EXPENSE_NOT_FOUND = "Expense not found"
}
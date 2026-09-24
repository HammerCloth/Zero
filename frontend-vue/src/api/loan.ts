import type { Loan, LoanRepayment } from '@/types/models'
import http from './http'

export interface LoanStats {
  year: number
  loanCount: number
  openCount: number
  settledCount: number
  principalTotal: number
  outstandingTotal: number
  repaidThisYear: number
}

export type LoanBody = {
  borrowerName: string
  relationship?: string | null
  amount: number | null
  loanDate: string
  dueDate?: string | null
  note?: string | null
}

export type RepaymentBody = {
  amount: number | null
  repayDate: string
  note?: string | null
}

export async function listLoans(params: { keyword?: string; status?: string } = {}) {
  const { data } = await http.get<{ loans: Loan[] }>('/api/v1/loans', { params })
  return data.loans
}

export async function getLoan(id: string) {
  const { data } = await http.get<{ loan: Loan }>(`/api/v1/loans/${id}`)
  return data.loan
}

export async function createLoan(body: LoanBody) {
  const { data } = await http.post<{ loan: Loan }>('/api/v1/loans', body)
  return data.loan
}

export async function updateLoan(id: string, body: LoanBody) {
  await http.put(`/api/v1/loans/${id}`, body)
}

export async function deleteLoan(id: string) {
  await http.delete(`/api/v1/loans/${id}`)
}

export async function listRepayments(loanId: string) {
  const { data } = await http.get<{ repayments: LoanRepayment[] }>(`/api/v1/loans/${loanId}/repayments`)
  return data.repayments
}

export async function createRepayment(loanId: string, body: RepaymentBody) {
  const { data } = await http.post<{ repayment: LoanRepayment }>(`/api/v1/loans/${loanId}/repayments`, body)
  return data.repayment
}

export async function updateRepayment(loanId: string, repaymentId: string, body: RepaymentBody) {
  await http.put(`/api/v1/loans/${loanId}/repayments/${repaymentId}`, body)
}

export async function deleteRepayment(loanId: string, repaymentId: string) {
  await http.delete(`/api/v1/loans/${loanId}/repayments/${repaymentId}`)
}

export async function loanStats(year: number) {
  const { data } = await http.get<LoanStats>('/api/v1/loans/stats', { params: { year } })
  return data
}

package bdn.quantum.service;

import java.util.List;

import bdn.quantum.model.Transaction;

public interface TransactionService {
	
	public List<Transaction> getTransactions();
	public List<Transaction> getTransactionsForSecurity(Integer secId);
	public List<Transaction> getTransactionsForSecurityAndType(Integer secId, String type);
	public Transaction getTransaction(Integer id);
	public Transaction createTransaction(Transaction tranEntry);
	public Transaction updateTransaction(Integer id, Transaction transaction);
	public void deleteTransaction(Integer id);
	
}

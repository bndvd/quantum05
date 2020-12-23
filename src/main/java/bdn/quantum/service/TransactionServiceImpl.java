package bdn.quantum.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bdn.quantum.QuantumConstants;
import bdn.quantum.contoller.TransactionController;
import bdn.quantum.model.TranEntity;
import bdn.quantum.model.Transaction;
import bdn.quantum.model.util.TransactionComparator;
import bdn.quantum.repository.TransactionRepository;

@Service("transactionService")
public class TransactionServiceImpl implements TransactionService {

	@Autowired
	private TransactionController transactionController;
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	@Autowired
	private TransactionComparator transactionComparator;


	@Override
	public Iterable<Transaction> getTransactions() {
		Iterable<TranEntity> teIter = transactionRepository.findAll();
		
		List<Transaction> result = new ArrayList<>();
		for (TranEntity te : teIter) {
			Transaction t = new Transaction(te);
			result.add(t);
		}
		result.sort(transactionComparator);
		
		return result;
	}
	
	@Override
	public Iterable<Transaction> getTransactionsForSecurity(Integer secId) {
		Iterable<TranEntity> teIter = transactionRepository.findBySecId(secId);
		
		List<Transaction> result = new ArrayList<>();
		for (TranEntity te : teIter) {
			Transaction t = new Transaction(te);
			result.add(t);
		}
		result.sort(transactionComparator);
		
		return result;
	}

	@Override
	public Transaction getTransaction(Integer id) {
		Optional<TranEntity> t = transactionRepository.findById(id);
		
		Transaction result = null;
		if (t.isPresent()) {
			TranEntity te = t.get();
			if (te != null) {
				result = new Transaction(te);
			}
		}
		return result;
	}

	@Override
	public Transaction createTransaction(Transaction transaction) {
		// if another transaction exists with the exact timestamp, add 1 sec to new transaction dttm
		Date newDate = (Date) transaction.getTranDate().clone();
		while (transactionRepository.countByTranDate(newDate) > 0) {
			newDate.setTime(newDate.getTime() + QuantumConstants.MILLIS_BETWEEN_TRANSACTIONS_ON_SAME_DATE);
		}
		transaction.setTranDate(newDate);
		
		return saveTransaction(transaction);
	}

	@Override
	public Transaction updateTransaction(Integer id, Transaction transaction) {
		transaction.setId(id);
		return saveTransaction(transaction);
	}
	
	// used for creating (POST) and updating (PUT)
	private Transaction saveTransaction(Transaction transaction) {
		TranEntity te = new TranEntity(transaction.getId(), transaction.getSecId(), transaction.getUserId(),
				transaction.getTranDate(), transaction.getType(), transaction.getShares(), transaction.getPrice());
		te = transactionRepository.save(te);
		transactionController.transactionsUpdated();
		
		Transaction result = null;
		if (te != null) {
			result = new Transaction(te);
		}
		return result;
	}

	@Override
	public void deleteTransaction(Integer id) {
		transactionRepository.deleteById(id);
		transactionController.transactionsUpdated();
	}

}

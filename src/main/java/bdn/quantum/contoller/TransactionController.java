package bdn.quantum.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import bdn.quantum.QuantumConstants;
import bdn.quantum.model.Transaction;
import bdn.quantum.service.TransactionService;
import bdn.quantum.util.ServiceError;

@RestController("transactionController")
@RequestMapping(QuantumConstants.REST_URL_BASE)
public class TransactionController {
	
	@Autowired
	TransactionService transactionService;
	
	@Autowired
	QPlotController qPlotController;
	
	
	@RequestMapping(value = "/transactions", method = RequestMethod.GET)
	public Iterable<Transaction> getTransactions() {
		return transactionService.getTransactions();
	}
	
	@RequestMapping(value = "/transactions/{secId}", method = RequestMethod.GET)
	public Iterable<Transaction> getTransactions(@PathVariable(value="secId") Integer secId) {
		return transactionService.getTransactionsForSecurity(secId);
	}
	
	@RequestMapping(value = "/transaction/{tranId}", method = RequestMethod.GET)
	public Transaction getTransaction(@PathVariable(value="tranId") Integer tranId) {
		return transactionService.getTransaction(tranId);
	}

	@RequestMapping(value = "/transaction", method = RequestMethod.POST)
	public Transaction createTransaction(@RequestBody Transaction transaction) {
		return transactionService.createTransaction(transaction);
	}

	@RequestMapping(value = "/transaction/{tranId}", method = RequestMethod.PUT)
	public Transaction updateTransaction(@PathVariable(value="tranId") Integer tranId, @RequestBody Transaction transaction) {
		return transactionService.updateTransaction(tranId, transaction);
	}

	@RequestMapping(value = "/transaction/{tranId}", method = RequestMethod.DELETE)
	public void deleteTransaction(@PathVariable(value="tranId") Integer tranId) {
		transactionService.deleteTransaction(tranId);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ServiceError> handle(RuntimeException exc) {
		ServiceError error = new ServiceError(HttpStatus.OK.value(), exc.getMessage());
		return new ResponseEntity<>(error, HttpStatus.OK);
	}

	public void transactionsUpdated() {
		qPlotController.plotDataChanged();
	}
}

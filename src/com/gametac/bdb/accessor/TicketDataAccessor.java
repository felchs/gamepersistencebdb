package com.gametac.bdb.accessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import com.gametac.bdb.Command;
import com.gametac.bdb.RunTransaction;
import com.gametac.bdb.entities.EnTicket;
import com.gametac.bdb.entities.EnTicketMessage;
import com.gametac.bdb.entities.TicketMessageType;
import com.gametac.bdb.entities.TicketStatusType;
import com.sleepycat.je.EnvironmentFailureException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.rep.ReplicatedEnvironment;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

public class TicketDataAccessor extends DataAccessor {
	private PrimaryIndex<Long, EnTicket> pkTicketById;
	
	private SecondaryIndex<String, Long, EnTicket> skTicketByEmail;
	
	private PrimaryIndex<Long, EnTicketMessage> pkTicketMessageByTicket;
	
	public TicketDataAccessor(String dataAccessorKey, ReplicatedEnvironment repEnv, EntityStore store) throws EnvironmentFailureException, InterruptedException {
		super(dataAccessorKey, false, repEnv, store);
	}

	@Override
	public void initalize(boolean readOnly) throws Exception {
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {

			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				pkTicketById = store.getPrimaryIndex(Long.class, EnTicket.class);
				skTicketByEmail = store.getSecondaryIndex(pkTicketById, String.class, "email"); 
				pkTicketMessageByTicket = store.getPrimaryIndex(Long.class, EnTicketMessage.class);
			}
		};
		runTransaction.run(readOnly);
	}
	
	public EnTicket addTicket(final String email, final String ticketSubject, final long ticketDate, final String ticketMessageString) throws EnvironmentFailureException, InterruptedException {
		final TransactionReturn<EnTicket> ret = new TransactionReturn<EnTicket>();
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnTicket enTicket = new EnTicket();
				enTicket.setEmail(email);
				enTicket.setStatus(TicketStatusType.OPEN.ordinal());
				enTicket.setDate(ticketDate);
				enTicket.setSubject(ticketSubject);
				
				// update messages
				EnTicketMessage ticketMessage = new EnTicketMessage();
				ticketMessage.setType(TicketMessageType.QUESTION.ordinal());
				ticketMessage.setDate(ticketDate);
				ticketMessage.setMessage(ticketMessageString);
				pkTicketMessageByTicket.put(txn, ticketMessage);
				
				pkTicketById.put(txn, enTicket);
				
				ret.value = enTicket;
			}
		};
		
		transaction.run(false);
		return ret.value;
	}
	
	public EnTicket updateTicketStatus(final long ticketId, final int status) throws EnvironmentFailureException, InterruptedException {
		final TransactionReturn<EnTicket> ret = new TransactionReturn<EnTicket>();
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnTicket enTicket = pkTicketById.get(txn, ticketId, LockMode.DEFAULT);
				enTicket.setStatus(status);
				pkTicketById.put(txn, enTicket);
				ret.value = enTicket;
			}
		};
		
		transaction.run(false);
		return ret.value;
	}

	public ArrayList<EnTicket> getTickets(final String email) throws EnvironmentFailureException, InterruptedException {
		final ArrayList<EnTicket> ticketsDTOToRet = new ArrayList<EnTicket>();

		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EntityCursor<EnTicket> cursor = skTicketByEmail.subIndex(email).entities();
				
				Iterator<EnTicket> entitiesIt = cursor.iterator();
				while (entitiesIt.hasNext()) {
					EnTicket enTicket = entitiesIt.next();
					ticketsDTOToRet.add(enTicket);
				}
				
				cursor.close();
			}
		};
		runTransaction.run(true);
		
		return ticketsDTOToRet;
	}
	
	public ArrayList<EnTicketMessage> getMessages(final long ticketId) throws EnvironmentFailureException, InterruptedException {
		final ArrayList<EnTicketMessage> ticketsMessagesDTOToRet = new ArrayList<EnTicketMessage>();

		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnTicket enTicket = pkTicketById.get(ticketId);
				Set<Long> messages = enTicket.getMessages();
				Iterator<Long> messagesIt = messages.iterator();
				while (messagesIt.hasNext()) {
					Long key = messagesIt.next();
					EnTicketMessage ticketMessage = pkTicketMessageByTicket.get(txn, key, LockMode.DEFAULT);
					ticketsMessagesDTOToRet.add(ticketMessage);
				}
			}
		};
		runTransaction.run(true);
		
		return ticketsMessagesDTOToRet;		
	}
	
	public boolean addQuestion(String message, long date, long id) throws EnvironmentFailureException, InterruptedException {
		return addMessage(0, message, date, id);
	}
	
	public boolean addAnswer(String message, long date, long id) throws EnvironmentFailureException, InterruptedException {
		return addMessage(1, message, date, id);
	}
	
	public boolean addMessage(final int messageType, final String message, final long date, final long id) throws EnvironmentFailureException, InterruptedException {
		final TransactionReturn<Boolean> ret = new TransactionReturn<Boolean>();
		
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnTicketMessage newEnTicketMessage = new EnTicketMessage();
				newEnTicketMessage.setDate(date);
				newEnTicketMessage.setType(messageType);
				newEnTicketMessage.setMessage(message);
				ret.value = pkTicketMessageByTicket.put(txn, newEnTicketMessage) != null;
			}
		};
		runTransaction.run(false);
		
		return ret.value;
	}

}
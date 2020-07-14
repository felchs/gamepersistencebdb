package com.gametac.bdb.accessor;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.Vector;

import com.gametac.bdb.Command;
import com.gametac.bdb.RunTransaction;
import com.gametac.bdb.entities.EnLoggedUser;
import com.gametac.bdb.entities.EnPassRedefinition;
import com.gametac.bdb.entities.EnUser;
import com.gametac.utils.Base64;
import com.gametac.utils.Crypt;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.EnvironmentFailureException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.rep.ReplicatedEnvironment;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

public class UserDataAccessor extends DataAccessor {
	private PrimaryIndex<String, EnUser> pkUserByEmail;
	
	private PrimaryIndex<Long, EnLoggedUser> pkLoggedUserByIp;
	
	private SecondaryIndex<String, Long, EnLoggedUser> skLoggedUserByEmail;

	public UserDataAccessor(String dataAccessorKey, ReplicatedEnvironment repEnv, EntityStore store) throws EnvironmentFailureException, InterruptedException {
		super(dataAccessorKey, false, repEnv, store);
	}

	@Override
	public void initalize(boolean readOnly) throws EnvironmentFailureException, InterruptedException {
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				pkUserByEmail = store.getPrimaryIndex(String.class, EnUser.class);
				pkLoggedUserByIp = store.getPrimaryIndex(Long.class, EnLoggedUser.class);
				skLoggedUserByEmail = store.getSecondaryIndex(pkLoggedUserByIp, String.class, "email");
			}
		};
		runTransaction.run(readOnly);
	}

	public PrimaryIndex<Long, EnLoggedUser> getPkLoggedUserByEmail() {
		return pkLoggedUserByIp;
	}
	
	public boolean isUserRegistered(final String email) throws EnvironmentFailureException, InterruptedException {
		final TransactionReturn<Boolean> ret = new TransactionReturn<Boolean>();
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				ret.value = pkUserByEmail.contains(txn, email, LockMode.DEFAULT);
			}
		};
		transaction.run(true);
		return ret.value;
	}
	
	public EnUser getUserWithCredentials(final String email, final String encodedKey) throws EnvironmentFailureException, InterruptedException {
		final TransactionReturn<EnUser> ret = new TransactionReturn<EnUser>();
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnUser user = pkUserByEmail.get(txn, email, LockMode.DEFAULT);
				if (user != null) {
					String encryptedPassword = user.getPassword();
					String dataDecoded = Base64.decode(encodedKey);
					String plainPassword = dataDecoded.split("&")[1];
					if (Crypt.get().check(plainPassword, encryptedPassword)) {
						ret.value = user;
					}
				}
			}
		};
		runTransaction.run(true);
		return ret.value;
	}
	
	public boolean isValidCredentials(final String email, final String encodedKey) throws EnvironmentFailureException, InterruptedException {
		System.out.println("isvalid credentials 0");
		final TransactionReturn<Boolean> ret = new TransactionReturn<Boolean>();
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				System.out.println("isvalid credentials 1");
				EnUser user = pkUserByEmail.get(txn, email, LockMode.DEFAULT);
				System.out.println("isvalid credentials 2");
				if (user != null) {
					System.out.println("isvalid credentials 3");
					String encryptedPassword = user.getPassword();
					System.out.println("isvalid credentials 4");
					String dataDecoded = Base64.decode(encodedKey);
					System.out.println("isvalid credentials 5, dataDecoded: " + dataDecoded);
					String plainPassword = dataDecoded;//.split("&")[1];
					System.out.println("isvalid credentials 6");
					ret.value = Crypt.get().check(plainPassword, encryptedPassword);
					System.out.println("isvalid credentials 7");
				}
			}
		};
		runTransaction.run(true);
		System.out.println("isvalid credentials 8");
		return ret.value;
	}
	
	public boolean checkCanLogin(final String email, final String encodedKey) {
		System.out.println("checking can login, email: " + email + ", encodedKey: " + encodedKey);
		try {
			if (!isUserLogged(email)) {
				return false;
			}
			boolean validCredentials = isValidCredentials(email, encodedKey);
			return validCredentials;
		} catch (EnvironmentFailureException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean checkCanLoginWithPassword(final String email, final String plainPassword) throws EnvironmentFailureException, InterruptedException {
		final TransactionReturn<Boolean> ret = new TransactionReturn<Boolean>();
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnUser user = pkUserByEmail.get(txn, email, LockMode.DEFAULT);
				if (user != null) {
					String encryptedPassword = user.getPassword();
					ret.value = Crypt.get().check(plainPassword, encryptedPassword);
				}
			}
		};
		runTransaction.run(true);
		return ret.value;
	}
	
	public boolean loginUser(final String email, final String plainPassword, final long ip, final String userAgent) throws EnvironmentFailureException, InterruptedException {
		final TransactionReturn<EnUser> retUser = new TransactionReturn<EnUser>();
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnUser user = pkUserByEmail.get(email);
				retUser.value = user;
			}
		};
		runTransaction.run(false);
		
		boolean canLogin = false;
		if (retUser.value != null) {
			String encryptedPassword = retUser.value.getPassword();
			canLogin = Crypt.get().check(plainPassword, encryptedPassword);
	        if (canLogin) {
	        	canLogin = addAsLoggedUser(email, ip, userAgent) != null;
	        }
		}
        return canLogin;
	}
	
	public boolean logoutUser(final String email, final String plainPassword, final long ip, final String userAgent) throws EnvironmentFailureException, InterruptedException {
		final TransactionReturn<EnUser> retUser = new TransactionReturn<EnUser>();
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				retUser.value = pkUserByEmail.get(txn, email, LockMode.DEFAULT);
			}
		};
		runTransaction.run(false);
		
		boolean couldLogout = false;
		if (retUser.value != null) {
			String encryptedPassword = pkUserByEmail.get(email).getPassword();
			couldLogout = Crypt.get().check(plainPassword, encryptedPassword);
			if (couldLogout) {
				couldLogout = removeAsLoggedUser(ip, userAgent);
			}
		}
		
        return couldLogout;
	}
	
	public EnLoggedUser addAsLoggedUser(final String email, final long ip, final String userAgent) throws EnvironmentFailureException, InterruptedException {
		final TransactionReturn<EnLoggedUser> ret = new TransactionReturn<EnLoggedUser>();
		
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
		        EnLoggedUser newLoggedUser = new EnLoggedUser();
		        newLoggedUser.setEmail(email);
		        newLoggedUser.setIp(ip);
		        newLoggedUser.setUserAgent(userAgent);
		        newLoggedUser.setLoginTime(System.currentTimeMillis());
		        ret.value = newLoggedUser;
		        if (pkLoggedUserByIp.contains(txn, ip, LockMode.DEFAULT)) {
		        	pkLoggedUserByIp.delete(ip);
		        }
		        pkLoggedUserByIp.put(txn, newLoggedUser);
			}
		};
		runTransaction.run(false);
		
		return ret.value;
	}
	
	public boolean removeAsLoggedUser(final long ip, final String userAgent) throws EnvironmentFailureException, InterruptedException {
		final TransactionReturn<Boolean> ret = new TransactionReturn<Boolean>();
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
		        EnLoggedUser loggedUser = pkLoggedUserByIp.get(ip);
		        if (loggedUser != null) {
			        if (loggedUser.getIp() != ip || !loggedUser.getUserAgent().equals(userAgent)) {
			        	return;
			        }
					
			        ret.value = pkLoggedUserByIp.delete(ip);        
		        }
			}
		};
		runTransaction.run(false);
		return ret.value;
	}
	
	public void removeOldLoggedUsers(final long pastTimeToCheck) throws EnvironmentFailureException, InterruptedException {
		final Vector<EnLoggedUser> usersToRemove = new Vector<EnLoggedUser>();
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
		        long timeLoggedToRemove = System.currentTimeMillis() - pastTimeToCheck;
		        
		        Iterator<EnLoggedUser> entriesIt = pkLoggedUserByIp.entities(txn, CursorConfig.DEFAULT).iterator();
		        while (entriesIt.hasNext()) {
		        	EnLoggedUser loggedUser = entriesIt.next();
		        	long timeLogged = System.currentTimeMillis() - loggedUser.getLoginTime();
		        	if (timeLogged > timeLoggedToRemove) {
		        		usersToRemove.add(loggedUser);
		        	}
		        }		        
			}
		};
		transaction.run(false);
		
        Iterator<EnLoggedUser> usersToRemoveIt = usersToRemove.iterator();
        while (usersToRemoveIt.hasNext()) {
        	EnLoggedUser loggedUserToRemove = usersToRemoveIt.next();
			long ip = loggedUserToRemove.getIp();
			String userAgent = loggedUserToRemove.getUserAgent();
			removeAsLoggedUser(ip, userAgent);
        }
	}
	
	public boolean isUserLogged(final String email) throws EnvironmentFailureException, InterruptedException {
		System.out.println("is user logged, email: "+ email);
		final TransactionReturn<Boolean> ret = new TransactionReturn<Boolean>();
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				ret.value = skLoggedUserByEmail.contains(txn, email, LockMode.DEFAULT);
			}
		};
		transaction.run(true);
		System.out.println("dotransation is logged ret: " + ret.value);
		return ret.value;
	}
	
	public boolean loginUserWithEncodedCredentials(String email, String encodedKey, long ip, String userAgent) throws EnvironmentFailureException, InterruptedException {
		String dataDecoded = Base64.decode(encodedKey);
		String plainPassword = dataDecoded.split("&")[1];
		return loginUser(email, plainPassword, ip, userAgent);
	}
	
	public EnUser createUser(final String email, final String password, final int userType) throws EnvironmentFailureException, InterruptedException {
		final TransactionReturn<EnUser> ret = new TransactionReturn<EnUser>();
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnUser newUser = new EnUser();
				newUser.setPassword(password);
				newUser.setEmail(email);
				newUser.setUserType(userType);
				pkUserByEmail.put(txn, newUser);
				ret.value = newUser;
			}
		};
		runTransaction.run(true);
		return ret.value;
	}
	
	public EnUser createAnonymousUser(final String name, final String password, long ip, String userAgent) throws EnvironmentFailureException, InterruptedException {
		if (isIpLogged(ip)) {
			return null;
		}
		
		final TransactionReturn<EnUser> ret = new TransactionReturn<EnUser>();
		RunTransaction runTransaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnUser newUser = new EnUser();
				String emailKey = name + "@anonymous";
				newUser.setEmail(emailKey);
				newUser.setName(name);
				newUser.setPassword(password);
				newUser.setUserType(1);
				pkUserByEmail.put(txn, newUser);
				ret.value = newUser;
			}
		};
		runTransaction.run(false);
		
		return ret.value;
	}
	
	public boolean isIpLogged(final long ip) throws EnvironmentFailureException, InterruptedException {
		final TransactionReturn<Boolean> ret = new TransactionReturn<Boolean>();
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnLoggedUser enLoggedUser = pkLoggedUserByIp.get(txn, ip, LockMode.DEFAULT);
				boolean b = enLoggedUser != null;
				ret.value = b;
			}
		};
		transaction.run(true);
		return ret.value;
	}
	
	public EnUser updateUserData(String name, String oldEmail, String email, String pass, int sex, int majority, String cep, String phone, String avatarURL) throws EnvironmentFailureException, InterruptedException {
		final EnUser user = getUser(oldEmail);
		if (user == null) {
			return null;
		}
		
		if (name != null && !name.isEmpty()) {
			user.setName(name);
		}
		if (email != null && !email.isEmpty()) {
			user.setEmail(email);
		}
		if (pass != null && !pass.isEmpty()) {
			user.setPassword(pass);
		}
		user.setSex(sex);
		if (cep != null && !cep.isEmpty()) {
			user.setCep(cep);
		}
		user.setMajority(majority);
		if (phone != null && !phone.isEmpty()) {
			user.setPhone(phone);
		}
		user.setAvatarURL(avatarURL);
		
		final TransactionReturn<EnUser> ret = new TransactionReturn<EnUser>();
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				ret.value = pkUserByEmail.put(txn, user);
			}
		};
		transaction.run(false);
		
		return ret.value;
	}
	
	public EnUser createUser(final String email, final String pass, final String name, final int sex, final int majority, final String cep) throws EnvironmentFailureException, InterruptedException {
		if (isUserRegistered(email)) {
			return getUser(email);
		}
	
		final TransactionReturn<EnUser> ret = new TransactionReturn<EnUser>();
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				EnUser newUser = new EnUser();
				newUser.setEmail(email);
				newUser.setPassword(pass);
				newUser.setName(name);
				newUser.setSex(sex);
				newUser.setCep(cep);
				newUser.setMajority(majority);
				ret.value = pkUserByEmail.put(txn, newUser);				
			}
		};
		transaction.run(false);
		
		return ret.value;
	}
	
	public EnUser getUser(final String email) throws EnvironmentFailureException, InterruptedException {
		final TransactionReturn<EnUser> ret = new TransactionReturn<EnUser>();
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				ret.value = pkUserByEmail.get(email);
			}
		};
		transaction.run(false);
		
		return ret.value;
	}
	
	public SortedMap<String, EnUser> getUsers() {
		final TransactionReturn<SortedMap<String, EnUser>> ret = new TransactionReturn<SortedMap<String,EnUser>>();
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				ret.value = pkUserByEmail.sortedMap();
			}
		};
		try {
			transaction.run(false);
		} catch (EnvironmentFailureException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return ret.value;
	}
	
	public EnUser redefinePass(String email, String pass) throws EnvironmentFailureException, InterruptedException {
		final EnUser user = getUser(email);
		if (user == null) {
			return null;
		}
		
		user.setPassword(pass);
		final TransactionReturn<EnUser> ret = new TransactionReturn<EnUser>();
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {
				ret.value = pkUserByEmail.put(txn, user);				
			}
		};
		transaction.run(false);
		return ret.value;
	}
	
	public void createPassRedefinition(String email, String passKey) {
		EnPassRedefinition passRedefinition = new EnPassRedefinition();
		passRedefinition.setEmail(email);
		passRedefinition.setDate(System.currentTimeMillis());
        passRedefinition.setPassKey(passKey);
	}
	
	public EnUser updateAvatar(String email, String avatarURL) throws EnvironmentFailureException, InterruptedException {
		final EnUser user = getUser(email);
		if (user == null) {
			return null;
		}

		user.setAvatarURL(avatarURL);
		
		final TransactionReturn<EnUser> ret = new TransactionReturn<EnUser>();
		RunTransaction transaction = new RunTransaction(repEnv, System.out) {
			@Override
			public void doTransactionWork(Transaction txn, Command cmd) {				
				ret.value = pkUserByEmail.put(txn, user);
			}
		};
		transaction.run(false);
		return ret.value;
	}

}
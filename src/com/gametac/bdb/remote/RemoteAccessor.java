package com.gametac.bdb.remote;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import com.gametac.bdb.accessor.DataAccessor;
import com.gametac.socket.ByteUtils;
import com.gametac.socket.MessageWorker;
import com.gametac.socket.NioServer;
import com.gametac.socket.RemoteAccessorInterface;
import com.gametac.socket.ServerDataEvent;

public class RemoteAccessor implements RemoteAccessorInterface {
	private NioServer nioServer;
	
	private HashMap<String, DataAccessor> dataAccessorMap = new HashMap<String, DataAccessor>();
	
	public RemoteAccessor() throws IOException {
		MessageWorker worker = new MessageWorker(this);
		new Thread(worker).start();
		nioServer = new NioServer(null, 9090, worker);
		new Thread(nioServer).start();
	}
	
	public void stop() {
		nioServer.setServerRunning(false);
	}
	
	public void addDataAcessor(DataAccessor dataAcessor) {
		String dataAccessorKey = dataAcessor.getDataAccessorKey();
		dataAccessorMap.put(dataAccessorKey, dataAcessor);
	}
	
	@Override
	public byte[] processMessage(ServerDataEvent dataEvent) {
		String rawStringMessage = new String(dataEvent.data);
		return processMessage(rawStringMessage);
	}
	
	private byte[] processMessage(String rawStringMessage) {
		try {
			System.out.println("rawmessage: " + rawStringMessage);
			//String raw = "achievements.updateAchievements?achievementId={0}&gameId={1}&email={2}&achievementDate={3}&quantityAchieved={4}&quantityRequired={5}";
			int indexOf = rawStringMessage.indexOf('.');
			String dataAccessorKey = rawStringMessage.substring(0, indexOf);
			String methodRaw = rawStringMessage.substring(indexOf + 1, rawStringMessage.length());
			String[] methodParams = methodRaw.split("\\?");
			String methodName = methodParams[0];
			String rawParams = methodParams[1];
			String[] rawParamsValues = rawParams.split("\\&");
			String[] params = new String[rawParamsValues.length];
			String[] values = new String[rawParamsValues.length];
			String[] types = new String[rawParamsValues.length];
			for (int i = 0; i < rawParamsValues.length; i++) {
				String[] paramValue = rawParamsValues[i].split("=");
				params[i] = paramValue[0];
				String rawValueType = paramValue[1];
				String[] valueType = rawValueType.split("-");
				values[i] = valueType[0];
				types[i] = valueType.length == 1 ? "s" : valueType[1];
			}
			
			DataAccessor dataAccessor = dataAccessorMap.get(dataAccessorKey);
			if (dataAccessor == null) {
				throw new NullPointerException(dataAccessorKey + ": is null");
			}
			Class<?> parameterTypes[] = new Class<?>[rawParamsValues.length];
			Object[] args = new Object[rawParamsValues.length];
			for (int i = 0; i < rawParamsValues.length; i++) {
				parameterTypes[i] = getTypeClass(types[i]);
				args[i] = getValueWithType(values[i], parameterTypes[i]);
			}
			try {
				Class<? extends DataAccessor> class1 = dataAccessor.getClass();
				Method method = class1.getMethod(methodName, parameterTypes);
				try {
					Object invoke = method.invoke(dataAccessor, args);
					byte[] bytesWithObject = getBytesWithObject(invoke);
					return bytesWithObject;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new byte[0];
	}

	private byte[] getBytesWithObject(Object object) {
		Class<? extends Object> class_ = object.getClass();
		
		if (class_ == int.class) {
			int intValue = (Integer)object;
			byte[] bytes = ByteUtils.getBytes(intValue);
			return bytes;
		} else if (class_ == String.class) {
			String stringValue = (String)object;
			byte[] bytes = ByteUtils.getBytes(stringValue);
			return bytes;
		} else if (class_ == boolean.class || class_ == Boolean.class) {
			boolean booleanValue = (Boolean)object;
			byte[] bytes = ByteUtils.getBytes(booleanValue);
			return bytes;
		} else if (class_ == float.class) {
			float floatValue = (Float)object;
			byte[] bytes = ByteUtils.getBytes(floatValue);
			return bytes;
		} else if (class_ == double.class) {
			double doubleValue = (Double)object;
			byte[] bytes = ByteUtils.getBytes(doubleValue);
			return bytes;
		}
		
		throw new IllegalArgumentException("Illegal type argument: " + class_);	
	}

	private Object getValueWithType(String string, Class<?> class_) {
		if (class_ == int.class) {
			return Integer.parseInt(string);
		} else if (class_ == String.class) {
			return string;
		} else if (class_ == boolean.class) {
			return Boolean.parseBoolean(string);
		} else if (class_ == float.class) {
			return Float.parseFloat(string);
		} else if (class_ == double.class) {
			return Double.parseDouble(string);
		}
		
		throw new IllegalArgumentException("Illegal type argument: " + class_);
	}

	private Class<?> getTypeClass(String type) {
		if (type.equals("i")) {
			return int.class;
		} else if (type.equals("s")) {
			return String.class;
		} else if (type.equals("b")) {
			return boolean.class;
		} else if (type.equals("f")) {
			return float.class;
		} else if (type.equals("d")) {
			return double.class;
		}
		throw new IllegalArgumentException("Illegal type argument: " + type);
	}
}

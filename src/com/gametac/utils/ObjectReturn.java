package com.gametac.utils;

public class ObjectReturn<E> implements ReturnInterface<E>{
	public Object obj;
	
	public ObjectReturn() {
	}
	
	public ObjectReturn(E obj) {
		this.obj = obj;
	}
	
	@Override
	public void onReturn(E object) {
		this.obj = object;
	}
}

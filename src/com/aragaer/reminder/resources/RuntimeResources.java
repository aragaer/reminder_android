package com.aragaer.reminder.resources;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import android.content.res.Resources;
import android.util.Log;
import android.util.Pair;

final class RHKey extends Pair<Constructor<? extends RuntimeResources>, Resources> {
	public RHKey(Constructor<? extends RuntimeResources> first, Resources second) {
		super(first, second);
	}
}

abstract class RuntimeResources {
	protected final Resources r;

	protected RuntimeResources(final Resources base) {
		r = base;
	}

	private final static Map<RHKey, RuntimeResources> singletons = new HashMap<RHKey, RuntimeResources>();
	private final static Map<Class<? extends RuntimeResources>, Constructor<? extends RuntimeResources>> constructors = new HashMap<Class<? extends RuntimeResources>, Constructor<? extends RuntimeResources>>();

	@SuppressWarnings("unchecked")
	protected static final <E extends RuntimeResources> E getInstance(
			Class<E> clazz, Resources r) {
		Constructor<E> ctor = (Constructor<E>) constructors.get(clazz);
		if (ctor == null)
			try {
				ctor = clazz.getDeclaredConstructor(Resources.class);
				constructors.put(clazz, ctor);
			} catch (NoSuchMethodException e) {
				Log.e("RR", "Failed to find constructor", e);
				return null;
			}
		final RHKey key = new RHKey(ctor, r);
		E res = (E) singletons.get(key);
		if (res == null)
			try {
				res = ctor.newInstance(r);
				singletons.put(key, res);
			} catch (IllegalArgumentException e) {
				Log.e("RR", "Failed to call constructor", e);
			} catch (InstantiationException e) {
				Log.e("RR", "Failed to call constructor", e);
			} catch (IllegalAccessException e) {
				Log.e("RR", "Failed to call constructor", e);
			} catch (InvocationTargetException e) {
				Log.e("RR", "Failed to call constructor", e);
			}
		return res;
	}
}

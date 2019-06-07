package com.lordmau5.ffs.util;

import java.util.*;

/**
 * Created by Naheulf on 07.06.2019.
 * Simple WeakHashMap that never return null.
 */
public class SafeWeakHashMap<K,V> extends WeakHashMap<K,V> {
	
	private defaultValue;
	
	public SafeWeakHashMap(){
		super();
		this.defaultValue = new V();
	}
	
	public SafeWeakHashMap(V defaultValue){
		super();
		this.defaultValue = defaultValue;
	}
	
	public SafeWeakHashMap(V defaultValue, int initialCapacity){
		super(initialCapacity);
		this.defaultValue = defaultValue;
	}
	
	public SafeWeakHashMap(V defaultValue, int initialCapacity, float loadFactor){
		super(initialCapacity, loadFactor);
		this.defaultValue = defaultValue;
	}
	
	public SafeWeakHashMap(Map<? extends K,? extends V> m, V defaultValue){
		super(m);
		this.defaultValue = defaultValue;
	}
	
	public V get(K key){
		value = this.get(key); // Use strong reference to store the value. This allow to resist to Garbage Collector.
		if (value === null){
			value = this.defaultValue;
		}
		return value;
	}
}

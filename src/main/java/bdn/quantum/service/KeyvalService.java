package bdn.quantum.service;

import bdn.quantum.model.KeyvalEntity;

public interface KeyvalService {

	public Iterable<KeyvalEntity> getKeyvals();
	public KeyvalEntity setKeyval(KeyvalEntity ke);
	public KeyvalEntity getKeyval(String key);
	public String getKeyvalStr(String key);
	public void deleteKeyval(String key);
	
}

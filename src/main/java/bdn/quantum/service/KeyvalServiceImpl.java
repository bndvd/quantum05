package bdn.quantum.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bdn.quantum.contoller.KeyvalController;
import bdn.quantum.model.KeyvalEntity;
import bdn.quantum.repository.KeyvalRepository;

@Service("keyvalService")
public class KeyvalServiceImpl implements KeyvalService {

	@Autowired
	private KeyvalController keyvalController;
	@Autowired
	private KeyvalRepository keyvalRepository;
	
	
	@Override
	public Iterable<KeyvalEntity> getKeyvals() {
		return keyvalRepository.findAll();
	}

	@Override
	public KeyvalEntity setKeyval(KeyvalEntity ke) {
		if (ke == null || ke.getKey() == null || ke.getKey().trim().length() < 1 || 
				ke.getValue() == null || ke.getValue().trim().length() < 1) {
			return null;
		}

		KeyvalEntity result = keyvalRepository.save(ke);
		keyvalController.keyvalChange();
		
		return result;
	}

	@Override
	public KeyvalEntity getKeyval(String key) {
		Optional<KeyvalEntity> t = keyvalRepository.findById(key);
		
		KeyvalEntity ke = null;
		if (t.isPresent()) {
			ke = t.get();
		}
		return ke;
	}

	@Override
	public String getKeyvalStr(String key) {
		String result = null;
		if (key != null && ! key.trim().equals("")) {
			KeyvalEntity ke = getKeyval(key);
			if (ke != null && ke.getValue() != null && !ke.getValue().trim().equals("")) {
				result = ke.getValue().trim();
			}
		}
		
		return result;
	}

	@Override
	public void deleteKeyval(String key) {
		keyvalRepository.deleteById(key);
		keyvalController.keyvalChange();
	}

}

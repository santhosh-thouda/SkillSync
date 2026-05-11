package com.capgemini.group.service;

import com.capgemini.group.entity.ContactMessage;
import com.capgemini.group.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository repository;

    @Override
    public ContactMessage saveMessage(ContactMessage message) {
        return repository.save(message);
    }
}

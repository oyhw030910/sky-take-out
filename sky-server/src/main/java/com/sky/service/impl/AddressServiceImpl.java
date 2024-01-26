package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressMapper;
import com.sky.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    AddressMapper addressMapper;

    @Override
    public void save(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressMapper.insert(addressBook);
    }

    @Override
    public List<AddressBook> list() {
        AddressBook addressBook=AddressBook.builder().userId(BaseContext.getCurrentId()).build();

        return addressMapper.list(addressBook);
    }

    @Override
    public AddressBook listDefault() {
        AddressBook addressBook=AddressBook.builder().userId(BaseContext.getCurrentId()).isDefault(1).build();
        List<AddressBook> addressBooks=addressMapper.list(addressBook);
        if (addressBooks!=null&& !addressBooks.isEmpty()){
            return addressBooks.get(0);
        }
        return null;
    }

    @Override
    @Transactional
    public void setDefault(AddressBook addressBook) {
        AddressBook addressBook1=listDefault();
        if(addressBook1!=null){
            addressBook1.setIsDefault(0);
            addressMapper.update(addressBook1);
        }
        addressBook=addressMapper.list(addressBook).get(0);
        addressBook.setIsDefault(1);
        addressMapper.update(addressBook);
    }

    @Override
    public AddressBook getById(Long id) {
        AddressBook addressBook=AddressBook.builder().id(id).build();
        List<AddressBook> addressBooks=addressMapper.list(addressBook);
        return addressBooks.get(0);
    }

    @Override
    public void change(AddressBook addressBook) {
        addressMapper.update(addressBook);
    }

    @Override
    public void delete(Long id) {
        addressMapper.deleteById(id);
    }
}

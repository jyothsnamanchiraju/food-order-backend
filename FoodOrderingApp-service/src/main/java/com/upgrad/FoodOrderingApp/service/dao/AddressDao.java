package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class AddressDao {

    @PersistenceContext
    private EntityManager entityManager;

    public StateEntity getStateDetails(String uuid){
        try{
            return entityManager.createNamedQuery("getStateByUuid", StateEntity.class).setParameter("uuid",uuid).getSingleResult();
        }catch(NoResultException nre){
            return null;
        }
    }

    //createNewAddress
    public AddressEntity createNewAddress(AddressEntity addressEntity){
        try {
            entityManager.persist(addressEntity);
            return addressEntity;
        }catch (NoResultException nre){
            return null;
        }
    }

    //get address by primary key value
    public AddressEntity getAddressById(Integer id) {
        try {
            return this.entityManager.createNamedQuery("getAddressById", AddressEntity.class).setParameter("id",id).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    //recordCustomerAddressEntity(customerAddressEntity);
    public CustomerAddressEntity recordCustomerAddressEntity(CustomerAddressEntity customerAddressEntity){
        try {
            entityManager.persist(customerAddressEntity);
            return customerAddressEntity;
        }catch (NoResultException nre){
            return null;
        }
    }

    //getAllAddresses(customerId)
    public List<CustomerAddressEntity> getAllAddresses(CustomerEntity customer){
        try{
            return entityManager.createNamedQuery("getAddrByCustomerId", CustomerAddressEntity.class).setParameter("customer",customer).getResultList();
        }catch(NoResultException nre){
            return null;
        }
    }

    //getAddressByUuid(addressUuid)
    public AddressEntity getAddressByUuid(String uuid) {
        try {
            return this.entityManager.createNamedQuery("getAddressByUuId", AddressEntity.class).setParameter("uuid",uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    //deleteAddress(address)
    public void deleteAddress(AddressEntity address) {
        try {
            entityManager.remove(address);
        }catch(NoResultException nre){
            System.err.println(nre);
        }
    }
}

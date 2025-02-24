package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class CustomerDao {

    @PersistenceContext
    private EntityManager entityManager;

    public CustomerEntity createCustomer(CustomerEntity customerEntity) {
        try {
            entityManager.persist(customerEntity);
            return customerEntity;
        }catch (NoResultException nre){
            return null;
        }
    }

    public CustomerEntity getCustomerByContactNumber(String contactNumber){
        try{
            return entityManager.createNamedQuery("customerByContactNumber", CustomerEntity.class).setParameter("contactNumber",contactNumber).getSingleResult();
        }catch(NoResultException nre){
            return null;
        }
    }

    public CustomerAuthEntity createCustomerAuth(CustomerAuthEntity customerAuthEntity){
        try {
            entityManager.persist(customerAuthEntity);
            return customerAuthEntity;
        }catch (NoResultException nre){
            return null;
        }
    }

    public CustomerAuthEntity getCustomerAuthByToken(String accessToken){
        try{
            return entityManager.createNamedQuery("customerAuthByToken", CustomerAuthEntity.class).setParameter("accessToken",accessToken).getSingleResult();
        }catch(NoResultException nre){
            return null;
        }
    }

    public CustomerAuthEntity updateLogoutTime(final CustomerAuthEntity customerAuth){
        try {
            entityManager.merge(customerAuth);
            return customerAuth;
        }catch (NoResultException nre){
            return null;
        }
    }

    //updateCustomerEntity(updateCustomer)
    public CustomerEntity updateCustomerEntity(final CustomerEntity updateCustomer){
        try {
            entityManager.merge(updateCustomer);
            return updateCustomer;
        }catch (NoResultException nre){
            return null;
        }
    }




}

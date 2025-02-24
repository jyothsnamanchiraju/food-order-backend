package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.businness.PasswordCryptographyProvider;

import com.upgrad.FoodOrderingApp.service.exception.*;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class CustomerService {
    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity authenticate(final String contactNumber, final String password) throws AuthenticationFailedException {

        CustomerEntity customerEntity = customerDao.getCustomerByContactNumber(contactNumber);
        if (customerEntity == null) {
            throw new AuthenticationFailedException("ATH-001", "This contact number has not been registered!");
        }

        final String encryptedPassword = cryptographyProvider.encrypt(password, customerEntity.getSalt());

        if (encryptedPassword.equals(customerEntity.getPassword())) {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);

            CustomerAuthEntity customerAuthEntity = new CustomerAuthEntity();
            customerAuthEntity.setCustomer(customerEntity);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            customerAuthEntity.setAccessToken(jwtTokenProvider.generateToken(customerEntity.getUuid(), now, expiresAt));

            customerAuthEntity.setUuid(UUID.randomUUID().toString());
            customerAuthEntity.setLoginAt(now);
            customerAuthEntity.setExpiresAt(expiresAt);

            customerDao.createCustomerAuth(customerAuthEntity);

            return customerAuthEntity;
        } else {
            throw new AuthenticationFailedException("ATH-002", "Invalid Credentials");
        }

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity logout(final String accessToken) throws AuthorizationFailedException{
        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthByToken(accessToken);

        if(customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001","Customer is not Logged in.");
        }
        if(customerAuthEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002","Customer is logged out. Log in again to access this endpoint.");
        }

        final ZonedDateTime now = ZonedDateTime.now();

        if((customerAuthEntity.getExpiresAt().compareTo(now)) < 0){
            throw new AuthorizationFailedException("ATHR-003","Your session is expired. Log in again to access this endpoint.");
        }


        customerAuthEntity.setLogoutAt(now);
        customerDao.updateLogoutTime(customerAuthEntity);

        CustomerEntity customerEntity = customerAuthEntity.getCustomer();

        return customerEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomer(String accessToken, String firstName, String lastName)
            throws UpdateCustomerException, AuthorizationFailedException{

        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthByToken(accessToken);

        if(customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001","Customer is not Logged in.");
        }

        if(customerAuthEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002","Customer is logged out. Log in again to access this endpoint.");
        }

        final ZonedDateTime now = ZonedDateTime.now();

        if((customerAuthEntity.getExpiresAt().compareTo(now)) < 0){
            throw new AuthorizationFailedException("ATHR-003","Your session is expired. Log in again to access this endpoint.");
        }

        CustomerEntity updateCustomer = customerAuthEntity.getCustomer();
        updateCustomer.setFirstName(firstName);
        updateCustomer.setLastName(lastName);

        CustomerEntity updatedCust = customerDao.updateCustomerEntity(updateCustomer);
        return updatedCust;

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updatePassword(String accessToken, String oldPassword, String newPassword)
            throws UpdateCustomerException, AuthorizationFailedException{

        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthByToken(accessToken);

        if(customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001","Customer is not Logged in.");
        }

        if(customerAuthEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002","Customer is logged out. Log in again to access this endpoint.");
        }

        final ZonedDateTime now = ZonedDateTime.now();

        if((customerAuthEntity.getExpiresAt().compareTo(now)) < 0){
            throw new AuthorizationFailedException("ATHR-003","Your session is expired. Log in again to access this endpoint.");
        }

        if(!checkPasswordStrength(newPassword)){
            throw new UpdateCustomerException("UCR-001", "Weak password!");
        }

        CustomerEntity customerEntity = customerAuthEntity.getCustomer();

        String encryptedOldPassword = cryptographyProvider.encrypt(oldPassword,customerEntity.getSalt());

        if(encryptedOldPassword.compareTo(customerEntity.getPassword()) != 0){
            throw new UpdateCustomerException("UCR-004", "Incorrect old password!");
        }

        String[] encryptedText = cryptographyProvider.encrypt(newPassword);
        customerEntity.setSalt(encryptedText[0]);
        customerEntity.setPassword(encryptedText[1]);

        CustomerEntity updatedCustomer  = customerDao.updateCustomerEntity(customerEntity);
        return updatedCustomer;
    }


    private boolean checkPasswordStrength(String password){
        if (password.length()<8)
            return false;

        boolean hasNumber = false;
        boolean hasCaps = false;
        boolean hasSplChar = false;
        Set<Character> splChar = new HashSet<Character>();
        Character[] A= {'#', '@','$','%','&','*','!','^' };
        splChar.addAll(Arrays.asList(A));

        for(int i=0; i<password.length(); i++){
            if(password.charAt(i)>='A' && password.charAt(i)<='Z'){
                hasCaps= true;
            }
            if((int)password.charAt(i)>=48 && (int)password.charAt(i)<=57)
                hasNumber= true;

            if (splChar.contains(password.charAt(i)))
                hasSplChar = true;
        }

        if(hasNumber && hasCaps && hasSplChar)
            return true;
        else
            return false;
    }
}


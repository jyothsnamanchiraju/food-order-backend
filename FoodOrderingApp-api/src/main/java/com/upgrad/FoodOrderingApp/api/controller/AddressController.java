package com.upgrad.FoodOrderingApp.api.controller;


import com.upgrad.FoodOrderingApp.api.model.AddressList;
import com.upgrad.FoodOrderingApp.api.model.AddressListResponse;
import com.upgrad.FoodOrderingApp.api.model.AddressListState;
import com.upgrad.FoodOrderingApp.api.model.DeleteAddressResponse;
import com.upgrad.FoodOrderingApp.service.businness.AddressService;

import com.upgrad.FoodOrderingApp.api.model.SaveAddressRequest;
import com.upgrad.FoodOrderingApp.api.model.SaveAddressResponse;

import com.upgrad.FoodOrderingApp.service.exception.*;

import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Base64;

@RestController
@RequestMapping("/")

public class AddressController {

    @Autowired
    private AddressService addressService;

    @RequestMapping(method= RequestMethod.POST, path ="address", consumes=MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> createCustomerAddress(@RequestHeader("authorization") final String customerAccessToken, final SaveAddressRequest saveAddressRequest)
            throws AuthorizationFailedException, SaveAddressException, AddressNotFoundException {

        AddressEntity addressEntity = new AddressEntity();

        if(saveAddressRequest.getFlatBuildingName()== null || saveAddressRequest.getFlatBuildingName()==" "
           || saveAddressRequest.getLocality() == null || saveAddressRequest.getLocality() == null
           || saveAddressRequest.getCity()== null || saveAddressRequest.getCity()== " "
           || saveAddressRequest.getPincode()== null ||saveAddressRequest.getPincode()== " "
           || saveAddressRequest.getStateUuid()==null || saveAddressRequest.getStateUuid()== " "){
            throw new SaveAddressException("SAR-001", "No field can be empty");
        }

        if(!validPincode(saveAddressRequest.getPincode())){
            throw new SaveAddressException("SAR-002", "Invalid pincode");
        }

        addressEntity.setUuid(UUID.randomUUID().toString());
        addressEntity.setFlatBuildingNumber(saveAddressRequest.getFlatBuildingName());
        addressEntity.setLocality(saveAddressRequest.getLocality());
        addressEntity.setCity(saveAddressRequest.getCity());
        addressEntity.setPincode(saveAddressRequest.getPincode());
        String stateUuid = saveAddressRequest.getStateUuid();

        final AddressEntity addressCreated = addressService.createAddress(customerAccessToken, addressEntity, stateUuid);
        SaveAddressResponse saveAddressResponse = new SaveAddressResponse().id(addressCreated.getUuid()).status("ADDRESS SUCCESSFULLY REGISTERED") ;
        return new ResponseEntity<SaveAddressResponse>(saveAddressResponse, HttpStatus.CREATED);
        }


    @RequestMapping(method= RequestMethod.GET, path ="address/customer", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AddressListResponse> getAllAddresses(@RequestHeader("authorization") final String customerAccessToken)
            throws AuthorizationFailedException {

        List<AddressList> listOfAddresses = new <AddressList> ArrayList();
        List<AddressEntity> customerAddressList = new<AddressEntity> ArrayList();

        customerAddressList = addressService.getAddressList(customerAccessToken);

        for(AddressEntity addr: customerAddressList){
            AddressList customerAddress = new AddressList();
            StateEntity state = new StateEntity();
            AddressListState aState = new AddressListState();
            //UUID. fromString(uuidAsString);
            customerAddress.setId(UUID.fromString(addr.getUuid()));
            customerAddress.setFlatBuildingName(addr.getFlatBuildingNumber());
            customerAddress.setLocality(addr.getLocality());
            customerAddress.setCity(addr.getCity());
            customerAddress.setPincode(addr.getPincode());

            state= addr.getState();
            aState.setId(UUID.fromString(state.getUuid()));
            aState.setStateName(state.getStateName());

            customerAddress.setState(aState);

            listOfAddresses.add(customerAddress);
        }

        AddressListResponse addressListResponse = new AddressListResponse().addresses(listOfAddresses);
        return new ResponseEntity<AddressListResponse>(addressListResponse, HttpStatus.FOUND);

    }


    @RequestMapping(method= RequestMethod.DELETE, path ="address/{address_id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DeleteAddressResponse> deleteAddress(@RequestHeader("authorization") final String customerAccessToken,
                                                               @PathVariable("address_id") final String addressUuid)
            throws AuthorizationFailedException, AddressNotFoundException {

        DeleteAddressResponse deleteAddressResponse = new DeleteAddressResponse();
        addressService.deleteAddress(customerAccessToken, addressUuid);
        deleteAddressResponse.id(UUID.fromString(addressUuid)).status("ADDRESS_DELETED");
        return new ResponseEntity<DeleteAddressResponse>(deleteAddressResponse, HttpStatus.OK);

    }



        private boolean validPincode(String pincode){
            if(pincode.length() !=6)
                return false;

            boolean allNumeric = true;

                for(int i=0; i<pincode.length(); i++){
                    if(!((int)pincode.charAt(i) >=48 && (int)pincode.charAt(i)<=57)){
                        allNumeric = false;
                    }
                }

                return allNumeric;
        }
    }

package com.capgemini.accounts.service.impl;

import com.capgemini.accounts.dto.AccountsDto;
import com.capgemini.accounts.dto.CardsDto;
import com.capgemini.accounts.dto.CustomerDetailsDto;
import com.capgemini.accounts.dto.LoansDto;
import com.capgemini.accounts.entity.Accounts;
import com.capgemini.accounts.entity.Customer;
import com.capgemini.accounts.exception.ResourceNotFoundException;
import com.capgemini.accounts.mapper.AccountsMapper;
import com.capgemini.accounts.mapper.CustomerMapper;
import com.capgemini.accounts.repository.AccountsRepository;
import com.capgemini.accounts.repository.CustomerRepository;
import com.capgemini.accounts.service.ICustomerService;
import com.capgemini.accounts.service.client.CardsFeignClient;
import com.capgemini.accounts.service.client.LoansFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomerServiceImpl implements ICustomerService {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;
    private CardsFeignClient cardsFeignClient;
    private LoansFeignClient loansFeignClient;


    @Override
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );
        CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer, new CustomerDetailsDto());
        customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts,new AccountsDto()));

        ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoanDetails(mobileNumber);
        customerDetailsDto.setLoansDto(loansDtoResponseEntity.getBody());

        ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(mobileNumber);
        customerDetailsDto.setCardsDto(cardsDtoResponseEntity.getBody());
        return customerDetailsDto;
    }
}

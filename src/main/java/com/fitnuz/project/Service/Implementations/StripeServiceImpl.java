package com.fitnuz.project.Service.Implementations;

import com.fitnuz.project.Payload.DTO.StripePaymentDto;
import com.fitnuz.project.Service.Definations.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerSearchResult;
import com.stripe.model.PaymentIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerSearchParams;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class StripeServiceImpl implements StripeService {

    @Value("${stripe.secret.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init(){
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    public PaymentIntent paymentIntent(StripePaymentDto stripePaymentDto) throws StripeException {

        Customer customer;

        //search for customer in stripe
        CustomerSearchParams customerSearchParams = CustomerSearchParams.builder()
                .setQuery("email:'" + stripePaymentDto.getEmail() + "'")
                .build();
        CustomerSearchResult customers = Customer.search(customerSearchParams);

        //create customer if does not exist in stripe
        if(customers.getData().isEmpty()){
            CustomerCreateParams customerCreateParams =
                    CustomerCreateParams.builder()
                            .setName(stripePaymentDto.getName())
                            .setEmail(stripePaymentDto.getEmail())
                            .setAddress(
                                    CustomerCreateParams.Address.builder()
                                            .setLine1(stripePaymentDto.getAddress().getStreet())
                                            .setCity(stripePaymentDto.getAddress().getCity())
                                            .setState(stripePaymentDto.getAddress().getState())
                                            .setPostalCode(stripePaymentDto.getAddress().getPincode())
                                            .setCountry(stripePaymentDto.getAddress().getCountry())
                                            .build()
                                    )
                            .build();

            customer = Customer.create(customerCreateParams);
        }else{
            customer = customers.getData().getFirst();
        }

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(stripePaymentDto.getAmount())
                        .setCurrency(stripePaymentDto.getCurrency())
                        .setCustomer(customer.getId())
                        .setDescription(stripePaymentDto.getDescription())
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .build();

        return PaymentIntent.create(params);
    }
}

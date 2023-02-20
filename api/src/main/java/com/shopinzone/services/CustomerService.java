package com.shopinzone.services;

import com.shopinzone.dto.*;
import com.shopinzone.dto.requestDto.CartRequestDto;
import com.shopinzone.enums.SignInStatusCode;
import com.shopinzone.enums.SignUpStatusCode;
import com.shopinzone.repository.UserDetailsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CustomerService {

    UserDetailsRepository userDetailsRepository;

    public SignInStatusCode signIn(String email, String password) {
        return null;
    }

    public SignUpStatusCode signUp(ProfileDto profileDto) {
        return null;
    }

    public ProfileDto getProfile(String email) {
        return null;
    }

    public List<OrderDto> getOrderList(String email) {
        return null;
    }

    public List<AddressDto> getAddressList(String email) {
        return null;
    }

    public List<PaymentOptionDto> getPaymentOptionList(String email) {
        return null;
    }

    public CheckOutDto getCheckOut(String email) {
        return null;
    }

    public ProfileDto updateProfile(String email, ProfileDto profileDto) {
        return null;
    }

    public List<PaymentOptionDto> addPaymentOption(String email, PaymentOptionDto paymentOption) {
        return null;
    }

    public List<PaymentOptionDto> deletePaymentOption(String email, String paymentOptionId) {
        return null;
    }

    public List<AddressDto> addAddress(String email, AddressDto address) {
        return null;
    }

    public List<AddressDto> deleteAddress(String email, String addressId) {
        return null;
    }

    public List<ItemDto> getCartItemList(String email, String city, String shopId, CartRequestDto cartRequestDto) {
        return null;
    }

    public List<ItemDto> deleteItemFromCart(String email, String city, String shopId, String itemId,
            CartRequestDto cartRequestDto) {
        return null;
    }

    public List<CityDto> getCityList(String email) {
        return null;
    }

    public List<ItemDto> getItemListFromShop(String email, String city, String shopId) {
        return null;
    }

    public List<ShopDto> getShopListFromCategory(String email, String city, String categoryId) {
        return null;
    }

    public List<CategoryDto> getCategoryListFromCity(String email, String city) {
        return null;
    }

    public List<ItemDto> addItemIntoCart(String email, String city, String shopId, String itemId,
            CartRequestDto cartRequestDto) {
        return null;
    }

    public List<ItemDto> updateItemIntoCart(String email, String city, String shopId, String itemId,
            CartRequestDto cartRequestDto) {
        return null;
    }
}

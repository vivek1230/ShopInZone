package com.shopinzone.controllers;

import com.shopinzone.dto.*;
import com.shopinzone.dto.requestDto.CartRequestDto;
import com.shopinzone.enums.SignInStatusCode;
import com.shopinzone.enums.SignUpStatusCode;
import com.shopinzone.services.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/customer/v1/{email}")
@Slf4j
@RestController
@Validated
@AllArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Customer APIs")
public class CustomerController {

    CustomerService customerService;

    @GetMapping(path = "/signIn/{password}", produces = "application/json")
    public ResponseEntity<SignInStatusCode> signIn(@PathVariable("email") String email,
            @PathVariable("password") String password) {

        SignInStatusCode signInStatusCode = customerService.signIn(email, password);
        return new ResponseEntity<>(signInStatusCode, HttpStatus.OK);
    }

    @PostMapping(path = "/signUp", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SignUpStatusCode> signUp(@RequestBody ProfileDto profile) {

        SignUpStatusCode signUpStatusCode = customerService.signUp(profile);
        return new ResponseEntity<>(signUpStatusCode, HttpStatus.OK);
    }

    @GetMapping(path = "/getProfile", produces = "application/json")
    public ResponseEntity<ProfileDto> getProfile(@PathVariable("email") String email) {

        ProfileDto profile = customerService.getProfile(email);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @PostMapping(path = "/updateProfile", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ProfileDto> updateProfile(@PathVariable("email") String email,
            @RequestBody ProfileDto profile) {

        profile = customerService.updateProfile(email, profile);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @GetMapping(path = "/getOrderList", produces = "application/json")
    public ResponseEntity<List<OrderDto>> getOrderList(@PathVariable("email") String email) {

        List<OrderDto> orders = customerService.getOrderList(email);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping(path = "/getPaymentOptionList", produces = "application/json")
    public ResponseEntity<List<PaymentOptionDto>> getPaymentOptionList(@PathVariable("email") String email) {

        List<PaymentOptionDto> paymentOptions = customerService.getPaymentOptionList(email);
        return new ResponseEntity<>(paymentOptions, HttpStatus.OK);
    }

    @PostMapping(path = "/addPaymentOption", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<PaymentOptionDto>> addPaymentOption(@PathVariable("email") String email,
            @RequestBody PaymentOptionDto paymentOption) {

        List<PaymentOptionDto> paymentOptions = customerService.addPaymentOption(email, paymentOption);
        return new ResponseEntity<>(paymentOptions, HttpStatus.OK);
    }

    @PostMapping(path = "/deletePaymentOption/{paymentOptionId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<PaymentOptionDto>> deletePaymentOption(@PathVariable("email") String email,
            @PathVariable("paymentOptionId") String paymentOptionId) {

        List<PaymentOptionDto> paymentOptions = customerService.deletePaymentOption(email, paymentOptionId);
        return new ResponseEntity<>(paymentOptions, HttpStatus.OK);
    }

    @GetMapping(path = "/getAddressList", produces = "application/json")
    public ResponseEntity<List<AddressDto>> getAddressList(@PathVariable("email") String email) {

        List<AddressDto> addresses = customerService.getAddressList(email);
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @PostMapping(path = "/addAddress", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<AddressDto>> addAddress(@PathVariable("email") String email,
            @RequestBody AddressDto address) {

        List<AddressDto> addresses = customerService.addAddress(email, address);
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @PostMapping(path = "/deleteAddress/{addressId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<AddressDto>> deleteAddress(@PathVariable("email") String email,
            @PathVariable("addressId") String addressId) {

        List<AddressDto> addresses = customerService.deleteAddress(email, addressId);
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @PostMapping(path = "/home", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<CityDto>> getCityList(@PathVariable("email") String email) {

        List<CityDto> cityList = customerService.getCityList(email);
        return new ResponseEntity<>(cityList, HttpStatus.OK);
    }

    @PostMapping(path = "/getCategoryListFromCity/{city}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<CategoryDto>> getCategoryListFromCity(@PathVariable("email") String email,
            @PathVariable("city") String city) {

        List<CategoryDto> categoryList = customerService.getCategoryListFromCity(email, city);
        return new ResponseEntity<>(categoryList, HttpStatus.OK);
    }

    @PostMapping(path = "/getShopListFromCategory/{city}/{categoryId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ShopDto>> getShopListFromCategory(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("categoryId") String categoryId) {

        List<ShopDto> shopList = customerService.getShopListFromCategory(email, city, categoryId);
        return new ResponseEntity<>(shopList, HttpStatus.OK);
    }

    @PostMapping(path = "/getItemListFromShop/{city}/{shopId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ItemDto>> getItemListFromShop(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId) {

        List<ItemDto> itemList = customerService.getItemListFromShop(email, city, shopId);
        return new ResponseEntity<>(itemList, HttpStatus.OK);
    }

    @PostMapping(path = "/getCartItemList/{city}/{shopId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ItemDto>> getCartItemList(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId,
            @RequestBody CartRequestDto cartRequestDto) {

        List<ItemDto> itemList = customerService.getCartItemList(email, city, shopId, cartRequestDto);
        return new ResponseEntity<>(itemList, HttpStatus.OK);
    }

    @PostMapping(path = "/addItemIntoCart/{city}/{shopId}/{itemId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ItemDto>> addItemIntoCart(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId,
            @PathVariable("itemId") String itemId, @RequestBody CartRequestDto cartRequestDto) {

        List<ItemDto> itemList = customerService.addItemIntoCart(email, city, shopId, itemId, cartRequestDto);
        return new ResponseEntity<>(itemList, HttpStatus.OK);
    }

    @PostMapping(path = "/updateItemIntoCart/{city}/{shopId}/{itemId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ItemDto>> updateItemIntoCart(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId,
            @PathVariable("itemId") String itemId, @RequestBody CartRequestDto cartRequestDto) {

        List<ItemDto> itemList = customerService.updateItemIntoCart(email, city, shopId, itemId, cartRequestDto);
        return new ResponseEntity<>(itemList, HttpStatus.OK);
    }

    @PostMapping(path = "/deleteItemFromCart/{city}/{shopId}/{itemId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ItemDto>> deleteItemFromCart(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId,
            @PathVariable("itemId") String itemId, @RequestBody CartRequestDto cartRequestDto) {

        List<ItemDto> itemList = customerService.deleteItemFromCart(email, city, shopId, itemId, cartRequestDto);
        return new ResponseEntity<>(itemList, HttpStatus.OK);
    }

    @GetMapping(path = "/checkOut", produces = "application/json")
    public ResponseEntity<CheckOutDto> getCheckOut(@PathVariable("email") String email) {

        CheckOutDto checkOut = customerService.getCheckOut(email);
        return new ResponseEntity<>(checkOut, HttpStatus.OK);
    }

}

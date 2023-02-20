package com.shopinzone.controllers;

import com.shopinzone.dto.requestDto.*;
import com.shopinzone.dto.responseDto.*;
import com.shopinzone.enums.SignInStatusCode;
import com.shopinzone.enums.SignUpStatusCode;
import com.shopinzone.services.FirstAppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/firstApp/v1/{email}")
@Slf4j
@RestController
@Validated
@AllArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "First App APIs")
public class FirstAppController {

    FirstAppService firstAppService;

    @PostMapping(path = "/signIn/{password}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SignInStatusCode> signIn(@PathVariable("email") String email,
            @PathVariable("password") String password) {

        log.info("Calling signIn");
        SignInStatusCode signInStatusCode = firstAppService.signIn(email, password);
        return new ResponseEntity<>(signInStatusCode, HttpStatus.OK);
    }

    @PostMapping(path = "/signUp", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SignUpStatusCode> signUp(@RequestBody UserRequestDto userRequest) {

        log.info("Calling signUp");
        SignUpStatusCode signUpStatusCode = firstAppService.signUp(userRequest);
        return new ResponseEntity<>(signUpStatusCode, HttpStatus.OK);
    }

    @GetMapping(path = "/getProfile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfileResponseDto> getProfile(@PathVariable("email") String email) {

        log.info("Calling getProfile");
        ProfileResponseDto profileResponse = firstAppService.getProfile(email);
        return new ResponseEntity<>(profileResponse, HttpStatus.OK);
    }

    @PostMapping(path = "/addProfile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfileResponseDto> addProfile(@PathVariable("email") String email,
            @RequestBody ProfileRequestDto profileRequest) {

        log.info("Calling addProfile");
        ProfileResponseDto profileResponse = firstAppService.addProfile(email, profileRequest);
        return new ResponseEntity<>(profileResponse, HttpStatus.OK);
    }

    @PostMapping(path = "/updateProfile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfileResponseDto> updateProfile(@PathVariable("email") String email,
            @RequestBody ProfileRequestDto profileRequest) {

        log.info("Calling updateProfile");
        ProfileResponseDto profileResponse = firstAppService.updateProfile(email, profileRequest);
        return new ResponseEntity<>(profileResponse, HttpStatus.OK);
    }

    @PostMapping(path = "/addAddress", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AddressResponseDto>> addAddress(@PathVariable("email") String email,
            @RequestBody AddressRequestDto addressRequest) {

        log.info("Calling addAddress");
        List<AddressResponseDto> addressResponseList = firstAppService.addAddress(email, addressRequest);
        return new ResponseEntity<>(addressResponseList, HttpStatus.OK);
    }

    @GetMapping(path = "/getAddressList", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AddressResponseDto>> getAddressList(@PathVariable("email") String email) {

        log.info("Calling getAddressList");
        List<AddressResponseDto> addressResponseList = firstAppService.getAddressList(email);
        return new ResponseEntity<>(addressResponseList, HttpStatus.OK);
    }

    @DeleteMapping(path = "/deleteAddress/{addressId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AddressResponseDto>> deleteAddress(@PathVariable("email") String email,
            @PathVariable("addressId") Long addressId) {

        log.info("Calling deleteAddress");
        List<AddressResponseDto> addressResponseList = firstAppService.deleteAddress(email, addressId);
        return new ResponseEntity<>(addressResponseList, HttpStatus.OK);
    }

    @GetMapping(path = "/getOrderList", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OrderResponseDto>> getOrderList(@PathVariable("email") String email) {

        log.info("Calling getOrderList");
        List<OrderResponseDto> orderResponseList = firstAppService.getOrderList(email);
        return new ResponseEntity<>(orderResponseList, HttpStatus.OK);
    }

    @GetMapping(path = "/getPaymentOptionList", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PaymentOptionResponseDto>> getPaymentOptionList(@PathVariable("email") String email) {

        log.info("Calling getPaymentOptionList");
        List<PaymentOptionResponseDto> paymentOptionResponseList = firstAppService.getPaymentOptionList(email);
        return new ResponseEntity<>(paymentOptionResponseList, HttpStatus.OK);
    }

    @PostMapping(path = "/addPaymentOption", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PaymentOptionResponseDto>> addPaymentOption(@PathVariable("email") String email,
            @RequestBody PaymentOptionRequestDto paymentOptionRequest) {

        log.info("Calling addPaymentOption");
        List<PaymentOptionResponseDto> paymentOptionResponseList = firstAppService.addPaymentOption(email,
                paymentOptionRequest);
        return new ResponseEntity<>(paymentOptionResponseList, HttpStatus.OK);
    }

    @DeleteMapping(path = "/deletePaymentOption/{paymentOptionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PaymentOptionResponseDto>> deletePaymentOption(@PathVariable("email") String email,
            @PathVariable("paymentOptionId") String paymentOptionId) {

        log.info("Calling deletePaymentOption");
        List<PaymentOptionResponseDto> paymentOptionResponseList = firstAppService.deletePaymentOption(email,
                paymentOptionId);
        return new ResponseEntity<>(paymentOptionResponseList, HttpStatus.OK);
    }

    @GetMapping(path = "/home", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CityResponseDto>> home(@PathVariable("email") String email) {

        log.info("Calling home");
        List<CityResponseDto> cityResponseList = firstAppService.getCityList(email);
        return new ResponseEntity<>(cityResponseList, HttpStatus.OK);
    }

    @GetMapping(path = "/getCityList", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CityResponseDto>> getCityList(@PathVariable("email") String email) {

        log.info("Calling home");
        List<CityResponseDto> cityResponseList = firstAppService.getCityList(email);
        return new ResponseEntity<>(cityResponseList, HttpStatus.OK);
    }

    @PostMapping(path = "/addCity", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CityResponseDto>> addCity(@PathVariable("email") String email,
            @RequestBody CityRequestDto cityRequest) {

        log.info("Calling addCity");
        List<CityResponseDto> cityResponseList = firstAppService.addCity(email, cityRequest);
        return new ResponseEntity<>(cityResponseList, HttpStatus.OK);
    }

    @PostMapping(path = "/updateCity/{cityId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CityResponseDto>> updateCity(@PathVariable("email") String email,
            @PathVariable("cityId") Long cityId, @RequestBody CityRequestDto cityRequest) {

        log.info("Calling updateCity");
        List<CityResponseDto> cityResponseList = firstAppService.updateCity(email, cityId, cityRequest);
        return new ResponseEntity<>(cityResponseList, HttpStatus.OK);
    }

    @DeleteMapping(path = "/deleteCity/{cityId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CityResponseDto>> deleteCity(@PathVariable("email") String email,
            @PathVariable("cityId") Long cityId) {

        log.info("Calling deleteCity");
        List<CityResponseDto> cityResponseList = firstAppService.deleteCity(email, cityId);
        return new ResponseEntity<>(cityResponseList, HttpStatus.OK);
    }

    @GetMapping(path = "/getCategoryList", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CategoryResponseDto>> getCategoryList(@PathVariable("email") String email) {

        log.info("Calling getCategoryList");
        List<CategoryResponseDto> categoryResponseList = firstAppService.getCategoryList(email);
        return new ResponseEntity<>(categoryResponseList, HttpStatus.OK);
    }

    @PostMapping(path = "/addCategory", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CategoryResponseDto>> addCategory(@PathVariable("email") String email,
            @RequestBody CategoryRequestDto categoryRequest) {

        log.info("Calling addCategory");
        List<CategoryResponseDto> categoryResponseList = firstAppService.addCategory(email, categoryRequest);
        return new ResponseEntity<>(categoryResponseList, HttpStatus.OK);
    }

    @PostMapping(path = "/updateCategory/{categoryId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CategoryResponseDto>> updateCategory(@PathVariable("email") String email,
            @PathVariable("categoryId") Long categoryId, @RequestBody CategoryRequestDto categoryRequest) {

        log.info("Calling updateCategory");
        List<CategoryResponseDto> categoryResponseList = firstAppService.updateCategory(email, categoryId,
                categoryRequest);
        return new ResponseEntity<>(categoryResponseList, HttpStatus.OK);
    }

    @DeleteMapping(path = "/deleteCategory/{categoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CategoryResponseDto>> deleteCategory(@PathVariable("email") String email,
            @PathVariable("categoryId") Long categoryId) {

        log.info("Calling deleteCategory");
        List<CategoryResponseDto> categoryResponseList = firstAppService.deleteCategory(email, categoryId);
        return new ResponseEntity<>(categoryResponseList, HttpStatus.OK);
    }

    @GetMapping(path = "/getCategoryListFromCity/{cityId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CategoryResponseDto>> getCategoryListFromCity(@PathVariable("email") String email,
            @PathVariable("cityId") Long cityId) {

        log.info("Calling getCategoryListFromCity");
        List<CategoryResponseDto> categoryResponseList = firstAppService.getCategoryListFromCity(email, cityId);
        return new ResponseEntity<>(categoryResponseList, HttpStatus.OK);
    }

    @PostMapping(path = "/addCategoryIntoCity/{cityId}/{categoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CategoryResponseDto>> addCategoryIntoCity(@PathVariable("email") String email,
            @PathVariable("cityId") Long cityId, @PathVariable("categoryId") Long categoryId) {

        log.info("Calling addCategoryIntoCity");
        List<CategoryResponseDto> categoryResponseList = firstAppService.addCategoryIntoCity(email, cityId, categoryId);
        return new ResponseEntity<>(categoryResponseList, HttpStatus.OK);
    }

    @PostMapping(path = "/updateCategoryIntoCity/{cityId}/{categoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CategoryResponseDto>> updateCategoryIntoCity(@PathVariable("email") String email,
            @PathVariable("cityId") Long cityId, @PathVariable("categoryId") Long categoryId) {

        log.info("Calling updateCategoryIntoCity");
        List<CategoryResponseDto> categoryResponseList = firstAppService.updateCategoryIntoCity(email, cityId,
                categoryId);
        return new ResponseEntity<>(categoryResponseList, HttpStatus.OK);
    }

    @DeleteMapping(path = "/deleteCategoryFromCity/{cityId}/{categoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CategoryResponseDto>> deleteCategoryFromCity(@PathVariable("email") String email,
            @PathVariable("cityId") Long cityId, @PathVariable("categoryId") Long categoryId) {

        log.info("Calling deleteCategoryFromCity");
        List<CategoryResponseDto> categoryResponseList = firstAppService.deleteCategoryFromCity(email, cityId,
                categoryId);
        return new ResponseEntity<>(categoryResponseList, HttpStatus.OK);
    }

    @GetMapping(path = "/getShopListFromCity/{cityId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ShopResponseDto>> getShopListFromCity(@PathVariable("email") String email,
            @PathVariable("cityId") Long cityId) {

        log.info("Calling getShopListFromCity");
        List<ShopResponseDto> shopResponseList = firstAppService.getShopListFromCity(email, cityId);
        return new ResponseEntity<>(shopResponseList, HttpStatus.OK);
    }

    @PostMapping(path = "/addShopIntoCity/{cityId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ShopResponseDto>> addShopIntoCity(@PathVariable("email") String email,
            @PathVariable("cityId") Long cityId, @RequestBody ShopRequestDto shopRequest) {

        log.info("Calling addShopIntoCity");
        List<ShopResponseDto> shopResponseList = firstAppService.addShopIntoCity(email, cityId, shopRequest);
        return new ResponseEntity<>(shopResponseList, HttpStatus.OK);
    }

    @PostMapping(path = "/updateShopIntoCity/{cityId}/{shopId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ShopResponseDto>> updateShopIntoCity(@PathVariable("email") String email,
            @PathVariable("cityId") Long cityId, @PathVariable("shopId") Long shopId,
            @RequestBody ShopRequestDto shopRequest) {

        log.info("Calling updateShopIntoCity");
        List<ShopResponseDto> shopResponseList = firstAppService.updateShopIntoCity(email, cityId, shopId, shopRequest);
        return new ResponseEntity<>(shopResponseList, HttpStatus.OK);
    }

    @DeleteMapping(path = "/deleteShopFromCity/{cityId}/{shopId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ShopResponseDto>> deleteShopFromCity(@PathVariable("email") String email,
            @PathVariable("cityId") Long cityId, @PathVariable("shopId") Long shopId) {

        log.info("Calling deleteShopFromCity");
        List<ShopResponseDto> shopResponseList = firstAppService.deleteShopFromCity(email, cityId, shopId);
        return new ResponseEntity<>(shopResponseList, HttpStatus.OK);
    }

    @GetMapping(path = "/getShopListFromCityCategoryMapping/{cityId}/{categoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ShopResponseDto>> getShopListFromAndCityCategory(@PathVariable("email") String email,
            @PathVariable("cityId") Long cityId, @PathVariable("categoryId") Long categoryId) {

        log.info("Calling getShopListFromCityCategoryMapping");
        List<ShopResponseDto> shopResponseList = firstAppService.getShopListFromCityCategoryMapping(email, cityId,
                categoryId);
        return new ResponseEntity<>(shopResponseList, HttpStatus.OK);
    }

    @PostMapping(path = "/addShopIntoCityCategoryMapping/{cityId}/{categoryId}/{shopId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ShopResponseDto>> addShopIntoCityCategoryMapping(@PathVariable("email") String email,
            @PathVariable("cityId") Long cityId, @PathVariable("categoryId") Long categoryId,
            @PathVariable("shopId") Long shopId) {

        log.info("Calling addShopIntoCityCategoryMapping");
        List<ShopResponseDto> shopResponseList = firstAppService.addShopIntoCityCategoryMapping(email, cityId,
                categoryId, shopId);
        return new ResponseEntity<>(shopResponseList, HttpStatus.OK);
    }

    @PostMapping(path = "/updateShopIntoCityCategoryMapping/{cityId}/{categoryId}/{shopId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ShopResponseDto>> updateShopIntoCityCategoryMapping(@PathVariable("email") String email,
            @PathVariable("cityId") Long cityId, @PathVariable("categoryId") Long categoryId,
            @PathVariable("shopId") Long shopId) {

        log.info("Calling updateShopIntoCityCategoryMapping");
        List<ShopResponseDto> shopResponseList = firstAppService.updateShopIntoCityCategoryMapping(email, cityId,
                categoryId, shopId);
        return new ResponseEntity<>(shopResponseList, HttpStatus.OK);
    }

    @DeleteMapping(path = "/deleteShopFromCityCategoryMapping/{cityId}/{categoryId}/{shopId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ShopResponseDto>> deleteShopFromCityCategoryMapping(@PathVariable("email") String email,
            @PathVariable("cityId") Long cityId, @PathVariable("categoryId") Long categoryId,
            @PathVariable("shopId") Long shopId) {

        log.info("Calling deleteShopFromCityCategoryMapping");
        List<ShopResponseDto> shopResponseList = firstAppService.deleteShopFromCityCategoryMapping(email, cityId,
                categoryId, shopId);
        return new ResponseEntity<>(shopResponseList, HttpStatus.OK);
    }

    @GetMapping(path = "/getItemListFromShop/{city}/{shopId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemResponseDto>> getItemListFromShop(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId) {

        log.info("Calling getItemListFromShop");
        List<ItemResponseDto> itemResponseList = firstAppService.getItemListFromShop(email, city, shopId);
        return new ResponseEntity<>(itemResponseList, HttpStatus.OK);
    }

    @PostMapping(path = "/addItemIntoShop/{city}/{shopId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemResponseDto>> addItemIntoShop(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId,
            @RequestBody ItemRequestDto itemRequest) {

        log.info("Calling addItemIntoShop");
        List<ItemResponseDto> itemResponseList = firstAppService.addItemIntoShop(email, city, shopId, itemRequest);
        return new ResponseEntity<>(itemResponseList, HttpStatus.OK);
    }

    @PostMapping(path = "/updateItemIntoShop/{city}/{shopId}/{itemId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemResponseDto>> updateItemIntoShop(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId,
            @PathVariable("itemId") String itemId, @RequestBody ItemRequestDto itemRequest) {

        log.info("Calling updateItemIntoShop");
        List<ItemResponseDto> itemResponseList = firstAppService.updateItemIntoShop(email, city, shopId, itemId,
                itemRequest);
        return new ResponseEntity<>(itemResponseList, HttpStatus.OK);
    }

    @DeleteMapping(path = "/deleteItemFromShop/{city}/{shopId}/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemResponseDto>> deleteItemFromShop(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId,
            @PathVariable("itemId") String itemId) {

        log.info("Calling deleteItemFromShop");
        List<ItemResponseDto> itemResponseList = firstAppService.deleteItemFromShop(email, city, shopId, itemId);
        return new ResponseEntity<>(itemResponseList, HttpStatus.OK);
    }

    @GetMapping(path = "/getCartItemList/{city}/{shopId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemResponseDto>> getCartItemList(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId,
            @RequestBody CartRequestDto cartRequestDto) {

        log.info("Calling getCartItemList");
        List<ItemResponseDto> itemResponseList = firstAppService.getCartItemList(email, city, shopId, cartRequestDto);
        return new ResponseEntity<>(itemResponseList, HttpStatus.OK);
    }

    @PostMapping(path = "/addItemIntoCart/{city}/{shopId}/{itemId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemResponseDto>> addItemIntoCart(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId,
            @PathVariable("itemId") String itemId, @RequestBody CartRequestDto cartRequestDto) {

        log.info("Calling addItemIntoCart");
        List<ItemResponseDto> itemResponseList = firstAppService.addItemIntoCart(email, city, shopId, itemId,
                cartRequestDto);
        return new ResponseEntity<>(itemResponseList, HttpStatus.OK);
    }

    @PostMapping(path = "/updateItemIntoCart/{city}/{shopId}/{itemId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemResponseDto>> updateItemIntoCart(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId,
            @PathVariable("itemId") String itemId, @RequestBody CartRequestDto cartRequestDto) {

        log.info("Calling updateItemIntoCart");
        List<ItemResponseDto> itemResponseList = firstAppService.updateItemIntoCart(email, city, shopId, itemId,
                cartRequestDto);
        return new ResponseEntity<>(itemResponseList, HttpStatus.OK);
    }

    @DeleteMapping(path = "/deleteItemFromCart/{city}/{shopId}/{itemId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemResponseDto>> deleteItemFromCart(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId,
            @PathVariable("itemId") String itemId, @RequestBody CartRequestDto cartRequestDto) {

        log.info("Calling deleteItemFromCart");
        List<ItemResponseDto> itemResponseList = firstAppService.deleteItemFromCart(email, city, shopId, itemId,
                cartRequestDto);
        return new ResponseEntity<>(itemResponseList, HttpStatus.OK);
    }

    @PostMapping(path = "/checkOut", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CheckOutResponseDto> getCheckOut(@PathVariable("email") String email) {

        log.info("Calling checkOut");
        CheckOutResponseDto checkOutResponse = firstAppService.getCheckOut(email);
        return new ResponseEntity<>(checkOutResponse, HttpStatus.OK);
    }

}

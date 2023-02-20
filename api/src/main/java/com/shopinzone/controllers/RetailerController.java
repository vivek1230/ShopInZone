package com.shopinzone.controllers;

import com.shopinzone.dto.*;
import com.shopinzone.enums.SignInStatusCode;
import com.shopinzone.enums.SignUpStatusCode;
import com.shopinzone.services.RetailerService;
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

@RequestMapping("/retailer/v1/{email}")
@Slf4j
@RestController
@Validated
@AllArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Retailer APIs")
public class RetailerController {

    RetailerService retailerService;

    @GetMapping(path = "/signIn/{password}", produces = "application/json")
    public ResponseEntity<SignInStatusCode> signIn(@PathVariable("email") String email,
            @PathVariable("password") String password) {

        SignInStatusCode signInStatusCode = retailerService.signIn(email, password);
        return new ResponseEntity<>(signInStatusCode, HttpStatus.OK);
    }

    @PostMapping(path = "/signUp", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SignUpStatusCode> signUp(@RequestBody ProfileDto profile) {

        SignUpStatusCode signUpStatusCode = retailerService.signUp(profile);
        return new ResponseEntity<>(signUpStatusCode, HttpStatus.OK);
    }

    @GetMapping(path = "/getProfile", produces = "application/json")
    public ResponseEntity<ProfileDto> getProfile(@PathVariable("email") String email) {

        ProfileDto profile = retailerService.getProfile(email);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @PostMapping(path = "/updateProfile", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ProfileDto> updateProfile(@PathVariable("email") String email,
            @RequestBody ProfileDto profile) {

        profile = retailerService.updateProfile(email, profile);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @PostMapping(path = "/home", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<CityDto>> getCityList(@PathVariable("email") String email) {

        List<CityDto> cityList = retailerService.getCityList(email);
        return new ResponseEntity<>(cityList, HttpStatus.OK);
    }

    @PostMapping(path = "/getCategoryListFromCity/{city}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<CategoryDto>> getCategoryListFromCity(@PathVariable("email") String email,
            @PathVariable("city") String city) {

        List<CategoryDto> categoryList = retailerService.getCategoryListFromCity(email, city);
        return new ResponseEntity<>(categoryList, HttpStatus.OK);
    }

    @PostMapping(path = "/getShopListFromCategory/{city}/{categoryId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ShopDto>> getShopListFromCategory(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("categoryId") String categoryId) {

        List<ShopDto> shopList = retailerService.getShopListFromCategory(email, city, categoryId);
        return new ResponseEntity<>(shopList, HttpStatus.OK);
    }

    @PostMapping(path = "/addShopIntoCategory/{city}/{categoryId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ShopDto>> addShopIntoCategory(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("categoryId") String categoryId,
            @RequestBody ShopDto shop) {

        List<ShopDto> shopList = retailerService.addShopIntoCategory(email, city, categoryId, shop);
        return new ResponseEntity<>(shopList, HttpStatus.OK);
    }

    @PostMapping(path = "/updateShopIntoCategory/{city}/{categoryId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ShopDto>> updateShopIntoCategory(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("categoryId") String categoryId,
            @PathVariable("shopId") String shopId, @RequestBody ShopDto shop) {

        List<ShopDto> shopList = retailerService.updateShopIntoCategory(email, city, categoryId, shopId, shop);
        return new ResponseEntity<>(shopList, HttpStatus.OK);
    }

    @PostMapping(path = "/getItemListFromShop/{city}/{shopId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ItemDto>> getItemListFromShop(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId) {

        List<ItemDto> itemList = retailerService.getItemListFromShop(email, city, shopId);
        return new ResponseEntity<>(itemList, HttpStatus.OK);
    }

    @PostMapping(path = "/addItemIntoShop/{city}/{shopId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ItemDto>> addItemIntoShop(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId, @RequestBody ItemDto item) {

        List<ItemDto> itemList = retailerService.addItemIntoShop(email, city, shopId, item);
        return new ResponseEntity<>(itemList, HttpStatus.OK);
    }

    @PostMapping(path = "/updateItemIntoShop/{city}/{shopId}/{itemId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ItemDto>> updateItemIntoShop(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId,
            @PathVariable("itemId") String itemId, @RequestBody ItemDto item) {

        List<ItemDto> itemList = retailerService.updateItemIntoShop(email, city, shopId, itemId, item);
        return new ResponseEntity<>(itemList, HttpStatus.OK);
    }

    @PostMapping(path = "/deleteItemFromShop/{city}/{shopId}/{itemId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ItemDto>> deleteItemFromShop(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId,
            @PathVariable("itemId") String itemId) {

        List<ItemDto> itemList = retailerService.deleteItemFromShop(email, city, shopId, itemId);
        return new ResponseEntity<>(itemList, HttpStatus.OK);
    }

}

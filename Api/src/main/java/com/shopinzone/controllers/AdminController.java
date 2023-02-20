package com.shopinzone.controllers;

import com.shopinzone.dto.*;
import com.shopinzone.enums.SignInStatusCode;
import com.shopinzone.enums.SignUpStatusCode;
import com.shopinzone.services.AdminService;
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

@RequestMapping("/admin/v1/{email}")
@Slf4j
@RestController
@Validated
@AllArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Admin APIs")
public class AdminController {

    AdminService adminService;

    @GetMapping(path = "/signIn/{password}", produces = "application/json")
    public ResponseEntity<SignInStatusCode> signIn(@PathVariable("email") String email,
            @PathVariable("password") String password) {

        SignInStatusCode signInStatusCode = adminService.signIn(email, password);
        return new ResponseEntity<>(signInStatusCode, HttpStatus.OK);
    }

    @PostMapping(path = "/signUp", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SignUpStatusCode> signUp(@RequestBody ProfileDto profile) {

        SignUpStatusCode signUpStatusCode = adminService.signUp(profile);
        return new ResponseEntity<>(signUpStatusCode, HttpStatus.OK);
    }

    @GetMapping(path = "/getProfile", produces = "application/json")
    public ResponseEntity<ProfileDto> getProfile(@PathVariable("email") String email) {

        ProfileDto profile = adminService.getProfile(email);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @PostMapping(path = "/updateProfile", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ProfileDto> updateProfile(@PathVariable("email") String email,
            @RequestBody ProfileDto profile) {

        profile = adminService.updateProfile(email, profile);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @PostMapping(path = "/home", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<CityDto>> getCityList(@PathVariable("email") String email) {

        List<CityDto> cityList = adminService.getCityList(email);
        return new ResponseEntity<>(cityList, HttpStatus.OK);
    }

    @PostMapping(path = "/addCity", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<CityDto>> addCity(@PathVariable("email") String email, @RequestBody CityDto city) {

        List<CityDto> cityList = adminService.addCity(email, city);
        return new ResponseEntity<>(cityList, HttpStatus.OK);
    }

    @PostMapping(path = "/updateCity/{city}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<CityDto>> updateCity(@PathVariable("email") String email,
            @PathVariable("city") String cityId, @RequestBody CityDto city) {

        List<CityDto> cityList = adminService.updateCity(email, cityId, city);
        return new ResponseEntity<>(cityList, HttpStatus.OK);
    }

    @PostMapping(path = "/deleteCity/{city}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<CityDto>> deleteCity(@PathVariable("email") String email,
            @PathVariable("city") String cityId) {

        List<CityDto> cityList = adminService.deleteCity(email, cityId);
        return new ResponseEntity<>(cityList, HttpStatus.OK);
    }

    @PostMapping(path = "/getCategoryListFromCity/{city}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<CategoryDto>> getCategoryListFromCity(@PathVariable("email") String email,
            @PathVariable("city") String city) {

        List<CategoryDto> categoryList = adminService.getCategoryListFromCity(email, city);
        return new ResponseEntity<>(categoryList, HttpStatus.OK);
    }

    @PostMapping(path = "/addCategoryIntoCity/{city}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<CategoryDto>> addCategoryIntoCity(@PathVariable("email") String email,
            @PathVariable("city") String city, @RequestBody CategoryDto category) {

        List<CategoryDto> categoryList = adminService.addCategoryIntoCity(email, city, category);
        return new ResponseEntity<>(categoryList, HttpStatus.OK);
    }

    @PostMapping(path = "/updateCategoryIntoCity/{city}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<CategoryDto>> updateCategoryIntoCity(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("categoryId") String categoryId,
            @RequestBody CategoryDto category) {

        List<CategoryDto> categoryList = adminService.updateCategoryIntoCity(email, city, categoryId, category);
        return new ResponseEntity<>(categoryList, HttpStatus.OK);
    }

    @PostMapping(path = "/deleteCategoryFromCity/{city}/{categoryId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<CategoryDto>> deleteCategoryFromCity(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("categoryId") String categoryId) {

        List<CategoryDto> categoryList = adminService.deleteCategoryFromCity(email, city, categoryId);
        return new ResponseEntity<>(categoryList, HttpStatus.OK);
    }

    @PostMapping(path = "/getShopListFromCategory/{city}/{categoryId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ShopDto>> getShopListFromCategory(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("categoryId") String categoryId) {

        List<ShopDto> shopList = adminService.getShopListFromCategory(email, city, categoryId);
        return new ResponseEntity<>(shopList, HttpStatus.OK);
    }

    @PostMapping(path = "/addShopIntoCategory/{city}/{categoryId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ShopDto>> addShopIntoCategory(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("categoryId") String categoryId,
            @RequestBody ShopDto shop) {

        List<ShopDto> shopList = adminService.addShopIntoCategory(email, city, categoryId, shop);
        return new ResponseEntity<>(shopList, HttpStatus.OK);
    }

    @PostMapping(path = "/updateShopIntoCategory/{city}/{categoryId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ShopDto>> updateShopIntoCategory(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("categoryId") String categoryId,
            @PathVariable("shopId") String shopId, @RequestBody ShopDto shop) {

        List<ShopDto> shopList = adminService.updateShopIntoCategory(email, city, categoryId, shopId, shop);
        return new ResponseEntity<>(shopList, HttpStatus.OK);
    }

    @PostMapping(path = "/deleteShopFromCategory/{city}/{categoryId}/{shopId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ShopDto>> deleteShopFromCategory(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("categoryId") String categoryId,
            @PathVariable("shopId") String shopId) {

        List<ShopDto> shopList = adminService.deleteShopFromCategory(email, city, categoryId, shopId);
        return new ResponseEntity<>(shopList, HttpStatus.OK);
    }

    @PostMapping(path = "/getItemListFromShop/{city}/{shopId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ItemDto>> getItemListFromShop(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId) {

        List<ItemDto> itemList = adminService.getItemListFromShop(email, city, shopId);
        return new ResponseEntity<>(itemList, HttpStatus.OK);
    }

    @PostMapping(path = "/addItemIntoShop/{city}/{shopId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ItemDto>> addItemIntoShop(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId, @RequestBody ItemDto item) {

        List<ItemDto> itemList = adminService.addItemIntoShop(email, city, shopId, item);
        return new ResponseEntity<>(itemList, HttpStatus.OK);
    }

    @PostMapping(path = "/updateItemIntoShop/{city}/{shopId}/{itemId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ItemDto>> updateItemIntoShop(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId,
            @PathVariable("itemId") String itemId, @RequestBody ItemDto item) {

        List<ItemDto> itemList = adminService.updateItemIntoShop(email, city, shopId, itemId, item);
        return new ResponseEntity<>(itemList, HttpStatus.OK);
    }

    @PostMapping(path = "/deleteItemFromShop/{city}/{shopId}/{itemId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<ItemDto>> deleteItemFromShop(@PathVariable("email") String email,
            @PathVariable("city") String city, @PathVariable("shopId") String shopId,
            @PathVariable("itemId") String itemId) {

        List<ItemDto> itemList = adminService.deleteItemFromShop(email, city, shopId, itemId);
        return new ResponseEntity<>(itemList, HttpStatus.OK);
    }

}

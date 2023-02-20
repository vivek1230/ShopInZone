package com.shopinzone.services;

import com.shopinzone.dto.*;
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
public class RetailerService {

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

    public ProfileDto updateProfile(String email, ProfileDto profileDto) {
        return null;
    }

    public List<ItemDto> deleteItemFromShop(String email, String city, String shopId, String itemId) {
        return null;
    }

    public List<ItemDto> updateItemIntoShop(String email, String city, String shopId, String itemId, ItemDto item) {
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

    public List<ShopDto> addShopIntoCategory(String email, String city, String categoryId, ShopDto shop) {
        return null;
    }

    public List<ShopDto> updateShopIntoCategory(String email, String city, String categoryId, String shopId,
            ShopDto shop) {
        return null;
    }

    public List<ItemDto> addItemIntoShop(String email, String city, String shopId, ItemDto item) {
        return null;
    }

}

package com.shopinzone.mapping;

import com.shopinzone.dto.requestDto.CategoryRequestDto;
import com.shopinzone.dto.requestDto.CityRequestDto;
import com.shopinzone.dto.requestDto.ProfileRequestDto;
import com.shopinzone.dto.requestDto.ShopRequestDto;
import com.shopinzone.entity.*;

import java.time.OffsetDateTime;

public class EntityToEntityMapping {

    public static CityEntity updateCityEntity(CityRequestDto cityRequest, CityEntity cityEntity) {
        cityEntity.setCityName(cityRequest.getCityName());
        cityEntity.setDescription(cityRequest.getDescription());
        cityEntity.setLastUpdated(OffsetDateTime.now());
        return cityEntity;
    }

    public static CategoryEntity updateCategoryEntity(CategoryRequestDto categoryRequest,
            CategoryEntity categoryEntity) {
        categoryEntity.setCategoryName(categoryRequest.getCategoryName());
        categoryEntity.setDescription(categoryRequest.getDescription());
        categoryEntity.setLastUpdated(OffsetDateTime.now());
        return categoryEntity;
    }

    public static ShopEntity updateShopEntity(ShopRequestDto shopRequest, ShopEntity shopEntity) {
        shopEntity.setShopName(shopRequest.getShopName());
        shopEntity.setDescription(shopRequest.getDescription());
        shopEntity.setAddressId(shopRequest.getAddressId());
        shopEntity.setImageUrl(shopRequest.getImageUrl());
        shopEntity.setRating(shopRequest.getRating());
        shopEntity.setLastUpdated(OffsetDateTime.now());
        return shopEntity;
    }

    public static CityCategoryMapping updateCityCategoryMapping(CityCategoryMapping cityCategoryMapping) {
        cityCategoryMapping.setLastUpdated(OffsetDateTime.now());
        return cityCategoryMapping;
    }

    public static CityCategoryShopMapping updateCityCategoryShopMapping(
            CityCategoryShopMapping cityCategoryShopMapping) {
        cityCategoryShopMapping.setLastUpdated(OffsetDateTime.now());
        return cityCategoryShopMapping;
    }

    public static ProfileEntity updateProfileEntity(String email, ProfileRequestDto profileRequest, Long profileId) {
        ProfileEntity profileEntity = ProfileEntity.builder().email(email).mobile(profileRequest.getMobile())
                .name(profileRequest.getName()).gender(profileRequest.getGender()).city(profileRequest.getCity())
                .pinCode(profileRequest.getPinCode()).created(OffsetDateTime.now()).lastUpdated(OffsetDateTime.now())
                .build();
        profileEntity.setProfileId(profileId);
        return profileEntity;
    }
}

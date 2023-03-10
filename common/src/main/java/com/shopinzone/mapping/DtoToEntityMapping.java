package com.shopinzone.mapping;

import com.shopinzone.dto.requestDto.*;
import com.shopinzone.entity.*;
import com.shopinzone.enums.UserRoleType;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class DtoToEntityMapping {

    public static UserEntity getUserEntity(UserRequestDto userRequest) {
        return UserEntity.builder().userRoleType(UserRoleType.CUSTOMER).email(userRequest.getEmail())
                .mobile(userRequest.getMobile()).password(userRequest.getPassword()).created(OffsetDateTime.now())
                .lastUpdated(OffsetDateTime.now()).build();
    }

    public static ProfileEntity getProfileEntity(String email, ProfileRequestDto profileRequest) {
        return ProfileEntity.builder().email(email).mobile(profileRequest.getMobile()).name(profileRequest.getName())
                .gender(profileRequest.getGender()).city(profileRequest.getCity()).pinCode(profileRequest.getPinCode())
                .created(OffsetDateTime.now()).lastUpdated(OffsetDateTime.now()).build();
    }

    public static AddressEntity getAddressEntity(String email, AddressRequestDto addressRequest) {
        return AddressEntity.builder().email(email).name(addressRequest.getName()).mobile(addressRequest.getMobile())
                .city(addressRequest.getCity()).pinCode(addressRequest.getPinCode())
                .landMark(addressRequest.getLandMark()).addressType(addressRequest.getAddressType())
                .state(addressRequest.getState()).addressText(addressRequest.getAddressText())
                .created(OffsetDateTime.now()).lastUpdated(OffsetDateTime.now()).build();
    }

    public static CityEntity getCityEntity(CityRequestDto cityRequest) {
        return CityEntity.builder().cityName(cityRequest.getCityName()).description(cityRequest.getDescription())
                .cityImageUrl(cityRequest.getCityImageUrl()).created(OffsetDateTime.now())
                .lastUpdated(OffsetDateTime.now()).build();
    }

    public static List<CityImageUrlMapping> getCityImageUrlMapping(Long cityId, List<String> cityImageUrlList) {
        List<CityImageUrlMapping> cityImageUrlMappingList = new ArrayList<>();
        cityImageUrlList.forEach(cityImageUrl -> {
            CityImageUrlMapping cityImageUrlMapping = CityImageUrlMapping.builder().cityId(cityId)
                    .imageUrl(cityImageUrl).created(OffsetDateTime.now()).lastUpdated(OffsetDateTime.now()).build();
            cityImageUrlMappingList.add(cityImageUrlMapping);
        });
        return cityImageUrlMappingList;
    }

    public static List<CityPinCodeMapping> getCityPinCodeMapping(Long cityId, List<String> cityPinCodeList) {
        List<CityPinCodeMapping> cityPinCodeMappingList = new ArrayList<>();
        cityPinCodeList.forEach(cityPinCode -> {
            CityPinCodeMapping cityPinCodeMapping = CityPinCodeMapping.builder().cityId(cityId).pinCode(cityPinCode)
                    .created(OffsetDateTime.now()).lastUpdated(OffsetDateTime.now()).build();
            cityPinCodeMappingList.add(cityPinCodeMapping);
        });
        return cityPinCodeMappingList;
    }

    public static List<CityNameMapping> getCityNameMapping(Long cityId, List<String> cityNameList) {
        List<CityNameMapping> cityNameMappingList = new ArrayList<>();
        cityNameList.forEach(cityName -> {
            CityNameMapping cityNameMapping = CityNameMapping.builder().cityId(cityId).cityName(cityName)
                    .created(OffsetDateTime.now()).lastUpdated(OffsetDateTime.now()).build();
            cityNameMappingList.add(cityNameMapping);
        });
        return cityNameMappingList;
    }

    public static CategoryEntity getCategoryEntity(CategoryRequestDto categoryRequest) {
        return CategoryEntity.builder().categoryName(categoryRequest.getCategoryName())
                .description(categoryRequest.getDescription()).created(OffsetDateTime.now())
                .lastUpdated(OffsetDateTime.now()).build();
    }

    public static CityCategoryMapping getCityCategoryMapping(Long cityId, Long categoryId) {
        return CityCategoryMapping.builder().cityId(cityId).categoryId(categoryId).created(OffsetDateTime.now())
                .lastUpdated(OffsetDateTime.now()).build();
    }

    public static ShopEntity getShopEntity(Long cityId, ShopRequestDto shopRequest) {
        return ShopEntity.builder().cityId(cityId).shopName(shopRequest.getShopName())
                .description(shopRequest.getDescription()).rating(shopRequest.getRating())
                .imageUrl(shopRequest.getImageUrl()).addressId(shopRequest.getAddressId()).created(OffsetDateTime.now())
                .lastUpdated(OffsetDateTime.now()).build();
    }

    public static CityCategoryShopMapping getCityCategoryShopMapping(Long cityId, Long categoryId, Long shopId) {
        return CityCategoryShopMapping.builder().cityId(cityId).categoryId(categoryId).shopId(shopId)
                .created(OffsetDateTime.now()).lastUpdated(OffsetDateTime.now()).build();
    }
}

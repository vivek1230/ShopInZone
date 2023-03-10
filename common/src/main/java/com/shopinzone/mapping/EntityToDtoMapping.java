package com.shopinzone.mapping;

import com.shopinzone.dto.responseDto.*;
import com.shopinzone.entity.*;

import java.util.ArrayList;
import java.util.List;

public class EntityToDtoMapping {

    public static ProfileResponseDto getProfileResponse(ProfileEntity profileEntity) {
        return ProfileResponseDto.builder().profileId(profileEntity.getProfileId()).email(profileEntity.getEmail())
                .mobile(profileEntity.getMobile()).name(profileEntity.getName()).city(profileEntity.getCity())
                .pinCode(profileEntity.getPinCode()).gender(profileEntity.getGender()).build();
    }

    public static AddressResponseDto getAddressResponse(AddressEntity addressEntity) {
        return AddressResponseDto.builder().addressId(addressEntity.getAddressId()).name(addressEntity.getName())
                .mobile(addressEntity.getMobile()).addressType(addressEntity.getAddressType())
                .city(addressEntity.getCity()).pinCode(addressEntity.getPinCode()).landMark(addressEntity.getLandMark())
                .state(addressEntity.getState()).addressText(addressEntity.getAddressText()).build();
    }

    public static List<AddressResponseDto> getAddressResponseList(List<AddressEntity> addressEntityList) {
        List<AddressResponseDto> addressResponseList = new ArrayList<>();
        addressEntityList.forEach(addressEntity -> {
            addressResponseList.add(getAddressResponse(addressEntity));
        });
        return addressResponseList;
    }

    public static CityResponseDto getCityResponse(CityEntity cityEntity) {
        return CityResponseDto.builder().cityId(cityEntity.getCityId()).cityName(cityEntity.getCityName())
                .description(cityEntity.getDescription()).cityImageUrl(cityEntity.getCityImageUrl()).build();
    }

    public static List<CityResponseDto> getCityResponseList(List<CityEntity> cityEntityList) {
        List<CityResponseDto> cityResponseList = new ArrayList<>();
        cityEntityList.forEach(cityEntity -> {
            cityResponseList.add(getCityResponse(cityEntity));
        });
        return cityResponseList;
    }

    public static List<CategoryResponseDto> getCategoryResponseList(List<CategoryEntity> categoryEntityList) {
        List<CategoryResponseDto> categoryResponseList = new ArrayList<>();
        categoryEntityList.forEach(categoryEntity -> {
            categoryResponseList.add(getCategoryResponse(categoryEntity));
        });
        return categoryResponseList;
    }

    private static CategoryResponseDto getCategoryResponse(CategoryEntity categoryEntity) {
        return CategoryResponseDto.builder().categoryId(categoryEntity.getCategoryId())
                .categoryName(categoryEntity.getCategoryName()).description(categoryEntity.getDescription()).build();
    }

    public static CityResponseDto getCityResponse(CityEntity cityEntity, String cityImageUrl) {
        return CityResponseDto.builder().cityId(cityEntity.getCityId()).cityName(cityEntity.getCityName())
                .description(cityEntity.getDescription()).cityImageUrl(cityImageUrl).build();
    }

    public static List<ShopResponseDto> getShopResponseList(List<ShopEntity> shopEntityList) {
        List<ShopResponseDto> shopResponseList = new ArrayList<>();
        shopEntityList.forEach(shopEntity -> {
            shopResponseList.add(getShopResponse(shopEntity));
        });
        return shopResponseList;
    }

    private static ShopResponseDto getShopResponse(ShopEntity shopEntity) {
        return ShopResponseDto.builder().cityId(shopEntity.getCityId()).shopId(shopEntity.getShopId())
                .shopName(shopEntity.getShopName()).description(shopEntity.getDescription())
                .addressId(shopEntity.getAddressId()).rating(shopEntity.getRating()).imageUrl(shopEntity.getImageUrl())
                .build();
    }
}

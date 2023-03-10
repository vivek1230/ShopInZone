package com.shopinzone.services;

import com.shopinzone.dto.requestDto.*;
import com.shopinzone.dto.responseDto.*;
import com.shopinzone.entity.*;
import com.shopinzone.enums.SignInStatusCode;
import com.shopinzone.enums.SignUpStatusCode;
import com.shopinzone.mapping.DtoToEntityMapping;
import com.shopinzone.mapping.EntityToDtoMapping;
import com.shopinzone.mapping.EntityToEntityMapping;
import com.shopinzone.repository.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class FirstAppService {

    UserEntityRepository userEntityRepository;
    ProfileEntityRepository profileEntityRepository;
    AddressEntityRepository addressEntityRepository;
    CityEntityRepository cityEntityRepository;
    CityImageUrlMappingRepository cityImageUrlMappingRepository;
    CityPinCodeMappingRepository cityPinCodeMappingRepository;
    CityNameMappingRepository cityNameMappingRepository;
    CategoryEntityRepository categoryEntityRepository;
    CityCategoryMappingRepository cityCategoryMappingRepository;
    ShopEntityRepository shopEntityRepository;
    CityCategoryShopMappingRepository cityCategoryShopMappingRepository;

    public SignInStatusCode signIn(String email, String password) {
        log.info("email : {}, password : {}", email, password);
        Optional<UserEntity> userEntityOptional;
        userEntityOptional = userEntityRepository.findByEmail(email);
        if (userEntityOptional.isEmpty())
            return SignInStatusCode.NOT_REGISTERED_WITH_EMAIL;
        if (!StringUtils.equals(userEntityOptional.get().getPassword(), password))
            return SignInStatusCode.AUTHENTICATION_FAILED;
        return SignInStatusCode.AUTHENTICATION_DONE;
    }

    private void authenticateUserWithEmail(String email) {
        log.info("email : {}", email);
        Optional<UserEntity> userEntityOptional = userEntityRepository.findByEmail(email);
        if (userEntityOptional.isEmpty())
            throw new RuntimeException("Exception: " + SignInStatusCode.NOT_REGISTERED_WITH_EMAIL);
    }

    public SignUpStatusCode signUp(UserRequestDto userRequest) {

        log.info("UserRequest : {}", userRequest);
        Optional<UserEntity> userEntityOptional;
        userEntityOptional = userEntityRepository.findByEmail(userRequest.getEmail());
        if (userEntityOptional.isPresent())
            return SignUpStatusCode.DUPLICATE_EMAIL;

        userEntityOptional = userEntityRepository.findByMobile(userRequest.getMobile());
        if (userEntityOptional.isPresent())
            return SignUpStatusCode.DUPLICATE_MOBILE;

        UserEntity userEntity = DtoToEntityMapping.getUserEntity(userRequest);
        userEntity = userEntityRepository.save(userEntity);
        log.info("UserEntity : {}", userEntity);
        return SignUpStatusCode.SIGN_UP_DONE;
    }

    public ProfileResponseDto getProfile(String email) {

        authenticateUserWithEmail(email);
        Optional<ProfileEntity> profileEntity = profileEntityRepository.findByEmail(email);
        if (profileEntity.isEmpty())
            return null;
        ProfileResponseDto profileResponse = EntityToDtoMapping.getProfileResponse(profileEntity.get());
        log.info("ProfileResponse : {}", profileResponse);
        return profileResponse;
    }

    public ProfileResponseDto addProfile(String email, ProfileRequestDto profileRequest) {

        authenticateUserWithEmail(email);
        log.info("ProfileRequest : {}", profileRequest);
        ProfileEntity profileEntity = DtoToEntityMapping.getProfileEntity(email, profileRequest);
        profileEntityRepository.save(profileEntity);
        return getProfile(email);
    }

    public ProfileResponseDto updateProfile(String email, ProfileRequestDto profileRequest) {

        authenticateUserWithEmail(email);
        log.info("ProfileRequest : {}", profileRequest);
        ProfileEntity profileEntity;
        Optional<ProfileEntity> profileEntityOptional = profileEntityRepository.findByEmail(email);
        if (profileEntityOptional.isPresent()) {
            profileEntity = EntityToEntityMapping.updateProfileEntity(email, profileRequest,
                    profileEntityOptional.get().getProfileId());
            log.info("Updating ProfileEntity for email: {}, ProfileEntity: {}", email, profileEntity);
        } else {
            profileEntity = DtoToEntityMapping.getProfileEntity(email, profileRequest);
            log.info("Saving ProfileEntity for email: {}, ProfileEntity: {}", email, profileEntity);
        }
        profileEntityRepository.save(profileEntity);
        return getProfile(email);
    }

    public List<AddressResponseDto> getAddressList(String email) {

        authenticateUserWithEmail(email);
        List<AddressEntity> addressEntityList = addressEntityRepository.findByEmail(email);
        log.info("AddressEntityList for email: {}, AddressEntityList: {}", email, addressEntityList);
        return EntityToDtoMapping.getAddressResponseList(addressEntityList);
    }

    public List<AddressResponseDto> addAddress(String email, AddressRequestDto addressRequest) {

        authenticateUserWithEmail(email);
        log.info("AddressRequest : {}", addressRequest);
        AddressEntity addressEntity = DtoToEntityMapping.getAddressEntity(email, addressRequest);
        addressEntity = addressEntityRepository.save(addressEntity);
        log.info("AddressEntity : {}", addressEntity);
        return getAddressList(email);
    }

    public List<AddressResponseDto> deleteAddress(String email, Long addressId) {
        authenticateUserWithEmail(email);
        Optional<AddressEntity> addressEntityOptional = addressEntityRepository.findByAddressIdAndEmail(addressId,
                email);
        if (addressEntityOptional.isPresent()) {
            log.info("Deleting AddressEntity for email: {} and addressId: {}, AddressEntity: {}", email, addressId,
                    addressEntityOptional);
            addressEntityRepository.deleteByAddressIdAndEmail(addressId, email);
        }
        return getAddressList(email);
    }

    public List<OrderResponseDto> getOrderList(String email) {
        return null;
    }

    public List<PaymentOptionResponseDto> getPaymentOptionList(String email) {
        return null;
    }

    public CheckOutResponseDto getCheckOut(String email) {
        return null;
    }

    public List<PaymentOptionResponseDto> addPaymentOption(String email, PaymentOptionRequestDto paymentOption) {
        return null;
    }

    public List<PaymentOptionResponseDto> deletePaymentOption(String email, String paymentOptionId) {
        return null;
    }

    public List<ItemResponseDto> deleteItemFromShop(String email, String city, String shopId, String itemId) {
        return null;
    }

    public List<ItemResponseDto> getCartItemList(String email, String city, String shopId,
            CartRequestDto cartRequestDto) {
        return null;
    }

    public List<ItemResponseDto> deleteItemFromCart(String email, String city, String shopId, String itemId,
            CartRequestDto cartRequestDto) {
        return null;
    }

    public List<ItemResponseDto> updateItemIntoShop(String email, String city, String shopId, String itemId,
            ItemRequestDto item) {
        return null;
    }

    public List<CityResponseDto> addCity(String email, CityRequestDto cityRequest) {

        authenticateUserWithEmail(email);
        log.info("CityRequest : {}", cityRequest);
        CityEntity cityEntity = DtoToEntityMapping.getCityEntity(cityRequest);
        cityEntity = cityEntityRepository.save(cityEntity);
        log.info("Added CityEntity : {}", cityEntity);
        return getCityList(email);
    }

    public List<CityResponseDto> getCityList(String email) {

        authenticateUserWithEmail(email);
        List<CityEntity> cityEntityList = cityEntityRepository.findAll();

        List<CityResponseDto> cityResponseList = EntityToDtoMapping.getCityResponseList(cityEntityList);
        log.info("CityResponseList : {}", cityResponseList);
        return cityResponseList;
    }

    public List<CityResponseDto> updateCity(String email, Long cityId, CityRequestDto cityRequest) {

        authenticateUserWithEmail(email);
        Optional<CityEntity> cityEntityOptional = cityEntityRepository.findByCityId(cityId);
        if (cityEntityOptional.isPresent()) {
            CityEntity cityEntity = EntityToEntityMapping.updateCityEntity(cityRequest, cityEntityOptional.get());
            cityEntityRepository.save(cityEntity);
            log.info("Updated CityEntity for cityId: {}, CityEntity: {}", cityId, cityEntity);
        }
        return getCityList(email);
    }

    public List<CityResponseDto> deleteCity(String email, Long cityId) {

        authenticateUserWithEmail(email);
        Optional<CityEntity> cityEntityOptional = cityEntityRepository.findByCityId(cityId);
        if (cityEntityOptional.isPresent()) {
            log.info("Deleting CityEntity for cityId: {}, CityEntity: {}", cityId, cityEntityOptional.get());
            cityEntityRepository.deleteByCityId(cityId);
        }
        return getCityList(email);
    }

    public List<CategoryResponseDto> getCategoryList(String email) {
        authenticateUserWithEmail(email);
        List<CategoryEntity> categoryEntityList = categoryEntityRepository.findAll();
        List<CategoryResponseDto> categoryResponseList = EntityToDtoMapping.getCategoryResponseList(categoryEntityList);
        log.info("CategoryResponseList : {}", categoryResponseList);
        return categoryResponseList;
    }

    public List<CategoryResponseDto> addCategory(String email, CategoryRequestDto categoryRequest) {

        authenticateUserWithEmail(email);
        log.info("CategoryRequest : {}", categoryRequest);
        CategoryEntity categoryEntity = DtoToEntityMapping.getCategoryEntity(categoryRequest);
        categoryEntity = categoryEntityRepository.save(categoryEntity);
        log.info("Added CategoryEntity: {}", categoryEntity);
        return getCategoryList(email);
    }

    public List<CategoryResponseDto> updateCategory(String email, Long categoryId, CategoryRequestDto categoryRequest) {

        authenticateUserWithEmail(email);
        Optional<CategoryEntity> categoryEntityOptional = categoryEntityRepository.findByCategoryId(categoryId);
        if (categoryEntityOptional.isPresent()) {
            CategoryEntity categoryEntity = EntityToEntityMapping.updateCategoryEntity(categoryRequest,
                    categoryEntityOptional.get());
            categoryEntityRepository.save(categoryEntity);
            log.info("Updated categoryEntity for categoryId: {}, categoryEntity: {}", categoryId, categoryEntity);
        }
        return getCategoryList(email);
    }

    public List<CategoryResponseDto> deleteCategory(String email, Long categoryId) {

        authenticateUserWithEmail(email);
        Optional<CategoryEntity> categoryEntityOptional = categoryEntityRepository.findByCategoryId(categoryId);
        if (categoryEntityOptional.isPresent()) {
            log.info("Deleting categoryEntity for categoryId: {}, categoryEntity: {}", categoryId,
                    categoryEntityOptional.get());
            categoryEntityRepository.deleteByCategoryId(categoryId);
        }
        return getCategoryList(email);
    }

    public List<CategoryResponseDto> getCategoryListFromCity(String email, Long cityId) {

        authenticateUserWithEmail(email);
        List<CategoryEntity> categoryEntityList = new ArrayList<>();
        List<CityCategoryMapping> cityCategoryMappingList = cityCategoryMappingRepository.findByCityId(cityId);
        if (!CollectionUtils.isEmpty(cityCategoryMappingList)) {
            cityCategoryMappingList.forEach(cityCategoryMapping -> {
                Optional<CategoryEntity> categoryEntityOptional = categoryEntityRepository
                        .findByCategoryId(cityCategoryMapping.getCategoryId());
                categoryEntityOptional.ifPresent(categoryEntityList::add);
            });
        }

        List<CategoryResponseDto> categoryResponseList = EntityToDtoMapping.getCategoryResponseList(categoryEntityList);
        log.info("CategoryResponseList : {}", categoryResponseList);
        return categoryResponseList;
    }

    public List<CategoryResponseDto> addCategoryIntoCity(String email, Long cityId, Long categoryId) {

        authenticateUserWithEmail(email);
        log.info("CityId: {}, CategoryId : {}", cityId, categoryId);
        CityCategoryMapping cityCategoryMapping = DtoToEntityMapping.getCityCategoryMapping(cityId, categoryId);
        cityCategoryMapping = cityCategoryMappingRepository.save(cityCategoryMapping);
        log.info("Added cityCategoryMapping for cityId: {}, categoryId: {}, cityCategoryMapping: {}", cityId,
                categoryId, cityCategoryMapping);
        return getCategoryListFromCity(email, cityId);
    }

    public List<CategoryResponseDto> updateCategoryIntoCity(String email, Long cityId, Long categoryId) {

        authenticateUserWithEmail(email);
        log.info("CityId: {}, CategoryId : {}", cityId, categoryId);
        Optional<CityCategoryMapping> cityCategoryMappingOptional = cityCategoryMappingRepository
                .findByCityIdAndCategoryId(cityId, categoryId);
        if (cityCategoryMappingOptional.isPresent()) {
            CityCategoryMapping cityCategoryMapping = EntityToEntityMapping
                    .updateCityCategoryMapping(cityCategoryMappingOptional.get());
            cityCategoryMappingRepository.save(cityCategoryMapping);
            log.info("Updated cityCategoryMapping for CityId: {}, CategoryId : {}, CityCategoryMapping: {}", cityId,
                    categoryId, cityCategoryMapping);
        }
        return getCategoryListFromCity(email, cityId);
    }

    public List<CategoryResponseDto> deleteCategoryFromCity(String email, Long cityId, Long categoryId) {

        authenticateUserWithEmail(email);
        log.info("CityId: {}, CategoryId : {}", cityId, categoryId);
        Optional<CityCategoryMapping> cityCategoryMappingOptional = cityCategoryMappingRepository
                .findByCityIdAndCategoryId(cityId, categoryId);
        if (cityCategoryMappingOptional.isPresent()) {
            log.info("Deleting cityCategoryMapping for CityId: {}, CategoryId : {}, CityCategoryMapping: {}", cityId,
                    categoryId, cityCategoryMappingOptional.get());
            cityCategoryMappingRepository.deleteByCityIdAndCategoryId(cityId, categoryId);
        }
        return getCategoryListFromCity(email, cityId);
    }

    public List<ItemResponseDto> getItemListFromShop(String email, String cityId, String shopId) {
        return null;
    }

    public List<ItemResponseDto> addItemIntoShop(String email, String cityId, String shopId, ItemRequestDto item) {
        return null;
    }

    public List<ItemResponseDto> addItemIntoCart(String email, String city, String shopId, String itemId,
            CartRequestDto cartRequestDto) {
        return null;
    }

    public List<ItemResponseDto> updateItemIntoCart(String email, String city, String shopId, String itemId,
            CartRequestDto cartRequestDto) {
        return null;
    }

    public List<ShopResponseDto> getShopListFromCity(String email, Long cityId) {

        authenticateUserWithEmail(email);
        List<ShopEntity> shopEntityList = shopEntityRepository.findByCityId(cityId);
        List<ShopResponseDto> shopResponseList = EntityToDtoMapping.getShopResponseList(shopEntityList);
        log.info("shopResponseList : {}", shopResponseList);
        return shopResponseList;
    }

    public List<ShopResponseDto> addShopIntoCity(String email, Long cityId, ShopRequestDto shopRequest) {

        authenticateUserWithEmail(email);
        log.info("CityId: {}, shopRequest : {}", cityId, shopRequest);
        ShopEntity shopEntity = DtoToEntityMapping.getShopEntity(cityId, shopRequest);
        shopEntity = shopEntityRepository.save(shopEntity);
        log.info("Added shopEntity for cityId: {}, shopEntity: {}", cityId, shopEntity);
        return getShopListFromCity(email, cityId);
    }

    public List<ShopResponseDto> updateShopIntoCity(String email, Long cityId, Long shopId,
            ShopRequestDto shopRequest) {

        authenticateUserWithEmail(email);
        log.info("CityId: {}, shopId : {}, shopRequest: {}", cityId, shopId, shopRequest);
        Optional<ShopEntity> shopEntityOptional = shopEntityRepository.findByCityIdAndShopId(cityId, shopId);
        if (shopEntityOptional.isPresent()) {
            ShopEntity shopEntity = EntityToEntityMapping.updateShopEntity(shopRequest, shopEntityOptional.get());
            shopEntityRepository.save(shopEntity);
            log.info("Updated shopEntity for CityId: {}, shopId : {}, shopEntity: {}", cityId, shopId, shopEntity);
        }
        return getShopListFromCity(email, cityId);
    }

    public List<ShopResponseDto> deleteShopFromCity(String email, Long cityId, Long shopId) {

        authenticateUserWithEmail(email);
        Optional<ShopEntity> shopEntityOptional = shopEntityRepository.findByCityIdAndShopId(cityId, shopId);
        if (shopEntityOptional.isPresent()) {
            log.info("Deleting shopEntity for cityId:{}, shopId: {}, shopEntity: {}", cityId, shopId,
                    shopEntityOptional.get());
            shopEntityRepository.deleteByCityIdAndShopId(cityId, shopId);
        }
        return getShopListFromCity(email, cityId);
    }

    public List<ShopResponseDto> getShopListFromCityCategoryMapping(String email, Long cityId, Long categoryId) {

        authenticateUserWithEmail(email);
        List<CityCategoryShopMapping> cityCategoryShopMappingList = cityCategoryShopMappingRepository
                .findByCityIdAndCategoryId(cityId, categoryId);
        List<ShopEntity> shopEntityList = new ArrayList<>();
        cityCategoryShopMappingList.forEach(cityCategoryShopMapping -> {
            Optional<ShopEntity> shopEntityOptional = shopEntityRepository.findByCityIdAndShopId(cityId,
                    cityCategoryShopMapping.getShopId());
            shopEntityOptional.ifPresent(shopEntityList::add);
        });
        List<ShopResponseDto> shopResponseList = EntityToDtoMapping.getShopResponseList(shopEntityList);
        log.info("shopResponseList : {}", shopResponseList);
        return shopResponseList;
    }

    public List<ShopResponseDto> addShopIntoCityCategoryMapping(String email, Long cityId, Long categoryId,
            Long shopId) {

        authenticateUserWithEmail(email);
        log.info("CityId: {}, categoryId: {}, shopId: {}", cityId, categoryId, shopId);
        CityCategoryShopMapping cityCategoryShopMapping = DtoToEntityMapping.getCityCategoryShopMapping(cityId,
                categoryId, shopId);
        cityCategoryShopMapping = cityCategoryShopMappingRepository.save(cityCategoryShopMapping);
        log.info("Added shopEntity for cityId: {}, categoryId: {}, shopId: {}, cityCategoryShopMapping: {}", cityId,
                categoryId, shopId, cityCategoryShopMapping);
        return getShopListFromCityCategoryMapping(email, cityId, categoryId);
    }

    public List<ShopResponseDto> updateShopIntoCityCategoryMapping(String email, Long cityId, Long categoryId,
            Long shopId) {

        authenticateUserWithEmail(email);
        log.info("Updating ShopIntoCityCategoryMapping for CityId: {}, CategoryId : {}, ShopId : {}", cityId,
                categoryId, shopId);
        Optional<CityCategoryShopMapping> cityCategoryShopMappingOptional = cityCategoryShopMappingRepository
                .findByCityIdAndCategoryIdAndShopId(cityId, categoryId, shopId);
        if (cityCategoryShopMappingOptional.isPresent()) {
            CityCategoryShopMapping cityCategoryShopMapping = EntityToEntityMapping
                    .updateCityCategoryShopMapping(cityCategoryShopMappingOptional.get());
            cityCategoryShopMappingRepository.save(cityCategoryShopMapping);
            log.info(
                    "Updated cityCategoryMapping for CityId: {}, CategoryId : {}, ShopId : {}, cityCategoryShopMapping: {}",
                    cityId, categoryId, shopId, cityCategoryShopMapping);
        }
        return getShopListFromCityCategoryMapping(email, cityId, categoryId);
    }

    public List<ShopResponseDto> deleteShopFromCityCategoryMapping(String email, Long cityId, Long categoryId,
            Long shopId) {

        authenticateUserWithEmail(email);
        log.info("Deleting ShopIntoCityCategoryMapping for CityId: {}, CategoryId : {}, ShopId : {}", cityId,
                categoryId, shopId);
        Optional<CityCategoryShopMapping> cityCategoryShopMappingOptional = cityCategoryShopMappingRepository
                .findByCityIdAndCategoryIdAndShopId(cityId, categoryId, shopId);
        if (cityCategoryShopMappingOptional.isPresent()) {
            log.info("Deleting cityCategoryMapping for CityId: {}, CategoryId : {}, cityCategoryShopMapping: {}",
                    cityId, categoryId, cityCategoryShopMappingOptional.get());
            cityCategoryShopMappingRepository.deleteByCityIdAndCategoryIdAndShopId(cityId, categoryId, shopId);
        }
        return getShopListFromCityCategoryMapping(email, cityId, categoryId);
    }
}

package br.com.potential.supermarket.service;

import br.com.potential.supermarket.dto.PageResponseDto;
import br.com.potential.supermarket.dto.request.ProductRequest;
import br.com.potential.supermarket.dto.response.ProductResponse;
import br.com.potential.supermarket.dto.response.SuccessResponse;
import br.com.potential.supermarket.entity.ProductEntity;
import br.com.potential.supermarket.exception.ExceptionMessages;
import br.com.potential.supermarket.exception.ValidationException;
import br.com.potential.supermarket.interfaces.CategoryService;
import br.com.potential.supermarket.interfaces.ProductService;
import br.com.potential.supermarket.interfaces.SupplierService;
import br.com.potential.supermarket.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static br.com.potential.supermarket.exception.ExceptionMessages.PRODUCT_ID_NOT_FOUND;
import static br.com.potential.supermarket.exception.ExceptionMessages.QUANTITY_SHOULD_NOT_BE_LESS_THAN_OR_EQUAL_TO_ZERO;
import static org.springframework.util.ObjectUtils.isEmpty;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Double ZERO = 0.0;
    private static final String PRODUCT_DELETED = "Product was deleted.";

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SupplierService supplierService;
    @Autowired
    private CategoryService categoryService;

    @Override
    public ProductResponse save(ProductRequest productRequest){
        validateQuantityAvailable(productRequest.getQuantityAvailable());
        var categoryEntity = categoryService.findByIdEntity(productRequest.getCategoryId());
        var supplierEntity = supplierService.findByIdEntity(productRequest.getSupplierId());
        var productEntity = productRepository.save(ProductEntity.of(productRequest, supplierEntity, categoryEntity));
        return ProductResponse.of(productEntity);
    }

    @Override
    public ProductResponse update(ProductRequest productRequest, UUID productId){
        validateInformedId(productId);
        validateQuantityAvailable(productRequest.getQuantityAvailable());
        var categoryEntity = categoryService.findByIdEntity(productRequest.getCategoryId());
        var supplierEntity = supplierService.findByIdEntity(productRequest.getSupplierId());
        var productEntity = ProductEntity.of(productRequest, supplierEntity, categoryEntity);
        productEntity.setId(productId);
        productRepository.save(productEntity);
        return ProductResponse.of(productEntity);
    }

    @Override
    public ProductResponse findById(UUID id){
        return ProductResponse.of(this.findByIdEntity(id));
    }

    @Override
    public List<ProductResponse> findByName(String name) {
        return findByNameEntity(name);
    }

    @Override
    public List<ProductResponse> findByCategoryId(UUID categoryId) {
        if(isEmpty(categoryId)){
            throw new ValidationException(ExceptionMessages.CATEGORY_ID_MUST_BE_INFORMED);
        }

        return productRepository.findByCategoryEntityId(categoryId)
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> findBySupplierId(UUID supplierId) {
        if(isEmpty(supplierId)){
            throw new ValidationException(ExceptionMessages.SUPPLIER_ID_MUST_BE_INFORMED);
        }

        return productRepository.findBySupplierEntityId(supplierId)
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponseDto<ProductResponse> findAll(Pageable pagination) {
        var productPage = productRepository.findAll(pagination)
                .map(ProductResponse::of);

        return new PageResponseDto<>(
                productPage.getContent(),
                productPage.getTotalElements(),
                productPage.getTotalPages());
    }

    @Override
    public ProductEntity findByIdEntity(UUID id){
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ValidationException(PRODUCT_ID_NOT_FOUND));
    }

    @Override
    public Boolean existsByCategoryId(UUID categoryId){
        return productRepository.existsByCategoryEntityId(categoryId);
    }

    @Override
    public Boolean existsBySupplierId(UUID supplierId){
        return productRepository.existsBySupplierEntityId(supplierId);
    }

    @Override
    public SuccessResponse delete(UUID productId) {
        validateInformedId(productId);
        productRepository.deleteById(productId);
        return SuccessResponse.create(PRODUCT_DELETED);
    }

    private void validateInformedId(UUID productId) {
        if(isEmpty(productId)){
            throw new ValidationException(ExceptionMessages.PRODUCT_ID_MUST_BE_INFORMED);
        }
    }

    private List<ProductResponse> findByNameEntity(String name) {
        if(isEmpty(name)){
            throw new ValidationException(ExceptionMessages.PRODUCT_NAME_MUST_BE_INFORMED);
        }

        return productRepository.findByNameIgnoreCaseContaining(name)
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    private void validateQuantityAvailable(Double quantityAvailable) {
        if(quantityAvailable <= ZERO){
            throw new ValidationException(QUANTITY_SHOULD_NOT_BE_LESS_THAN_OR_EQUAL_TO_ZERO);
        }
    }
}
package br.com.potential.supermarket.entity;

import br.com.potential.supermarket.dto.request.CategoryRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "CATEGORY")
public class CategoryEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String description;

    public static CategoryEntity of(CategoryRequest categoryRequest){
        var categoryEntity = new CategoryEntity();
        BeanUtils.copyProperties(categoryRequest, categoryEntity);
        return categoryEntity;
    }
}

package kr.hhplus.be.server.product.domain.repository;

import kr.hhplus.be.server.product.domain.entity.PopularProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PopularProductRepository extends JpaRepository<PopularProductEntity, Long> {

}
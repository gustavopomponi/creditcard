package br.com.gustavopomponi.repository;

import br.com.gustavopomponi.entity.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    List<CreditCard> findByUserIdAndIsActive(Long userId, Boolean isActive);
    List<CreditCard> findByUserId(Long userId);
}

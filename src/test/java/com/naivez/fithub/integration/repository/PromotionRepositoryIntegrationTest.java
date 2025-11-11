package com.naivez.fithub.integration.repository;

import com.naivez.fithub.entity.Promotion;
import com.naivez.fithub.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PromotionRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PromotionRepository promotionRepository;

    private Promotion currentPromotion;
    private Promotion expiredPromotion;
    private Promotion futurePromotion;
    private Promotion highDiscountPromotion;

    @BeforeEach
    void setUp() {
        currentPromotion = Promotion.builder()
                .title("title1")
                .description("description1")
                .discountPercent(new BigDecimal("20.00"))
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(20))
                .build();
        entityManager.persistAndFlush(currentPromotion);

        expiredPromotion = Promotion.builder()
                .title("title2")
                .description("description2")
                .discountPercent(new BigDecimal("15.00"))
                .startDate(LocalDate.now().minusDays(60))
                .endDate(LocalDate.now().minusDays(30))
                .build();
        entityManager.persistAndFlush(expiredPromotion);

        futurePromotion = Promotion.builder()
                .title("title3")
                .description("description3")
                .discountPercent(new BigDecimal("25.00"))
                .startDate(LocalDate.now().plusDays(30))
                .endDate(LocalDate.now().plusDays(60))
                .build();
        entityManager.persistAndFlush(futurePromotion);

        highDiscountPromotion = Promotion.builder()
                .title("title4")
                .description("description4")
                .discountPercent(new BigDecimal("40.00"))
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(10))
                .build();
        entityManager.persistAndFlush(highDiscountPromotion);

        entityManager.clear();
    }

    @Test
    void findActivePromotions_shouldReturnCurrentPromotions() {
        List<Promotion> activePromotions = promotionRepository.findActivePromotions(LocalDate.now());

        assertThat(activePromotions).hasSize(2);
        assertThat(activePromotions).allSatisfy(promotion -> {
            assertThat(promotion.getStartDate()).isBeforeOrEqualTo(LocalDate.now());
            assertThat(promotion.getEndDate()).isAfterOrEqualTo(LocalDate.now());
        });
        assertThat(activePromotions).extracting(Promotion::getTitle)
                .containsExactlyInAnyOrder("title1", "title4");
    }

    @Test
    void findActivePromotions_withPastDate_shouldReturnHistoricalPromotions() {
        LocalDate pastDate = LocalDate.now().minusDays(40);

        List<Promotion> historicalPromotions = promotionRepository.findActivePromotions(pastDate);

        assertThat(historicalPromotions).hasSize(1);
        assertThat(historicalPromotions.get(0).getTitle()).isEqualTo("title2");
    }

    @Test
    void findActivePromotions_withFutureDate_shouldReturnFuturePromotions() {
        LocalDate futureDate = LocalDate.now().plusDays(45);

        List<Promotion> futurePromotions = promotionRepository.findActivePromotions(futureDate);

        assertThat(futurePromotions).hasSize(1);
        assertThat(futurePromotions.get(0).getTitle()).isEqualTo("title3");
    }
}
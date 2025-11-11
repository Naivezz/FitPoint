package com.naivez.fithub.unit.service;

import com.naivez.fithub.dto.MembershipDTO;
import com.naivez.fithub.dto.PurchaseMembershipRequest;
import com.naivez.fithub.entity.Membership;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.MembershipMapper;
import com.naivez.fithub.repository.MembershipRepository;
import com.naivez.fithub.repository.UserRepository;
import com.naivez.fithub.service.MembershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MembershipMapper membershipMapper;

    @InjectMocks
    private MembershipService membershipService;

    private User testUser;
    private Membership testMembership;
    private MembershipDTO testMembershipDTO;
    private PurchaseMembershipRequest testPurchaseRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user1@gmail.com")
                .firstName("user1")
                .lastName("user1")
                .build();

        testMembership = Membership.builder()
                .id(1L)
                .user(testUser)
                .type("MONTHLY")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .price(new BigDecimal("99.99"))
                .active(true)
                .build();

        testMembershipDTO = MembershipDTO.builder()
                .id(1L)
                .type("MONTHLY")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .price(new BigDecimal("99.99"))
                .active(true)
                .daysRemaining(30L)
                .build();

        testPurchaseRequest = PurchaseMembershipRequest.builder()
                .type("MONTHLY")
                .build();
    }

    @Test
    void getUserMemberships_whenUserExists_shouldReturnMembershipList() {
        Membership membership2 = Membership.builder()
                .id(2L)
                .user(testUser)
                .type("QUARTERLY")
                .startDate(LocalDate.now().minusDays(90))
                .endDate(LocalDate.now().minusDays(60))
                .price(new BigDecimal("249.99"))
                .active(false)
                .build();

        MembershipDTO membershipDTO2 = MembershipDTO.builder()
                .id(2L)
                .type("QUARTERLY")
                .startDate(LocalDate.now().minusDays(90))
                .endDate(LocalDate.now().minusDays(60))
                .price(new BigDecimal("249.99"))
                .active(false)
                .daysRemaining(0L)
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(membershipRepository.findByUserIdOrderByEndDateDesc(1L)).thenReturn(List.of(testMembership, membership2));
        when(membershipMapper.toDto(testMembership)).thenReturn(testMembershipDTO);
        when(membershipMapper.toDto(membership2)).thenReturn(membershipDTO2);

        List<MembershipDTO> result = membershipService.getUserMemberships("user1@gmail.com");

        assertThat(result).hasSize(2);
        assertThat(result).contains(testMembershipDTO, membershipDTO2);
        verify(userRepository).findByEmail("user1@gmail.com");
        verify(membershipRepository).findByUserIdOrderByEndDateDesc(1L);
        verify(membershipMapper, times(2)).toDto(any(Membership.class));
    }

    @Test
    void getUserMemberships_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail("none@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.getUserMemberships("none@gmail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("none@gmail.com");
        verify(membershipRepository, never()).findByUserIdOrderByEndDateDesc(anyLong());
    }

    @Test
    void getUserMemberships_whenUserHasNoMemberships_shouldReturnEmptyList() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(membershipRepository.findByUserIdOrderByEndDateDesc(1L)).thenReturn(List.of());

        List<MembershipDTO> result = membershipService.getUserMemberships("user1@gmail.com");

        assertThat(result).isEmpty();
        verify(userRepository).findByEmail("user1@gmail.com");
        verify(membershipRepository).findByUserIdOrderByEndDateDesc(1L);
        verify(membershipMapper, never()).toDto(any(Membership.class));
    }

    @Test
    void getActiveMemberships_whenUserHasActiveMembership_shouldReturnActiveMemberships() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(membershipRepository.findActiveByUserId(eq(1L), any(LocalDate.class))).thenReturn(List.of(testMembership));
        when(membershipMapper.toDto(testMembership)).thenReturn(testMembershipDTO);

        List<MembershipDTO> result = membershipService.getActiveMemberships("user1@gmail.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testMembershipDTO);
        assertThat(result.get(0).isActive()).isTrue();
        verify(userRepository).findByEmail("user1@gmail.com");
        verify(membershipRepository).findActiveByUserId(eq(1L), any(LocalDate.class));
        verify(membershipMapper).toDto(testMembership);
    }

    @Test
    void getActiveMemberships_whenUserHasNoActiveMembership_shouldReturnEmptyList() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(membershipRepository.findActiveByUserId(eq(1L), any(LocalDate.class))).thenReturn(List.of());

        List<MembershipDTO> result = membershipService.getActiveMemberships("user1@gmail.com");

        assertThat(result).isEmpty();
        verify(userRepository).findByEmail("user1@gmail.com");
        verify(membershipRepository).findActiveByUserId(eq(1L), any(LocalDate.class));
        verify(membershipMapper, never()).toDto(any(Membership.class));
    }

    @Test
    void hasActiveMembership_whenUserHasActiveMembership_shouldReturnTrue() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(membershipRepository.findActiveByUserId(eq(1L), any(LocalDate.class))).thenReturn(List.of(testMembership));

        boolean result = membershipService.hasActiveMembership("user1@gmail.com");

        assertThat(result).isTrue();
        verify(userRepository).findByEmail("user1@gmail.com");
        verify(membershipRepository).findActiveByUserId(eq(1L), any(LocalDate.class));
    }

    @Test
    void hasActiveMembership_whenUserHasNoActiveMembership_shouldReturnFalse() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(membershipRepository.findActiveByUserId(eq(1L), any(LocalDate.class))).thenReturn(List.of());

        boolean result = membershipService.hasActiveMembership("user1@gmail.com");

        assertThat(result).isFalse();
        verify(userRepository).findByEmail("user1@gmail.com");
        verify(membershipRepository).findActiveByUserId(eq(1L), any(LocalDate.class));
    }

    @Test
    void hasActiveMembership_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail("none@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.hasActiveMembership("none@gmail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("none@gmail.com");
        verify(membershipRepository, never()).findActiveByUserId(anyLong(), any(LocalDate.class));
    }

    @Test
    void purchaseMembership_withMonthlyType_shouldCreateMembershipWithCorrectDuration() {
        Membership savedMembership = Membership.builder()
                .id(2L)
                .user(testUser)
                .type("MONTHLY")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .price(new BigDecimal("99.99"))
                .active(true)
                .build();

        MembershipDTO savedDTO = MembershipDTO.builder()
                .id(2L)
                .type("MONTHLY")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .price(new BigDecimal("99.99"))
                .active(true)
                .daysRemaining(30L)
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(membershipRepository.save(any(Membership.class))).thenReturn(savedMembership);
        when(membershipMapper.toDto(savedMembership)).thenReturn(savedDTO);

        MembershipDTO result = membershipService.purchaseMembership("user1@gmail.com", testPurchaseRequest);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("MONTHLY");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(result.isActive()).isTrue();

        verify(userRepository).findByEmail("user1@gmail.com");
        verify(membershipRepository).save(any(Membership.class));
        verify(membershipMapper).toDto(savedMembership);
    }

    @Test
    void purchaseMembership_withQuarterlyType_shouldCreateMembershipWithCorrectPrice() {
        PurchaseMembershipRequest quarterlyRequest = PurchaseMembershipRequest.builder()
                .type("QUARTERLY")
                .build();

        Membership quarterlySaved = Membership.builder()
                .id(3L)
                .user(testUser)
                .type("QUARTERLY")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(90))
                .price(new BigDecimal("249.99"))
                .active(true)
                .build();

        MembershipDTO quarterlyDTO = MembershipDTO.builder()
                .id(3L)
                .type("QUARTERLY")
                .price(new BigDecimal("249.99"))
                .active(true)
                .daysRemaining(90L)
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(membershipRepository.save(any(Membership.class))).thenReturn(quarterlySaved);
        when(membershipMapper.toDto(quarterlySaved)).thenReturn(quarterlyDTO);

        MembershipDTO result = membershipService.purchaseMembership("user1@gmail.com", quarterlyRequest);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("QUARTERLY");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("249.99"));
        verify(membershipRepository).save(any(Membership.class));
    }

    @Test
    void purchaseMembership_withInvalidType_shouldThrowException() {
        PurchaseMembershipRequest invalidRequest = PurchaseMembershipRequest.builder()
                .type("INVALID_TYPE")
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> membershipService.purchaseMembership("user1@gmail.com", invalidRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid membership type");

        verify(userRepository).findByEmail("user1@gmail.com");
        verify(membershipRepository, never()).save(any(Membership.class));
    }

    @Test
    void purchaseMembership_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail("none@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.purchaseMembership("none@gmail.com", testPurchaseRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("none@gmail.com");
        verify(membershipRepository, never()).save(any(Membership.class));
    }

    @Test
    void topUpBalance_whenUserHasValidMembership_shouldExtendExistingMembership() {
        Membership existingMembership = Membership.builder()
                .id(1L)
                .user(testUser)
                .type("MONTHLY")
                .startDate(LocalDate.now().minusDays(15))
                .endDate(LocalDate.now().plusDays(15))
                .price(new BigDecimal("99.99"))
                .active(true)
                .build();

        Membership extendedMembership = Membership.builder()
                .id(1L)
                .user(testUser)
                .type("MONTHLY")
                .startDate(LocalDate.now().minusDays(15))
                .endDate(LocalDate.now().plusDays(45))
                .price(new BigDecimal("99.99"))
                .active(true)
                .build();

        MembershipDTO extendedDTO = MembershipDTO.builder()
                .id(1L)
                .type("MONTHLY")
                .startDate(LocalDate.now().minusDays(15))
                .endDate(LocalDate.now().plusDays(45))
                .price(new BigDecimal("99.99"))
                .active(true)
                .daysRemaining(45L)
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(membershipRepository.findValidByUserId(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(existingMembership));
        when(membershipRepository.save(any(Membership.class))).thenReturn(extendedMembership);
        when(membershipMapper.toDto(extendedMembership)).thenReturn(extendedDTO);

        MembershipDTO result = membershipService.topUpBalance("user1@gmail.com", testPurchaseRequest);

        assertThat(result).isNotNull();
        assertThat(result.getEndDate()).isEqualTo(LocalDate.now().plusDays(45));
        verify(membershipRepository).findValidByUserId(eq(1L), any(LocalDate.class));
        verify(membershipRepository).save(existingMembership);
        verify(membershipMapper).toDto(extendedMembership);
    }

    @Test
    void topUpBalance_whenUserHasNoValidMembership_shouldCreateNewMembership() {
        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));
        when(membershipRepository.findValidByUserId(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of());
        when(membershipRepository.save(any(Membership.class))).thenReturn(testMembership);
        when(membershipMapper.toDto(testMembership)).thenReturn(testMembershipDTO);

        MembershipDTO result = membershipService.topUpBalance("user1@gmail.com", testPurchaseRequest);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("MONTHLY");
        verify(membershipRepository).findValidByUserId(eq(1L), any(LocalDate.class));
        verify(membershipRepository).save(any(Membership.class));
        verify(membershipMapper).toDto(testMembership);
    }

    @Test
    void topUpBalance_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail("none@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.topUpBalance("none@gmail.com", testPurchaseRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("none@gmail.com");
        verify(membershipRepository, never()).findValidByUserId(anyLong(), any(LocalDate.class));
        verify(membershipRepository, never()).save(any(Membership.class));
    }

    @Test
    void topUpBalance_withInvalidType_shouldThrowException() {
        PurchaseMembershipRequest invalidRequest = PurchaseMembershipRequest.builder()
                .type("INVALID_TYPE")
                .build();

        when(userRepository.findByEmail("user1@gmail.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> membershipService.topUpBalance("user1@gmail.com", invalidRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid membership type");

        verify(userRepository).findByEmail("user1@gmail.com");
        verify(membershipRepository, never()).findValidByUserId(anyLong(), any(LocalDate.class));
        verify(membershipRepository, never()).save(any(Membership.class));
    }
}
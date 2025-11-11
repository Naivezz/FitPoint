package com.naivez.fithub.integration.repository;

import com.naivez.fithub.entity.Equipment;
import com.naivez.fithub.entity.Room;
import com.naivez.fithub.repository.EquipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EquipmentRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EquipmentRepository equipmentRepository;

    private Room testRoom;
    private Equipment testEquipment;

    @BeforeEach
    void setUp() {
        testRoom = Room.builder()
                .name("room1")
                .capacity(20)
                .equipmentList(new HashSet<>())
                .classes(new HashSet<>())
                .build();

        testEquipment = Equipment.builder()
                .name("equipment2")
                .quantity(10)
                .status("AVAILABLE")
                .room(testRoom)
                .build();
    }

    @Test
    void save_withValidEquipment_shouldPersistSuccessfully() {
        entityManager.persist(testRoom);
        entityManager.flush();

        Equipment savedEquipment = equipmentRepository.save(testEquipment);

        assertThat(savedEquipment).isNotNull();
        assertThat(savedEquipment.getId()).isNotNull();
        assertThat(savedEquipment.getName()).isEqualTo("equipment2");
        assertThat(savedEquipment.getQuantity()).isEqualTo(10);
        assertThat(savedEquipment.getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void findById_whenEquipmentExists_shouldReturnEquipment() {
        entityManager.persist(testRoom);
        Equipment persistedEquipment = entityManager.persistAndFlush(testEquipment);
        entityManager.clear();

        Optional<Equipment> result = equipmentRepository.findById(persistedEquipment.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("equipment2");
    }

    @Test
    void findById_whenEquipmentDoesNotExist_shouldReturnEmpty() {
        Optional<Equipment> result = equipmentRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findByRoomId_whenEquipmentExists_shouldReturnList() {
        entityManager.persist(testRoom);
        entityManager.persist(testEquipment);

        Equipment equipment2 = Equipment.builder()
                .name("name")
                .quantity(20)
                .status("AVAILABLE")
                .room(testRoom)
                .build();
        entityManager.persist(equipment2);
        entityManager.flush();
        entityManager.clear();

        List<Equipment> result = equipmentRepository.findByRoomId(testRoom.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Equipment::getName)
                .containsExactlyInAnyOrder("equipment2", "name");
    }

    @Test
    void findByRoomId_whenNoEquipment_shouldReturnEmptyList() {
        entityManager.persist(testRoom);
        entityManager.flush();

        List<Equipment> result = equipmentRepository.findByRoomId(testRoom.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByStatus_whenNoEquipmentWithStatus_shouldReturnEmptyList() {
        List<Equipment> result = equipmentRepository.findByStatus("Broken");

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllEquipment() {
        entityManager.persist(testRoom);
        entityManager.persist(testEquipment);

        Equipment equipment2 = Equipment.builder()
                .name("equipment3")
                .quantity(5)
                .status("AVAILABLE")
                .room(testRoom)
                .build();
        entityManager.persist(equipment2);
        entityManager.flush();

        List<Equipment> result = equipmentRepository.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void update_existingEquipment_shouldModifyAndPersist() {
        entityManager.persist(testRoom);
        Equipment persistedEquipment = entityManager.persistAndFlush(testEquipment);
        entityManager.clear();

        Equipment equipmentToUpdate = equipmentRepository.findById(persistedEquipment.getId()).orElseThrow();
        equipmentToUpdate.setQuantity(15);
        equipmentToUpdate.setStatus("AVAILABLE");
        equipmentRepository.save(equipmentToUpdate);
        entityManager.flush();
        entityManager.clear();

        Equipment verifyEquipment = entityManager.find(Equipment.class, persistedEquipment.getId());
        assertThat(verifyEquipment.getQuantity()).isEqualTo(15);
        assertThat(verifyEquipment.getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void deleteById_whenEquipmentExists_shouldRemoveRecord() {
        entityManager.persist(testRoom);
        Equipment persistedEquipment = entityManager.persistAndFlush(testEquipment);
        Long equipmentId = persistedEquipment.getId();
        entityManager.clear();

        equipmentRepository.deleteById(equipmentId);
        entityManager.flush();

        Equipment deletedEquipment = entityManager.find(Equipment.class, equipmentId);
        assertThat(deletedEquipment).isNull();
    }

    @Test
    void existsById_whenEquipmentExists_shouldReturnTrue() {
        entityManager.persist(testRoom);
        Equipment persistedEquipment = entityManager.persistAndFlush(testEquipment);
        entityManager.clear();

        boolean exists = equipmentRepository.existsById(persistedEquipment.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsById_whenEquipmentDoesNotExist_shouldReturnFalse() {
        boolean exists = equipmentRepository.existsById(999L);

        assertThat(exists).isFalse();
    }
}
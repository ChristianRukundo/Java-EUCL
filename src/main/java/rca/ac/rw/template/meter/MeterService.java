package rca.ac.rw.template.meter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rca.ac.rw.template.commons.exceptions.ResourceNotFoundException; 
import rca.ac.rw.template.commons.exceptions.ValidationException;   
import rca.ac.rw.template.meter.dto.MeterResponseDto;
import rca.ac.rw.template.meter.dto.RegisterMeterRequestDto;
import rca.ac.rw.template.users.User;
import rca.ac.rw.template.users.UserRepository;
import rca.ac.rw.template.users.Role; 


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeterService {

    private final MeterRepository meterRepository;
    private final UserRepository userRepository; 

    /**
     * Admin registers a new meter number to a specific user.
     *
     * @param dto The request DTO containing meter number and user ID.
     * @return MeterResponseDto of the newly registered meter.
     */
    @Transactional
    public MeterResponseDto registerMeter(RegisterMeterRequestDto dto) {
        log.info("Admin attempting to register meter number: {} for user ID: {}", dto.getMeterNumber(), dto.getUserId());

        User user = userRepository.findById(dto.getUserId())
                .filter(u -> !u.isDeleted()) 
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", dto.getUserId()));

        
        if (user.getRole() != Role.ROLE_CUSTOMER) {
            log.warn("Attempt to register meter for a non-customer user. User ID: {}, Role: {}", user.getId(), user.getRole());
            
            throw new ValidationException("Meters can only be registered for users with ROLE_CUSTOMER.");
        }

        if (meterRepository.existsByMeterNumber(dto.getMeterNumber())) {
            throw new ValidationException("Meter number '" + dto.getMeterNumber() + "' already exists.");
        }

        Meter newMeter = new Meter(dto.getMeterNumber(), user);
        Meter savedMeter = meterRepository.save(newMeter);
        log.info("Meter registered successfully. ID: {}, Meter Number: {}, User ID: {}",
                savedMeter.getId(), savedMeter.getMeterNumber(), user.getId());

        return MeterConverter.toDto(savedMeter);
    }

    /**
     * Retrieves all meters registered to a specific user (customer).
     *
     * @param userId The ID of the user.
     * @param pageable Pagination information.
     * @return A Page of MeterResponseDto.
     */
    @Transactional(readOnly = true)
    public Page<MeterResponseDto> getMetersByUserId(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));

        log.debug("Fetching meters for user ID: {}", userId);

        Page<Meter> meterPage = meterRepository.findByUserAndDeletedFalse(user, pageable);

        List<MeterResponseDto> dtos = meterPage.getContent().stream()
                .map(MeterConverter::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, meterPage.getTotalElements());
    }

    /**
     * Retrieves a specific meter by its ID.
     * @param meterId The ID of the meter.
     * @return MeterResponseDto.
     */
    @Transactional(readOnly = true)
    public MeterResponseDto getMeterById(UUID meterId) {
        Meter meter = meterRepository.findById(meterId)
                .filter(m -> !m.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Meter", "ID", meterId));
        return MeterConverter.toDto(meter);
    }


    /**
     * Retrieves all meters (paginated) for admin view, with optional filters.
     * @param pageable Pagination information.
     * @param meterNumberFilter Optional filter by meter number.
     * @param userIdFilter Optional filter by user ID.
     * @return Page of MeterResponseDto.
     */
    @Transactional(readOnly = true)
    public Page<MeterResponseDto> getAllMetersAdmin(Pageable pageable, String meterNumberFilter, UUID userIdFilter) {
        log.debug("Admin fetching all meters. MeterNumberFilter: {}, UserIdFilter: {}", meterNumberFilter, userIdFilter);
        Specification<Meter> spec = MeterSpecifications.filterMeters(meterNumberFilter, userIdFilter, false); 
        Page<Meter> meterPage = meterRepository.findAll(spec, pageable);

        List<MeterResponseDto> dtos = meterPage.getContent().stream()
                .map(MeterConverter::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, meterPage.getTotalElements());
    }

    
    @Transactional
    public void softDeleteMeter(UUID meterId) {
        Meter meter = meterRepository.findById(meterId)
                .filter(m -> !m.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Meter", "ID", meterId));

        log.info("Admin soft deleting meter ID: {}", meterId);
        meter.setDeleted(true);
        meterRepository.save(meter);
    }
}
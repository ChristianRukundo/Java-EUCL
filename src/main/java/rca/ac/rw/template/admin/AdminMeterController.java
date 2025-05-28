package rca.ac.rw.template.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rca.ac.rw.template.meter.MeterService;
import rca.ac.rw.template.meter.dto.MeterResponseDto;
import rca.ac.rw.template.meter.dto.RegisterMeterRequestDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/meters")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminMeterController {

    private final MeterService meterService;

    /**
     * POST /api/v1/admin/meters : Admin registers a meter number to a specific user.
     * (Task 2a)
     */
    @PostMapping
    public ResponseEntity<MeterResponseDto> registerMeter(
            @Valid @RequestBody RegisterMeterRequestDto registerMeterDto) {
        log.info("Admin API request to register meter: {}", registerMeterDto.getMeterNumber());
        MeterResponseDto registeredMeter = meterService.registerMeter(registerMeterDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredMeter);
    }

    /**
     * GET /api/v1/admin/meters/by-user/{userId} : Admin displays meters associated with a given user.
     * (Task 2b - "A person can own multiple meter numbers" implies needing to see them)
     */
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<Page<MeterResponseDto>> getMetersByUserId(
            @PathVariable UUID userId,
            @PageableDefault(size = 10, sort = "meterNumber") Pageable pageable) {
        log.info("Admin API request to get meters for user ID: {}", userId);
        Page<MeterResponseDto> meters = meterService.getMetersByUserId(userId, pageable);
        return ResponseEntity.ok(meters);
    }

    /**
     * GET /api/v1/admin/meters/{meterId} : Admin gets a specific meter by ID.
     */
    @GetMapping("/{meterId}")
    public ResponseEntity<MeterResponseDto> getMeterById(@PathVariable UUID meterId) {
        log.info("Admin API request to get meter by ID: {}", meterId);
        MeterResponseDto meter = meterService.getMeterById(meterId);
        return ResponseEntity.ok(meter);
    }

    /**
     * GET /api/v1/admin/meters : Admin lists all meters with optional filters.
     */
    @GetMapping
    public ResponseEntity<Page<MeterResponseDto>> getAllMeters(
            @PageableDefault(size = 10, sort = "meterNumber") Pageable pageable,
            @RequestParam(required = false) String meterNumber,
            @RequestParam(name = "user", required = false) UUID userId) { 
        log.info("Admin API request to get all meters. Filter by meterNumber: {}, userId: {}", meterNumber, userId);
        Page<MeterResponseDto> meters = meterService.getAllMetersAdmin(pageable, meterNumber, userId);
        return ResponseEntity.ok(meters);
    }

    /**
     * DELETE /api/v1/admin/meters/{meterId} : Admin soft deletes a meter.
     */
    @DeleteMapping("/{meterId}")
    public ResponseEntity<Void> softDeleteMeter(@PathVariable UUID meterId) {
        log.info("Admin API request to soft delete meter ID: {}", meterId);
        meterService.softDeleteMeter(meterId);
        return ResponseEntity.noContent().build();
    }
}
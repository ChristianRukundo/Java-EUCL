package rca.ac.rw.template.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rca.ac.rw.template.token.TokenService;
import rca.ac.rw.template.token.dto.TokenSummaryResponseDto;
// Potentially ValidatedTokenResponseDto if admin can validate too

@RestController
@RequestMapping("/api/v1/admin/tokens") // Separate admin path for tokens
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminTokenController {

    private final TokenService tokenService;

    /**
     * GET /api/v1/admin/tokens/by-meter/{meterNumber} : Admin views all tokens for a specific meter.
     * (Task 4b for admin)
     */
    @GetMapping("/by-meter/{meterNumber}")
    public ResponseEntity<Page<TokenSummaryResponseDto>> getAllTokensByMeterNumber(
            @PathVariable String meterNumber,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {
        log.info("Admin API request to get all tokens for meter: {}", meterNumber);
        Page<TokenSummaryResponseDto> tokens = tokenService.getAllTokensByMeterNumberAdmin(meterNumber, pageable);
        return ResponseEntity.ok(tokens);
    }

}
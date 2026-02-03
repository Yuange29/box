package storage.com.box.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import storage.com.box.dto.request.FeeCreationRequest;
import storage.com.box.dto.request.FeeUpdateRequest;
import storage.com.box.dto.response.ApiResponse;
import storage.com.box.dto.response.FeeCreationResponse;
import storage.com.box.service.FeeService;

import java.util.List;


@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/fee")
@Builder
public class FeeController {

    FeeService feeService;

    @PostMapping
    public ApiResponse<FeeCreationResponse> createFee(@RequestBody FeeCreationRequest request) {
        return ApiResponse.<FeeCreationResponse>builder()
                .result(feeService.createFee(request))
                .build();
    }

    @PutMapping("/{feeId}")
    public ApiResponse<FeeCreationResponse> createFeeId(@PathVariable String feeId
            , @RequestBody FeeUpdateRequest request) {
        return ApiResponse.<FeeCreationResponse>builder()
                .result(feeService.updateFee(feeId, request))
                .build();
    }

    @DeleteMapping("/{feeId}")
    public ApiResponse<Void> deleteFee(@PathVariable String feeId) {
        feeService.deleteFee(feeId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/{feeId}")
    public ApiResponse<FeeCreationResponse> getFee(@PathVariable String feeId) {
        return ApiResponse.<FeeCreationResponse>builder()
                .result(feeService.getFee(feeId))
                .build();
    }

    @GetMapping("/userFees")
    public ApiResponse<List<FeeCreationResponse>> getUserFees() {
        return ApiResponse.<List<FeeCreationResponse>>builder()
                .result(feeService.getUserFee())
                .build();
    }
}

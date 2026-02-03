package storage.com.box.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import storage.com.box.dto.request.FeeCreationRequest;
import storage.com.box.dto.request.FeeUpdateRequest;
import storage.com.box.dto.response.FeeCreationResponse;
import storage.com.box.entity.Fee;
import storage.com.box.entity.User;
import storage.com.box.exception.AppException;
import storage.com.box.exception.ErrorCode;
import storage.com.box.mapper.FeeMapper;
import storage.com.box.repository.FeeRepository;
import storage.com.box.repository.UserRepository;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FeeService {

    UserRepository userRepository;
    FeeRepository feeRepository;
    FeeMapper  feeMapper;

    @PreAuthorize("hasRole('CREATE')")
    public FeeCreationResponse createFee(FeeCreationRequest request) {

        var context = SecurityContextHolder.getContext().getAuthentication();
        String name = Objects.requireNonNull(context).getName();

        var user = userRepository.findByUserName(name);

        Fee fee = feeMapper.toFee(request);
        fee.setUserId(user.get().getUserId());

        return feeMapper.toFeeCreationResponse(feeRepository.save(fee));
    }

    @PreAuthorize("hasRole('UPDATE')")
    public FeeCreationResponse updateFee(String feeId, FeeUpdateRequest request) {

        Fee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new AppException(ErrorCode.FEE_NOT_FOUND));

        feeMapper.updateFee(request, fee);

        feeRepository.save(fee);

        return feeMapper.toFeeCreationResponse(fee);
    }

    @PreAuthorize("hasRole('DELETE')")
    public void deleteFee(String feeId) {
        feeRepository.findById(feeId)
                .orElseThrow(()-> new AppException(ErrorCode.FEE_NOT_FOUND));

        feeRepository.deleteById(feeId);
    }

    @PreAuthorize("hasRole('GET')")
    public FeeCreationResponse getFee(String feeId) {

        Fee fee = feeRepository.findById(feeId).orElseThrow(() ->
                new AppException(ErrorCode.FEE_NOT_FOUND));

        return feeMapper.toFeeCreationResponse(fee);
    }

    @PreAuthorize("hasRole('GET')")
    public List<FeeCreationResponse> getUserFee() {

        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        return feeRepository.findByUserId(user.getUserId()).stream()
                .map(feeMapper::toFeeCreationResponse)
                .toList();
    }

}

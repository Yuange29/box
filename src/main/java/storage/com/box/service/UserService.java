package storage.com.box.service;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import storage.com.box.constant.PredefineRole;
import storage.com.box.dto.request.UserCreationRequest;
import storage.com.box.dto.request.UserUpdateRequest;
import storage.com.box.dto.response.UserResponse;
import storage.com.box.entity.Role;
import storage.com.box.entity.User;
import storage.com.box.exception.AppException;
import storage.com.box.exception.ErrorCode;
import storage.com.box.mapper.UserMapper;
import storage.com.box.repository.RoleRepository;
import storage.com.box.repository.UserRepository;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUserName(request.getUserName()))
                throw new AppException(ErrorCode.USER_ALREADY_EXIST);

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        HashSet<Role> roles = new HashSet<>();

        roleRepository.findById(PredefineRole.USER).ifPresent(roles::add);

        user.setRoles(roles);

        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('UPDATE')")
    public UserResponse updateUser(String userId, UserUpdateRequest request) {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        userMapper.updateUser(user, request);

        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('DELETE')")
    public void deleteUser(String userId) {
        userRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('GET')")
    public UserResponse getUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('GET')")
    public UserResponse myInfo() {

        var context =  SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        User user =  userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> findAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse).toList();
    }
}

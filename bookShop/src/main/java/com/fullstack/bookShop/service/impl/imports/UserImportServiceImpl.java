package com.fullstack.bookShop.service.impl.imports;

import com.fullstack.bookShop.dto.request.imports.UserImportRequest;
import com.fullstack.bookShop.dto.response.UserResponse;
import com.fullstack.bookShop.mapper.UserMapper;
import com.fullstack.bookShop.model.Role;
import com.fullstack.bookShop.model.User;
import com.fullstack.bookShop.repository.RatingRepository;
import com.fullstack.bookShop.service.impl.BookRatingSummaryUpdater;
import com.fullstack.bookShop.repository.RoleRepository;
import com.fullstack.bookShop.repository.UserRepository;
import com.fullstack.bookShop.service.impl.imports.UserImportServiceImpl.UserImportContext;
import com.fullstack.bookShop.util.batch.ImportExecutor;
import com.fullstack.bookShop.util.batch.ImportRow;
import com.fullstack.bookShop.util.constant.CommonMessage;
import com.fullstack.bookShop.util.constant.PasswordConstant;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.fullstack.bookShop.util.constant.auth.AuthorityConstant;
import com.fullstack.bookShop.util.enums.ImportActionEnum;
import com.fullstack.bookShop.util.enums.RoleEnum;
import com.fullstack.bookShop.util.enums.UserEnum;
import com.fullstack.bookShop.util.message.MessageTranslator;
import com.fullstack.bookShop.util.string.StringUtils;
import jakarta.validation.Validator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserImportServiceImpl
        extends AbstractEntityImportService<UserImportRequest, User, UserResponse, UserImportContext> {

    private final UserMapper userMapper;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final RatingRepository ratingRepository;

    private final BookRatingSummaryUpdater bookRatingSummaryUpdater;

    private final PasswordEncoder passwordEncoder;

    public UserImportServiceImpl(
            Validator validator,
            ImportExecutor importExecutor,
            MessageTranslator messageTranslator,
            UserMapper userMapper,
            UserRepository userRepository,
            RoleRepository roleRepository,
            RatingRepository ratingRepository,
            BookRatingSummaryUpdater bookRatingSummaryUpdater,
            PasswordEncoder passwordEncoder
    ) {
        super(validator, importExecutor, messageTranslator);
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.ratingRepository = ratingRepository;
        this.bookRatingSummaryUpdater = bookRatingSummaryUpdater;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Du lieu tra cuu cua ca file, nap bang mot vai cau query.
     * Vai tro mac dinh cung nap san: neu khong thi moi dong CREATE lai them mot cau tim role.
     */
    protected record UserImportContext(
            Set<String> existingEmails,
            Set<String> existingPhones,
            Role defaultRole
    ) {}

    @Override
    protected UserImportContext buildContext(List<ImportRow<UserImportRequest>> rows) {
        Set<String> emails = rows.stream()
                .map(row -> businessKeyOf(row.source()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> phones = rows.stream()
                .map(row -> StringUtils.trimBlank(row.source().getPhone()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> existingEmails = emails.isEmpty()
                ? Set.of()
                : userRepository.findExistingEmails(emails)
                        .stream()
                        .map(email -> email.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toSet());

        Set<String> existingPhones = phones.isEmpty()
                ? Set.of()
                : userRepository.findExistingPhones(phones);

        Role defaultRole = roleRepository.findByName(AuthorityConstant.USER).orElse(null);

        return new UserImportContext(existingEmails, existingPhones, defaultRole);
    }

    @Override
    protected String businessKeyOf(UserImportRequest request) {
        return StringUtils.trimLowerBlank(request.getEmail());
    }

    @Override
    protected ImportActionEnum actionOf(UserImportRequest request) {
        return request.getAction();
    }

    @Override
    protected Optional<User> findExistingEntity(UserImportRequest request) {
        return userRepository.findByEmail(StringUtils.trimBlank(request.getEmail()));
    }

    @Override
    protected String notFoundMessage(UserImportRequest request) {
        return messageTranslator.toLocale(UserEnum.EMAIL_NOT_FOUND.getMessageKey(), request.getEmail());
    }

    @Override
    protected User createEntity(UserImportRequest request, UserImportContext context) {
        if (context.existingEmails().contains(businessKeyOf(request))) {
            throw new IllegalArgumentException(messageTranslator.toLocale(
                    UserEnum.EMAIL_EXISTS.getMessageKey(), request.getEmail()));
        }

        String phone = StringUtils.trimBlank(request.getPhone());
        if (phone != null && context.existingPhones().contains(phone)) {
            throw new IllegalArgumentException(messageTranslator.toLocale(
                    UserEnum.PHONE_EXISTS.getMessageKey(), phone));
        }

        if (context.defaultRole() == null) {
            throw new IllegalStateException(messageTranslator.toLocale(
                    RoleEnum.NAME_NOT_FOUND.getMessageKey(), AuthorityConstant.USER));
        }

        User user = new User();
        user.setEmail(StringUtils.trimLowerBlank(request.getEmail()));
        user.setFullName(StringUtils.normalizeSpaces(request.getFullName()));
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(PasswordConstant.DEFAULT_PASSWORD));
        user.setRole(context.defaultRole());
        return userRepository.save(user);
    }

    @Override
    protected User updateEntity(User existing, UserImportRequest request, UserImportContext context) {
        if (StringUtils.hasValue(request.getFullName())) {
            existing.setFullName(StringUtils.normalizeSpaces(request.getFullName()));
        }
        if (StringUtils.hasValue(request.getPhone())) {
            existing.setPhone(StringUtils.trimBlank(request.getPhone()));
        }
        return userRepository.save(existing);
    }

    @Override
    protected void deleteEntity(User existing) {
        // Giu nguyen luat cua luong xoa nguoi dung dang co: khong duoc xoa tai khoan quan tri
        Role role = existing.getRole();
        if (role != null && AuthorityConstant.ADMIN.equals(role.getName())) {
            throw new IllegalStateException(messageTranslator.toLocale(CommonMessage.NOT_PERMISSION_DELETE_USER));
        }

        Set<Long> ratedBookIds = ratingRepository.findRatedBookIdsByUserId(existing.getId());

        ratingRepository.deleteByUserId(existing.getId());
        userRepository.delete(existing);
        bookRatingSummaryUpdater.refresh(ratedBookIds);
    }

    @Override
    protected UserResponse toResponse(User entity, ImportActionEnum action) {
        return userMapper.toResponse(entity, switch (action) {
            case CREATE -> ActionConstant.CREATE;
            case UPDATE -> ActionConstant.UPDATE;
            case DELETE -> ActionConstant.DELETE;
        });
    }
}

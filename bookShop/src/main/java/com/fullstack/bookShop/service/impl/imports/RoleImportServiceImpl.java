package com.fullstack.bookShop.service.impl.imports;

import com.fullstack.bookShop.dto.request.imports.RoleImportRequest;
import com.fullstack.bookShop.dto.response.RoleResponse;
import com.fullstack.bookShop.mapper.RoleMapper;
import com.fullstack.bookShop.model.Role;
import com.fullstack.bookShop.repository.RoleRepository;
import com.fullstack.bookShop.repository.UserRepository;
import com.fullstack.bookShop.util.batch.ImportExecutor;
import com.fullstack.bookShop.util.batch.ImportRow;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.fullstack.bookShop.util.enums.ImportActionEnum;
import com.fullstack.bookShop.util.enums.RoleEnum;
import com.fullstack.bookShop.util.message.MessageTranslator;
import com.fullstack.bookShop.util.string.StringUtils;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleImportServiceImpl
        extends AbstractEntityImportService<RoleImportRequest, Role, RoleResponse, Set<String>> {

    private final RoleMapper roleMapper;

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;

    public RoleImportServiceImpl(
            Validator validator,
            ImportExecutor importExecutor,
            MessageTranslator messageTranslator,
            RoleMapper roleMapper,
            RoleRepository roleRepository,
            UserRepository userRepository
    ) {
        super(validator, importExecutor, messageTranslator);
        this.roleMapper = roleMapper;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    /** Tap ten da ton tai, nap bang DUNG mot cau query thay vi hoi DB tung dong. */
    @Override
    protected Set<String> buildContext(List<ImportRow<RoleImportRequest>> rows) {
        Set<String> names = rows.stream()
                .map(row -> businessKeyOf(row.source()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return names.isEmpty() ? Set.of() : roleRepository.findExistingNamesIgnoreCase(names);
    }

    @Override
    protected String businessKeyOf(RoleImportRequest request) {
        return StringUtils.trimLowerBlank(request.getName());
    }

    @Override
    protected ImportActionEnum actionOf(RoleImportRequest request) {
        return request.getAction();
    }

    @Override
    protected Optional<Role> findExistingEntity(RoleImportRequest request) {
        return roleRepository.findByName(StringUtils.trimBlank(request.getName()));
    }

    @Override
    protected String notFoundMessage(RoleImportRequest request) {
        return messageTranslator.toLocale(RoleEnum.NAME_NOT_FOUND.getMessageKey(), request.getName());
    }

    @Override
    protected Role createEntity(RoleImportRequest request, Set<String> existingNames) {
        if (existingNames.contains(businessKeyOf(request))) {
            throw new IllegalArgumentException(messageTranslator.toLocale(
                    RoleEnum.NAME_EXISTS.getMessageKey(), request.getName()));
        }

        Role role = new Role();
        role.setName(StringUtils.normalizeSpaces(request.getName()));
        role.setDescription(request.getDescription());
        role.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);
        return roleRepository.save(role);
    }

    @Override
    protected Role updateEntity(Role existing, RoleImportRequest request, Set<String> existingNames) {
        if (StringUtils.hasValue(request.getDescription())) {
            existing.setDescription(request.getDescription());
        }
        if (request.getActive() != null) {
            existing.setActive(request.getActive());
        }
        return roleRepository.save(existing);
    }

    @Override
    protected void deleteEntity(Role existing) {
        // Giu nguyen luat cua luong xoa vai tro dang co: xoa vai tro keo theo nguoi dung thuoc vai tro do
        userRepository.deleteByRoleId(existing.getId());
        roleRepository.delete(existing);
    }

    @Override
    protected RoleResponse toResponse(Role entity, ImportActionEnum action) {
        return roleMapper.toRoleResponse(entity, switch (action) {
            case CREATE -> ActionConstant.CREATE;
            case UPDATE -> ActionConstant.UPDATE;
            case DELETE -> ActionConstant.DELETE;
        });
    }
}

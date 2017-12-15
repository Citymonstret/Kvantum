package xyz.kvantum.server.api.account;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNegative;
import net.sf.oval.constraint.NotNull;
import xyz.kvantum.server.api.account.roles.AccountRole;
import xyz.kvantum.server.api.pojo.KvantumPojo;
import xyz.kvantum.server.api.pojo.KvantumPojoFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public final class AccountDO
{

    @Getter
    private static final KvantumPojoFactory<AccountDO> kvantumPojoFactory =
            KvantumPojoFactory.forClass( AccountDO.class );
    private static final Validator validator = new Validator();

    @NotNegative
    private final int id;

    @NotEmpty
    @NotNull
    @NonNull
    private final String username;

    @NonNull
    @NotNull
    private final Collection<AccountRole> accountRoles;

    public AccountDO(@NotNull final IAccount account)
    {
        this.id = account.getId();
        this.username = account.getUsername();
        this.accountRoles = new ArrayList<>( account.getAccountRoles() );
    }

    public List<ConstraintViolation> validate()
    {
        return validator.validate( this );
    }

    public KvantumPojo<AccountDO> toPojo()
    {
        return kvantumPojoFactory.of( this );
    }
}

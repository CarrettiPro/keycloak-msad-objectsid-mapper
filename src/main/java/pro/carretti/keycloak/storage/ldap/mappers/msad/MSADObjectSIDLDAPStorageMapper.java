package pro.carretti.keycloak.storage.ldap.mappers.msad;

import java.io.IOException;

import org.jboss.logging.Logger;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.Condition;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;

import java.util.Collections;
import java.util.Set;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapper;

public class MSADObjectSIDLDAPStorageMapper extends AbstractLDAPStorageMapper {

    private static final Logger LOG = Logger.getLogger(MSADObjectSIDLDAPStorageMapper.class);

    public static final String USER_MODEL_ATTRIBUTE = "user.model.attribute";
    public static final String LDAP_ATTRIBUTE = "ldap.attribute";

    public MSADObjectSIDLDAPStorageMapper(ComponentModel mapperModel, LDAPStorageProvider ldapProvider) {
        super(mapperModel, ldapProvider);
    }

    @Override
    public void onImportUserFromLDAP(LDAPObject ldapUser, UserModel user, RealmModel realm, boolean isCreate) {
        String userModelAttrName = getUserModelAttribute();
        String ldapAttrName = getLdapAttributeName();

        String ldapAttrValue = ldapUser.getAttributeAsString(ldapAttrName);
        String localValue = user.getFirstAttribute(userModelAttrName);
        if (ldapAttrValue != null && !ldapAttrValue.equals(localValue)) {
            SID sid;
            try {
                sid = SID.decode(ldapAttrValue);
                user.setSingleAttribute(userModelAttrName, sid.toString());
            } catch (IOException ex) {
                LOG.warnv("Error decoding ObjectSID: {0}", ldapAttrValue);
            }
        } else {
            user.removeAttribute(userModelAttrName);
        }
    }

    @Override
    public void onRegisterUserToLDAP(LDAPObject ldapUser, UserModel localUser, RealmModel realm) {
        // no-op
    }

    @Override
    public Set<String> getUserAttributes() {
        return Collections.singleton(getUserModelAttribute());
    }

    @Override
    public UserModel proxy(final LDAPObject ldapUser, UserModel delegate, RealmModel realm) {
        return delegate;
    }

    @Override
    public void beforeLDAPQuery(LDAPQuery query) {
        String userModelAttrName = getUserModelAttribute();
        String ldapAttrName = getLdapAttributeName();

        // Add mapped attribute to returning ldap attributes
        query.addReturningLdapAttribute(ldapAttrName);
        query.addReturningReadOnlyLdapAttribute(ldapAttrName);

        // Change conditions and use ldapAttribute instead of userModel
        for (Condition condition : query.getConditions()) {
            condition.updateParameterName(userModelAttrName, ldapAttrName);
            String parameterName = condition.getParameterName();
            if (parameterName != null && (parameterName.equalsIgnoreCase(userModelAttrName) || parameterName.equalsIgnoreCase(ldapAttrName))) {
                condition.setBinary(true);
            }
        }
    }

    private String getUserModelAttribute() {
        return mapperModel.getConfig().getFirst(USER_MODEL_ATTRIBUTE);
    }

    String getLdapAttributeName() {
        return mapperModel.getConfig().getFirst(LDAP_ATTRIBUTE);
    }

}

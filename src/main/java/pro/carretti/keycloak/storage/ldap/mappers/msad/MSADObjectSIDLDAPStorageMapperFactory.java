package pro.carretti.keycloak.storage.ldap.mappers.msad;

import java.util.List;

import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.ldap.LDAPConfig;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapper;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapperFactory;
import org.keycloak.storage.ldap.mappers.LDAPConfigDecorator;

public class MSADObjectSIDLDAPStorageMapperFactory extends AbstractLDAPStorageMapperFactory implements LDAPConfigDecorator {

    public static final String PROVIDER_ID = "msad-objectsid-ldap-mapper";
    protected static final List<ProviderConfigProperty> configProperties;

    static {
        List<ProviderConfigProperty> props = getConfigProps(null);
        configProperties = props;
    }

    static List<ProviderConfigProperty> getConfigProps(ComponentModel p) {
        ProviderConfigurationBuilder config = ProviderConfigurationBuilder.create()
                .property().name(MSADObjectSIDLDAPStorageMapper.USER_MODEL_ATTRIBUTE)
                .label("User Model Attribute")
                .helpText("Name of the UserModel attribute you want to map the MS Active Directory attribute to. For example, 'objectSID'")
                .type(ProviderConfigProperty.USER_PROFILE_ATTRIBUTE_LIST_TYPE)
                .required(true)
                .add()
                .property().name(MSADObjectSIDLDAPStorageMapper.LDAP_ATTRIBUTE).label("LDAP Attribute").helpText("Name of the mapped attribute of the MS Active Directory object. For example, 'objectSID'")
                .type(ProviderConfigProperty.STRING_TYPE)
                .required(true)
                .add();

        return config.build();
    }

    @Override
    public String getHelpText() {
        return "Used to map an ObjectSID encoded attribute from MS Active Directory to attribute of UserModel in Keycloak DB";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        checkMandatoryConfigAttribute(MSADObjectSIDLDAPStorageMapper.USER_MODEL_ATTRIBUTE, "User Model Attribute", config);
        checkMandatoryConfigAttribute(MSADObjectSIDLDAPStorageMapper.LDAP_ATTRIBUTE, "LDAP Attribute", config);
    }

    @Override
    protected AbstractLDAPStorageMapper createMapper(ComponentModel mapperModel, LDAPStorageProvider federationProvider) {
        return new MSADObjectSIDLDAPStorageMapper(mapperModel, federationProvider);
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties(RealmModel realm, ComponentModel parent) {
        return getConfigProps(parent);
    }

    @Override
    public void updateLDAPConfig(LDAPConfig ldapConfig, ComponentModel mapperModel) {
        String ldapAttrName = mapperModel.getConfig().getFirst(MSADObjectSIDLDAPStorageMapper.LDAP_ATTRIBUTE);
        ldapConfig.addBinaryAttribute(ldapAttrName);
    }
}
